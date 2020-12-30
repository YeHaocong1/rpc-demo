package codc;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.CharsetUtil;

/**
 * @author YeHaocong
 * @decription TODO
 * @Date 2020/12/30 16:51
 */

public class MessageProtocolEncode extends MessageToByteEncoder<String> {


    @Override
    protected void encode(ChannelHandlerContext ctx, String msg, ByteBuf out) throws Exception {
        //对协议的编码时，把数据的前4个字节固定为数据的长度，跟着就是数据的内容
        byte[] content = msg.getBytes(CharsetUtil.UTF_8);
        int len = content.length;
        //先写数据的长度
        out.writeInt(len);
        //再写数据的内容
        out.writeBytes(content);
    }
}
