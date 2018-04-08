package com.github.mostroverkhov.firebase_rsocket.model.delete;

import java.util.Arrays;

/** Created with IntelliJ IDEA. Author: mostroverkhov */
public class DeleteResponse {
  private final String[] pathChildren;

  public DeleteResponse(String[] pathChildren) {
    assertArgs(pathChildren);
    this.pathChildren = pathChildren;
  }

  public String[] getPathChildren() {
    return pathChildren;
  }

  private static void assertArgs(String[] pathChildren) {
    if (pathChildren == null) {
      throw new IllegalArgumentException("Path should not be null");
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("DeleteResponse{");
    sb.append("pathChildren=").append(Arrays.toString(pathChildren));
    sb.append('}');
    return sb.toString();
  }
}
