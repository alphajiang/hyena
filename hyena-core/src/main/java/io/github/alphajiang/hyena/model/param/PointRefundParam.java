package io.github.alphajiang.hyena.model.param;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "退款请求参数")
public class PointRefundParam extends PointOpParam {

    /**
     * 实际成本
     */
    @ApiModelProperty(value = "实际成本")
    private Long cost;


    @ApiModelProperty(value = "要退款的积分记录ID")
    private Long recId; // 积分记录的ID

    @ApiModelProperty(value = "解冻积分数量", example = "1000")
    private Long unfreezePoint;
}
