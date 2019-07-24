package com.github.gumtreediff.client;

import com.github.gumtreediff.actions.ChawatheScriptGenerator;
import com.github.gumtreediff.actions.EditScript;
import com.github.gumtreediff.gen.jdt.JdtTreeGenerator;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.TreeContext;

import java.util.ArrayList;

public class ChangeAnalyzer {
    public static void main(String[] args) {
        String REPO_DIR = "F:\\workspace\\dev\\gumtree";
        // run git status --porcelain to get changeset
        String output =
                Utils.runSystemCommand(
                        REPO_DIR,
                        "git",
                        "status",
                        "--porcelain");
        ArrayList<FilePair> changedFilePairs = Utils.getChangedFiles(REPO_DIR, output);
        // compute ast diff with gumtree api
        for (FilePair filePair : changedFilePairs) {
            if (filePair.getChangeType().equals(ChangeType.MODIFIED) && filePair.getNewPath().endsWith(".java")) {
                computeDiff(filePair.getOldContent(), filePair.getNewContent());
            }
        }
        // save actions into graph

        // build links between actions
    }

    private static EditScript computeDiff(String oldContent, String newContent) {
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
}
