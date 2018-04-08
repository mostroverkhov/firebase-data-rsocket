package com.github.mostroverkhov.firebase_rsocket;

/** Created by Maksym Ostroverkhov on 01.03.17. */
public class Data {
  private String data;
  private String id;

  public Data(String data, String id) {
    this.data = data;
    this.id = id;
  }

  public Data() {}

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return "Data{" + "data='" + data + '\'' + ", id='" + id + '\'' + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Data data1 = (Data) o;

    if (data != null ? !data.equals(data1.data) : data1.data != null) return false;
    return id != null ? id.equals(data1.id) : data1.id == null;
  }

  @Override
  public int hashCode() {
    int result = data != null ? data.hashCode() : 0;
    result = 31 * result + (id != null ? id.hashCode() : 0);
    return result;
  }
}
