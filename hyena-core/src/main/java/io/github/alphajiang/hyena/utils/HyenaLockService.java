package io.github.alphajiang.hyena.utils;

import io.github.alphajiang.hyena.model.exception.HyenaServiceException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HyenaLockService {


    private static final int LOCK_NUM = 10000;
    private final Map<Integer, Semaphore> locks = new ConcurrentHashMap<>();

//    public static void main(String[] args) throws InterruptedException {
//        HyenaLockService ls = new HyenaLockService();
//        ls.init();
//
//        boolean ret = ls.lock("111", "222");
//        System.out.println("lock result = " + ret);
//
//        for (int i = 0; i < 3; i++) {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    log.info("start");
//                    boolean ret = ls.lock("111", "222");
//                    System.out.println("lock result = " + ret);
//                    try {
//                        Thread.sleep(3000L);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//            }).start();
//        }
//
//        Thread.sleep(1000L);
//        ls.unlock("111", "222");
//        ls.unlock("111", "222");
//    }

    @PostConstruct
    public void init() {
        for (int i = 0; i < LOCK_NUM; i++) {
            Semaphore lock = new Semaphore(1, true);
            this.locks.put(i, lock);
        }
    }

    public boolean lock(String type, String uid, String subUid) {
        int num = getLockNum(type, uid, subUid);
        Semaphore lock = this.locks.get(num);
        boolean ret = false;
        try {
            long startTime = System.nanoTime();
            ret = lock.tryAcquire(5, TimeUnit.SECONDS);
            long lockMs = (System.nanoTime() - startTime) / 1000000;
            log.info("lock takes {} ms, local lock ret = {}, uid = {}, subUid = {}, num = {}",
                lockMs, ret, uid, subUid, num);

        } catch (Exception e) {
            log.error("uid = {}, subUid = {}, num = {}, exception {}",
                uid, subUid, num, e.getMessage(), e);
        }
        if (!ret) {
            log.warn("lock failed. type = {}, uid = {}, subUid = {}, num = {}",
                type, uid, subUid, num);
            throw new HyenaServiceException("acquire lock failed", Level.WARN);
        }
        return ret;
    }

    public void unlock(String type, String uid, String subUid) {
        int num = getLockNum(type, uid, subUid);

        log.info("unlock type = {}, uid = {}, subUid = {}, num = {}",
            type, uid, subUid, num);
        try {
            Semaphore lock = this.locks.get(num);
            log.debug("permits = {}", lock.availablePermits());
            if (lock.availablePermits() > 0) {
                return;
            }
            lock.release(1);
//            lock.drainPermits();
        } catch (Exception e) {
            log.warn("uid = {}, subUid = {}, num = {},  exception: {}",
                uid, subUid, num, e.getMessage(), e);
        }
    }

    private int getLockNum(String type, String uid, String subUid) {
        if (type == null && subUid == null) {
            return Math.abs(uid.hashCode()) % LOCK_NUM;
        } else if (type == null) {
            return Math.abs(uid.hashCode() + subUid.hashCode()) % LOCK_NUM;
        } else {
            return Math.abs(
                type.hashCode() + uid.hashCode() + subUid.hashCode())
                % LOCK_NUM;
        }
    }
}
