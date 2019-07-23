package com.github.gumtreediff.client;

import java.util.ArrayList;

public class ChangeAnalyzer {
    public static void main(String[] args) {
        String REPO_DIR="F:\\workspace\\dev\\gumtree";
        // run git status --porcelain to get changeset
        String output =
                Utils.runSystemCommand(
                        REPO_DIR,
                        "git",
                        "status",
                        "--porcelain");
        ArrayList<FilePair> changedFilePairs= Utils.getChangedFiles(output);
        // compute ast diff with gumtree api

        // save actions into graph

        // build links between actions
    }
}
