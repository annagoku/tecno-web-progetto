package it.unito.sabatelli.ripetizioni.model;

public class CatalogItem {

  Slot slot;
  Day day;
  Course course;
  Teacher teacher;
  boolean selected = false;

  public Slot getSlot() {
    return slot;
  }

  public void setSlot(Slot slot) {
    this.slot = slot;
  }

  public Day getDay() {
    return day;
  }

  public void setDay(Day day) {
    this.day = day;
  }

  public Course getCourse() {
    return course;
  }

  public void setCourse(Course course) {
    this.course = course;
  }

  public Teacher getTeacher() {
    return teacher;
  }

  public void setTeacher(Teacher teacher) {
    this.teacher = teacher;
  }


  public boolean isSelected() {
    return selected;
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }
}
