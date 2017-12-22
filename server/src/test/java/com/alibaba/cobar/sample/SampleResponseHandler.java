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

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import com.alibaba.cobar.config.Fields;
import com.alibaba.cobar.mysql.CharsetUtil;
import com.alibaba.cobar.net.mysql.EOFPacket;
import com.alibaba.cobar.net.mysql.FieldPacket;
import com.alibaba.cobar.net.mysql.ResultSetHeaderPacket;
import com.alibaba.cobar.net.mysql.RowDataPacket;

/**
 * 基于MySQL协议的返回数据包[header|field,field,...|eof|row,row,...|eof]
 *
 * @author xianmao.hexm
 */
public class SampleResponseHandler {

    public static void response(SampleConnection connection, String message) {
        byte packetId = 0;
        ByteBuffer buffer = connection.allocate();

        // header
        ResultSetHeaderPacket headerPacket = new ResultSetHeaderPacket();
        headerPacket.packetId = ++packetId;
        headerPacket.fieldCount = 1;
        buffer = headerPacket.write(buffer, connection);

        // fields
        FieldPacket[] fields = new FieldPacket[headerPacket.fieldCount];
        for (FieldPacket field : fields) {
            field = new FieldPacket();
            field.packetId = ++packetId;
            field.charsetIndex = CharsetUtil.getIndex("Cp1252");
            field.name = "SampleServer".getBytes();
            field.type = Fields.FIELD_TYPE_VAR_STRING;
            buffer = field.write(buffer, connection);
        }

        // eof
        EOFPacket eof = new EOFPacket();
        eof.packetId = ++packetId;
        buffer = eof.write(buffer, connection);

        // rows
        RowDataPacket row = new RowDataPacket(headerPacket.fieldCount);
        row.add(message != null ? encode(message, connection.getCharset()) : encode("HelloWorld!", connection.getCharset()));
        row.packetId = ++packetId;
        buffer = row.write(buffer, connection);

        // write lastEof
        EOFPacket lastEof = new EOFPacket();
        lastEof.packetId = ++packetId;
        buffer = lastEof.write(buffer, connection);

        // write buffer
        connection.write(buffer);
    }

    private static byte[] encode(String src, String charset) {
        if (src == null) {
            return null;
        }
        try {
            return src.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            // log something
            return src.getBytes();
        }
    }
}
