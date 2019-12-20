package io.github.alphajiang.hyena.model.param;

import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;

/**
 * 退款解冻
 */
public class PointRefundFreezeParam extends PointOpParam {
    @ApiModelProperty(value = "要冻结的余额", example = "10.00")
    private BigDecimal point;
    @ApiModelProperty(value = "要冻结的成本", example = "10.00")
    private BigDecimal cost;


}
