package node;

import node.requestpojo.FileDownloadMessage;
import node.requestpojo.FileSaveMessage;
import node.requestpojo.FileSearchMessage;
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
    public static final String NAMESPLIT = "-|-*-|-";
    public static final String DIR_PATH = "files";
    private static final Logger LOG = LoggerFactory.getLogger(NodeContext.class);
    // first node to link
    public static final String START_IP = "100.66.228.198";
    public static final int SERVER_POST = 45455;
    // this node's LOCAL_IP
    public static final String LOCAL_IP = getLocalHostLANIp();
    // all neighbors
    public static ConcurrentHashMap<String, NodeClient> neighbors;
    // all message id which had received
    public static ConcurrentHashMap<String, Integer> messageSearched;
    // all files upload
    public static ConcurrentHashMap<String, String> filenameAndAddress;

    /**
     * init NodeContext, set start node to link and build topology automatic
     */
    static {
        neighbors = new ConcurrentHashMap<String, NodeClient>();
        messageSearched = new ConcurrentHashMap<String, Integer>();
        filenameAndAddress = new ConcurrentHashMap<String, String>();
        LOG.info("local IP : " + LOCAL_IP);

        // init filenameAndAddress
        File dir = new File(DIR_PATH);
        for (File f : dir.listFiles()) {
            filenameAndAddress.put(f.getName(), LOCAL_IP);
        }
    }

    /**
     * build topology
     */
    public static void buildTopology() {
        // use to collect other LOCAL_IP
        List<String> otherIp = new ArrayList<>();

        // search by neighbors us identity message id
        String messageId = RequestId.next();
        for (Map.Entry<String, NodeClient> n : neighbors.entrySet()) {
            List<String> find = n.getValue().searchNode(messageId);
            if (find != null) {
                otherIp.addAll(find);
            }
        }

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
            }
        }
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
     * @param ip where to download
     */
    public static void downloadFile(String filename, String ip) {
        String messageId = RequestId.next();
        FileDownloadMessage message = new FileDownloadMessage(messageId, filename, LOCAL_IP);
        NodeClient client = neighbors.get(ip);
        if (client.downloadFile(message)) {
            LOG.info("download complete : " + filename);
        } else {
            LOG.info("download failed : " + filename);
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
    private static byte[] subBytes(byte[] bytes, int start, int end) {
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
        // store filename and local ip
        filenameAndAddress.put(newName, LOCAL_IP);
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
    }


    /**
     * search file
     *
     * @return
     */
    public static Set<String> searchFile(String key) {
        String messageId = RequestId.next();
        return searchFile(messageId, key);
    }

    /**
     * search file use specify messageId
     *
     * @return
     */
    public static Set<String> searchFile(String messageId, String key) {
        Set files = new HashSet();
        // add all filename in this node to set
        Enumeration<String> keys = filenameAndAddress.keys();
        while (keys.hasMoreElements()) {
            String filename = keys.nextElement();
            if (filename.contains(key)) {
                files.add(filename);
            }
        }

        // add all neighbor's filename
        for (Map.Entry<String, NodeClient> n : neighbors.entrySet()) {
            Set<String> find = n.getValue().searchFile(new FileSearchMessage(messageId, key));
            if (find != null) {
                files.addAll(find);
            }
        }

        return files;
    }

    public static byte[] readFile(String path) {
        BufferedInputStream bufIn = null;
        List<Byte> data = new ArrayList<Byte>();
        try {
            bufIn = new BufferedInputStream(new FileInputStream(path));
            byte[] b = new byte[1024];
            int length = 0;
            while ((length = bufIn.read(b)) != -1) {
                for (int i = 0; i < length; i++) {
                    data.add(b[i]);
                }
            }
        } catch (FileNotFoundException e) {
            LOG.info("please ensure hte path is exist");
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
}
