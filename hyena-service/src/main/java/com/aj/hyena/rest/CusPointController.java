package com.aj.hyena.rest;

import com.aj.hyena.model.base.ObjectResponse;
import com.aj.hyena.service.CusPointService;
import com.aj.hyena.utils.LoggerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/hyena/cus/point")
public class CusPointController {

    private static final Logger logger = LoggerFactory.getLogger(CusPointController.class);

    @Autowired
    private CusPointService cusPointService;

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
