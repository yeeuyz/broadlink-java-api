/*******************************************************************************
 * MIT License
 *
 * Copyright (c) 2016, 2017 Anthony Law
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/

package com.github.mob41.blapi;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import com.github.mob41.blapi.mac.Mac;
import com.github.mob41.blapi.pkt.CmdPayload;
import com.github.mob41.blapi.pkt.Payload;

public class SP4Device extends BLDevice
{

    protected SP4Device(short deviceType, String deviceDesc, String host, Mac mac) throws IOException
    {
        super(deviceType, deviceDesc, host, mac);
    }



    public SP4Device(String host, Mac mac) throws IOException
    {
        super(BLDevice.DEV_SP4L_CN, BLDevice.DESC_SP4L_CN, host, mac);
    }




    public void setState(final boolean pwr, final boolean ntlight, final boolean indicator, final int ntlbrightness, final int maxworktime, final boolean childlock) throws Exception  // set_power
    {
        DatagramPacket packet = sendCmdPkt(new CmdPayload()
        {
            @Override
            public byte getCommand()
            {
                return 0x6a;
            }

            @Override
            public Payload getPayload()
            {
                return new Payload()
                {
                    @Override
                    public byte[] getData()
                    {
                        Map<String, Object> state = new HashMap<>();
                        state.put("pwr", pwr ? 1 : 0);
                        state.put("ntlight", ntlight ? 1 : 0);
                        state.put("indicator", indicator ? 1 : 0);
                        state.put("ntlbrightness", ntlbrightness);
                        state.put("maxworktime", maxworktime);
                        state.put("childlock", childlock ? 1 : 0);
                        return _encode(2, state);
                    }
                };
            }

        });

        byte[] data = packet.getData();
        log.debug("SP4 set state received encrypted bytes: " + DatatypeConverter.printHexBinary(data));

        int err = data[0x22] | (data[0x23] << 8);
        if (err == 0)
        {
            Map<String, Object> status = _decode(data);
            log.debug("setState returned pwr: " + status.get("\"pwr\""));
            log.debug("setState returned ntlight: " + status.get("\"ntlight\""));
            log.debug("setState returned indicator: " + status.get("\"indicator\""));
            log.debug("setState returned ntlbrightness: " + status.get("\"ntlbrightness\""));
            log.debug("setState returned maxworktime: " + status.get("\"maxworktime\""));
            log.debug("setState returned childlock: " + status.get("\"childlock\""));
        }
        else
        {
            log.warn("SP4 set state received returned err: " + Integer.toHexString(err) + " / " + err);
        }
    }



    public Map<String, Object> getState() throws Exception  // check_power
    {
        DatagramPacket packet = sendCmdPkt(new CmdPayload()
        {
            @Override
            public byte getCommand()
            {
                return 0x6a;
            }

            @Override
            public Payload getPayload()
            {
                return new Payload()
                {

                    @Override
                    public byte[] getData()
                    {
                        return _encode(1, new HashMap<>());
                    }
                };
            }

        });

        byte[] data = packet.getData();
        log.debug("SP4 get state received encrypted bytes: " + DatatypeConverter.printHexBinary(data));

        int err = data[0x22] | (data[0x23] << 8);
        if (err == 0)
        {
            return _decode(data);
        }
        else
        {
            log.warn("SP4 get state received an error: " + Integer.toHexString(err) + " / " + err);
        }
        return new HashMap<>();
    }



    /**
     * 编码消息，等价于Python的_encode方法（纯原生实现，无第三方库）
     * @param flag 标志位整数
     * @param state 待序列化的字典（Java用Map替代）
     * @return 编码后的字节数组
     */
    private byte[] _encode(int flag, Map<String, Object> state)
    {
        // 1. 原生实现：将Map序列化为紧凑JSON字符串（对应Python的json.dumps(separators=(",", ":"))）
        String jsonStr = Map_JSON.map_2_json(state);
        log.debug(jsonStr);
        // 转字节数组（默认UTF-8编码，和Python的encode()一致）
        byte[] data = jsonStr.getBytes();

        // 2. 初始化12字节的缓冲区，设置小端序（对应Python的bytearray(12)和struct的<）
        ByteBuffer packetBuffer = ByteBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN);

        // 3. 按<HHHBBI格式打包数据（对应Python的struct.pack_into）
        // H: unsigned short (2字节) | B: unsigned byte (1字节) | I: unsigned int (4字节)
        packetBuffer.putShort((short) 0xA5A5);    // 第1个H: 0xA5A5
        packetBuffer.putShort((short) 0x5A5A);    // 第2个H: 0x5A5A
        packetBuffer.putShort((short) 0x0000);    // 第3个H: 0x0000（校验和占位）
        packetBuffer.put((byte) flag);            // B: flag
        packetBuffer.put((byte) 0x0B);            // B: 0x0B
        packetBuffer.putInt(data.length);         // I: data的长度

        // 4. 合并初始12字节和data字节数组（对应Python的packet.extend(data)）
        ByteBuffer fullPacket = ByteBuffer.allocate(12 + data.length)
                .order(ByteOrder.LITTLE_ENDIAN);
        fullPacket.put(packetBuffer.array());     // 写入初始12字节
        fullPacket.put(data);                     // 追加data
        fullPacket.flip();                        // 切换为读模式

        // 5. 计算校验和：sum(packet, 0xBEAF) & 0xFFFF（对应Python的校验和逻辑）
        int checksum = 0xBEAF;
        byte[] fullPacketBytes = fullPacket.array();
        for (byte b : fullPacketBytes)
        {
            checksum += (b & 0xFF); // 转无符号字节后累加（Java字节是有符号的，需&0xFF）
        }
        checksum &= 0xFFFF; // 保留低16位

        // 6. 将校验和写入packet的0x04-0x06位置（对应Python的packet[0x04:0x06] = ...）
        ByteBuffer checksumBuffer = ByteBuffer.wrap(fullPacketBytes).order(ByteOrder.LITTLE_ENDIAN);
        checksumBuffer.position(4); // 定位到0x04位置（第5个字节）
        checksumBuffer.putShort((short) checksum); // 写入2字节小端序校验和

        return fullPacketBytes;
    }



    private Map<String, Object> _decode(byte[] data) throws Exception
    {

        byte[] payload = decryptFromDeviceMessage(data);
        log.debug("SP4 get state  received bytes (decrypted): " + DatatypeConverter.printHexBinary(payload));

        // 解析js_len：struct.unpack_from("<I", payload, 0x08)（小端序int，0x08=8）  // js_len = struct.unpack_from("<I", payload, 0x08)[0]
        ByteBuffer buffer_1 = ByteBuffer.wrap(payload);
        buffer_1.order(ByteOrder.LITTLE_ENDIAN); // 小端序，对应Python的<
        buffer_1.position(0x08);  // 偏移0x08
        int jsLen = buffer_1.getInt();  // 解析int，对应I格式

        // 截取JSON字节：payload[0x0C:0x0C+js_len]（0x0C=12）  // 本条和下一条: state = json.loads(payload[0x0C:0x0C+js_len])
        int jsonStart = 0x0C;
        int jsonEnd = jsonStart + jsLen;
        if (jsonEnd > payload.length)
        {
            throw new IllegalArgumentException("JSON长度超出payload范围，可能是数据损坏");
        }
        byte[] jsonBytes = Arrays.copyOfRange(payload, jsonStart, jsonEnd);
        String jsonStr = new String(jsonBytes);
        log.debug("SP4 get state  received jsonStr: " + jsonStr);

        // 5. 原生解析JSON字符串为Map（替代Python的json.loads）
        return Map_JSON.json_2_map(jsonStr);
    }
}
