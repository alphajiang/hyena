package com.aj.hyena.model.base;

import com.aj.hyena.HyenaConstants;
import com.fasterxml.jackson.annotation.JsonInclude;

public class BaseResponse extends BaseObject {


    protected int status = HyenaConstants.RES_CODE_SUCCESS;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    protected String error = "";

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

}
