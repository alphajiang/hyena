package io.github.alphajiang.hyena.biz.cache;

import io.github.alphajiang.hyena.biz.point.PointCache;
import io.github.alphajiang.hyena.biz.point.PointWrapper;
import io.github.alphajiang.hyena.model.vo.PointVo;

import java.util.Collection;

public interface IPointCache {

    String getCacheType();

    PointWrapper getPoint(String type, String uid, String subUid, boolean lock);

    void updatePoint(String type, String uid, String subUid, PointVo point);

    void removePoint(String type, String uid, String subUid);

    Collection<PointCache> dump();

    void expireCache();

    void unlock(String type, String uid, String subUid);
}
