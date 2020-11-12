package netty;

import commands.Commands;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import service.Convert;
import service.ServerCommands;

import java.util.Arrays;

public class MainHandler extends ChannelInboundHandlerAdapter {

    private ByteBuf buffer;

    private final ServerCommands serverCommands = new ServerCommands();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        buffer = (ByteBuf) msg;
        System.out.println(buffer);

        try {
            /*if (needToReadMore) {
                endOfWriting(ctx);
            } else {
                messageHandler(ctx);
            }*/
            messageHandler(ctx);
        } finally {
            buffer.release();
        }
    }

    private void messageHandler(ChannelHandlerContext ctx) {

        int lengthOfLengthInBytes = buffer.readByte();
        byte[] lengthInBytes = new byte[lengthOfLengthInBytes];
        for (int i = 0; i < lengthOfLengthInBytes; i++) {
            lengthInBytes[i] = buffer.readByte();
        }

        System.out.println("Length of length = " + lengthOfLengthInBytes);
        int lengthOfMsg = Convert.bytesToInt(lengthInBytes);
        System.out.println("Length of message = " + lengthOfMsg);
        byte command = buffer.readByte();
        System.out.println("Byte of command = " + command);
        System.out.println(buffer.readableBytes());
        byte[] msg = new byte[lengthOfMsg - 1];
        System.out.println(msg.length);
        buffer.readBytes(msg);
        operationHandler(ctx, msg, command);

        if (buffer.readableBytes() > 0) {
            buffer.retain();
            channelRead(ctx, buffer);
        }

        /*int length = buffer.readByte();
        byte command = buffer.readByte();
        byte[] msg = new byte[length - 1];
        buffer.readBytes(msg);
        operationHandler(ctx, msg, command);
        if (buffer.readableBytes() > 0) {
            buffer.retain();
            channelRead(ctx, buffer);
        }*/
    }

    /*private void endOfWriting(ChannelHandlerContext ctx) {
        for (int i = bytes.length - howMuchRead; i < howMuchRead; i++) {
            bytes[i] = buffer.readByte();
        }

        if (buffer.readableBytes() > 0) {
            buffer.retain();
            channelRead(ctx, buffer);
        } else {
            needToReadMore = false;
            operationHandler(ctx, bytes, command);
        }
    }*/

    private void operationHandler(ChannelHandlerContext ctx, byte[] msg, byte command) {
//        System.out.printf("%s [%d] : %s\r\n", Commands.getCommand(command).toString(), command, Arrays.toString(msg));
//        System.out.println(serverCommands.getRootPath());
        switch (Commands.getCommand(command)) {
            case DOWNLOAD:
                break;
            case UPLOAD:
                serverCommands.upload(msg);
                break;
            case MKDIR:
                sendMsg(ctx, ((char) Commands.MKDIR.getBt() + "Creating directory " + (serverCommands.mkdir(Convert.bytesToStr(msg)) ? "success" : "failed")).getBytes());
                break;
            case TOUCH:
                sendMsg(ctx, ((char) Commands.TOUCH.getBt() + "Creating file " + (serverCommands.touch(Convert.bytesToStr(msg)) ? "success" : "failed")).getBytes());
                break;
            case REMOVE:
                sendMsg(ctx, ((char) Commands.REMOVE.getBt() + "Removing " + (serverCommands.rm(Convert.bytesToStr(msg)) ? "success" : "failed")).getBytes());
                break;
            case GET:
                sendMsg(ctx, ((char) Commands.GET.getBt() + serverCommands.get()).getBytes());
                sendMsg(ctx, ((char) Commands.LOG.getBt() + "File list updated").getBytes());
                break;
            case CD:
                sendMsg(ctx, ((char) Commands.LOG.getBt() + serverCommands.cd(Convert.bytesToStr(msg))).getBytes());
                break;
        }
    }

    private void sendMsg(ChannelHandlerContext ctx, byte[] msg) {
        if (msg.length > 2048) {
            System.out.println("TOO MUCH BYTES, FIX IT");
        } else {
            ctx.writeAndFlush(new byte[]{(byte) (msg.length - 1)});
            ctx.writeAndFlush(msg);
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
