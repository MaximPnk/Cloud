package server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import server.service.ServerCommands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MessageHandler extends SimpleChannelInboundHandler<Object> {

    private final ServerCommands serverCommands = new ServerCommands();
    private boolean uploading;
    private String filename;
    private FileOutputStream fos;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) {
        if (!uploading) {

            String command = ((String) o).replace(System.lineSeparator(), "");

            if (!command.equals("getfiles")) {
                System.out.println("Message from client: " + command);
            }

            if (command.matches("^upload [\\w]+\\.[a-zA-Z]+")) {
                uploading = true;
                serverCommands.getAnswer("rm " + command.split(" ")[1]);
                serverCommands.getAnswer("touch " + command.split(" ")[1]);
                filename = command.split(" ")[1];
                try {
                    fos = new FileOutputStream(new File(serverCommands.getRootPath() + "/" + filename), true);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else if (!command.equals("")) {
                channelHandlerContext.writeAndFlush(serverCommands.getAnswer(command) + System.lineSeparator());
            }
        } else {
            try {
                if (((String) o).replace(System.lineSeparator(), "").matches(".*END OF FILE.*")) {
                    fos.write(((String) o).replace(System.lineSeparator(), "|").replaceAll("END OF FILE.*", "").replace("|", System.lineSeparator()).getBytes());
                    uploading = false;
                    fos.close();
                } else {
                    fos.write((int) o);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
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
