package it.unito.sabatelli.ripetizioni.model;

public class User {

  private String id;
  private String username;
  private String name;
  private String surname;
  private String email;
  private String role;

  public User (){}

  public User (String id, String user, String name, String surname, String email){
    this.id=id;
    this.username=user;
    this.email=email;
  }

  public User (String id, String user, String psw, String name, String surname, String email){
    this.id=id;
    this.username=user;
    this.email=email;
  }

  public String toString() {
    return this.username + "("+this.role+")";
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

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }
}
