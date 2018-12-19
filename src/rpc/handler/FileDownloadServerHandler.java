package rpc.handler;

import io.netty.channel.ChannelHandlerContext;
import node.NodeClient;
import node.requestpojo.FileDownloadMessage;
import node.requestpojo.FileSaveMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.common.IMessageHandler;
import rpc.common.MessageOutput;
import rpc.common.RequestId;

import static node.NodeContext.*;

public class FileDownloadServerHandler implements IMessageHandler<FileDownloadMessage> {
    private final static Logger LOG = LoggerFactory.getLogger(FileDownloadServerHandler.class);

    /**
     * search file wants to download and then use NodeClient.saveFile
     *
     * @param ctx
     * @param requestId
     * @param message
     */
    @Override
    public void handle(ChannelHandlerContext ctx, String requestId, FileDownloadMessage message) {
        // if this message have searched before, ignore it
        String messageId = message.getMessageId();
        String filename = message.getFilename();
        String requestIp = message.getRequestIp();
        if (messageSearched.containsKey(messageId)) {
            ctx.writeAndFlush(new MessageOutput(requestId, "save_res", false));
            return;
        }
        messageSearched.put(messageId, 1);

        // download
        LOG.info("start send file : " + filename);
        download(filename, requestIp);
        LOG.info("file send complete: " + filename);
        ctx.writeAndFlush(new MessageOutput(requestId, "save_res", true));
    }

    private void download(String filename, String ip) {
        // read file
        byte[] bytes = readFile(DIR_PATH + "/" + filename);
        /** send **/
        String messageId = RequestId.next();
        NodeClient client = neighbors.get(ip);
        FileSaveMessage message = new FileSaveMessage(messageId, filename, null, bytes);
        client.saveFile(message);
    }
}

