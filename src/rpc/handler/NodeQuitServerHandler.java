package rpc.handler;

import io.netty.channel.ChannelHandlerContext;
import node.NodeClient;
import node.NodeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.common.IMessageHandler;
import rpc.common.MessageOutput;

import static node.NodeContext.messageSearched;

public class NodeQuitServerHandler implements IMessageHandler<String> {
    private final static Logger LOG = LoggerFactory.getLogger(NodeQuitServerHandler.class);

    @Override
    public void handle(ChannelHandlerContext ctx, String requestId, String messageId) {
        String clientIp = getIp(ctx);
        // if this message have searched before, ignore it
        if (messageSearched.containsKey(messageId)) {
            ctx.writeAndFlush(new MessageOutput(requestId, "quit_res", false));
            return;
        }
        messageSearched.put(messageId, 1);
        LOG.info("start quit " + clientIp);
        NodeContext.quit(clientIp);
        LOG.info("quit complete");
        ctx.writeAndFlush(new MessageOutput(requestId, "quit_res", true));
    }

    private String getIp(ChannelHandlerContext ctx){
        String ipString = "";
        String socketString = ctx.channel().remoteAddress().toString();
        int colonAt = socketString.indexOf(":");
        ipString = socketString.substring(1, colonAt);
        return ipString;
    }
}

