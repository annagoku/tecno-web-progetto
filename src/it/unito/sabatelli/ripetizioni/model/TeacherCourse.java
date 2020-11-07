package it.unito.sabatelli.ripetizioni.model;

public class TeacherCourse {
   int id;
   String badge;
   String name;
   String surname;
   String courseCode;
   String courseName;
   String state;
   int statecode;

   public TeacherCourse(){}

   public TeacherCourse (int id, String badge, String name, String surname, String courseCode, String courseName){
     this.id=id;
     this.badge=badge;
     this.name=name;
     this.surname=surname;
     this.courseCode=courseCode;
     this.courseName=courseName;
   }

  public void bindState(int state) {
    this.statecode = state;
    if(state==1){
      this.state="attiva";
    }else {
      this.state="non attiva";
    }
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
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

  public String getCourseCode() {
    return courseCode;
  }

  public void setCourseCode(String courseCode) {
    this.courseCode = courseCode;
  }

  public String getCourseName() {
    return courseName;
  }

  public void setCourseName(String courseName) {
    this.courseName = courseName;
  }

  public int getStatecode() {
    return statecode;
  }

  public void setStatecode(int stateCode) {
    this.statecode = stateCode;
  }
}
