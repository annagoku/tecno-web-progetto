package it.unito.sabatelli.ripetizioni.model;

public class Slot {
  int id;
  String startHour;
  String endHour;

  public Slot(int id, String startHour, String endHour) {
    this.id = id;
    this.startHour = startHour;
    this.endHour = endHour;
  }

  @Override
  public String toString() {
    return "Slot{" +
            "id=" + id +
            ", startHour='" + startHour + '\'' +
            ", endHour='" + endHour + '\'' +
            '}';
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getStartHour() {
    return startHour;
  }

  public void setStartHour(String startHour) {
    this.startHour = startHour;
  }

  public String getEndHour() {
    return endHour;
  }

  public void setEndHour(String endHour) {
    this.endHour = endHour;
  }
}
