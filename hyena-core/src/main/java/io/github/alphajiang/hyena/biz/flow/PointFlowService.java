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

package io.github.alphajiang.hyena.biz.flow;

import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.type.CalcType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PointFlowService {

    private final int THREAD_SIZE = 10;
    private List<PointFlowProcessor> processorList = new ArrayList<>();

    @Autowired
    private PointFlowStrategyFactory pointFlowStrategyFactory;

    @PostConstruct
    public void init() {
        for (int i = 0; i < THREAD_SIZE; i++) {
            PointFlowProcessor processor = new PointFlowProcessor(pointFlowStrategyFactory);
            new Thread(processor).start();
            processorList.add(processor);
        }
    }


    public void addFlow(CalcType calcType, PointUsage usage, PointPo point) {
        PointFlowProcessor processor = processorList.get((int) (point.getId() % THREAD_SIZE));
        processor.push(calcType, usage, point);
    }


}
