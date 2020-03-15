package io.github.alphajiang.hyena.model.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PointFreezeByRecIdParam extends PointOpParam {

    @ApiModelProperty(value = "积分块ID", example = "123")
    private Long recId;

}
