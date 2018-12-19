package rpc.handler;

import io.netty.channel.ChannelHandlerContext;
import node.NodeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.common.IMessageHandler;
import rpc.common.MessageOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static node.NodeContext.messageSearched;
import static node.NodeContext.neighbors;

public class SearchNodeServerHandler implements IMessageHandler<String> {
    private final static Logger LOG = LoggerFactory.getLogger(SearchNodeServerHandler.class);

    @Override
    public void handle(ChannelHandlerContext ctx, String requestId, String messageId) {
        // if this message have searched before, ignore it
        if (messageSearched.containsKey(messageId)) {
            ctx.writeAndFlush(new MessageOutput(requestId, "search_res", null));
            return;
        }
        messageSearched.put(messageId, 1);
        // return LOCAL_IP of neighbors

        LOG.info("start search node");
        List<String> allIp = new ArrayList<String>();
        for (Map.Entry<String, NodeClient> entries : neighbors.entrySet()) {
            allIp.add(entries.getKey());
        }
        LOG.info("search node complete");

        ctx.writeAndFlush(new MessageOutput(requestId, "search_res", allIp));
    }
}

