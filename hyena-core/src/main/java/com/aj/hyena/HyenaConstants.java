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


package com.aj.hyena;

public interface HyenaConstants {

    int RES_CODE_SUCCESS = 0;
    int RES_CODE_SERVICE_ERROR = 1000;
    int RES_CODE_NO_ENOUGH_POINT = 1010;
    int RES_CODE_PARAMETER_ERROR = 1100;
    int RES_CODE_STATUS_ERROR = 1200;
    int RES_CODE_DUPLICATE = 2000;
    int RES_CODE_DUPLICATE_IDEMPOTENT = 2001;
    int RES_CODE_SERVER_ERROR = 9000;
    int RES_CODE_UNKNOW_ERROR = 9999;

    String PREFIX_POINT_TABLE_NAME = "t_point_";

    String REQ_IDEMPOTENT_SEQ_KEY = "seq";
}
