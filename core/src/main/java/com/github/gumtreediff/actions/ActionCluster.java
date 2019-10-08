package com.github.gumtreediff.actions;


import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.ITree;

import java.util.Set;

public class ActionCluster {
    public String oldPath;
    public String newPath;
    public Set<Action> actions;
    public Action rootAction;
    public Set<ITree> leafNodes;

    public ActionCluster(Set<Action> actions, Action rootAction, Set<ITree> leafNodes) {
        this.actions = actions;
        this.rootAction = rootAction;
        this.leafNodes = leafNodes;
    }

    public ActionCluster(String oldPath, String newPath, Set<Action> actions, Action rootAction, Set<ITree> leafNodes) {
        this.oldPath = oldPath;
        this.newPath = newPath;

        this.actions = actions;
        this.rootAction = rootAction;
        this.leafNodes = leafNodes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        // TODO more elegant
        result = prime * result + ((rootAction == null) ? 0 : rootAction.hashCode());
        result = prime * result + ((actions == null) ? 0 : actions.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "ActionCluster{" +
                "operation=" + rootAction.getName() +
                ", nodeType=" + rootAction.getNode().getType().name +
                ", label=" + rootAction.getNode().getLabel() +
                "[" + rootAction.getNode().getStartLine() + "-" + rootAction.getNode().getEndLine() +
                '}';
    }

    public String asString() {
        // TODO more elegant
        return actions.toString() + leafNodes.toString();
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof ActionCluster) && (asString().equals(((ActionCluster) o).asString()));
    }
}