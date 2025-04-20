import cn.hutool.core.thread.ThreadUtil;
import com.taylor.common.access.service.impl.InMemoryRateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * InMemoryRateLimiter测试
 *
 * @author loveCamille
 * @date 2025-04-20 14:50:05
 */
public class InMemoryRateLimiterTest {

    private InMemoryRateLimiter limiter;

    @BeforeEach
    void setUp() {
        limiter = new InMemoryRateLimiter();
    }

    private void testKeyAcquire(String key) {
        long time = 3;
        assertTrue(limiter.tryAcquire(key, 2, time, TimeUnit.SECONDS));
        assertFalse(limiter.tryAcquire(key, 1, time, TimeUnit.SECONDS));
        assertTrue(limiter.tryAcquire(key, 2, time, TimeUnit.SECONDS));
        assertFalse(limiter.tryAcquire(key, 2, time, TimeUnit.SECONDS));
        ThreadUtil.safeSleep(time * 1000);
        assertTrue(limiter.tryAcquire(key, 2, time, TimeUnit.SECONDS));
    }

    /**
     * 测试同一窗口内只能获取一次令牌
     */
    @Test
    void testSingleKeyAcquire() {
        String key = "15872004097";
        testKeyAcquire(key);
    }

    @Test
    void testDifferentKeys() throws InterruptedException {
        List<String> keys = Arrays.asList("key1", "key2");
        CountDownLatch countDownLatch = new CountDownLatch(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        keys.forEach(key -> executor.execute(() -> {
            testKeyAcquire(key);
            countDownLatch.countDown();
        }));

        countDownLatch.await();

    }

}
