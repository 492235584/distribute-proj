package node.requestpojo;

public class DistributeCalculateMessage {
    private String messageId;
    private String srcIp;
    private String[] data;

    public DistributeCalculateMessage(String messageId, String srcIp, String[] data) {
        this.messageId = messageId;
        this.srcIp = srcIp;
        this.data = data;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSrcIp() {
        return srcIp;
    }

    public void setSrcIp(String srcIp) {
        this.srcIp = srcIp;
    }

    public String[] getData() {
        return data;
    }

    public void setData(String[] data) {
        this.data = data;
    }
}
