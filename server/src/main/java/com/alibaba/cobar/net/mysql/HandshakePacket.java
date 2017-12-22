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
package com.alibaba.cobar.net.mysql;

import java.nio.ByteBuffer;

import com.alibaba.cobar.mysql.BufferUtil;
import com.alibaba.cobar.mysql.MySQLMessage;
import com.alibaba.cobar.net.FrontendConnection;

/**
 * From server to client during initial handshake.
 * <p>
 * <pre>
 * Bytes                        Name
 * -----                        ----
 * 1                            protocol_version
 * n (Null-Terminated String)   server_version
 * 4                            thread_id
 * 8                            scramble_buff
 * 1                            (filler) always 0x00
 * 2                            server_capabilities
 * 1                            server_language
 * 2                            server_status
 * 13                           (filler) always 0x00 ...
 * 13                           rest of scramble_buff (4.1)
 *
 * @see http://forge.mysql.com/wiki/MySQL_Internals_ClientServer_Protocol#Handshake_Initialization_Packet
 * </pre>
 *
 * @author xianmao.hexm 2010-7-14 下午05:18:15
 */
public class HandshakePacket extends MySQLPacket {

    private static final byte[] FILLER_13 = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    public byte protocolVersion;
    public byte[] serverVersion;
    public long threadId;
    public byte[] seed;
    public int serverCapabilities;
    public byte serverCharsetIndex;
    public int serverStatus;
    public byte[] restOfScrambleBuff;

    public void read(BinaryPacket bin) {
        packetLength = bin.packetLength;
        packetId = bin.packetId;
        MySQLMessage mySQLMessage = new MySQLMessage(bin.data);
        protocolVersion = mySQLMessage.read();
        serverVersion = mySQLMessage.readBytesWithNull();
        threadId = mySQLMessage.readUB4();
        seed = mySQLMessage.readBytesWithNull();
        serverCapabilities = mySQLMessage.readUB2();
        serverCharsetIndex = mySQLMessage.read();
        serverStatus = mySQLMessage.readUB2();
        mySQLMessage.move(13);
        restOfScrambleBuff = mySQLMessage.readBytesWithNull();
    }

    public void read(byte[] data) {
        MySQLMessage mySQLMessage = new MySQLMessage(data);
        packetLength = mySQLMessage.readUB3();
        packetId = mySQLMessage.read();
        protocolVersion = mySQLMessage.read();
        serverVersion = mySQLMessage.readBytesWithNull();
        threadId = mySQLMessage.readUB4();
        seed = mySQLMessage.readBytesWithNull();
        serverCapabilities = mySQLMessage.readUB2();
        serverCharsetIndex = mySQLMessage.read();
        serverStatus = mySQLMessage.readUB2();
        mySQLMessage.move(13);
        restOfScrambleBuff = mySQLMessage.readBytesWithNull();
    }

    public void write(FrontendConnection frontendConnection) {
        ByteBuffer buffer = frontendConnection.allocate();
        BufferUtil.writeUB3(buffer, calcPacketSize());
        buffer.put(packetId);
        buffer.put(protocolVersion);
        BufferUtil.writeWithNull(buffer, serverVersion);
        BufferUtil.writeUB4(buffer, threadId);
        BufferUtil.writeWithNull(buffer, seed);
        BufferUtil.writeUB2(buffer, serverCapabilities);
        buffer.put(serverCharsetIndex);
        BufferUtil.writeUB2(buffer, serverStatus);
        buffer.put(FILLER_13);
        // buffer.position(buffer.position() + 13);
        BufferUtil.writeWithNull(buffer, restOfScrambleBuff);
        //网络中传输
        frontendConnection.write(buffer);
    }

    @Override
    public int calcPacketSize() {
        int size = 1;
        size += serverVersion.length;// n
        size += 5;// 1+4
        size += seed.length;// 8
        size += 19;// 1+2+1+2+13
        size += restOfScrambleBuff.length;// 12
        size += 1;// 1
        return size;
    }

    @Override
    protected String getPacketInfo() {
        return "MySQL Handshake Packet";
    }

}
