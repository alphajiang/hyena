package io.github.alphajiang.hyena.model.param;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ApiModel(value = "退款请求参数")
public class PointRefundParam extends PointUnfreezeParam {

    /**
     * 实际成本
     */
    @ApiModelProperty(value = "实际成本", notes = "退款按实际成本计算", example = "1.00")
    private BigDecimal cost;


//    @ApiModelProperty(value = "要退款的积分记录ID")
//    private Long recId; // 积分记录的ID

    @ApiModelProperty(value = "解冻积分数量", example = "10.00")
    private BigDecimal unfreezePoint;
}
