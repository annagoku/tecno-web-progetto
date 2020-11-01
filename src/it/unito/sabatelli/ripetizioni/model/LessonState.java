package it.unito.sabatelli.ripetizioni.model;

public class LessonState {
  int code;
  String name;

  public LessonState(int code, String name) {
    this.code = code;
    this.name = name;
  }

  @Override
  public String toString() {
    return "{" +
            "code:" + code +
            ", name: \"" + name + "\"" +
            '}';
  }

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
