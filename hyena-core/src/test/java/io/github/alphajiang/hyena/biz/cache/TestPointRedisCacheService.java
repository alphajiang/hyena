package io.github.alphajiang.hyena.biz.cache;

import io.github.alphajiang.hyena.biz.strategy.TestPointStrategyBase;
import io.github.alphajiang.hyena.model.vo.PointVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class TestPointRedisCacheService extends TestPointStrategyBase {

    @Autowired
    private PointRedisCacheService pointRedisCacheService;


    @Test
    public void test_updatePoint() {
        PointVo pv = new PointVo();
        BeanUtils.copyProperties(super.getUserPoint(), pv);
        //log.info("aaaaaaaaaaaaaaaa");
        //for(int i = 0; i < 1000; i ++) {
            log.info("before updatePoint");
            pointRedisCacheService.lock(super.getPointType(), super.getUid(), super.getSubUid());
            pointRedisCacheService.updatePoint(super.getPointType(), super.getUid(), super.getSubUid(), pv);
            log.info("after updatePoint");
        //}
        //log.info("bbbbbbbbbbbbbb");
    }
}
