package io.github.alphajiang.hyena.biz.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HyenaCacheFactory {

//    @Autowired(required = false)
//    private PointMemCacheService pointMemCacheService;

    @Value("${hyena.cache:memory}")
    private String cacheType;

    private IPointCache pointCacheService;

//    @PostConstruct
//    public void init() {
//
//    }

    public IPointCache getPointCacheService() {
        return this.pointCacheService;
    }

    public void setPointCacheService(IPointCache cacheService) {
        if (cacheService.getCacheType().equalsIgnoreCase(cacheType)) {
            log.info("use {} point cache service", cacheService.getCacheType());
            this.pointCacheService = cacheService;
        }
    }


}
