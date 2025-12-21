package features.expr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.noear.solon.expression.util.LRUCache;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LRUCache 单元测试
 */
class LRUCacheTest {
    private LRUCache<Integer, String> cache;
    private final int CAPACITY = 10;

    @BeforeEach
    void setUp() {
        cache = new LRUCache<>(CAPACITY);
    }

    @Test
    @DisplayName("基础功能：put 和 get 是否正常")
    void testBasicOps() {
        cache.put(1, "A");
        cache.put(2, "B");

        assertEquals("A", cache.get(1));
        assertEquals("B", cache.get(2));
        assertEquals(2, cache.size());
    }

    @Test
    @DisplayName("淘汰策略：确保写操作能即时刷新 LRU 状态并正确淘汰")
    void testEviction() {
        // 1. 填满缓存
        for (int i = 1; i <= CAPACITY; i++) {
            cache.put(i, "V" + i);
        }

        // 2. 访问 1，使其在 readBuffer 中标记为“新”
        // 由于 put 会强制刷新 readBuffer，所以下一次 put 会确保 1 被移到尾部
        cache.get(1);

        // 3. 插入新元素触发淘汰
        cache.put(11, "V11");

        // 预期：1 还在，2 被剔除（因为 2 是最老且未被访问的）
        assertNotNull(cache.get(1), "Key 1 应由于最近被访问而保留");
        assertNull(cache.get(2), "Key 2 应该是最老的数据，已被剔除");
        assertEquals(CAPACITY, cache.size());
    }

    @Test
    @DisplayName("原子计算：确保 computeIfAbsent 只计算一次且逻辑正确")
    void testComputeIfAbsent() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger(0);
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 多个线程并发计算同一个 key
        for (int i = 0; i < threadCount; i++) {
            executor.execute(() -> {
                try {
                    cache.computeIfAbsent(99, k -> {
                        counter.incrementAndGet();
                        return "Result";
                    });
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertEquals(1, counter.get(), "映射函数只能被执行一次");
        assertEquals("Result", cache.get(99));
    }

    @Test
    @DisplayName("并发压力测试：验证在高并发下 size 不会失控")
    void testConcurrency() throws InterruptedException {
        int threads = 16;
        int countPerThread = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            int start = i * countPerThread;
            executor.execute(() -> {
                try {
                    for (int j = 0; j < countPerThread; j++) {
                        cache.put(start + j, "v");
                        cache.get(start + (j % 10)); // 制造读写混合
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // 最终 size 必须等于 capacity（因为 put 是强一致淘汰）
        assertEquals(CAPACITY, cache.size(), "并发写入后 size 必须严格受控");
    }

    @Test
    @DisplayName("清理功能：验证 clear 是否彻底")
    void testClear() {
        cache.put(1, "A");
        cache.clear();
        assertEquals(0, cache.size());
        assertNull(cache.get(1));
    }
}