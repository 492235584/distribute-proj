package rpc.handler;

import io.netty.channel.ChannelHandlerContext;
import node.requestpojo.FileSaveMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.common.IMessageHandler;
import rpc.common.MessageOutput;

import static node.NodeContext.messageSearched;
import static node.NodeContext.saveFile;

public class FileSaveServerHandler implements IMessageHandler<FileSaveMessage> {
    private final static Logger LOG = LoggerFactory.getLogger(FileSaveServerHandler.class);

    @Override
    public void handle(ChannelHandlerContext ctx, String requestId, FileSaveMessage message) {
        // if this message have searched before, ignore it
        String messageId = message.getMessageId();
        String filename = message.getFilename();
        String srcIp = message.getSrcIp();
        byte[] data = message.getData();
        if (messageSearched.containsKey(messageId)) {
            ctx.writeAndFlush(new MessageOutput(requestId, "save_res", false));
            return;
        }
        messageSearched.put(messageId, 1);

        // save data
        LOG.info("start save file : " + filename);
        saveFile(filename, data, srcIp);
        LOG.info("file save complete : " + filename);
        ctx.writeAndFlush(new MessageOutput(requestId, "save_res", true));
    }
}

