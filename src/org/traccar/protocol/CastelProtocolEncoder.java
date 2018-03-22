/*
 * Copyright 2018 Anton Tananaev (anton@traccar.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.protocol;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.traccar.BaseProtocolEncoder;
import org.traccar.Context;
import org.traccar.helper.Checksum;
import org.traccar.helper.Log;
import org.traccar.model.Command;

import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class CastelProtocolEncoder extends BaseProtocolEncoder {

    private ChannelBuffer encodeContent(long deviceId, short type, ChannelBuffer content) {
        ChannelBuffer buf = ChannelBuffers.dynamicBuffer(ByteOrder.LITTLE_ENDIAN, 0);
        String uniqueId = Context.getIdentityManager().getById(deviceId).getUniqueId();

        buf.writeByte('@');
        buf.writeByte('@');

        buf.writeShort(2 + 2 + 1 + 20 + content.readableBytes()); // length

        buf.writeByte(4); // protocol version

        buf.writeBytes(uniqueId.getBytes(StandardCharsets.US_ASCII));
        buf.writeZero(20 - uniqueId.length());

        buf.writeShort(ChannelBuffers.swapShort(type));
        buf.writeBytes(content);

        buf.writeShort(Checksum.crc16(Checksum.CRC16_X25, buf.toByteBuffer()));

        buf.writeByte('\r');
        buf.writeByte('\n');

        return buf;
    }

    @Override
    protected Object encodeCommand(Command command) {
        ChannelBuffer content = ChannelBuffers.dynamicBuffer(ByteOrder.LITTLE_ENDIAN, 0);
        switch (command.getType()) {
            case Command.TYPE_ENGINE_STOP:
                content.writeByte(1);
                return encodeContent(command.getDeviceId(), CastelProtocolDecoder.MSG_CC_PETROL_CONTROL, content);
            case Command.TYPE_ENGINE_RESUME:
                content.writeByte(0);
                return encodeContent(command.getDeviceId(), CastelProtocolDecoder.MSG_CC_PETROL_CONTROL, content);
            default:
                Log.warning(new UnsupportedOperationException(command.getType()));
                break;
        }
        return null;
    }

}