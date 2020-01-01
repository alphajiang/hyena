package io.github.alphajiang.hyena.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class PointLogBi {
    private Integer logType;
    private BigDecimal delta;
    private BigDecimal deltaCost;

}
