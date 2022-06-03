package io.github.alphajiang.hyena.biz.cache;

import io.github.alphajiang.hyena.biz.strategy.TestPointStrategyBase;
import io.github.alphajiang.hyena.model.vo.PointVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

@Slf4j
public class TestPointRedisCacheService extends TestPointStrategyBase {

    @Autowired
    private PointRedisCacheService pointRedisCacheService;

    @MockBean
    private RedisTemplate<String, String> redisStringTemplate;


    @Test
    public void test_updatePoint() {
        PointVo pv = new PointVo();
        BeanUtils.copyProperties(super.getUserPoint(), pv);

        Mockito.when(redisStringTemplate.exec(Mockito.any())).thenReturn(List.of(Boolean.TRUE));

        //log.info("aaaaaaaaaaaaaaaa");
        //for(int i = 0; i < 1000; i ++) {
        log.info("before updatePoint");
        pointRedisCacheService.lock(super.getPointType(), super.getUid(), super.getSubUid(), 5)
                .flatMap(lockRet -> pointRedisCacheService.updatePoint(super.getPointType(), super.getUid(), super.getSubUid(), pv))
                .subscribe();
        log.info("after updatePoint");
        //}
        //log.info("bbbbbbbbbbbbbb");
    }
}
