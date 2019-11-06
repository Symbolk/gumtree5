package com.github.gumtreediff.client.smartcommit;

import com.github.gumtreediff.actions.ActionCluster;
import io.reflectoring.diffparser.api.model.Hunk;
import java.util.ArrayList;
import java.util.List;

public class DiffHunk {

  private Integer indexInFile;  // start from 1
  private String oldFilePath;
  private String newFilePath;
  private Hunk hunk;
  private List<ActionCluster> codeActions;
  private Integer oldStartLine;
  private Integer oldEndLine;
  private Integer newStartLine;
  private Integer newEndLine;

  public Integer getIndexInFile() {
    return indexInFile;
  }

  public void setIndexInFile(Integer indexInFile) {
    this.indexInFile = indexInFile;
  }

  public String getOldFilePath() {
    return oldFilePath;
  }

  public void setOldFilePath(String oldFilePath) {
    this.oldFilePath = oldFilePath;
  }

  public String getNewFilePath() {
    return newFilePath;
  }

  public void setNewFilePath(String newFilePath) {
    this.newFilePath = newFilePath;
  }

  public Hunk getHunk() {
    return hunk;
  }

  public void setHunk(Hunk hunk) {
    this.hunk = hunk;
  }

  public List<ActionCluster> getCodeActions() {
    return codeActions;
  }

  public void setCodeActions(
      List<ActionCluster> codeActions) {
    this.codeActions = codeActions;
  }

  public Integer getOldStartLine() {
    return oldStartLine;
  }

  public void setOldStartLine(Integer oldStartLine) {
    this.oldStartLine = oldStartLine;
  }

  public Integer getOldEndLine() {
    return oldEndLine;
  }

  public void setOldEndLine(Integer oldEndLine) {
    this.oldEndLine = oldEndLine;
  }

  public Integer getNewStartLine() {
    return newStartLine;
  }

  public void setNewStartLine(Integer newStartLine) {
    this.newStartLine = newStartLine;
  }

  public Integer getNewEndLine() {
    return newEndLine;
  }

  public void setNewEndLine(Integer newEndLine) {
    this.newEndLine = newEndLine;
  }

  public void addCodeAction(ActionCluster actionCluster) {
    if (this.codeActions == null) {
      this.codeActions = new ArrayList<>();
    }
    this.codeActions.add(actionCluster);
  }
}
