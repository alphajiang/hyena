package io.github.alphajiang.hyena.model.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PointFreezeByRecIdParam extends PointOpParam {

    @Schema(title = "积分块ID", example = "123")
    private Long recId;

}
