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
package com.alibaba.cobar.server;

import org.apache.log4j.Logger;

import com.alibaba.cobar.config.ErrorCode;
import com.alibaba.cobar.net.handler.FrontendQueryHandler;
import com.alibaba.cobar.server.handler.BeginHandler;
import com.alibaba.cobar.server.handler.ExplainHandler;
import com.alibaba.cobar.server.handler.KillHandler;
import com.alibaba.cobar.server.handler.SavepointHandler;
import com.alibaba.cobar.server.handler.SelectHandler;
import com.alibaba.cobar.server.handler.SetHandler;
import com.alibaba.cobar.server.handler.ShowHandler;
import com.alibaba.cobar.server.handler.StartHandler;
import com.alibaba.cobar.server.handler.UseHandler;
import com.alibaba.cobar.server.parser.ServerParse;

/**
 * @author xianmao.hexm
 */
public class ServerQueryHandler implements FrontendQueryHandler {

    private static final Logger LOGGER = Logger.getLogger(ServerQueryHandler.class);

    private final ServerConnection source;

    public ServerQueryHandler(ServerConnection source) {
        this.source = source;
    }

    @Override
    public void query(String sql) {
        ServerConnection connection = this.source;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(new StringBuilder().append(connection).append(sql).toString());
        }
        int rs = ServerParse.parse(sql);
        switch (rs & 0xff) {
            case ServerParse.EXPLAIN:
                ExplainHandler.handle(sql, connection, rs >>> 8);
                break;
            case ServerParse.SET:
                SetHandler.handle(sql, connection, rs >>> 8);
                break;
            case ServerParse.SHOW:
                ShowHandler.handle(sql, connection, rs >>> 8);
                break;
            case ServerParse.SELECT:
                SelectHandler.handle(sql, connection, rs >>> 8);
                break;
            case ServerParse.START:
                StartHandler.handle(sql, connection, rs >>> 8);
                break;
            case ServerParse.BEGIN:
                BeginHandler.handle(sql, connection);
                break;
            case ServerParse.SAVEPOINT:
                SavepointHandler.handle(sql, connection);
                break;
            case ServerParse.KILL:
                KillHandler.handle(sql, rs >>> 8, connection);
                break;
            case ServerParse.KILL_QUERY:
                connection.writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Unsupported command");
                break;
            case ServerParse.USE:
                UseHandler.handle(sql, connection, rs >>> 8);
                break;
            case ServerParse.COMMIT:
                connection.commit();
                break;
            case ServerParse.ROLLBACK:
                connection.rollback();
                break;
            default:
                connection.execute(sql, rs);
        }
    }

}
