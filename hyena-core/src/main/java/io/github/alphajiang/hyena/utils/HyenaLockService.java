package io.github.alphajiang.hyena.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
public class HyenaLockService {


    private static final int LOCK_NUM = 1000;
    private final Map<Integer, Semaphore> locks = new ConcurrentHashMap<>();


    @PostConstruct
    public void init() {
        for (int i = 0; i < LOCK_NUM; i++) {
            Semaphore lock = new Semaphore(1);
            this.locks.put(i, lock);
        }
    }

    public boolean lock(String uid, String subUid) {
        int num = Math.abs((uid.hashCode() + (subUid == null ? 0 : subUid.hashCode()))) % LOCK_NUM;
        Semaphore lock = this.locks.get(num);
        boolean ret = false;
        try {
            ret = lock.tryAcquire(5, TimeUnit.SECONDS);
            log.info("local lock ret = {}, uid = {}, subUid = {}, num = {}",
                    ret, uid, subUid, num);
        } catch (Exception e) {
            log.error("uid = {}, subUid = {}, num = {}, exception {}",
                    uid, subUid, num, e.getMessage(), e);
        }
        return ret;
    }

    public void unlock(String uid, String subUid) {
        int num = Math.abs((uid.hashCode() + (subUid == null ? 0 : subUid.hashCode()))) % LOCK_NUM;

        log.info("local unlock uid = {}, subUid = {}, num = {}",
                uid, subUid, num);
        try {
            Semaphore lock = this.locks.get(num);
            lock.release();
        } catch (Exception e) {
            log.warn("uid = {}, subUid = {}, num = {},  exception: {}",
                    uid, subUid, num, e.getMessage(), e);
        }
    }
}
