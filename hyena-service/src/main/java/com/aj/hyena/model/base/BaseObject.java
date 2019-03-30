package com.aj.hyena.model.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;

public abstract class BaseObject {

    public String toJsonString() {
        String ret = "";
        ObjectMapper om = new ObjectMapper();
        try {
            ret = om.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            LogManager.getLogger(this).trace(e.getMessage(), e);

        }
        return ret;
    }

    @Override
    public String toString() {
        String ret = "";
        ObjectMapper om = new ObjectMapper();
        try {
            ret = om.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            LogManager.getLogger(this).trace(e.getMessage(), e);

        }
        return ret;
    }
}
