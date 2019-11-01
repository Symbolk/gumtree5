package com.github.gumtreediff.client.smartcommit;

import com.github.gumtreediff.actions.ActionCluster;
import io.reflectoring.diffparser.api.model.Hunk;

import java.util.List;

public class DiffHunk {
    private Hunk hunk;
    private List<ActionCluster> actionClusters;
}
