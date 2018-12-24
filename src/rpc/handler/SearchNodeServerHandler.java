package rpc.handler;

import io.netty.channel.ChannelHandlerContext;
import node.NodeClient;
import node.NodeContext;
import node.requestpojo.NodeSearchMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.common.IMessageHandler;
import rpc.common.MessageOutput;

import java.util.*;

import static node.NodeContext.messageSearched;
import static node.NodeContext.neighbors;

public class SearchNodeServerHandler implements IMessageHandler<NodeSearchMessage> {
    private final static Logger LOG = LoggerFactory.getLogger(SearchNodeServerHandler.class);

    @Override
    public void handle(ChannelHandlerContext ctx, String requestId, NodeSearchMessage message) {
        String messageId = message.getMessageId();
        List<String> searched = message.getSearched();

        // if this message have searched before, ignore it
        if (messageSearched.containsKey(messageId)) {
            ctx.writeAndFlush(new MessageOutput(requestId, "search_res", null));
            return;
        }
        messageSearched.put(messageId, 1);
        // return LOCAL_IP of neighbors

        LOG.info("start search node");
        Set<String> allIp = NodeContext.searchNode(messageId, searched);
        LOG.info("search node complete");

        ctx.writeAndFlush(new MessageOutput(requestId, "search_res", allIp));
    }
}

