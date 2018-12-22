package node;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import node.requestpojo.DistributeCalculateMessage;
import node.requestpojo.FileDownloadMessage;
import node.requestpojo.FileSaveMessage;
import node.requestpojo.FileSearchMessage;
import node.responsepojo.FileSearchResponse;
import rpc.client.RPCClient;
import rpc.common.RequestId;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static node.NodeContext.messageSearched;
import static node.NodeContext.neighbors;

/**
 * Client to do remote request
 */
public class NodeClient {

    private RPCClient client;

    public NodeClient(RPCClient client) {
        this.client = client;
        // there should register all
        this.client.rpc("search_res", List.class).
                rpc("save_res", Boolean.class).
                rpc("searchFile_res", Set.class).
                rpc("download_res", Boolean.class).
                rpc("link_res", Boolean.class).
                rpc("distributeCalculate_res", List.class);
    }

    public List<String> searchNode(String messageId) {
        messageSearched.put(messageId, 1);
        return (List<String>) client.send("search", messageId);
    }

    public Set<FileSearchResponse> searchFile(FileSearchMessage message) {
        messageSearched.put(message.getMessageId(), 1);
        Set<FileSearchResponse> result = new HashSet<>();

        // parse response from Set<JSONObject> to Set<FileSearchResponse>
        Set<JSONObject> responses = client.send("searchFile", message);
        for (JSONObject o : responses) {
            result.add(JSON.parseObject(o.toJSONString(), FileSearchResponse.class
            ));
        }

        return result;
    }

    public boolean link(String messageId) {
        messageSearched.put(messageId, 1);
        return client.send("link", messageId);
    }

    public Boolean saveFile(FileSaveMessage message) {
        messageSearched.put(message.getMessageId(), 1);
        return (Boolean) client.send("save", message);
    }

    public Boolean downloadFile(FileDownloadMessage message) {
        return (Boolean) client.send("download", message);
    }

    public ArrayList distributeCalculate(DistributeCalculateMessage message) {
        return (ArrayList) client.send("distributeCalculate", message);
    }

    /**
     * build a connect to serverIp:port
     *
     * @param serverIp
     * @param port
     */
    public static void start(String serverIp, int port) {
        // link to start LOCAL_IP,if this node haven't start LOCAL_IP,
        // skip this process(means it's the first node in the net)
        if (!(serverIp == null || serverIp.equals(""))) {
            // ignore if needn't connect
            if (neighbors.containsKey(serverIp) || NodeContext.LOCAL_IP.equals(serverIp)) {
                return;
            }
            NodeClient client = new NodeClient(new RPCClient(serverIp, port));
            neighbors.put(serverIp, client);
            String messageId = RequestId.next();
            client.link(messageId);
        }
    }

    /**
     * close client
     */
    public void close() {
        client.close();
    }
}

