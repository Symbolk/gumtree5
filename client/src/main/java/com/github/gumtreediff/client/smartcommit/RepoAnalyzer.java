package com.github.gumtreediff.client.smartcommit;

import com.github.gumtreediff.actions.ActionCluster;
import com.github.gumtreediff.actions.ActionClusterFinder;
import com.github.gumtreediff.actions.ChawatheScriptGenerator;
import com.github.gumtreediff.actions.EditScript;
//import com.github.gumtreediff.gen.jdt.JdtTreeGenerator;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.TreeContext;
import io.reflectoring.diffparser.api.model.Diff;
import io.reflectoring.diffparser.api.model.Hunk;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.NodeFinder;


import java.util.*;

public class RepoAnalyzer {
    public static void main(String[] args) {
        // given a git repo, get the file-level change set of the working directory
        String REPO_PATH = "/Users/symbolk/coding/dev/IntelliMerge";
        String COMMIT_ID = "7713b8e6b5be67a4272d96b265db077020be7ba8";

        // get the HEAD and current content of changed files
        ArrayList<DiffFile> diffFiles = Utils.getChangedFilesAtCommit(REPO_PATH, COMMIT_ID);
//        ArrayList<DiffFile> diffFiles = Utils.getChangedFilesInWorkingTree(REPO_PATH);
        List<Diff> diffResults = Utils.getDiffAtCommit(REPO_PATH, COMMIT_ID);
        List<DiffHunk> allDiffHunks = new ArrayList<>();
        for (Diff diff : diffResults) {
            Integer index = 0;
            String oldFilePath = Utils.generatePathFromName(REPO_PATH, diff.getFromFileName());
            String newFilePath = Utils.generatePathFromName(REPO_PATH, diff.getToFileName());

            Pair<CompilationUnit, CompilationUnit> CUs = generateCUs(diffFiles, diff.getFromFileName(), diff.getToFileName());
            for (Hunk hunk : diff.getHunks()) {
                index++;
                DiffHunk diffHunk = new DiffHunk();
                diffHunk.setIndexInFile(index);

                diffHunk.setOldFilePath(oldFilePath);
                diffHunk.setNewFilePath(newFilePath);
                diffHunk.setHunk(hunk);
                int oldStartLine = hunk.getFromFileRange().getLineStart();
                int oldEndLine = hunk.getFromFileRange().getLineStart() + hunk.getFromFileRange().getLineCount();
                int newStartLine = hunk.getToFileRange().getLineStart();
                int newEndLine = hunk.getToFileRange().getLineStart() + hunk.getToFileRange().getLineCount();
                diffHunk.setOldStartLine(oldStartLine);
                diffHunk.setOldEndLine(oldEndLine);
                diffHunk.setNewStartLine(newStartLine);
                diffHunk.setNewEndLine(newEndLine);
                allDiffHunks.add(diffHunk);
                if (CUs.getLeft() != null) {

                }

                if (CUs.getRight() != null) {

                }
            }
        }
        // find nodes covered or covering by each diff hunk

        // resolve symbols to get fully qualified name

        // build nodes for diff hunks and unchanged nodes

        // visualize the graph

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
                        // add actions into diff hunks
                        Integer startLine = actionCluster.rootAction.getNode().getStartLine();
                        Integer endLine = actionCluster.rootAction.getNode().getEndLine();
                        for (DiffHunk diffHunk : allDiffHunks) {
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

    private static Pair<CompilationUnit, CompilationUnit> generateCUs(List<DiffFile> diffFiles, String oldFilePath, String newFilePath) {
        // set up the parser and resolver options
        ASTParser parser = ASTParser.newParser(8);
        parser.setResolveBindings(true);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setBindingsRecovery(true);
        Map options = JavaCore.getOptions();
        parser.setCompilerOptions(options);

        // set up the arguments
        String[] sources = {}; // sources to resolve symbols
        String[] classpath = {}; // local java rt.java
        parser.setEnvironment(classpath, sources, new String[]{"UTF-8"}, true);
        // get the code content string
        Optional<DiffFile> diffFile = diffFiles.stream().filter(file -> {
            return oldFilePath.contains(file.getOldPath()) && newFilePath.contains(file.getNewPath());
        }).findFirst();
        if (diffFile.isPresent()) {
            parser.setUnitName(oldFilePath.substring(oldFilePath.lastIndexOf("/") + 1));
            parser.setSource(diffFile.get().getOldContent().toCharArray());
            CompilationUnit oldCU = (CompilationUnit) parser.createAST(null);

            parser.setUnitName(newFilePath.substring(newFilePath.lastIndexOf("/") + 1));
            parser.setSource(diffFile.get().getNewContent().toCharArray());
            CompilationUnit newCU = (CompilationUnit) parser.createAST(null);
            return Pair.of(oldCU, newCU);
        } else {
            return Pair.of(null, null);
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
