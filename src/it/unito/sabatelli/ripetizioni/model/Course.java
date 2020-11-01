package it.unito.sabatelli.ripetizioni.model;

public class Course {
  private String code;
  private String name;
  private String icon;

  public Course() {

  }

  public Course(String code, String name, String icon) {
    this.code = code;
    this.name = name;
    this.icon = icon;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getIcon() {
    return icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }
}
