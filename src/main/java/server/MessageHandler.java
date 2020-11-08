package server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import server.service.ServerCommands;

public class MessageHandler extends SimpleChannelInboundHandler<String> {

    private final ServerCommands serverCommands = new ServerCommands();

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) {
        String command = s.replace(System.lineSeparator(), "");
        if (!command.equals("getfiles")) {
            System.out.println("Message from client: " + command);
        }
        if (!command.equals("")) {
            channelHandlerContext.writeAndFlush(serverCommands.getAnswer(command) + System.lineSeparator());
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Client connected");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("Client disconnected");
    }
}
