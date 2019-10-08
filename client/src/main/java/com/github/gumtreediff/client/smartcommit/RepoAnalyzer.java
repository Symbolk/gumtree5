package com.github.gumtreediff.client.smartcommit;

import com.github.gumtreediff.actions.ActionCluster;
import com.github.gumtreediff.actions.ActionClusterFinder;
import com.github.gumtreediff.actions.ChawatheScriptGenerator;
import com.github.gumtreediff.actions.EditScript;
import com.github.gumtreediff.gen.jdt.JdtTreeGenerator;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;

import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class RepoAnalyzer {
    public static void main(String[] args) {
        // 1. process local repo
        String REPO_NAME = "guava";
        String REPO_DIR = "D:\\github\\repos\\" + REPO_NAME;
        String DATA_DIR = "D:\\commit_data";
        String commitID = "dcf63a6c97dfde";

        Graph<ActionCluster, DefaultWeightedEdge> graph = initGraph();

        ArrayList<DiffFile> filePairs = Utils.getChangedFilesAtCommit(REPO_DIR, commitID);
//        ArrayList<FilePair> filePairs = Utils.getChangedFilesUnstaged(REPO_DIR);
        // compute ast diff with gumtree api
        Map<String, List<ActionCluster>> fileToActionCluster = new HashMap<>();
        for (DiffFile filePair : filePairs) {
            if (filePair.getOldPath().endsWith(".java")) {
                if (filePair.getChangeType().equals(DiffFileStatus.MODIFIED)) {
                    List<ActionCluster> actionClusters = generateActionClusters(filePair.getOldContent(), filePair.getNewContent());
                    fileToActionCluster.put(filePair.getOldPath(), actionClusters);
                    // save action clusters into graph as vertices
                    for (ActionCluster actionCluster : actionClusters) {
                        actionCluster.oldPath = filePair.getOldPath();
                        actionCluster.newPath = filePair.getNewPath();
                        graph.addVertex(actionCluster);
                    }
                } else if (filePair.getChangeType().equals(DiffFileStatus.ADDED)) {
                    //...
                }
            }
        }

        // check def-use between action clusters as edges
        // naive version: number of overlapping identifiers as the weight
        System.out.println(graph.vertexSet().size());
        for (ActionCluster action1 : graph.vertexSet()) {
            for (ActionCluster action2 : graph.vertexSet()) {
                if (!action1.equals(action2)) {
                    int weight = computeIntersection(action1.leafNodes, action2.leafNodes);
                    if (weight > 0) {
                        DefaultWeightedEdge edge = graph.addEdge(action1, action2);
                        graph.setEdgeWeight(edge, weight);
                    }
                }
            }
        }
        // use a socket to connect to lang server
        // send init json with the file(s) content
        // get all symbols in diff areas
        // find def&ref, and check if it in other diff area
        // send request to find def/ref of symbols in action clusters
        // build links in graph

        graph.edgeSet();
    }

    private static int computeIntersection(Set<ITree> set1, Set<ITree> set2) {
        Set<String> intersection = new HashSet<>();
        Set<String> stringSet1 = set1.stream().map(iTree -> iTree.getLabel()).collect(Collectors.toSet());
        Set<String> stringSet2 = set2.stream().map(iTree -> iTree.getLabel()).collect(Collectors.toSet());
        intersection.addAll(stringSet1);
        intersection.retainAll(stringSet2);
        return intersection.size();
    }

    /**
     * Build and initialize an empty Graph
     *
     * @return
     */
    private static Graph<ActionCluster, DefaultWeightedEdge> initGraph() {
        return GraphTypeBuilder.<ActionCluster, DefaultWeightedEdge>directed()
                .allowingMultipleEdges(true)
                .allowingSelfLoops(false)
                .edgeClass(DefaultWeightedEdge.class)
                .weighted(true)
                .buildGraph();
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
