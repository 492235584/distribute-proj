package node.responsepojo;

import node.NodeContext;

public class FileSearchResponse {
    private String saveIp;
    private long size;
    private String filename;
    private String sourceIp;
    private int totalPart = 1;
    private int part = 1;

    public FileSearchResponse(String saveIp, long size, String filename, String sourceIp, int totalPart, int part) {
        this.saveIp = saveIp;
        this.size = size;
        this.filename = filename;
        this.sourceIp = sourceIp;
        this.totalPart = totalPart;
        this.part = part;
    }

    public FileSearchResponse(String saveIp, String filename, long size) {
        String[] strs = filename.split(NodeContext.NAMESPLIT);
        this.sourceIp = strs[0];
        this.filename = strs[1];
        this.saveIp = saveIp;
        this.size = size;

        if (strs.length == 4) {
            totalPart = Integer.valueOf(strs[2]);
            part = Integer.valueOf(strs[3]);
        }
    }

    public String getSaveIp() {
        return saveIp;
    }

    public void setSaveIp(String saveIp) {
        this.saveIp = saveIp;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public int getTotalPart() {
        return totalPart;
    }

    public void setTotalPart(int totalPart) {
        this.totalPart = totalPart;
    }

    public int getPart() {
        return part;
    }

    public void setPart(int part) {
        this.part = part;
    }

    public String completeName() {
        String name = sourceIp + NodeContext.NAMESPLIT + filename;
        if (totalPart > 1) {
            name = name + NodeContext.NAMESPLIT + totalPart + NodeContext.NAMESPLIT + part;
        }
        return name;
    }
}
