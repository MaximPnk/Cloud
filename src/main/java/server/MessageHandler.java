package server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class MessageHandler extends SimpleChannelInboundHandler<String> {

    private final Commands commands = new Commands();

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
        String command = s.replace(System.lineSeparator(), "");
        System.out.println("Message from client: " + command);
        if (!command.equals("")) {
            channelHandlerContext.writeAndFlush(commands.getAnswer(command) + System.lineSeparator());
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client connected");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client disconnected");
    }
}
