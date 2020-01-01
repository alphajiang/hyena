package io.github.alphajiang.hyena.model.param;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class TimeFilter {
    @ApiModelProperty(value = "开始时间. 闭区间", example = "2025-10-24 15:34:46")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", locale = "zh", timezone = "GMT+8")
    private Date startTime;

    @ApiModelProperty(value = "结束时间. 开区间", example = "2025-10-24 15:34:46")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", locale = "zh", timezone = "GMT+8")
    private Date endTime;
}
