package it.unito.sabatelli.ripetizioni.dao;

import it.unito.sabatelli.ripetizioni.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class Dao {

  public static String DAONAME = "DAO";

  public static String QUERY_LESSONS_ADMIN = "SELECT LESSONS.id, SLOTHOURS.idslot, " +
          "       SLOTHOURS.startslot, SLOTHOURS.endslot, DAY.daycode, DAY.dayname, USER.iduser, USER.username,  USER.name, USER.surname,  USER.email, " +
          "       COURSE.coursecode, COURSE.coursename, COURSE.icon as courseicon, " +
          "       teacher.badgenumber, TEACHER.NAME as teacher_name, TEACHER.SURNAME as teacher_surname, " +
          "       LESSONS.statecode, lesson_state.name as state_text " +
          "FROM (USER JOIN LESSONS JOIN COURSE JOIN TEACHER JOIN SLOTHOURS JOIN DAY JOIN LESSON_STATE) " +
          "WHERE LESSONS.IDUSER=USER.IDUSER AND LESSONS.COURSECODE=COURSE.COURSECODE " +
          "AND LESSONS.BADGENUMBER=TEACHER.BADGENUMBER AND LESSONS.IDSLOT=SLOTHOURS.IDSLOT " +
          "AND LESSONS.DAYCODE = day.daycode and lessons.statecode = lesson_state.code";

  public static String QUERY_GLOBAL_AVAILABILITY = "select  t.idslot, t.startslot, t.endslot, " +
          "        t.daycode, t.dayname,\n" +
          "        t.coursecode, t.coursename, t.icon, t.badgenumber, t.name, t.surname, t.avatar " +
          "FROM " +
          "(select  s.idslot, s.startslot, s.endslot, " +
          "     d.daycode, d.dayname, " +
          "     c.coursecode, c.coursename, c.icon, t.badgenumber, t.name, t.surname, t.avatar " +
          "     from slothours s, day d , teacher t, teacher_course tc, course c " +
          "     where t.badgenumber = tc.badgenumber and tc.coursecode = c.coursecode and t.active = 1 and tc.active = 1 and c.active = 1) t " +
          "LEFT JOIN " +
          "    (select l.idslot,l.daycode, l.coursecode, l.badgenumber from lessons l " +
          "    where statecode in (1,2)) t2 " +
          "ON t.badgenumber = t2.badgenumber " +
          "    AND t.daycode = t2.daycode " +
          "    AND t.idslot = t2.idslot " +
          "WHERE t2.idslot IS NULL " +
          "ORDER BY t.idslot, t.daycode, t.coursename, t.surname";

  public static String QUERY_LESSONS_USER = "SELECT LESSONS.id, SLOTHOURS.idslot, " +
          "       SLOTHOURS.startslot, SLOTHOURS.endslot, DAY.daycode, DAY.dayname, " +
          "       COURSE.coursecode, COURSE.coursename, COURSE.icon as courseicon, " +
          "       teacher.badgenumber, TEACHER.NAME as teacher_name, TEACHER.SURNAME as teacher_surname, " +
          "       LESSONS.statecode, lesson_state.name as state_text " +
          "FROM (USER JOIN LESSONS JOIN COURSE JOIN TEACHER JOIN SLOTHOURS JOIN DAY JOIN LESSON_STATE) " +
          "WHERE LESSONS.IDUSER=USER.IDUSER AND LESSONS.COURSECODE=COURSE.COURSECODE " +
          "AND LESSONS.BADGENUMBER=TEACHER.BADGENUMBER AND LESSONS.IDSLOT=SLOTHOURS.IDSLOT " +
          "AND LESSONS.DAYCODE = day.daycode and lessons.statecode = lesson_state.code and USER.IDUSER = ?";

  public static String UPDATE_LESSON_STATE = "UPDATE LESSONS SET STATECODE=? WHERE ID=?";

  public static String QUERY_COURSE_AVAILABILITY = "select  t.idslot, t.startslot, t.endslot, " +
          "        t.daycode, t.dayname,\n" +
          "        t.coursecode, t.coursename, t.icon, t.badgenumber, t.name, t.surname, t.avatar " +
          "FROM " +
          "(select  s.idslot, s.startslot, s.endslot, " +
          "     d.daycode, d.dayname, " +
          "     c.coursecode, c.coursename, c.icon, t.badgenumber, t.name, t.surname, t.avatar " +
          "     from slothours s, day d , teacher t, teacher_course tc, course c " +
          "     where t.badgenumber = tc.badgenumber and tc.coursecode = c.coursecode and t.active = 1 and tc.active = 1 and c.active = 1) t " +
          "LEFT JOIN " +
          "    (select l.idslot,l.daycode, l.coursecode, l.badgenumber from lessons l " +
          "    where statecode in (1,2)) t2 " +
          "ON t.badgenumber = t2.badgenumber " +
          "    AND t.daycode = t2.daycode " +
          "    AND t.idslot = t2.idslot " +
          "WHERE t2.idslot IS NULL  AND t.coursecode=? " +
          "ORDER BY t.idslot, t.daycode, t.coursename, t.surname";





  boolean initialized = false;
  String url;
  String user;
  String pwd;


//registrazione del driver JDBC per MySQL
  public void init(String url, String user, String pwd) throws SQLException {
    if(initialized) {
      System.out.println("Dao è già inizializzato");
    }
      DriverManager.registerDriver(new com.mysql.jdbc.Driver());
      System.out.println("Driver correttamente registrato");
      this.url = url;
      this.user = user;
      this.pwd = pwd;
      initialized = true;
  }

  private void checkInit() {
    if(!initialized)
      throw new RuntimeException("Dao non è inizializzato");
  }

  private Connection getConnection() throws SQLException {
    return DriverManager.getConnection(url, user, pwd);
  }

  private void safeCloseConnection(Connection conn) {
    if (conn != null) {
      try {
        conn.close();
      } catch (SQLException e2) {
        System.out.println(e2.getMessage());
      }
    }
  }

// Lettura da DB dei corsi
  public  ArrayList<Course> getCourseDB() throws SQLException{
    checkInit();
    Connection conn1 = null;
    ArrayList<Course> out = new ArrayList<>();
    try {
      conn1 = getConnection();
      if (conn1 != null) {
        System.out.println("Connected to the database test");
      }

      Statement st = conn1.createStatement();
      ResultSet rs = st.executeQuery("SELECT * FROM COURSE WHERE active = 1");
      while (rs.next()) {
        Course c = new Course(rs.getString("coursecode"),rs.getString("coursename"), rs.getString("icon"));
        out.add(c);
      }
      rs.close();

    }
    finally {
      safeCloseConnection(conn1);
    }
    return out;
  }
  public  ArrayList<CatalogItem> getCatalog() throws SQLException {
    checkInit();
    Connection conn = null;
    ArrayList<CatalogItem> list = new ArrayList<>();
    try {
      conn = getConnection();

      Statement st = conn.createStatement();
      ResultSet rs = st.executeQuery(QUERY_GLOBAL_AVAILABILITY);
      while (rs.next()) {
        CatalogItem c = new CatalogItem();
        c.setDay(new Day(rs.getInt("daycode"), rs.getString("dayname")));
        c.setCourse( new Course(rs.getString("coursecode"),rs.getString("coursename"), rs.getString("icon")));
        c.setSlot(new Slot(rs.getInt("idslot"), rs.getString("startslot"), rs.getString("endslot")));
        c.setTeacher(new Teacher(rs.getString("badgenumber"), rs.getString("name"), rs.getString("surname"), rs.getString("avatar")));
        list.add(c);
      }
      rs.close();

    }
    finally {
      safeCloseConnection(conn);
    }
    return list;
  }

  public  ArrayList<CatalogItem> getCatalogFiltered(String courseCode) throws SQLException {
    checkInit();
    Connection conn = null;
    ArrayList<CatalogItem> list = new ArrayList<>();
    try {
      conn = getConnection();

      PreparedStatement ps= conn.prepareStatement(QUERY_COURSE_AVAILABILITY);
      ps.setString(1, courseCode);
      ResultSet rs=ps.executeQuery();
      while (rs.next()) {
        CatalogItem c = new CatalogItem();
        c.setDay(new Day(rs.getInt("daycode"), rs.getString("dayname")));
        c.setCourse( new Course(rs.getString("coursecode"),rs.getString("coursename"), rs.getString("icon")));
        c.setSlot(new Slot(rs.getInt("idslot"), rs.getString("startslot"), rs.getString("endslot")));
        c.setTeacher(new Teacher(rs.getString("badgenumber"), rs.getString("name"), rs.getString("surname"), rs.getString("avatar")));
        list.add(c);
      }
      rs.close();

    }
    finally {
      safeCloseConnection(conn);
    }
    return list;
  }







//Lettura da DB degli insegnanti e dei corsi associati
  public  ArrayList<Teacher> getTeacherDB() throws SQLException {
    checkInit();
    Connection conn1 = null;
    ArrayList<Teacher> out = new ArrayList<>();
    try {
      conn1 = getConnection();
      if (conn1 != null) {
        System.out.println("Connected to the database test");
      }

      Statement st = conn1.createStatement();
      ResultSet rs1 = st.executeQuery("SELECT * FROM TEACHER WHERE active = 1");
      while (rs1.next()) {
        Teacher t = new Teacher(rs1.getString("badgenumber"),rs1.getString("name"), rs1.getString("surname"),rs1.getString("avatar"));
        out.add(t);
      }
      rs1.close();
      for (Teacher th: out){
        String sql= "SELECT COURSE.COURSECODE, COURSE.COURSENAME, COURSE.ICON FROM (COURSE JOIN TEACHER_COURSE JOIN TEACHER)  " +
                "WHERE TEACHER_COURSE.active = 1 AND COURSE.active = 1 AND TEACHER.active = 1 AND COURSE.COURSECODE=TEACHER_COURSE.COURSECODE AND TEACHER.BADGENUMBER=TEACHER_COURSE.BADGENUMBER AND TEACHER.BADGENUMBER= ?";
         PreparedStatement ps= conn1.prepareStatement(sql);
         ps.setString(1,th.getBadge());
         ResultSet rs2=ps.executeQuery();
        while (rs2.next()) {
          th.addCourseTeached(new Course (rs2.getString("coursecode"), rs2.getString("coursename"), rs2.getString("icon")));
        }
        rs2.close();
      }
    }
    finally {
      safeCloseConnection(conn1);
    }
    return out;
  }

//Check di User a DB per login

  public User checkUserDB(String username, String password) throws SQLException {
    checkInit();
    Connection conn1 = null;

    //TODO fare solo una query
    User user=null;
    try {
      conn1 = getConnection();
      if (conn1 != null) {
        System.out.println("Connected to the database test");
      }

      String psw= Utility.getMd5(password);
      String sql="SELECT iduser, username, password, name, surname, email  FROM USER WHERE USERNAME=? AND PASSWORD=?";
      PreparedStatement ps= conn1.prepareStatement(sql);
      ps.setString(1, username);
      ps.setString(2, psw);
      ResultSet rs3=ps.executeQuery();


      while (rs3.next()) {
        user = new User(rs3.getString("iduser"), rs3.getString("username"), rs3.getString("password"), rs3.getString("name"), rs3.getString("surname"), rs3.getString("email"));
      }

      rs3.close();

      if(user == null) {
        return null;
      }
      String sql2 = "SELECT ROLE.rolename " +
              "FROM (ROLE JOIN USER_ROLE JOIN USER) " +
              "WHERE ROLE.ROLECODE=USER_ROLE.ROLECODE " +
              "AND USER.iduser=USER_ROLE.iduser AND USER.iduser=?";
      PreparedStatement ps2 = conn1.prepareStatement(sql2);
      ps2.setString(1, user.getId());
      ResultSet rs4 = ps2.executeQuery();

      while (rs4.next()) {
        user.setRole(rs4.getString("rolename"));
      }
      rs4.close();
    }
    finally {
      safeCloseConnection(conn1);
    }
    return user;

  }

  //Caricamento delle lezioni di un user
  public ArrayList<Lesson> loadLesson (String iduser, String role) throws SQLException {
    checkInit();
    Connection conn = null;
    ArrayList<Lesson> lessonList = new ArrayList<>();
    try {
      conn = getConnection();
      if (conn != null) {
        System.out.println("Connected to the database test");
      }
      if (role.equals("administrator")) {
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(QUERY_LESSONS_ADMIN);

        while (rs.next()) {
          lessonList.add(mapLesson(rs, true));
        }
        rs.close();
      }
      else {
        PreparedStatement ps = conn.prepareStatement(QUERY_LESSONS_USER);
        ps.setInt(1, Integer.parseInt(iduser));
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
          lessonList.add(mapLesson(rs, false));
        }
        rs.close();
      }

    }finally {
      safeCloseConnection(conn);
    }
    return lessonList;
  }

  private Lesson mapLesson(ResultSet rs, boolean isAdmin) throws SQLException {
    Lesson l = new Lesson();
    l.setId(rs.getInt("id"));
    l.setDay(new Day(rs.getInt("daycode"), rs.getString("dayname")));
    l.setSlot(new Slot(rs.getInt("idslot"), rs.getString("startslot"), rs.getString("endslot")));
    if(isAdmin) {
      l.setUser(new User(""+rs.getInt("iduser"), rs.getString("username"), rs.getString("name"), rs.getString("surname"), rs.getString("email")));
    }
    l.setTeacher(new Teacher(rs.getString("badgenumber"), rs.getString("teacher_name"), rs.getString("teacher_surname"), null));
    l.setCourse(new Course(rs.getString("coursecode"), rs.getString("coursename"), rs.getString("courseicon")));
    l.setState(new LessonState(rs.getInt("statecode"), rs.getString("state_text")));
    return  l;

  }

  public int saveLessonState (int id, int stateCode ) throws  SQLException {
    checkInit();
    Connection conn = null;
    try {
      conn = getConnection();
      if (conn != null) {
        System.out.println("Connected to the database test");
      }

      PreparedStatement ps = conn.prepareStatement(UPDATE_LESSON_STATE);
      ps.setInt(1, stateCode);
      ps.setInt(2, id);

      int rows = ps.executeUpdate();

      return rows;

    }finally {
      safeCloseConnection(conn);
    }
  }

  }


