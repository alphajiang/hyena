package io.github.alphajiang.hyena.utils;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * he default id generator for non-cluster environment
 */
@Component
public class MemIdGenerator implements IdGenerator {

    private static final Long MAX_IDX = 1000000L;
    private Long time = getTime();
    private AtomicLong curIdx = new AtomicLong(1L);

    @Override
    public synchronized String getId() {
        if (curIdx.longValue() % MAX_IDX == 0) {
            time = getTime();
            curIdx = new AtomicLong(1L);
        }
        Long val = time * MAX_IDX + curIdx.getAndAdd(1L);
        return val.toString();
    }

    private Long getTime() {
        return System.currentTimeMillis() / 1000L;
    }
}
