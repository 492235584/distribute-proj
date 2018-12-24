package node;

import node.UI.UIPage;
import node.requestpojo.FileDownloadMessage;
import node.requestpojo.FileSaveMessage;
import node.requestpojo.FileSearchMessage;
import node.requestpojo.NodeSearchMessage;
import node.responsepojo.FileSearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.client.RPCClient;
import rpc.common.RequestId;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NodeContext {
    public static final String NAMESPLIT = "-=-";
    public static final String DIR_PATH = "files";
    private static final Logger LOG = LoggerFactory.getLogger(NodeContext.class);
    // first node to link
    public static final String START_IP = "";
    public static final int SERVER_POST = 45455;
    // this node's LOCAL_IP
    public static final String LOCAL_IP = getLocalHostLANIp();
    // all neighbors
    public static ConcurrentHashMap<String, NodeClient> neighbors;
    // all message id which had received
    public static ConcurrentHashMap<String, Integer> messageSearched;
    // all files upload
    public static ConcurrentHashMap<String, Boolean> filenameAndStatus;

    /**
     * init NodeContext, set start node to link and build topology automatic
     */
    static {
        neighbors = new ConcurrentHashMap<String, NodeClient>();
        messageSearched = new ConcurrentHashMap<String, Integer>();
        filenameAndStatus = new ConcurrentHashMap<String, Boolean>();
        LOG.info("local IP : " + LOCAL_IP);

        // init filenameAndStatus
        File dir = new File(DIR_PATH);
        for (File f : dir.listFiles()) {
            filenameAndStatus.put(f.getName(), true);
        }
    }

    /**
     * build topology
     */
    public static void buildTopology() {
        // use to collect other LOCAL_IP
        Set<String> otherIp = new HashSet<>();

        // search node in system
        otherIp = searchNode();

        // build no more than three link
        int linkNum = neighbors.size();
        for (String ip : otherIp) {
            // no more than three
            if (linkNum >= 3) {
                break;
            }
            // ignore LOCAL_IP haven been linked
            if (neighbors.containsKey(ip) || LOCAL_IP.equals(ip)) {
                continue;
            } else {
                // add new neighbor
                NodeClient client = new NodeClient(new RPCClient(ip, SERVER_POST));
                neighbors.put(ip, client);
                linkNum++;
                String messageId = RequestId.next();
                client.link(messageId);
            }
        }

        UIPage.updateNeiLabel(neighbors);
    }

    /**
     * quit this node
     */
    public static void quit() {
        quit(LOCAL_IP);
    }

    /**
     * quit ip from system
     */
    public static void quit(String ip) {
        // this node quit
        if (LOCAL_IP.equals(ip)) {
            String messageId = RequestId.next();
            for (Map.Entry<String, NodeClient> n : neighbors.entrySet()) {
                NodeClient client = n.getValue();
                boolean result = client.quit(messageId);
                if (result == true) {
                    client.close();
                }
            }
        } else { // delete a neighbor
            NodeClient client = neighbors.get(ip);
            if (client != null) {
                client.close();
                neighbors.remove(ip);
            }
        }
        UIPage.updateNeiLabel(neighbors);
    }

    // 正确的IP拿法，即优先拿site-local地址
    private static String getLocalHostLANIp() {
        try {
            for (Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces(); interfaces.hasMoreElements(); ) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    String ip = addresses.nextElement().getHostAddress();
                    // only get LAN, do limits
                    if (ip != null && ip.length() > 8 && ip.length() < 16) {
                        return ip;
                    }
                }
            }
        } catch (SocketException e) {
            LOG.error("Error when getting host ip address: <{}>.", e.getMessage());
        }
        return null;
    }

    /**
     * upload file to system
     *
     * @param path
     */
    public static void uploadFile(String path) {
        // get filename
        String[] pathSplits = path.split("[/\\\\]");
        String filename = pathSplits[pathSplits.length - 1];
        // read file
        byte[] bytes = readFile(path);

        /** distribute **/
        // if more than 10M,split the file and store to other node.
        if (bytes.length > 1 * 1024 * 1024) {
            // split to partNum part,use name as ip-filename-partNum-part
            int neighborSize = neighbors.size();
            int partNum = neighborSize > 4 ? 4 : neighborSize;
            int byteNum = bytes.length / partNum;
            int i = 1;
            for (Map.Entry<String, NodeClient> n : neighbors.entrySet()) {
                if (i > partNum) {
                    break;
                }
                String partName = filename + NAMESPLIT + partNum + NAMESPLIT + i;
                int start = (i - 1) * byteNum;
                int end = i * byteNum;
                if (i == partNum) {
                    end = bytes.length;
                }
                byte[] sub = subBytes(bytes, start, end);

                String messageId = RequestId.next();
                FileSaveMessage message = new FileSaveMessage(messageId, partName, LOCAL_IP, sub);
                n.getValue().saveFile(message);
                i++;
            }
        } else {
            // save file in other nodes
            String messageId = RequestId.next();
            int i = 0;
            for (Map.Entry<String, NodeClient> n : neighbors.entrySet()) {
                // most save two copy
                if (i >= 2) {
                    break;
                }
                FileSaveMessage message = new FileSaveMessage(messageId, filename, LOCAL_IP, bytes);
                n.getValue().saveFile(message);
            }
        }

        // save one copy in local
        saveFile(filename, bytes, LOCAL_IP);
    }

    /**
     * download file
     *
     * @param filename file
     * @param saveIp   where to download
     */
    public static void downloadFile(String filename, String saveIp) {
        String messageId = RequestId.next();
        FileDownloadMessage message = new FileDownloadMessage(messageId, filename, LOCAL_IP);

        boolean isTmp = false;
        NodeClient client = neighbors.get(saveIp);
        if (client == null) {
            isTmp = true;
            client = new NodeClient(new RPCClient(saveIp, NodeContext.SERVER_POST));
        }


        if (client.downloadFile(message)) {
            LOG.info("download complete : " + filename);
            filenameAndStatus.put(filename, true);
        } else {
            LOG.info("download failed : " + filename);
        }

        // if client is temporary
        if (isTmp) {
            client.close();
        }
    }

    /**
     * get sub bytes
     *
     * @param bytes
     * @param start
     * @param end
     * @return
     */
    public static byte[] subBytes(byte[] bytes, int start, int end) {
        byte[] sub = new byte[end - start];
        for (int i = start; i < end; i++) {
            sub[i - start] = bytes[i];
        }
        return sub;
    }

    /**
     * save file
     *
     * @param filename
     * @param data
     * @param srcIp
     */
    public static void saveFile(String filename, byte[] data, String srcIp) {
        String newName = null;
        if (srcIp != null && !srcIp.equals("")) {
            newName = srcIp + NAMESPLIT + filename;
        } else {
            newName = filename;
        }
        // writer file
        BufferedOutputStream bufOut = null;
        try {
            bufOut = new BufferedOutputStream(new FileOutputStream(DIR_PATH + "/" + newName));
            bufOut.write(data);
        } catch (IOException e) {
            LOG.error(e.getMessage());
        } finally {
            if (bufOut != null) {
                try {
                    bufOut.close();
                } catch (IOException e) {
                    LOG.error(e.getMessage());
                }
            }
        }
        // store filename and local ip
        filenameAndStatus.put(newName, true);
    }

    /**
     * search node
     *
     * @return list of ip
     */
    public static Set<String> searchNode() {
        String messageId = RequestId.next();
        List<String> searched = new ArrayList<>();
        searched.add(LOCAL_IP);
        return searchNode(messageId, searched);
    }

    /**
     * search node
     *
     * @return list of ip
     */
    public static Set<String> searchNode(String messageId, List<String> searched) {
        // add searched
        searched.add(LOCAL_IP);

        Set<String> nodes = new HashSet();
        // add all filename in this node to set
        Enumeration<String> keys = neighbors.keys();
        while (keys.hasMoreElements()) {
            String ip = keys.nextElement();
            nodes.add(ip);
        }

        // add all neighbor's neighbors
        for (Map.Entry<String, NodeClient> n : neighbors.entrySet()) {
            // skip searched node
            if (searched != null && searched.contains(n.getKey())) {
                continue;
            }

            Set<String> find = n.getValue().searchNode(new NodeSearchMessage(messageId, searched));
            if (find != null) {
                nodes.addAll(find);
            }
        }

        return nodes;
    }

    /**
     * search file
     *
     * @return
     */
    public static Set<FileSearchResponse> searchFile(String key) {
        String messageId = RequestId.next();
        List<String> searched = new ArrayList<>();
        return searchFile(messageId, key, searched);
    }

    /**
     * search file use specify messageId
     *
     * @return
     */
    public static Set<FileSearchResponse> searchFile(String messageId, String key, List<String> searched) {
        // add searced
        searched.add(LOCAL_IP);

        Set<FileSearchResponse> files = new HashSet();
        // add all filename in this node to set
        Enumeration<String> keys = filenameAndStatus.keys();
        while (keys.hasMoreElements()) {
            String filename = keys.nextElement();
            if (filename.contains(key) && filename.split(NAMESPLIT).length >= 2) {
                files.add(new FileSearchResponse(LOCAL_IP, filename, getFileSize(filename)));
            }
        }

        // add all neighbor's filename
        for (Map.Entry<String, NodeClient> n : neighbors.entrySet()) {
            // skip searched node
            if (searched != null && searched.contains(n.getKey())) {
                continue;
            }

            Set<FileSearchResponse> find = n.getValue().searchFile(new FileSearchMessage(messageId, searched, key));
            if (find != null) {
                files.addAll(find);
            }
        }

        return files;
    }

    /**
     * update file by data
     *
     * @param target       node
     * @param completeName
     * @param data         new data
     */
    public static void updateFile(String target, String completeName, byte[] data) {
        String messageId = RequestId.next();
        FileSaveMessage message = new FileSaveMessage(messageId, completeName, null, data);

        boolean isTmp = false;
        NodeClient client = neighbors.get(target);
        if (client == null) {
            isTmp = true;
            client = new NodeClient(new RPCClient(target, NodeContext.SERVER_POST));
        }
        client.saveFile(message);
        // if client is temporary
        if (isTmp) {
            client.close();
        }
    }


    public static byte[] readFile(String path) {
        BufferedInputStream bufIn = null;
        List<Byte> data = new ArrayList<Byte>();
        try {
            bufIn = new BufferedInputStream(new FileInputStream(path));
            byte[] b = new byte[1024];
            int length = -1;
            while ((length = bufIn.read(b)) != -1) {
                for (int i = 0; i < length; i++) {
                    data.add(b[i]);
                }
            }
        } catch (FileNotFoundException e) {
            LOG.info("please ensure hte path is exist : " + path);
            return null;
        } catch (IOException e) {
            LOG.error(e.getMessage());
            return null;
        } finally {
            if (bufIn != null) {
                try {
                    bufIn.close();
                } catch (IOException e) {
                    LOG.error(e.getMessage());
                }
            }
        }

        byte[] bytes = new byte[data.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = data.get(i);
        }
        return bytes;
    }

    private static long getFileSize(String filename) {
        File f = new File(NodeContext.DIR_PATH + "/" + filename);
        return f.length();
    }
}
