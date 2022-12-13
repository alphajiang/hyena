package io.github.alphajiang.hyena.biz.point;

import io.github.alphajiang.hyena.biz.flow.PointFlowService;
import io.github.alphajiang.hyena.ds.service.PointDs;
import io.github.alphajiang.hyena.ds.service.PointLogDs;
import io.github.alphajiang.hyena.ds.service.PointRecDs;
import io.github.alphajiang.hyena.model.dto.PointRecLogDto;
import io.github.alphajiang.hyena.model.po.PointLogPo;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.model.type.PointOpType;
import io.github.alphajiang.hyena.utils.DecimalUtils;
import io.github.alphajiang.hyena.utils.StringUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class PointBizService {
    @Autowired
    private PointDs pointDs;


    @Autowired
    private PointLogDs pointLogDs;

    @Autowired
    private PointRecDs pointRecDs;

    @Autowired
    private PointFlowService pointFlowService;


    @Autowired
    private PointBuilder pointBuilder;

    @Transactional
    public void addPoint(PointUsage usage, PointCache pc) {
        var point2Update = PointPo.buildPointPo();
        point2Update.setSeqNum(1L)
            .setPoint(usage.getPoint().setScale(DecimalUtils.SCALE_2, RoundingMode.HALF_UP))
            .setAvailable(usage.getPoint().setScale(DecimalUtils.SCALE_2, RoundingMode.HALF_UP))
            .setUid(usage.getUid())
            .setSubUid(usage.getSubUid())
            .setEnable(true)
            .setCreateTime(new Date())
            .setUpdateTime(new Date());
        BigDecimal cost =
            usage.getCost() != null && DecimalUtils.gt(usage.getCost(), DecimalUtils.ZERO)
                ? usage.getCost().setScale(DecimalUtils.SCALE_2, RoundingMode.HALF_UP)
                : DecimalUtils.ZERO;
        point2Update.setCost(cost);

        if (StringUtils.isNotBlank(usage.getName())) {
            point2Update.setName(usage.getName());
        }

        this.pointDs.addPoint(usage.getType(), point2Update);
        PointRecPo rec = null;
        if (DecimalUtils.gt(usage.getPoint(), DecimalUtils.ZERO)) {    // <= 0 表示仅创建帐号
            rec = this.pointRecDs.addPointRec(usage, point2Update.getId(),
                point2Update.getSeqNum());

            PointLogPo pointLog = this.pointBuilder.buildPointLog(PointOpType.INCREASE, usage,
                point2Update);
            PointRecLogDto recLog = this.pointBuilder.buildRecLog(rec, pointLog, usage.getPoint(),
                cost);

            pointFlowService.addFlow(usage, pointLog, List.of(recLog));
        }

        pc.setPoint(this.pointDs.getPointVo(usage.getType(), point2Update.getId(), null, null));
        if (rec != null) {
            if (pc.getPoint().getRecList() == null) {
                pc.getPoint().setRecList(new ArrayList<>());
            }
            long recSeqNo = rec.getSeqNum();
            if (pc.getPoint().getRecList().stream().filter(r -> r.getSeqNum() == recSeqNo).findAny()
                .isEmpty()) {
                pc.getPoint().getRecList().add(rec);
            }
        }
    }
}
