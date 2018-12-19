package node;

import node.requestpojo.FileDownloadMessage;
import node.requestpojo.FileSaveMessage;
import node.requestpojo.FileSearchMessage;
import node.requestpojo.caculateCallingMessage;
import rpc.handler.*;
import rpc.server.RPCServer;

public class NodeServer {
    public static void start(String ip) {
        RPCServer server = new RPCServer(ip, 45455, 2, 16);
        server.service("search", String.class, new SearchNodeServerHandler()).
                service("save", FileSaveMessage.class, new FileSaveServerHandler()).
                service("download", FileDownloadMessage.class, new FileDownloadServerHandler()).
                service("searchFile", FileSearchMessage.class, new FileSearchServerHandler()).
                service("holdFile", FileSearchMessage.class, new FileHoldServerHandler()).
                service("callingTimes", caculateCallingMessage.class,new caculateCallingHandler());
        server.start();
    }
}