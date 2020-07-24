package io.github.alphajiang.hyena.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
public class HyenaLockService {


    private static final int LOCK_NUM = 500;
    private Map<Integer, Lock> locks = new ConcurrentHashMap<>();


    @PostConstruct
    public void init() {
        for (int i = 0; i < LOCK_NUM; i++) {
            Lock lock = new ReentrantLock();
            this.locks.put(i, lock);
        }
    }


    public boolean lock(String uid, String subUid) {
        int num = (uid.hashCode() + (subUid == null ? 0 : subUid.hashCode())) % LOCK_NUM;
        Lock lock = this.locks.get(num);
        boolean ret = false;
        try {
            ret = lock.tryLock(5, TimeUnit.SECONDS);
            log.info("local lock ret = {}, uid = {}, subUid = {}, num = {}",
                    ret, uid, subUid, num);
        } catch (Exception e) {
            log.error("uid = {}, subUid = {}, num = {}, exception {}",
                    uid, subUid, num, e.getMessage(), e);
        }
        return ret;
    }

    public void unlock(String uid, String subUid) {
        int num = (uid.hashCode() + (subUid == null ? 0 : subUid.hashCode())) % LOCK_NUM;

        log.info("local unlock uid = {}, subUid = {}, num = {}",
                uid, subUid, num);
        try {
            Lock lock = this.locks.get(num);
            lock.unlock();
        } catch (Exception e) {
            log.warn("uid = {}, subUid = {}, num = {},  exception: {}",
                    uid, subUid, num, e.getMessage(), e);
        }
    }
}
