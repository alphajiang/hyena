package io.github.alphajiang.hyena.utils;

import io.github.alphajiang.hyena.HyenaTestBase;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class TestHyenaLockService  extends HyenaTestBase {

    @Autowired
    private HyenaLockService hyenaLockService;

    @Test
    public void test_lock() throws InterruptedException {
        this.hyenaLockService.lock("score", "84790", "34490");
        this.hyenaLockService.unlock("score", "84790", "34490");

        // this.hyenaLockService.lock("84790", "34490");
        //this.hyenaLockService.unlock("84790", "34490");
    }
}
