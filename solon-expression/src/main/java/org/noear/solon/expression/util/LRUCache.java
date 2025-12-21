/*
 * Copyright 2017-2025 noear.org and authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.noear.solon.expression.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * 高性能 LRU 缓存 (基于 ConcurrentHashMap + 数组异步缓冲思想)
 *
 * @author noear
 * @since 3.1
 * @since 3.8
 */
public class LRUCache<K, V> {
    private final int capacity;
    private final ConcurrentHashMap<K, Node<K, V>> data;
    private final NodeList<K, V> accessOrder;
    private final ReentrantLock evictionLock = new ReentrantLock();

    private static final int READ_BUFF_SIZE = 64;
    private static final int READ_BUFF_MASK = READ_BUFF_SIZE - 1;
    private final Node<K, V>[] readBuffer = new Node[READ_BUFF_SIZE];
    private final AtomicInteger readBufferIndex = new AtomicInteger(0);

    private final AtomicInteger sizeCounter = new AtomicInteger(0);

    public LRUCache(int capacity) {
        this.capacity = capacity;
        // 按照 0.75 负载因子预设初始大小，避免扩容
        this.data = new ConcurrentHashMap<>((int) (capacity / 0.75f) + 1);
        this.accessOrder = new NodeList<>();
    }

    public V get(K key) {
        Node<K, V> node = data.get(key);
        if (node != null) {
            recordAccess(node);
            return node.value;
        }
        return null;
    }

    public void put(K key, V value) {
        Node<K, V> newNode = new Node<>(key, value);
        Node<K, V> oldNode = data.put(key, newNode);

        if (oldNode == null) {
            sizeCounter.incrementAndGet();
        }

        evictionLock.lock();
        try {
            drainReadBuffer();
            accessOrder.makeTail(newNode);
            evict();
        } finally {
            evictionLock.unlock();
        }
    }

    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        Node<K, V> node = data.computeIfAbsent(key, k -> {
            V val = mappingFunction.apply(k);
            if (val == null) return null;
            sizeCounter.incrementAndGet();
            return new Node<>(k, val);
        });

        if (node != null) {
            if (evictionLock.tryLock()) {
                try {
                    drainReadBuffer();
                    accessOrder.makeTail(node);
                    evict();
                } finally {
                    evictionLock.unlock();
                }
            }
            return node.value;
        }
        return null;
    }

    public void remove(K key) {
        Node<K, V> node = data.remove(key);
        if (node != null) {
            sizeCounter.decrementAndGet();
            if (evictionLock.tryLock()) {
                try {
                    accessOrder.remove(node);
                } finally {
                    evictionLock.unlock();
                }
            }
        }
    }

    private void recordAccess(Node<K, V> node) {
        // 利用原子自增和掩码实现无锁入队
        int idx = readBufferIndex.getAndIncrement() & READ_BUFF_MASK;
        readBuffer[idx] = node;

        // 当索引回到 0 时（满一轮），尝试触发异步处理
        if (idx == 0) {
            if (evictionLock.tryLock()) {
                try {
                    drainReadBuffer();
                    evict();
                } finally {
                    evictionLock.unlock();
                }
            }
        }
    }

    private void drainReadBuffer() {
        // 这里的处理逻辑是：批量将 buffer 中的节点移到链表尾部
        for (int i = 0; i < READ_BUFF_SIZE; i++) {
            Node<K, V> node = readBuffer[i];
            if (node != null) {
                readBuffer[i] = null; // 显式清空，防止内存泄漏
                // 检查节点是否依然有效（未被删除）
                if (data.containsKey(node.key)) {
                    accessOrder.makeTail(node);
                }
            }
        }
    }

    private void evict() {
        int currentSize = sizeCounter.get();
        while (currentSize > capacity) {
            Node<K, V> oldest = accessOrder.removeHead();
            if (oldest != null) {
                if (data.remove(oldest.key) != null) {
                    currentSize = sizeCounter.decrementAndGet();
                }
            } else {
                break;
            }
        }
    }

    public int size() { return sizeCounter.get(); }

    public void clear() {
        evictionLock.lock();
        try {
            data.clear();
            for (int i = 0; i < READ_BUFF_SIZE; i++) readBuffer[i] = null;
            accessOrder.clear();
            sizeCounter.set(0);
        } finally {
            evictionLock.unlock();
        }
    }

    // --- 内部结构 ---

    private static class Node<K, V> {
        final K key;
        final V value;
        Node<K, V> prev, next;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private static class NodeList<K, V> {
        private Node<K, V> head, tail;

        void makeTail(Node<K, V> node) {
            if (node == tail) return;
            if (node.prev != null || node.next != null || node == head) {
                if (node.prev != null) node.prev.next = node.next;
                if (node.next != null) node.next.prev = node.prev;
                if (node == head) head = node.next;
            }
            node.prev = tail;
            node.next = null;
            if (tail == null) {
                head = tail = node;
            } else {
                tail.next = node;
                tail = node;
            }
        }

        Node<K, V> removeHead() {
            if (head == null) return null;
            Node<K, V> node = head;
            head = head.next;
            if (head == null) tail = null;
            else head.prev = null;
            node.prev = node.next = null;
            return node;
        }

        void remove(Node<K, V> node) {
            if (node.prev != null) node.prev.next = node.next;
            if (node.next != null) node.next.prev = node.prev;
            if (node == head) head = node.next;
            if (node == tail) tail = node.prev;
            node.prev = node.next = null;
        }

        void clear() { head = tail = null; }
    }
}