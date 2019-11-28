package com.github.gumtreediff.client.smartcommit;

import com.github.gumtreediff.actions.ActionCluster;
import com.github.gumtreediff.actions.EditScript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GumtreeService {
    /**
     * Compute change actions with guand bind with diff hunks
     *
     * @param diffFiles
     * @param diffHunks
     */
    private static void generateChangeActions(List<DiffFile> diffFiles, List<DiffHunk> diffHunks) {
        // compute ast diff with gumtree api
        Map<String, List<ActionCluster>> fileToActionCluster = new HashMap<>();
        for (DiffFile diffFile : diffFiles) {
            if (diffFile.getOldRelativePath().endsWith(".java")) {
                // for each type of change: A D M R
                if (diffFile.getStatus().equals(DiffFileStatus.MODIFIED)) {
                    // analyze change actions with gumtreediff
                    //  cluster change by subgraphs
                    List<ActionCluster> actionClusters =
                            generateActionClusters(diffFile.getOldContent(), diffFile.getNewContent());
                    fileToActionCluster.put(diffFile.getOldRelativePath(), actionClusters);
                    for (ActionCluster actionCluster : actionClusters) {
                        actionCluster.oldPath = diffFile.getOldRelativePath();
                        actionCluster.newPath = diffFile.getNewRelativePath();
                        // add actions into diff hunks
                        Integer startLine = actionCluster.rootAction.getNode().getStartLine();
                        Integer endLine = actionCluster.rootAction.getNode().getEndLine();
                        for (DiffHunk diffHunk : diffHunks) {
                            // a0 <= b1 && a1 >= b0
                            if (diffHunk.getOldStartLine() <= endLine && diffHunk.getOldEndLine() >= startLine) {
                                diffHunk.addCodeAction(actionCluster);
                            }
                        }

                        // resolve symbol bindings to build edges
                        // add actions to neo4j
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
//        JdtTreeGenerator generator = new JdtTreeGenerator();
//        //        Generators generator = Generators.getInstance();
//        try {
//            TreeContext oldContext = generator.generateFrom().string(oldContent);
//            TreeContext newContext = generator.generateFrom().string(newContent);
//            Matcher matcher = Matchers.getInstance().getMatcher();
//
//            MappingStore mappings = matcher.match(oldContext.getRoot(), newContext.getRoot());
//            EditScript editScript = new ChawatheScriptGenerator().computeActions(mappings);
//            return editScript;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return null;
    }

    /**
     * Compute diff and return actions
     * in clusters
     *
     * @param oldContent
     * @param newContent
     * @return
     */
    public static List<ActionCluster> generateActionClusters(String oldContent, String newContent) {
        List<ActionCluster> actionClusters = new ArrayList<>();
//        JdtTreeGenerator generator = new JdtTreeGenerator();
//        //        Generators generator = Generators.getInstance();
//        try {
//            TreeContext oldContext = generator.generateFrom().string(oldContent);
//            TreeContext newContext = generator.generateFrom().string(newContent);
//            Matcher matcher = Matchers.getInstance().getMatcher();
//
//            MappingStore mappings = matcher.match(oldContext.getRoot(), newContext.getRoot());
//            EditScript editScript = new ChawatheScriptGenerator().computeActions(mappings);
//
//            ActionClusterFinder finder = new ActionClusterFinder(oldContext, newContext, editScript);
//            actionClusters = finder.getActionClusters();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return actionClusters;
    }
}
