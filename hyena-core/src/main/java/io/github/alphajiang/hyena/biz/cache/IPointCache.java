package io.github.alphajiang.hyena.biz.cache;

import io.github.alphajiang.hyena.biz.point.PointCache;
import io.github.alphajiang.hyena.biz.point.PointWrapper;
import io.github.alphajiang.hyena.model.vo.PointVo;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface IPointCache {

    String getCacheType();

    Mono<PointWrapper> getPoint(String type, String uid, String subUid, boolean lock);

    Mono<PointVo> updatePoint(String type, String uid, String subUid, PointVo point);

    Mono<Boolean> removePoint(String type, String uid, String subUid);

    Collection<PointCache> dump();

    void expireCache();

    Mono<Boolean> unlock(String type, String uid, String subUid);
}
