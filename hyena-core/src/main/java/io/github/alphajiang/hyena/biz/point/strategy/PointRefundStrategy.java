package io.github.alphajiang.hyena.biz.point.strategy;

import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.ds.service.PointRecDs;
import io.github.alphajiang.hyena.model.exception.HyenaParameterException;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import io.github.alphajiang.hyena.model.type.CalcType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class PointRefundStrategy extends AbstractPointStrategy {

    @Autowired
    private PointRecDs pointRecDs;

    @Override
    public CalcType getType() {
        return CalcType.REFUND;
    }

    @Override
    @Transactional
    public PointPo process(PointUsage usage) {
        if(usage.getRecId() != null) {
            this.refundByRecId(usage);
        }else {
            this.refund(usage);
        }
        return null;
    }


    private void refundByRecId(PointUsage usage) {
        PointRecPo rec = pointRecDs.getById(usage.getType(), usage.getRecId(), true);
        if(rec == null){
            throw new HyenaParameterException("invalid parameter");
        }//else if(rec.get)
    }

    private void refund(PointUsage usage) {

    }
}
