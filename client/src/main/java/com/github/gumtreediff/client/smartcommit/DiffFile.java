package com.github.gumtreediff.client.smartcommit;

public class DiffFile {
    private DiffFileStatus status;
    private String oldPath;
    private String newPath;
    private String oldContent;
    private String newContent;

    public DiffFile(DiffFileStatus status, String oldPath, String newPath) {
        this.status = status;
        this.oldPath = oldPath;
        this.newPath = newPath;
    }

    public DiffFile(
            DiffFileStatus status, String oldPath, String newPath, String oldContent, String newContent) {
        this.status = status;
        this.oldPath = oldPath;
        this.newPath = newPath;
        this.oldContent = oldContent;
        this.newContent = newContent;
    }

    public DiffFileStatus getStatus() {
        return status;
    }

    public void setStatus(DiffFileStatus status) {
        this.status = status;
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
