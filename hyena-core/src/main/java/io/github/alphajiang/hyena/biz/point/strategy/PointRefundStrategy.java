package io.github.alphajiang.hyena.biz.point.strategy;

import io.github.alphajiang.hyena.HyenaConstants;
import io.github.alphajiang.hyena.biz.flow.PointFlowService;
import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.ds.service.PointDs;
import io.github.alphajiang.hyena.ds.service.PointLogDs;
import io.github.alphajiang.hyena.ds.service.PointRecDs;
import io.github.alphajiang.hyena.model.exception.HyenaParameterException;
import io.github.alphajiang.hyena.model.exception.HyenaServiceException;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.type.CalcType;
import io.github.alphajiang.hyena.utils.HyenaAssert;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PointRefundStrategy extends AbstractPointStrategy {

    @Autowired
    private PointDs pointDs;

    @Autowired
    private PointRecDs pointRecDs;

    @Autowired
    private PointLogDs pointLogDs;

    @Autowired
    private PointFlowService pointFlowService;

    @Override
    public CalcType getType() {
        return CalcType.REFUND;
    }

    @Override
    //@Transactional(propagation = Propagation.MANDATORY)
    public PointPo process(PointUsage usage) {
        log.info("refund.  usage = {}", usage);
        super.preProcess(usage);
        int retry = 3;
        RefundResult ret = null;
        for(int i = 0; i < retry; i ++){
            try {
                ret = this.refund(usage);
                if(ret != null) {
                    break;
                }
            }
            catch (HyenaParameterException e) {
                throw e;
            }
            catch (Exception e) {
                log.warn("refund. failed. retry = {}, error = {}", retry, e.getMessage(), e);
            }
        }
        if(ret == null) {
            throw new HyenaServiceException(HyenaConstants.RES_CODE_SERVICE_BUSY, "service busy, please retry later");
        }
        if(ret.getPostUnfreeze() != null) {
            pointFlowService.addFlow(CalcType.UNFREEZE, ret.getUsage4Unfreeze(), ret.getPostUnfreeze());
        }
        pointFlowService.addFlow(CalcType.REFUND, usage, ret.getPostDecrease());
        return ret.getPostDecrease();
    }



    private RefundResult refund(PointUsage usage) {
        PointPo curPoint = this.pointDs.getCusPoint(usage.getType(), usage.getUid(), false);
        HyenaAssert.notNull(curPoint, HyenaConstants.RES_CODE_PARAMETER_ERROR,
                "can't find point to the uid: " + usage.getUid(), Level.WARN);
        HyenaAssert.notNull(curPoint.getFrozen(), HyenaConstants.RES_CODE_PARAMETER_ERROR,
                "can't find point to the uid: " + usage.getUid(), Level.WARN);

        if(usage.getUnfreezePoint() != null) {
            HyenaAssert.isTrue(curPoint.getFrozen().longValue() >= usage.getUnfreezePoint(),
                    HyenaConstants.RES_CODE_NO_ENOUGH_POINT,
                    "no enough frozen point");
        }
        PointPo postUnfreeze = null;
        PointUsage usage4Unfreeze = null;
        if(usage.getUnfreezePoint() != null && usage.getUnfreezePoint() > 0L) {
            // 解冻
            curPoint.setFrozen(curPoint.getFrozen() - usage.getUnfreezePoint())
                    .setAvailable(curPoint.getAvailable() + usage.getUnfreezePoint());
            postUnfreeze = new PointPo();
            BeanUtils.copyProperties(curPoint, postUnfreeze);
            postUnfreeze.setSeqNum(postUnfreeze.getSeqNum() + 1);
            usage4Unfreeze = new PointUsage();
            BeanUtils.copyProperties(usage, usage4Unfreeze);
            usage4Unfreeze.setPoint(usage.getUnfreezePoint());

        }
        curPoint.setPoint(curPoint.getPoint() - usage.getPoint())
                .setAvailable(curPoint.getAvailable() - usage.getPoint())
                .setRefund(curPoint.getRefund() + usage.getPoint());
        if(curPoint.getAvailable() < 0L) {
            // 使用可用余额来抵扣超扣部分
            throw new HyenaParameterException("no enough available point");
        }

        var point2Update = new PointPo();
        point2Update.setPoint(curPoint.getPoint()).setFrozen(curPoint.getFrozen())
                .setAvailable(curPoint.getAvailable()).setRefund(curPoint.getRefund())
                .setSeqNum(curPoint.getSeqNum())
                .setId(curPoint.getId());


        boolean ret = this.pointDs.update(usage.getType(), point2Update);
        if(!ret) {
            log.warn("decrease frozen failed!!! please retry later. usage = {}", usage);
            return null;
        }
        HyenaAssert.isTrue(ret, HyenaConstants.RES_CODE_STATUS_ERROR, "status changed. please retry later");
        // var cusPoint = this.pointDs.getCusPoint(usage.getType(), usage.getUid(), false);
        curPoint.setSeqNum(curPoint.getSeqNum() + 1);

        RefundResult result = new RefundResult(postUnfreeze, usage4Unfreeze, curPoint);
        //pointFlowService.addFlow(CalcType.DECREASE, usage, curPoint);
        return result;
    }

    @Data
    @AllArgsConstructor
    class RefundResult {
        PointPo postUnfreeze;
        PointUsage usage4Unfreeze;
        PointPo postDecrease;
    }
}
