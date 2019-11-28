package com.github.gumtreediff.client.smartcommit;

public class DiffFile {
    private DiffFileStatus status;
    private String oldRelativePath;
    private String newRelativePath;
    private String oldContent;
    private String newContent;

    public DiffFile(DiffFileStatus status, String oldRelativePath, String newRelativePath) {
        this.status = status;
        this.oldRelativePath = oldRelativePath;
        this.newRelativePath = newRelativePath;
    }

    public DiffFile(
            DiffFileStatus status, String oldRelativePath, String newRelativePath, String oldContent, String newContent) {
        this.status = status;
        this.oldRelativePath = oldRelativePath;
        this.newRelativePath = newRelativePath;
        this.oldContent = oldContent;
        this.newContent = newContent;
    }

    public DiffFileStatus getStatus() {
        return status;
    }

    public void setStatus(DiffFileStatus status) {
        this.status = status;
    }

    public String getOldRelativePath() {
        return oldRelativePath;
    }

    public void setOldRelativePath(String oldRelativePath) {
        this.oldRelativePath = oldRelativePath;
    }

    public String getNewRelativePath() {
        return newRelativePath;
    }

    public void setNewRelativePath(String newRelativePath) {
        this.newRelativePath = newRelativePath;
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
