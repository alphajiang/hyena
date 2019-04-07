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

package io.github.alphajiang.hyena.model.param;

public class ListPointRecParam extends BaseListParam {

    private String cusId;
    private boolean available = false;
    private boolean frozen = false;

    public String getCusId() {
        return cusId;
    }

    public ListPointRecParam setCusId(String cusId) {
        this.cusId = cusId;
        return this;
    }

    public boolean getAvailable() {
        return available;
    }

    public ListPointRecParam setAvailable(boolean available) {
        this.available = available;
        return this;
    }

    public boolean getFrozen() {
        return frozen;
    }

    public ListPointRecParam setFrozen(boolean frozen) {
        this.frozen = frozen;
        return this;
    }
}
