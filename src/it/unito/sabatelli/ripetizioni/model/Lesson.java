package it.unito.sabatelli.ripetizioni.model;

public class Lesson {
  private int id;
  private Slot slot;
  private Day day;
  private User user;
  private Course course;
  private Teacher teacher;
  private LessonState state;

  public Lesson(){

  }

  public Slot getSlot() {
    return slot;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
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

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
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

  public LessonState getState() {
    return state;
  }

  public void setState(LessonState state) {
    this.state = state;
  }
}
