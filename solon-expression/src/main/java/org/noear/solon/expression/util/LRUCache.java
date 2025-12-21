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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * 高性能 LRU 缓存 (基于 ConcurrentHashMap + 异步缓冲思想)
 *
 * @author noear
 * @since 3.1
 * @since 3.8
 */
public class LRUCache<K, V> {
    private final int capacity;
    // 存储 Node 而不是直接存 V，减少 Map 维护开销
    private final ConcurrentHashMap<K, Node<K, V>> data;
    private final NodeList<K, V> accessOrder;
    private final ReentrantLock evictionLock = new ReentrantLock();

    // 读缓冲区：暂存访问记录，避免每次访问都竞争锁
    private final ConcurrentLinkedQueue<Node<K, V>> readBuffer = new ConcurrentLinkedQueue<>();
    private final AtomicInteger bufferSize = new AtomicInteger(0);
    private final AtomicInteger sizeCounter = new AtomicInteger(0);

    private static final int DRAIN_THRESHOLD = 64;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.data = new ConcurrentHashMap<>(capacity);
        this.accessOrder = new NodeList<>();
    }

    /**
     * 获取缓存
     */
    public V get(K key) {
        Node<K, V> node = data.get(key);
        if (node != null) {
            recordAccess(node);
            return node.value;
        }
        return null;
    }

    /**
     * 存入缓存
     */
    public void put(K key, V value) {
        Node<K, V> newNode = new Node<>(key, value);
        Node<K, V> oldNode = data.put(key, newNode);

        if (oldNode == null) {
            sizeCounter.incrementAndGet();
        }

        // 写入操作：使用强制锁，确保淘汰逻辑的准确性
        evictionLock.lock();
        try {
            drainReadBuffer();
            accessOrder.makeTail(newNode);
            evict();
        } finally {
            evictionLock.unlock();
        }
    }

    /**
     * 原子性地获取或计算缓存项
     */
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        Node<K, V> node = data.computeIfAbsent(key, k -> {
            V val = mappingFunction.apply(k);
            if (val == null) return null;
            sizeCounter.incrementAndGet();
            return new Node<>(k, val);
        });

        if (node != null) {
            evictionLock.lock();
            try {
                drainReadBuffer();
                accessOrder.makeTail(node);
                evict();
            } finally {
                evictionLock.unlock();
            }
            return node.value;
        }
        return null;
    }

    /**
     * 移除缓存
     */
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

    /**
     * 获取容量
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * 获取当前近似大小
     */
    public int size() {
        return sizeCounter.get();
    }

    /**
     * 清空缓存
     */
    public void clear() {
        evictionLock.lock();
        try {
            data.clear();
            readBuffer.clear();
            accessOrder.clear();
            sizeCounter.set(0);
            bufferSize.set(0);
        } finally {
            evictionLock.unlock();
        }
    }

    private void recordAccess(Node<K, V> node) {
        readBuffer.add(node);
        if (bufferSize.incrementAndGet() >= DRAIN_THRESHOLD) {
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
        Node<K, V> node;
        while ((node = readBuffer.poll()) != null) {
            bufferSize.decrementAndGet();
            if (data.get(node.key) == node) {
                accessOrder.makeTail(node);
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

    private static class Node<K, V> {
        final K key;
        final V value;
        Node<K, V> prev;
        Node<K, V> next;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private static class NodeList<K, V> {
        private Node<K, V> head;
        private Node<K, V> tail;

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
            if (head == null) {
                tail = null;
            } else {
                head.prev = null;
            }
            node.prev = null;
            node.next = null;
            return node;
        }

        void remove(Node<K, V> node) {
            if (node.prev != null) node.prev.next = node.next;
            if (node.next != null) node.next.prev = node.prev;
            if (node == head) head = node.next;
            if (node == tail) tail = node.prev;
            node.prev = null;
            node.next = null;
        }

        void clear() {
            head = tail = null;
        }
    }
}