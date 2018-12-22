package node.requestpojo;

import java.util.ArrayList;
import java.util.List;

public class FileSearchMessage {
    private String messageId;
    private List<String> searched;
    private String key;

    public FileSearchMessage(String messageId, List<String> searched, String key) {
        this.messageId = messageId;
        this.searched = searched;
        this.key = key;
    }

    public FileSearchMessage(String messageId, String key) {
        this.messageId = messageId;
        searched = new ArrayList<>();
        this.key = key;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<String> getSearched() {
        return searched;
    }

    public void setSearched(List<String> searched) {
        this.searched = searched;
    }
}
