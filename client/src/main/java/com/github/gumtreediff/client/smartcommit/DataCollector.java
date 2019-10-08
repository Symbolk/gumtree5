package com.github.gumtreediff.client.smartcommit;

import java.io.File;
import java.util.ArrayList;

public class DataCollector {
    public static void main(String[] args) {
        String REPO_NAME = "guava";
        String REPO_DIR = "D:\\github\\repos\\" + REPO_NAME;
        String DATA_DIR = "D:\\commit_data";
        String commitID = "dcf63a6c97dfde";

        ArrayList<DiffFile> filePairs = Utils.getChangedFilesAtCommit(REPO_DIR, commitID);
        // write old/new content to disk
        for (DiffFile filePair : filePairs) {
            // currently only collect MODIFIED Java files
            if (filePair.getOldPath().endsWith(".java") && filePair.getChangeType().equals(DiffFileStatus.MODIFIED)) {
                String dir = DATA_DIR + File.separator + REPO_NAME + File.separator + commitID + File.separator;
                String aPath = dir + "a" + File.separator + filePair.getOldPath();
                String bPath = dir + "b" + File.separator + filePair.getNewPath();
                boolean aOk = Utils.writeContentToPath(aPath, filePair.getOldContent());
                boolean bOk = Utils.writeContentToPath(bPath, filePair.getNewContent());
                if (!(aOk && bOk)) {
                    System.out.println("Error with: " + filePair.getOldPath());
                } else {
                    System.out.println(aPath);
                    System.out.println(bPath);
                }
            }
        }
    }
}
