package it.unito.sabatelli.ripetizioni.model;

public class Day {
  int daycode;
  String dayname;

  public Day(int daycode, String dayname) {
    this.daycode = daycode;
    this.dayname = dayname;
  }

  @Override
  public String toString() {
    return "Day{" +
            "daycode=" + daycode +
            ", dayname='" + dayname + '\'' +
            '}';
  }

  public int getDaycode() {
    return daycode;
  }

  public void setDaycode(int daycode) {
    this.daycode = daycode;
  }

  public String getDayname() {
    return dayname;
  }

  public void setDayname(String dayname) {
    this.dayname = dayname;
  }
}
