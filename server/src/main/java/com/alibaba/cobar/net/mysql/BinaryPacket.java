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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.alibaba.cobar.mysql.BufferUtil;
import com.alibaba.cobar.mysql.StreamUtil;
import com.alibaba.cobar.net.FrontendConnection;

/**
 * @author xianmao.hexm 2011-5-6 上午10:58:33
 */
public class BinaryPacket extends MySQLPacket {
    public static final byte OK = 1;
    public static final byte ERROR = 2;
    public static final byte HEADER = 3;
    public static final byte FIELD = 4;
    public static final byte FIELD_EOF = 5;
    public static final byte ROW = 6;
    public static final byte PACKET_EOF = 7;

    public byte[] data;

    public void read(InputStream in) throws IOException {
        packetLength = StreamUtil.readUB3(in);
        packetId = StreamUtil.read(in);
        byte[] ab = new byte[packetLength];
        StreamUtil.read(in, ab, 0, ab.length);
        data = ab;
    }

    @Override
    public ByteBuffer write(ByteBuffer buffer, FrontendConnection frontendConnection) {
        buffer = frontendConnection.checkWriteBuffer(buffer, frontendConnection.getPacketHeaderSize());
        BufferUtil.writeUB3(buffer, calcPacketSize());
        buffer.put(packetId);
        buffer = frontendConnection.writeToBuffer(data, buffer);
        return buffer;
    }

    @Override
    public int calcPacketSize() {
        return data == null ? 0 : data.length;
    }

    @Override
    protected String getPacketInfo() {
        return "MySQL Binary Packet";
    }

}
