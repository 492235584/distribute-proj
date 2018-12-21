package node.UI;

public class FilenameAndIp {
    private String filename;
    private String ip;
    private long size;

    public FilenameAndIp(String filename, String ip) {
        this.filename = filename;
        this.ip = ip;
    }

    public FilenameAndIp(String filename, String ip, int size) {
        this.filename = filename;
        this.ip = ip;
        this.size = size;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FilenameAndIp that = (FilenameAndIp) o;

        if (!filename.equals(that.filename)) return false;
        return ip.equals(that.ip);
    }

    @Override
    public int hashCode() {
        int result = filename.hashCode();
        result = 31 * result + ip.hashCode();
        return result;
    }
}
