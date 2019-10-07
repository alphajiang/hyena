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

package io.github.alphajiang.hyena.spring.boot.autoconfigure;

import io.github.alphajiang.hyena.HyenaConstants;
import io.github.alphajiang.hyena.ds.service.PointTableDs;
import io.github.alphajiang.hyena.ds.service.SysPropertyDs;
import io.github.alphajiang.hyena.ds.service.UpgradeSchemaDs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.util.List;

@Slf4j
@Configuration
public class HyenaInitialization {

    @Autowired
    private SysPropertyDs sysPropertyDs;

    @Autowired
    private PointTableDs pointTableDs;

    @Autowired
    private UpgradeSchemaDs upgradeSchemaDs;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("application ready");
        sysPropertyDs.createSysPropertyTable();

        int dbSqlVer = this.upgradeSql();
        if (dbSqlVer != HyenaConstants.SQL_VERSION) {
            sysPropertyDs.setSqlVersion(HyenaConstants.SQL_VERSION);
        }
    }

    public int upgradeSql() {
        int sqlVer = sysPropertyDs.getSqlVersion();
        List<String> tables = pointTableDs.listTable();
        if (sqlVer == 0) {
            upgradeSchemaDs.addRefundColumn(tables);
        }
        if (sqlVer < 2) {
            upgradeSchemaDs.addCostColumns(tables);
        }
        if (sqlVer < 3) {
            upgradeSchemaDs.addSqlV3(tables);
        }
        return sqlVer;
    }
}
