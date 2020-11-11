package netty;

import commands.Commands;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import operations.GetFiles;

public class InputHandler extends ChannelInboundHandlerAdapter {

    private ByteBuf buffer;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        buffer = (ByteBuf) msg;

        try {
            operationHandler(ctx);
        } finally {
            buffer.release();
        }
    }

    private void operationHandler(ChannelHandlerContext ctx) {

        byte command = buffer.readByte();
        byte[] array = new byte[buffer.readableBytes()];
        buffer.readBytes(array);
        System.out.println(command);

        switch (Commands.getCommand(command)) {
            case DOWNLOAD:
                ctx.writeAndFlush(buffer.writeBytes("DOWNLOAD MSG".getBytes()));
                break;
            case UPLOAD:
                ctx.writeAndFlush("UPLOAD MSG".getBytes());
                break;
            case MKDIR:
                ctx.writeAndFlush(array);
                break;
            case TOUCH:
                ctx.writeAndFlush("TOUCH MSG".getBytes());
                break;
            case REMOVE:
                ctx.writeAndFlush("REMOVE MSG".getBytes());
                break;
            case GET:
                System.out.println("CD");
                ctx.writeAndFlush(((char) Commands.GET.getBt() + GetFiles.list()).getBytes());
                break;
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        buffer = ctx.alloc().buffer(4096);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        buffer = null;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Client connected");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("Client disconnected");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
