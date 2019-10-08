package com.github.gumtreediff.client.smartcommit;

public class DiffFile {
    private DiffFileStatus changeType;
    private String oldPath;
    private String newPath;
    private String oldContent;
    private String newContent;

    public DiffFile(DiffFileStatus changeType, String oldPath, String newPath) {
        this.changeType = changeType;
        this.oldPath = oldPath;
        this.newPath = newPath;
    }

    public DiffFile(DiffFileStatus changeType, String oldPath, String newPath, String oldContent, String newContent) {
        this.changeType = changeType;
        this.oldPath = oldPath;
        this.newPath = newPath;
        this.oldContent = oldContent;
        this.newContent = newContent;
    }

    public DiffFileStatus getChangeType() {
        return changeType;
    }

    public void setChangeType(DiffFileStatus changeType) {
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

    public String getOldContent() {
        return oldContent;
    }

    public void setOldContent(String oldContent) {
        this.oldContent = oldContent;
    }

    public String getNewContent() {
        return newContent;
    }

    public void setNewContent(String newContent) {
        this.newContent = newContent;
    }
}
