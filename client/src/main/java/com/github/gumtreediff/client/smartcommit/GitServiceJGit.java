package com.github.gumtreediff.client.smartcommit;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of helper functions based on jGit (the java implementation of Git).
 */
public class GitServiceJGit implements GitService {
    @Override
    public ArrayList<DiffFile> getChangedFilesInWorkingTree(String repoDir) {
        return null;
    }

    @Override
    public ArrayList<DiffFile> getChangedFilesAtCommit(String repoDir, String commitID) {
        return null;
    }

    @Override
    public List<DiffHunk> getDiffHunksInWorkingTree(String repoPath) {
        return null;
    }

    @Override
    public List<DiffHunk> getDiffHunksAtCommit(String repoPath, String commitID) {
        return null;
    }

    @Override
    public String getContentAtHEAD(String repoDir, String relativePath) {
        return null;
    }

    @Override
    public String getContentAtCommit(String repoDir, String relativePath, String commitID) {
        return null;
    }
}
