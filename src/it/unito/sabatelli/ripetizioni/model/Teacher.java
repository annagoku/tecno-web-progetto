package it.unito.sabatelli.ripetizioni.model;

import java.util.ArrayList;

public class Teacher {
  private String badge;
  private String name;
  private String surname;
  private String avatar;
  private ArrayList<Course> courseTeached =new ArrayList<>();

  public Teacher (){

  }

  public Teacher (String badge, String name, String surname,String avatar){
    this.badge=badge;
    this.name=name;
    this.surname=surname;
    this.avatar=avatar;
  }

  public String getAvatar() {
    return avatar;
  }

  public void setAvatar(String avatar) {
    this.avatar = avatar;
  }

  public String getBadge() {
    return badge;
  }

  public void setBadge(String badge) {
    this.badge = badge;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSurname() {
    return surname;
  }

  public void setSurname(String surname) {
    this.surname = surname;
  }

  public ArrayList<Course> getCourseTeached() {
    return courseTeached;
  }

  public void setCourseTeached(ArrayList<Course> courseTeached) {
    this.courseTeached = courseTeached;
  }

  public void addCourseTeached(Course c){
    courseTeached.add(c);
  }
}
