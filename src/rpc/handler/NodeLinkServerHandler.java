package rpc.handler;

import io.netty.channel.ChannelHandlerContext;
import node.NodeClient;
import node.NodeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.client.RPCClient;
import rpc.common.IMessageHandler;
import rpc.common.MessageOutput;

import static node.NodeContext.*;

public class NodeLinkServerHandler implements IMessageHandler<String> {
    private final static Logger LOG = LoggerFactory.getLogger(NodeLinkServerHandler.class);

    @Override
    public void handle(ChannelHandlerContext ctx, String requestId, String messageId) {
        String clientIp = getIp(ctx);
        // if this message have searched before, ignore it
        if (messageSearched.containsKey(messageId)) {
            ctx.writeAndFlush(new MessageOutput(requestId, "link_res", null));
            return;
        }
        messageSearched.put(messageId, 1);
        LOG.info("start link " + clientIp);
        NodeClient.start(clientIp, NodeContext.SERVER_POST);
        LOG.info("link complete");
        ctx.writeAndFlush(new MessageOutput(requestId, "link_res", null));
    }

    private String getIp(ChannelHandlerContext ctx){
        String ipString = "";
        String socketString = ctx.channel().remoteAddress().toString();
        int colonAt = socketString.indexOf(":");
        ipString = socketString.substring(1, colonAt);
        return ipString;
    }
}

