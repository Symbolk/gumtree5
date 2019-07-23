package com.github.gumtreediff.client;

public class FilePair {
    private ChangeType changeType;
    private String oldPath;
    private String newPath;

    public FilePair(ChangeType changeType, String oldPath, String newPath) {
        this.changeType = changeType;
        this.oldPath = oldPath;
        this.newPath = newPath;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(ChangeType changeType) {
        this.changeType = changeType;
    }

    public String getOldPath() {
        return oldPath;
    }

    public void setOldPath(String oldPath) {
        this.oldPath = oldPath;
    }

    public String getNewPath() {
        return newPath;
    }

    public void setNewPath(String newPath) {
        this.newPath = newPath;
    }
}
