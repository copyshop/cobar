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
package com.alibaba.cobar.net.handler;

import com.alibaba.cobar.config.ErrorCode;
import com.alibaba.cobar.net.FrontendConnection;
import com.alibaba.cobar.net.NIOHandler;
import com.alibaba.cobar.net.mysql.MySQLPacket;
import com.alibaba.cobar.statistic.CommandCount;

/**
 * 前端命令处理器，处理一些常用sql 命令， use select等
 *
 * @author xianmao.hexm
 */
public class FrontendCommandHandler implements NIOHandler {

    protected final FrontendConnection frontendConnection;
    protected final CommandCount commandCount;

    public FrontendCommandHandler(FrontendConnection frontendConnection) {
        this.frontendConnection = frontendConnection;
        this.commandCount = frontendConnection.getProcessor().getCommands();
    }

    @Override
    public void handle(byte[] data) {
        System.out.print("packet length: ");
        for (int i = 0; i < 3; i++) {
            System.out.print(data[i] + "    ");
        }
        System.out.println();
        System.out.println("packet num: " + data[3]);
        System.out.print("statement: ");
        for (int i = 4; i < data.length; i++) {
            System.out.print(data[i] + "    ");
        }
        System.out.println();
        switch (data[4]) {
            case MySQLPacket.COM_INIT_DB:
                commandCount.doInitDB();
                frontendConnection.initDB(data);
                break;
            case MySQLPacket.COM_QUERY:
                commandCount.doQuery();
                frontendConnection.query(data);
                break;
            case MySQLPacket.COM_PING:
                commandCount.doPing();
                frontendConnection.ping();
                break;
            case MySQLPacket.COM_QUIT:
                commandCount.doQuit();
                frontendConnection.close();
                break;
            case MySQLPacket.COM_PROCESS_KILL:
                commandCount.doKill();
                frontendConnection.kill(data);
                break;
            case MySQLPacket.COM_STMT_PREPARE:
                commandCount.doStmtPrepare();
                frontendConnection.stmtPrepare(data);
                break;
            case MySQLPacket.COM_STMT_EXECUTE:
                commandCount.doStmtExecute();
                frontendConnection.stmtExecute(data);
                break;
            case MySQLPacket.COM_STMT_CLOSE:
                commandCount.doStmtClose();
                frontendConnection.stmtClose(data);
                break;
            case MySQLPacket.COM_HEARTBEAT:
                commandCount.doHeartbeat();
                frontendConnection.heartbeat(data);
                break;
            default:
                commandCount.doOther();
                frontendConnection.writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Unknown command");
        }
    }

}
