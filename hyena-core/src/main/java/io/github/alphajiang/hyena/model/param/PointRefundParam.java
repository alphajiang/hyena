package io.github.alphajiang.hyena.model.param;


import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(title = "退款请求参数")
public class PointRefundParam extends PointUnfreezeParam {

    /**
     * 实际成本
     */
    @Schema(title = "实际成本", description = "退款按实际成本计算", example = "1.00")
    private BigDecimal cost;


//    @Schema(title = "要退款的积分记录ID")
//    private Long recId; // 积分记录的ID

    @Schema(title = "解冻积分数量", example = "10.00")
    private BigDecimal unfreezePoint;
}
