package com.github.mostroverkhov.firebase_rsocket.model;

import java.util.Arrays;

/** Created by Maksym Ostroverkhov on 27.02.17. */
public class Path {
  private final String[] childPaths;

  public Path(String... childPaths) {
    this.childPaths = childPaths;
  }

  public String[] getChildPaths() {
    return childPaths;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Path{");
    sb.append("children=").append(Arrays.toString(childPaths));
    sb.append('}');
    return sb.toString();
  }
}
