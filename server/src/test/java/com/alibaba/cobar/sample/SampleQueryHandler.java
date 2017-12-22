/*
 * Copyright 1999-2012 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cobar.sample;

import org.apache.log4j.Logger;

import com.alibaba.cobar.net.handler.FrontendQueryHandler;

/**
 * @author xianmao.hexm
 */
public class SampleQueryHandler implements FrontendQueryHandler {

    private static final Logger LOGGER = Logger.getLogger(SampleQueryHandler.class);

    private SampleConnection connection;

    public SampleQueryHandler(SampleConnection connection) {
        this.connection = connection;
    }

    @Override
    public void query(String sql) {
        LOGGER.info(new StringBuilder().append(connection).append(sql).toString());
        // sample response
        SampleResponseHandler.response(connection, sql);
    }

}
