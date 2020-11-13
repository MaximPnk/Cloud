package netty;

import commands.Commands;
import db.DBCommands;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import service.Convert;
import operations.ServerCommands;

import java.math.BigInteger;

public class MainHandler extends ChannelInboundHandlerAdapter {

    private ByteBuf buffer;

    private ServerCommands serverCommands;
    private boolean auth;
    private final DBCommands db = new DBCommands();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf m = (ByteBuf) msg;
        buffer.writeBytes(m);
        m.release();

        //TODO раскомменить
        /*if (auth) {
            messageHandler(ctx);
        } else {
            authHandler(ctx);
        }*/
        messageHandler(ctx);
    }

    private void authHandler(ChannelHandlerContext ctx) {
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.writeBytes(bytes);

        String data = Convert.bytesToStr(bytes);
        String login = data.split(" ")[0];
        String password = data.split(" ")[1];
        if (db.auth(login, password)) {
            auth = true;
            serverCommands = new ServerCommands(login);
            ctx.writeAndFlush("YES".getBytes());
        } else {
            ctx.writeAndFlush("NO".getBytes());
        }
    }

    private void messageHandler(ChannelHandlerContext ctx) {

        int lengthOfLengthInBytes = buffer.readByte();
        byte[] lengthInBytes = new byte[lengthOfLengthInBytes];
        for (int i = 0; i < lengthOfLengthInBytes; i++) {
            lengthInBytes[i] = buffer.readByte();
        }

        int lengthOfMsg = Convert.bytesToInt(lengthInBytes);
        byte command = buffer.readByte();
        byte[] msg = new byte[lengthOfMsg - 1];
        buffer.readBytes(msg);
        operationHandler(ctx, msg, command);

        if (buffer.readableBytes() > 0) {
            buffer.retain();
            channelRead(ctx, buffer);
        }
    }

    private void operationHandler(ChannelHandlerContext ctx, byte[] msg, byte command) {
        switch (Commands.getCommand(command)) {
            case DOWNLOAD:
                serverCommands.download(Convert.bytesToStr(msg)).forEach(m -> sendMsg(ctx, m));
                sendMsg(ctx, ((char) Commands.DOWNLOAD_COMPLETED.getBt() + "Download completed").getBytes());
                break;
            case UPLOAD:
                serverCommands.upload(msg);
                break;
            case MKDIR:
                System.out.println("MKDIR");
                sendMsg(ctx, ((char) Commands.MKDIR.getBt() + "Creating directory " + (serverCommands.mkdir(Convert.bytesToStr(msg)) ? "success" : "failed")).getBytes());
                break;
            case TOUCH:
                System.out.println("TOUCH");
                sendMsg(ctx, ((char) Commands.TOUCH.getBt() + "Creating file " + (serverCommands.touch(Convert.bytesToStr(msg)) ? "success" : "failed")).getBytes());
                break;
            case REMOVE:
                System.out.println("REMOVE");
                sendMsg(ctx, ((char) Commands.REMOVE.getBt() + "Removing " + (serverCommands.rm(Convert.bytesToStr(msg)) ? "success" : "failed")).getBytes());
                break;
            case GET:
                System.out.println("GET");
                sendMsg(ctx, ((char) Commands.GET.getBt() + serverCommands.get()).getBytes());
                break;
            case CD:
                System.out.println("CD");
                sendMsg(ctx, ((char) Commands.LOG.getBt() + serverCommands.cd(Convert.bytesToStr(msg))).getBytes());
                break;
        }
    }

    private void sendMsg(ChannelHandlerContext ctx, byte[] msg) {
        byte[] msgLengthInBytes = BigInteger.valueOf(msg.length).toByteArray();
        int lengthOfBytes = msgLengthInBytes.length;
        byte[] data = new byte[1 + msgLengthInBytes.length + msg.length];
        data[0] = (byte) lengthOfBytes;
        System.arraycopy(msgLengthInBytes, 0, data, 1, msgLengthInBytes.length);
        System.arraycopy(msg, 0, data, msgLengthInBytes.length + 1, msg.length);
        ctx.writeAndFlush(data);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        buffer = ctx.alloc().buffer(10000);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        buffer.release();
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
