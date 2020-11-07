package it.unito.sabatelli.ripetizioni.model;

public class Course {
  private String code;
  private String name;
  private String icon;
  private String state;
  private int statecode;

  public Course() {

  }

  public Course(String code, String name, String icon) {
    this.code = code;
    this.name = name;
    this.icon = icon;
  }

  public void bindState(int state) {
    this.statecode = state;
    if(state==1){
      this.state="attivo";
    }else {
      this.state="non attivo";
    }
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getState() {
    return state;
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
