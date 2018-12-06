package rpc.handler;

import io.netty.channel.ChannelHandlerContext;
import node.NodeClient;
import rpc.common.IMessageHandler;
import rpc.common.MessageOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static node.NodeContext.messageSearched;
import static node.NodeContext.neighbors;

public class SearchNodeServerHandler implements IMessageHandler<String> {

    @Override
    public void handle(ChannelHandlerContext ctx, String requestId, String messageId) {
        // if this message have searched before, ignore it
        if (messageSearched.containsKey(messageId)) {
            return;
        }

        // return ip of neighbors
        List<String> allIp = new ArrayList<String>();
        for (Map.Entry<String, NodeClient> entries : neighbors.entrySet()) {
            allIp.add(entries.getKey());
        }

        ctx.writeAndFlush(new MessageOutput(requestId, "search_res", allIp));
    }
}
