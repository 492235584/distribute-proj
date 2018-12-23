package node;

import node.requestpojo.DistributeCalculateMessage;
import node.requestpojo.FileDownloadMessage;
import node.requestpojo.FileSaveMessage;
import node.requestpojo.FileSearchMessage;
import rpc.handler.*;
import rpc.server.RPCServer;

public class NodeServer {
    public static void start(String ip) {
        RPCServer server = new RPCServer(ip, 45455, 4, 16);
        server.service("search", String.class, new SearchNodeServerHandler()).
                service("save", FileSaveMessage.class, new FileSaveServerHandler()).
                service("download", FileDownloadMessage.class, new FileDownloadServerHandler()).
                service("searchFile", FileSearchMessage.class, new FileSearchServerHandler()).
                service("link", String.class, new NodeLinkServerHandler()).
                service("quit", String.class, new NodeQuitServerHandler()).
                service("distributeCalculate", DistributeCalculateMessage.class,new DistributeCalculateHandler());
        server.start();
    }
}