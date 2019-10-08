package com.github.gumtreediff.client.smartcommit;

import com.github.gumtreediff.actions.ActionCluster;
import com.github.gumtreediff.actions.ActionClusterFinder;
import com.github.gumtreediff.actions.ChawatheScriptGenerator;
import com.github.gumtreediff.actions.EditScript;
import com.github.gumtreediff.gen.jdt.JdtTreeGenerator;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.TreeContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepoAnalyzer {
    public static void main(String[] args) {
        // given a git repo, get the file-level change set of the working directory
        String REPO_PATH = "F:\\workspace\\dev\\IntelliMerge";
        String COMMIT_ID = "7713b8e6b5be67a4272d96b265db077020be7ba8";
        // get the HEAD and current content of changed files
        ArrayList<DiffFile> diffFiles = Utils.getChangedFilesAtCommit(REPO_PATH, COMMIT_ID);
//        ArrayList<DiffFile> diffFiles = Utils.getChangedFilesUnstaged(REPO_PATH);
        // compute ast diff with gumtree api
        Map<String, List<ActionCluster>> fileToActionCluster = new HashMap<>();
        for (DiffFile diffFile : diffFiles) {
            if (diffFile.getOldPath().endsWith(".java")) {
                // for each type of change: A D M R
                if (diffFile.getStatus().equals(DiffFileStatus.MODIFIED)) {
                    // analyze change actions with gumtreediff
                    //  cluster change by subgraphs
                    List<ActionCluster> actionClusters =
                            generateActionClusters(diffFile.getOldContent(), diffFile.getNewContent());
                    fileToActionCluster.put(diffFile.getOldPath(), actionClusters);
                    for (ActionCluster actionCluster : actionClusters) {
                        actionCluster.oldPath = diffFile.getOldPath();
                        actionCluster.newPath = diffFile.getNewPath();
                        // cluster change at method-level: extract change tuple from code change actions

                        // save ChangeClusters into neo4j

                        // run community detection to further cluster
                    }
                } else if (diffFile.getStatus().equals(DiffFileStatus.ADDED)) {
                    // ...
                }
            }
        }
    }

    /**
     * Compute diff and return edit script
     *
     * @param oldContent
     * @param newContent
     * @return
     */
    private static EditScript generateEditScript(String oldContent, String newContent) {
        JdtTreeGenerator generator = new JdtTreeGenerator();
        //        Generators generator = Generators.getInstance();
        try {
            TreeContext oldContext = generator.generateFrom().string(oldContent);
            TreeContext newContext = generator.generateFrom().string(newContent);
            Matcher matcher = Matchers.getInstance().getMatcher();

            MappingStore mappings = matcher.match(oldContext.getRoot(), newContext.getRoot());
            EditScript editScript = new ChawatheScriptGenerator().computeActions(mappings);
            return editScript;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Compute diff and return actions in clusters
     *
     * @param oldContent
     * @param newContent
     * @return
     */
    public static List<ActionCluster> generateActionClusters(String oldContent, String newContent) {
        List<ActionCluster> actionClusters = new ArrayList<>();
        JdtTreeGenerator generator = new JdtTreeGenerator();
        //        Generators generator = Generators.getInstance();
        try {
            TreeContext oldContext = generator.generateFrom().string(oldContent);
            TreeContext newContext = generator.generateFrom().string(newContent);
            Matcher matcher = Matchers.getInstance().getMatcher();

            MappingStore mappings = matcher.match(oldContext.getRoot(), newContext.getRoot());
            EditScript editScript = new ChawatheScriptGenerator().computeActions(mappings);

            ActionClusterFinder finder = new ActionClusterFinder(oldContext, newContext, editScript);
            actionClusters = finder.getActionClusters();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return actionClusters;
    }
}
