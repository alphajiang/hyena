/*
 *  Copyright (C) 2019 Alpha Jiang. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.aj.hyena.model.base;

import com.aj.hyena.HyenaConstants;
import com.fasterxml.jackson.annotation.JsonInclude;

public class BaseResponse extends BaseObject {


    private int status = HyenaConstants.RES_CODE_SUCCESS;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String error = "";

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String seq;

    public BaseResponse() {

    }

    public BaseResponse(int status) {
        this.status = status;
    }

    public BaseResponse(int status, String error) {
        this.status = status;
        this.error = error;
    }



    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getSeq() {
        return seq;
    }

    public void setSeq(String seq) {
        this.seq = seq;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

}
