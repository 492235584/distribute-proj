package rpc.handler;

import io.netty.channel.ChannelHandlerContext;
import node.requestpojo.FileSearchMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.common.IMessageHandler;
import rpc.common.MessageOutput;

import static node.NodeContext.*;

public class FileHoldServerHandler implements IMessageHandler<FileSearchMessage> {
    private final static Logger LOG = LoggerFactory.getLogger(FileHoldServerHandler.class);

    @Override
    public void handle(ChannelHandlerContext ctx, String requestId, FileSearchMessage message) {
        String messageId = message.getMessageId();
        String filename = message.getKey();

        // if this message have searched before, ignore it
        if (messageSearched.containsKey(messageId)) {
            ctx.writeAndFlush(new MessageOutput(requestId, "holdFile_res", null));
            return;
        }
        messageSearched.put(messageId, 1);

        LOG.info("start hold file");
        holdWhenUpdate(filename, messageId);
        LOG.info("hold file complete");
        ctx.writeAndFlush(new MessageOutput(requestId, "holdFile_res", null));
    }
}

