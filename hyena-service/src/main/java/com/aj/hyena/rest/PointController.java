package com.aj.hyena.rest;

import com.aj.hyena.model.base.ObjectResponse;
import com.aj.hyena.service.PointService;
import com.aj.hyena.utils.LoggerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/hyena/point")
public class PointController {

    private static final Logger logger = LoggerFactory.getLogger(PointController.class);

    @Autowired
    private PointService cusPointService;

    @PostMapping(value = "/addPoint")
    public ObjectResponse<Long> addPoint(HttpServletRequest request,
                                         @RequestParam(defaultValue = "default") String type,
                                         @RequestParam String cusId,
                                         @RequestParam long point) {
        logger.info(LoggerHelper.formatEnterLog(request));
//        CommonResponse<ChannelUser> result = new CommonResponse<>();
//        ChannelUser channelUser = this.channelUserService.bindVisaUser(uid, prefix, suffix);
//        result.setData(channelUser);
        var cusPoint = this.cusPointService.addPoint(type, cusId, point);
        ObjectResponse<Long> res = new ObjectResponse<>(cusPoint.getPoint());
        logger.info(LoggerHelper.formatLeaveLog(request));
        return res;
    }
}
