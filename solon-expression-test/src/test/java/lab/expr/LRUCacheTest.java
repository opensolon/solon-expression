package lab.expr;

import org.noear.solon.expression.util.LRUCache;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LRUCacheTest {
    public static void main(String[] args) throws InterruptedException {
        int capacity = 1000;
        int threadCount = 50; // 并发线程数
        int requestsPerThread = 20000; // 每个线程请求次数
        int keyRange = 2000; // 总 Key 范围（两倍于容量，观察淘汰情况）

        LRUCache<Integer, String> cache = new LRUCache<>(capacity);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger hitCount = new AtomicInteger(0);
        AtomicInteger missCount = new AtomicInteger(0);

        long start = System.currentTimeMillis();

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < requestsPerThread; j++) {
                        // 模拟 80/20 法则：80% 的请求集中在 20% 的热点 Key 上
                        int key;
                        if (ThreadLocalRandom.current().nextDouble() < 0.8) {
                            key = ThreadLocalRandom.current().nextInt((int) (keyRange * 0.2));
                        } else {
                            key = ThreadLocalRandom.current().nextInt(keyRange);
                        }

                        String val = cache.get(key);
                        if (val != null) {
                            hitCount.incrementAndGet();
                        } else {
                            missCount.incrementAndGet();
                            cache.put(key, "value-" + key);
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        long end = System.currentTimeMillis();
        executor.shutdown();

        // 结果分析
        int totalRequests = threadCount * requestsPerThread;
        double hitRate = (hitCount.get() / (double) totalRequests) * 100;

        System.out.println("--- 压力测试结果 ---");
        System.out.println("并发线程数: " + threadCount);
        System.out.println("总请求数: " + totalRequests);
        System.out.println("命中次数: " + hitCount.get());
        System.out.println("缺失次数: " + missCount.get());
        System.out.format("最终命中率: %.2f%%\n", hitRate);
        System.out.println("耗时: " + (end - start) + " ms");
        System.out.println("当前缓存实际大小: " + cache.size());
    }
}