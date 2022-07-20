package io.github.alphajiang.hyena.model.param;

import io.github.alphajiang.hyena.utils.JsonUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class PointFreezeByRecIdParam extends PointOpParam {

    @Schema(title = "积分块ID", example = "123")
    private Long recId;

    @Override
    public String toString() {
        return JsonUtils.toJsonString(this);
    }
}
