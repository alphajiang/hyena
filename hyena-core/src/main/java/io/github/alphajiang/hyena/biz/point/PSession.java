package io.github.alphajiang.hyena.biz.point;

import io.github.alphajiang.hyena.model.vo.PointOpResult;
import io.github.alphajiang.hyena.model.vo.PointVo;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PSession {
    private PointUsage usage;
    private PointWrapper pw;

    private PointVo originPoint;    // for roll back

    private PointOpResult result;

    public static PSession fromUsage(PointUsage usage) {
        PSession session = new PSession();
        session.setUsage(usage);
        return session;
    }
}
