package com.github.gumtreediff.client.smartcommit;

import io.reflectoring.diffparser.api.DiffParser;
import io.reflectoring.diffparser.api.UnifiedDiffParser;
import io.reflectoring.diffparser.api.model.Diff;
import io.reflectoring.diffparser.api.model.Hunk;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.github.gumtreediff.client.smartcommit.Utils.readFileToString;

/**
 * Implementation of helper functions based on the output of git commands.
 */
public class GitServiceCGit implements GitService {
    /**
     * Get the diff files in the current working tree
     *
     * @return
     */
    @Override
    public ArrayList<DiffFile> getChangedFilesInWorkingTree(String repoDir) {
        // run git status --porcelain to get changeset
        String output = Utils.runSystemCommand(repoDir, "git", "status", "--porcelain");
        ArrayList<DiffFile> DiffFileList = new ArrayList<>();
        String lines[] = output.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            String temp[] = lines[i].trim().split("\\s+");
            String symbol = temp[0];
            String relativePath = temp[1];
            String absolutePath = repoDir + File.separator + relativePath;
            DiffFileStatus status = Utils.convertSymbolToStatus(symbol);
            DiffFile DiffFile = null;
            switch (status) {
                case MODIFIED:
                    DiffFile =
                            new DiffFile(
                                    status,
                                    relativePath,
                                    relativePath,
                                    getContentAtHEAD(repoDir, relativePath),
                                    readFileToString(absolutePath));
                    break;
                case ADDED:
                case UNTRACKED:
                    DiffFile = new DiffFile(status, "", relativePath, "", readFileToString(absolutePath));
                    break;
                case DELETED:
                    DiffFile =
                            new DiffFile(status, relativePath, "", getContentAtHEAD(repoDir, relativePath), "");
                    break;
                case RENAMED:
                case COPIED:
                    if (temp.length == 4) {
                        String oldPath = temp[1];
                        String newPath = temp[3];
                        String newAbsPath = repoDir + File.separator + temp[3];
                        DiffFile =
                                new DiffFile(
                                        status,
                                        oldPath,
                                        newPath,
                                        getContentAtHEAD(repoDir, oldPath),
                                        readFileToString(newAbsPath));
                    }
                    break;
                default:
                    break;
            }
            if (DiffFile != null) {
                DiffFileList.add(DiffFile);
            }
        }
        return DiffFileList;
    }

    /**
     * Get the diff files between one commit and its previous commit
     *
     * @return
     */
    @Override
    public ArrayList<DiffFile> getChangedFilesAtCommit(String repoDir, String commitID) {
        String output =
                Utils.runSystemCommand(repoDir, "git", "diff", "--name-status", commitID, commitID + "~");
        ArrayList<DiffFile> DiffFileList = new ArrayList<>();
        String lines[] = output.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            String temp[] = lines[i].trim().split("\\s+");
            String symbol = temp[0];
            String relativePath = temp[1];
            //            String absolutePath = repoDir + File.separator + relativePath;
            DiffFileStatus status = Utils.convertSymbolToStatus(symbol);
            DiffFile DiffFile = null;
            switch (status) {
                case MODIFIED:
                    DiffFile =
                            new DiffFile(
                                    status,
                                    relativePath,
                                    relativePath,
                                    getContentAtCommit(repoDir, relativePath, commitID + "~"),
                                    getContentAtCommit(repoDir, relativePath, commitID));
                    break;
                case ADDED:
                case UNTRACKED:
                    DiffFile =
                            new DiffFile(
                                    status,
                                    "",
                                    relativePath,
                                    "",
                                    getContentAtCommit(repoDir, relativePath, commitID));
                    break;
                case DELETED:
                    DiffFile =
                            new DiffFile(
                                    status,
                                    relativePath,
                                    "",
                                    getContentAtCommit(repoDir, relativePath, commitID + "~"),
                                    "");
                    break;
                case RENAMED:
                case COPIED:
                    if (temp.length == 4) {
                        String oldPath = temp[1];
                        String newPath = temp[3];
                        DiffFile =
                                new DiffFile(
                                        status,
                                        oldPath,
                                        newPath,
                                        getContentAtCommit(repoDir, oldPath, commitID + "~"),
                                        getContentAtCommit(repoDir, newPath, commitID));
                    }
                    break;
                default:
                    break;
            }
            if (DiffFile != null) {
                DiffFileList.add(DiffFile);
            }
        }
        return DiffFileList;
    }

    @Override
    public List<DiffHunk> getDiffHunksInWorkingTree(String repoPath) {
        String diffOutput =
                Utils.runSystemCommand(
                        repoPath,
                        "git",
                        "diff", "-U0");
        DiffParser parser = new UnifiedDiffParser();
        List<Diff> diffs = parser.parse(new ByteArrayInputStream(diffOutput.getBytes()));
        return generateDiffHunks(diffs);
    }

    /**
     * Generate diff hunks from diffs parsed from git-diff output
     *
     * @param diffs
     * @return
     */
    private List<DiffHunk> generateDiffHunks(List<Diff> diffs) {
        List<DiffHunk> allDiffHunks = new ArrayList<>();
        for (Diff diff : diffs) {
            // the index of the diff hunk in the current file diff, start from 1
            Integer index = 0;
            for (Hunk hunk : diff.getHunks()) {
                index++;

                DiffHunk diffHunk = new DiffHunk();
                diffHunk.setIndexInFile(index);
                diffHunk.setOldRelativePath(diff.getFromFileName());
                diffHunk.setNewRelativePath(diff.getToFileName());
                diffHunk.setHunk(hunk);
                diffHunk.setOldStartLine(hunk.getFromFileRange().getLineStart());
                diffHunk.setOldEndLine(hunk.getFromFileRange().getLineStart() + hunk.getFromFileRange().getLineCount());
                diffHunk.setNewStartLine(hunk.getToFileRange().getLineStart());
                diffHunk.setNewEndLine(hunk.getToFileRange().getLineStart() + hunk.getToFileRange().getLineCount());
                allDiffHunks.add(diffHunk);
            }
        }
        return allDiffHunks;
    }

    /**
     * Get the diff hunks between one commit and its previous commit
     *
     * @param repoPath
     * @param commitID
     * @return
     */
    @Override
    public List<DiffHunk> getDiffHunksAtCommit(String repoPath, String commitID) {
        String diffOutput =
                Utils.runSystemCommand(
                        repoPath,
                        "git",
                        "diff", "-U0", commitID, commitID + "~");
        DiffParser parser = new UnifiedDiffParser();
        // TODO fix the bug within the library when parsing added files
        List<Diff> diffs = parser.parse(new ByteArrayInputStream(diffOutput.getBytes()));
        return generateDiffHunks(diffs);
    }

    /**
     * Get the file content at HEAD
     *
     * @param relativePath
     * @return
     */
    @Override
    public String getContentAtHEAD(String repoDir, String relativePath) {
        String output = Utils.runSystemCommand(repoDir, "git", "show", "HEAD:" + relativePath);
        if (output != null) {
            return output;
        } else {
            return "";
        }
    }

    /**
     * Get the file content at one specific commit
     *
     * @param relativePath
     * @returnØØ
     */
    @Override
    public String getContentAtCommit(String repoDir, String relativePath, String commitID) {
        String output = Utils.runSystemCommand(repoDir, "git", "show", commitID + ":" + relativePath);
        if (output != null) {
            return output;
        } else {
            return "";
        }
    }
}
