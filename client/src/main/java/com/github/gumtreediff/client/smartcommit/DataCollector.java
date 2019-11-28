package com.github.gumtreediff.client.smartcommit;

import java.io.File;
import java.util.ArrayList;

/**
 * Collect and write the diff file content into temp folders
 */
public class DataCollector {
    public static void main(String[] args) {
        String REPO_NAME = "guava";
        String REPO_DIR = "D:\\github\\repos\\" + REPO_NAME;
        String DATA_DIR = "D:\\commit_data";
        String commitID = "dcf63a6c97dfde";
        GitService gitService = new GitServiceCGit();

        ArrayList<DiffFile> filePairs = gitService.getChangedFilesAtCommit(REPO_DIR, commitID);
        // write old/new content to disk
        for (DiffFile filePair : filePairs) {
            // currently only collect MODIFIED Java files
            if (filePair.getOldRelativePath().endsWith(".java") && filePair.getStatus().equals(DiffFileStatus.MODIFIED)) {
                String dir = DATA_DIR + File.separator + REPO_NAME + File.separator + commitID + File.separator;
                String aPath = dir + "a" + File.separator + filePair.getOldRelativePath();
                String bPath = dir + "b" + File.separator + filePair.getNewRelativePath();
                boolean aOk = Utils.writeContentToPath(aPath, filePair.getOldContent());
                boolean bOk = Utils.writeContentToPath(bPath, filePair.getNewContent());
                if (!(aOk && bOk)) {
                    System.out.println("Error with: " + filePair.getOldRelativePath());
                } else {
                    System.out.println(aPath);
                    System.out.println(bPath);
                }
            }
        }
    }
}
