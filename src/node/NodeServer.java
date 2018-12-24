package node;

import node.requestpojo.*;
import rpc.handler.*;
import rpc.server.RPCServer;

public class NodeServer {
    public static void start(String ip) {
        RPCServer server = new RPCServer(ip, NodeContext.SERVER_POST, 4, 16);
        server.service("search", NodeSearchMessage.class, new SearchNodeServerHandler()).
                service("save", FileSaveMessage.class, new FileSaveServerHandler()).
                service("download", FileDownloadMessage.class, new FileDownloadServerHandler()).
                service("searchFile", FileSearchMessage.class, new FileSearchServerHandler()).
                service("link", String.class, new NodeLinkServerHandler()).
                service("quit", String.class, new NodeQuitServerHandler()).
                service("distributeCalculate", DistributeCalculateMessage.class,new DistributeCalculateHandler());
        server.start();
    }
}