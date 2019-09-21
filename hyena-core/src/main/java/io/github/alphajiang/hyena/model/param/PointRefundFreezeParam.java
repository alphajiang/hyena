package io.github.alphajiang.hyena.model.param;

import io.swagger.annotations.ApiModelProperty;

/**
 * 退款解冻
 */
public class PointRefundFreezeParam extends PointOpParam {
    @ApiModelProperty("要冻结的余额")
    private Long point;
    @ApiModelProperty("要冻结的成本")
    private Long cost;


}
