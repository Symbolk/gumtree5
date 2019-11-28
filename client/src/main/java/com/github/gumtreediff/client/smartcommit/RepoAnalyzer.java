package com.github.gumtreediff.client.smartcommit;

import com.github.gumtreediff.actions.ActionCluster;
import com.github.gumtreediff.actions.EditScript;
//import com.github.gumtreediff.gen.jdt.JdtTreeGenerator;
import io.reflectoring.diffparser.api.model.Diff;
import io.reflectoring.diffparser.api.model.Hunk;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;


import java.util.*;
import java.util.stream.Collectors;

public class RepoAnalyzer {
    private String repoPath;
    private String commitID;
    private List<DiffFile> diffFiles;
    private List<DiffHunk> diffHunks;

    public RepoAnalyzer(String repoPath) {
        this.repoPath = repoPath;
    }

    public RepoAnalyzer(String repoPath, String commitID) {
        this.repoPath = repoPath;
        this.commitID = commitID;
    }

    public String getRepoPath() {
        return repoPath;
    }

    public String getCommitID() {
        return commitID;
    }

    public List<DiffFile> getDiffFiles() {
        return diffFiles;
    }

    public void setDiffFiles(List<DiffFile> diffFiles) {
        this.diffFiles = diffFiles;
    }

    public List<DiffHunk> getDiffHunks() {
        return diffHunks;
    }

    public void setDiffHunks(List<DiffHunk> diffHunks) {
        this.diffHunks = diffHunks;
    }

    public static void main(String[] args) {
        // given a git repo, get the file-level change set of the working directory
        String REPO_PATH = "/Users/symbolk/coding/dev/IntelliMerge";
        String COMMIT_ID = "53c1c430de96e459fc6b633d20c328eaff7d0374";
        GitService gitService = new GitServiceCGit();
        RepoAnalyzer repoAnalyzer = new RepoAnalyzer(REPO_PATH, COMMIT_ID);

        // collect the changed files and diff hunks
        ArrayList<DiffFile> diffFiles = gitService.getChangedFilesAtCommit(REPO_PATH, COMMIT_ID);
//        ArrayList<DiffFile> diffFiles = gitService.getChangedFilesInWorkingTree(REPO_PATH);
        List<DiffHunk> diffHunks = gitService.getDiffHunksAtCommit(REPO_PATH, COMMIT_ID);
//        List<DiffHunk> diffHunks = gitService.getDiffHunksInWorkingTree(REPO_PATH);
        repoAnalyzer.setDiffFiles(diffFiles);
        repoAnalyzer.setDiffHunks(diffHunks);

        // find the AST nodes covered by each diff hunk
        for (DiffFile diffFile : diffFiles) {
            // get all diff hunks within this file
            List<DiffHunk> diffHunksInFile = repoAnalyzer.getDiffHunksInFile(diffFile, diffHunks);
            // parse the changed files into ASTs
            Pair<CompilationUnit, CompilationUnit> CUPair = repoAnalyzer.generateCUPair(diffFile);

            for (DiffHunk diffHunk : diffHunksInFile) {

            }

//            if (CUPair.getLeft() != null) {
//                // TODO determine the length
//                NodeFinder nodeFinder = new NodeFinder(CUPair.getLeft(), , );
//                ASTNode coveredNode = nodeFinder.getCoveredNode();
//                if (coveredNode == null) {
//                    coveredNode = nodeFinder.getCoveringNode();
//                }
//            }
//
//            if (CUPair.getRight() != null) {
//
//            }
        }
        // find nodes covered or covering by each diff hunk

        // resolve symbols to get fully qualified name

        // build nodes for diff hunks and unchanged nodes

        // visualize the graph


    }

    /**
     * Currently for Java 8
     *
     * @param diffFile
     * @return
     */
    private Pair<CompilationUnit, CompilationUnit> generateCUPair(DiffFile diffFile) {
        // set up the parser and resolver options
        ASTParser parser = ASTParser.newParser(8);
        parser.setResolveBindings(true);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setBindingsRecovery(true);
        Map options = JavaCore.getOptions();
        parser.setCompilerOptions(options);

        // set up the arguments
        String[] sources = {this.repoPath}; // sources to resolve symbols
        String[] classpath = {"/Library/Java/JavaVirtualMachines/jdk1.8.0_231.jdk/Contents/Home/jre/lib/rt.jar"}; // local java runtime (rt.jar) path
        parser.setEnvironment(classpath, sources, new String[]{"UTF-8"}, true);

        parser.setUnitName(Utils.getFileNameFromPath(diffFile.getOldRelativePath()));
        parser.setSource(diffFile.getOldContent().toCharArray());
        CompilationUnit oldCU = (CompilationUnit) parser.createAST(null);

        parser.setUnitName(Utils.getFileNameFromPath(diffFile.getNewRelativePath()));
        parser.setSource(diffFile.getNewContent().toCharArray());
        CompilationUnit newCU = (CompilationUnit) parser.createAST(null);
        return Pair.of(oldCU, newCU);
    }

    /**
     * Filter the diff hunks within the given diff file
     *
     * @param diffFile
     * @param diffHunks
     * @return
     */
    private List<DiffHunk> getDiffHunksInFile(DiffFile diffFile, List<DiffHunk> diffHunks) {
        String oldRelativePath = diffFile.getOldRelativePath();
        String newRelativePath = diffFile.getNewRelativePath();

        List<DiffHunk> results =
                diffHunks.stream().filter(diffHunk -> diffHunk.getOldRelativePath().contains(oldRelativePath) && diffHunk.getNewRelativePath().contains(newRelativePath)).collect(Collectors.toList());
        return results;
    }
}
