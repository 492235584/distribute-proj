package node.requestpojo;

import java.util.ArrayList;
import java.util.List;

public class NodeSearchMessage {
    private String messageId;
    private List<String> searched;

    public NodeSearchMessage(String messageId, List<String> searched, String key) {
        this.messageId = messageId;
        this.searched = searched;
    }

    public NodeSearchMessage(String messageId, String key) {
        this.messageId = messageId;
        searched = new ArrayList<>();
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public List<String> getSearched() {
        return searched;
    }

    public void setSearched(List<String> searched) {
        this.searched = searched;
    }
}
