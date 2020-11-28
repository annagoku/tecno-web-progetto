package it.unito.sabatelli.ripetizioni.dao;

import it.unito.sabatelli.ripetizioni.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class Dao {

  public static String DAONAME = "DAO";

  public static String  QUERY_COURSE_INDEX="SELECT COURSECODE, COURSENAME, ICON FROM COURSE WHERE active = 1";
  public static String  QUERY_COURSE_ADMIN="SELECT COURSECODE, COURSENAME, ICON, ACTIVE FROM COURSE";
  public static String QUERY_TEACHER_ADMIN="SELECT t.BADGENUMBER, t.NAME, t.SURNAME, t.AVATAR, t.ACTIVE FROM TEACHER t";
  public static String QUERY_TEACHER_INDEX="SELECT t.BADGENUMBER, t.NAME, t.SURNAME, t.AVATAR FROM TEACHER t WHERE ACTIVE=1";
  public static String QUERY_TEACHER_COURSE_ADMIN="SELECT tc.ID, t.BADGENUMBER, t.NAME, t.SURNAME, tc.COURSECODE, c.COURSENAME, tc.ACTIVE "+
                            "FROM TEACHER_COURSE tc, TEACHER t, COURSE c WHERE tc.BADGENUMBER=t.BADGENUMBER AND tc.COURSECODE=c.COURSECODE";

  public static String QUERY_LESSONS_ADMIN = "SELECT LESSONS.id, SLOTHOURS.idslot, " +
          "       SLOTHOURS.startslot, SLOTHOURS.endslot, DAY.daycode, DAY.dayname, USER.iduser, USER.username,  USER.name, USER.surname,  USER.email, " +
          "       COURSE.coursecode, COURSE.coursename, COURSE.icon as courseicon, " +
          "       teacher.badgenumber, TEACHER.NAME as teacher_name, TEACHER.SURNAME as teacher_surname, " +
          "       LESSONS.statecode, lesson_state.name as state_text " +
          "FROM (USER JOIN LESSONS JOIN COURSE JOIN TEACHER JOIN SLOTHOURS JOIN DAY JOIN LESSON_STATE) " +
          "WHERE LESSONS.IDUSER=USER.IDUSER AND LESSONS.COURSECODE=COURSE.COURSECODE " +
          "AND LESSONS.BADGENUMBER=TEACHER.BADGENUMBER AND LESSONS.IDSLOT=SLOTHOURS.IDSLOT " +
          "AND LESSONS.DAYCODE = day.daycode and lessons.statecode = lesson_state.code ORDER BY USER.USERNAME";

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
          "AND LESSONS.DAYCODE = day.daycode and lessons.statecode = lesson_state.code and USER.IDUSER = ? ORDER BY LESSONS.DAYCODE, LESSONS.IDSLOT";

  public static String QUERY_CHECKLESSON = "SELECT LESSONS.id, SLOTHOURS.idslot, " +
          "       SLOTHOURS.startslot, SLOTHOURS.endslot, DAY.daycode, DAY.dayname, " +
          "       COURSE.coursecode, COURSE.coursename,"+
          "       LESSONS.statecode, lesson_state.name as state_text " +
          "FROM (USER JOIN LESSONS JOIN COURSE JOIN SLOTHOURS JOIN DAY JOIN LESSON_STATE) " +
          "WHERE LESSONS.IDUSER=USER.IDUSER AND LESSONS.COURSECODE=COURSE.COURSECODE " +
          "AND LESSONS.IDSLOT=SLOTHOURS.IDSLOT " +
          "AND LESSONS.DAYCODE = day.daycode and lessons.statecode = lesson_state.code and USER.IDUSER = ? AND SLOTHOURS.idslot=? "+
          "AND DAY.daycode = ? AND LESSONS.statecode in (1,2)";

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
  public static String INSERT_NEW_LESSON = "INSERT INTO LESSONS (IDUSER, COURSECODE, BADGENUMBER, IDSLOT, DAYCODE, STATECODE) VALUES (?,?,?,?,?, 1)";
  public static String INSERT_NEW_COURSE= "INSERT INTO COURSE (COURSECODE, COURSENAME, ICON, ACTIVE) VALUES(?,?,?,1)";
  public static String CHECK_NEW_COURSE="SELECT COUNT(c.COURSECODE) FROM COURSE c WHERE c.COURSECODE=?";
  public static String INSERT_NEW_TEACHER= "INSERT INTO TEACHER (BADGENUMBER, NAME, SURNAME, AVATAR, ACTIVE) VALUES(?,?,?,?,1)";
  public static String CHECK_NEW_TEACHER="SELECT COUNT(t.BADGENUMBER) FROM TEACHER t WHERE t.BADGENUMBER=?";
  public static String FIND_NEW_COURSE_FOR_TEACHER="SELECT c.COURSECODE, c.COURSENAME, c.ICON, c.ACTIVE FROM COURSE c WHERE c.ACTIVE=1 AND c.COURSENAME NOT IN (SELECT c.COURSENAME FROM TEACHER_COURSE TC, COURSE c, TEACHER t WHERE tc.COURSECODE=c.COURSECODE " +
          "AND tc.BADGENUMBER=t.BADGENUMBER AND t.BADGENUMBER=?)";
  public static String INSERT_NEW_ASSOCIATION="INSERT INTO TEACHER_COURSE (COURSECODE, BADGENUMBER, ACTIVE) VALUES (?,?,1)";
  public static String QUERY_USER_ADMIN="SELECT u.IDUSER, u.NAME, u.SURNAME, u.USERNAME FROM USER u, USER_ROLE ur, ROLE r WHERE u.IDUSER=ur.IDUSER AND "+
          "ur.ROLECODE=r.ROLECODE AND r.ROLENAME='student'";
  public static String DELETE_ASSOCIATION_ADMIN="UPDATE TEACHER_COURSE tc SET tc.ACTIVE=0 WHERE tc.id=? ";
  public static String DELETE_LESSON1_ADMIN="UPDATE LESSONS l SET l.STATECODE=3 WHERE l.STATECODE=1 AND l.BADGENUMBER=? AND l.COURSECODE=?";
  public static String DELETE_COURSE_ADMIN="UPDATE COURSE c SET c.ACTIVE=0 WHERE c.COURSECODE=?";
  public static String DELETE_LESSON3_ADMIN="UPDATE LESSONS l SET l.STATECODE=3 WHERE l.STATECODE=1 AND l.COURSECODE=?";
  public static String DELETE_TEACHER_ADMIN="UPDATE TEACHER t SET t.ACTIVE=0 WHERE t.BADGENUMBER=?";
  public static String DELETE_LESSON2_ADMIN="UPDATE LESSONS l SET l.STATECODE=3 WHERE l.STATECODE=1 AND l.BADGENUMBER=?";

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
  public  ArrayList<Course> getCourseDB(boolean isAdmin) throws SQLException{
    checkInit();
    Connection conn1 = null;
    ArrayList<Course> out = new ArrayList<>();
    try {
      conn1 = getConnection();
      if (conn1 != null) {
        System.out.println("Connected to the database test");
      }
      Statement st = conn1.createStatement();
      if (!isAdmin) {
        System.out.println("getCourseDB -> non admin");
        ResultSet rs = st.executeQuery(QUERY_COURSE_INDEX);
        while (rs.next()) {
          Course c = new Course(rs.getString("coursecode"), rs.getString("coursename"), rs.getString("icon"));
          c.bindState(1);

          out.add(c);
        }
        rs.close();
      }
      else if(isAdmin){
        System.out.println("getCourseDB -> admin");
        ResultSet rs = st.executeQuery(QUERY_COURSE_ADMIN);
        while (rs.next()) {
          Course c = new Course(rs.getString("coursecode"), rs.getString("coursename"), rs.getString("icon"));
          c.bindState(rs.getInt("active"));
          out.add(c);
        }
        rs.close();
      }
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
        PreparedStatement ps = conn.prepareStatement(QUERY_COURSE_AVAILABILITY);
        ps.setString(1, courseCode);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
          CatalogItem c = new CatalogItem();
          c.setDay(new Day(rs.getInt("daycode"), rs.getString("dayname")));
          c.setCourse(new Course(rs.getString("coursecode"), rs.getString("coursename"), rs.getString("icon")));
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
  public  ArrayList<Teacher> getTeacherDB(boolean isAdmin) throws SQLException {
    checkInit();
    Connection conn1 = null;
    ArrayList<Teacher> out = new ArrayList<>();
    try {
      conn1 = getConnection();
      if (conn1 != null) {
        System.out.println("Connected to the database test");
      }

      Statement st = conn1.createStatement();
      if (!isAdmin) {
        ResultSet rs1 = st.executeQuery(QUERY_TEACHER_INDEX);
        while (rs1.next()) {
          Teacher t = new Teacher(rs1.getString("badgenumber"), rs1.getString("name"), rs1.getString("surname"), rs1.getString("avatar"));
          out.add(t);
        }
        rs1.close();
        for (Teacher th : out) {
          String sql = "SELECT COURSE.COURSECODE, COURSE.COURSENAME, COURSE.ICON FROM (COURSE JOIN TEACHER_COURSE JOIN TEACHER)  " +
                  "WHERE TEACHER_COURSE.active = 1 AND COURSE.active = 1 AND TEACHER.active = 1 AND COURSE.COURSECODE=TEACHER_COURSE.COURSECODE AND TEACHER.BADGENUMBER=TEACHER_COURSE.BADGENUMBER AND TEACHER.BADGENUMBER= ?";
          PreparedStatement ps = conn1.prepareStatement(sql);
          ps.setString(1, th.getBadge());
          ResultSet rs2 = ps.executeQuery();
          while (rs2.next()) {
            th.addCourseTeached(new Course(rs2.getString("coursecode"), rs2.getString("coursename"), rs2.getString("icon")));
          }
          rs2.close();
        }
      }else{
        ResultSet rs1 = st.executeQuery(QUERY_TEACHER_ADMIN);
        while (rs1.next()) {
          Teacher t = new Teacher(rs1.getString("badgenumber"), rs1.getString("name"), rs1.getString("surname"), rs1.getString("avatar"));
          t.bindState(rs1.getInt("active"));
          out.add(t);
        }
        rs1.close();
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
      String sql="SELECT iduser, username, name, surname, email  FROM USER WHERE USERNAME=? AND PASSWORD=?";
      PreparedStatement ps= conn1.prepareStatement(sql);
      ps.setString(1, username);
      ps.setString(2, psw);
      ResultSet rs3=ps.executeQuery();


      while (rs3.next()) {
        user = new User(rs3.getString("iduser"), rs3.getString("username"), rs3.getString("name"), rs3.getString("surname"), rs3.getString("email"));
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
  public Lesson checkLesson (String iduser, int daycode, int slotid) throws Exception {
    checkInit();
    Connection conn = null;
    Lesson l = null;
    ResultSet rs = null;
    try {
      conn = getConnection();
      if (conn != null) {
        System.out.println("Connected to the database test");
      }

      PreparedStatement ps = conn.prepareStatement(QUERY_CHECKLESSON);
      ps.setInt(1, Integer.parseInt(iduser));
      ps.setInt(2, slotid);
      ps.setInt(3, daycode);

      rs = ps.executeQuery();


      boolean found = false;
      while (rs.next()) {
        if(found) {
          throw new Exception("Errore lezioni duplicate");
        }
        l = new Lesson();
        l.setId(rs.getInt("id"));
        l.setDay(new Day(rs.getInt("daycode"), rs.getString("dayname")));
        l.setSlot(new Slot(rs.getInt("idslot"), rs.getString("startslot"), rs.getString("endslot")));
        l.setCourse(new Course(rs.getString("coursecode"), rs.getString("coursename"), null));
        l.setState(new LessonState(rs.getInt("statecode"), rs.getString("state_text")));
        found = true;
      }
      rs.close();


    }finally {
      if(rs != null) {
        try {
          rs.close();
        }catch (SQLException e) {
          e.printStackTrace();
        }
      }
      safeCloseConnection(conn);
    }
    return l;
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

  //inserimento nuova prenotazione profilo studente
  public int saveNewReservation(CatalogItem item, String userId, Lesson lessonToCancel) throws  SQLException {
    checkInit();
    Connection conn = null;
    try {
      conn = getConnection();
      conn.setAutoCommit(false);

      if(lessonToCancel!= null) {
        PreparedStatement ps = conn.prepareStatement(UPDATE_LESSON_STATE);
        ps.setInt(1, 3);
        ps.setInt(2, lessonToCancel.getId());

        int r = ps.executeUpdate();
        System.out.println("saveNewReservation - Lesson to cancel "+lessonToCancel.getId()+" row updated "+r);
      }

      PreparedStatement ps = conn.prepareStatement(INSERT_NEW_LESSON);
      ps.setInt(1, Integer.parseInt(userId));
      ps.setString(2, item.getCourse().getCode() );
      ps.setString(3, item.getTeacher().getBadge());
      ps.setInt(4, item.getSlot().getId());
      ps.setInt(5, item.getDay().getDaycode());

      int rows = ps.executeUpdate();
      conn.commit();
      System.out.println("saveNewReservation - row inserted "+rows);

      return rows;

    }catch (SQLException e) {
      System.out.println("Error "+e.getMessage()+" rolling back");
      conn.rollback();
      throw  e;
    }
    finally {
      safeCloseConnection(conn);
    }

  }

  // Query admin per leggere le associazioni corsi e insegnanti
  public ArrayList<TeacherCourse> getTeacherCourseList() throws SQLException {
    checkInit();
    Connection conn = null;
    ArrayList<TeacherCourse> listTC = new ArrayList<>();
    try {
      conn = getConnection();

      Statement st = conn.createStatement();
      ResultSet rs = st.executeQuery(QUERY_TEACHER_COURSE_ADMIN);
      while (rs.next()) {
        TeacherCourse tc = new TeacherCourse(rs.getInt("id"), rs.getString("badgenumber"), rs.getString("name"), rs.getString("surname"), rs.getString("coursecode"), rs.getString("coursename"));
        tc.bindState(rs.getInt("active"));
        listTC.add(tc);
      }
      rs.close();

    } finally {
      safeCloseConnection(conn);
    }
    return listTC;
  }

//Salvataggio nuovo corso amministratore
  public int saveNewCourse(String code, String name, String image) throws  SQLException {
    checkInit();
    Connection conn = null;

    try {
      conn = getConnection();

      PreparedStatement ps = conn.prepareStatement(INSERT_NEW_COURSE);
      ps.setString(1, code);
      ps.setString(2, name );
      ps.setString(3, image);

      int rows = ps.executeUpdate();
      System.out.println("saveNewCourse - row inserted "+rows);

      return rows;

    }catch (SQLException e) {
      e.getMessage();
      throw  e;
    }
    finally {
      safeCloseConnection(conn);
    }
  }

  public boolean checkNewCourse(String code) throws SQLException{
    checkInit();
    Connection conn = null;
    int count=0;
    try {
      conn = getConnection();
      if (conn != null) {
        System.out.println("Connected to the database test");
      }

      PreparedStatement ps = conn.prepareStatement(CHECK_NEW_COURSE);
      ps.setString(1, code);

      ResultSet rs = ps.executeQuery();

      while (rs.next()) {
       count=rs.getInt(1);
      }
      if(count>0) return false;
      else return true;


    }catch (SQLException e) {
      e.getMessage();
      throw  e;
    }finally {
      safeCloseConnection(conn);
    }

  }
//Salvataggio nuovo insegnante amministratore
public int saveNewTeacher(String badge, String name, String surname, String avatar) throws  SQLException {
  checkInit();
  Connection conn = null;

  try {
    conn = getConnection();

    PreparedStatement ps = conn.prepareStatement(INSERT_NEW_TEACHER);
    ps.setString(1, badge);
    ps.setString(2, name );
    ps.setString(3, surname);
    ps.setString(4, avatar);

    int rows = ps.executeUpdate();
    System.out.println("saveNewTeacher - row inserted "+rows);

    return rows;

  }catch (SQLException e) {
    e.getMessage();
    throw  e;
  }
  finally {
    safeCloseConnection(conn);
  }
}

  public boolean checkNewTeacher(String badge) throws SQLException{
    checkInit();
    Connection conn = null;
    int count=0;
    try {
      conn = getConnection();
      if (conn != null) {
        System.out.println("Connected to the database test");
      }

      PreparedStatement ps = conn.prepareStatement(CHECK_NEW_TEACHER);
      ps.setString(1, badge);

      ResultSet rs = ps.executeQuery();

      while (rs.next()) {
        count=rs.getInt(1);
      }
      if(count>0) return false;
      else return true;


    }catch (SQLException e) {
      e.getMessage();
      throw  e;
    }finally {
      safeCloseConnection(conn);
    }

  }

  public ArrayList<Course> findNewCourseForTeacher(String badge) throws SQLException {
    checkInit();
    Connection conn = null;
    int count=0;
    ArrayList<Course> list=new ArrayList<>();
    try {
      conn = getConnection();
      if (conn != null) {
        System.out.println("Connected to the database test");
      }


      PreparedStatement ps = conn.prepareStatement(FIND_NEW_COURSE_FOR_TEACHER);
      ps.setString(1, badge);

      ResultSet rs = ps.executeQuery();

      while (rs.next()) {
        Course c= new Course (rs.getString("coursecode"), rs.getString("coursename"), rs.getString("icon"));
        c.bindState(rs.getInt("active"));
        list.add(c);
      }

    }catch (SQLException e) {
      e.getMessage();
      throw  e;
    }finally {
      safeCloseConnection(conn);
    }
    return list;

  }

  public int saveNewAssociation(String coursecode, String badgenumber) throws  SQLException {
    checkInit();
    Connection conn = null;

    try {
      conn = getConnection();

      PreparedStatement ps = conn.prepareStatement(INSERT_NEW_ASSOCIATION);
      ps.setString(1, coursecode);
      ps.setString(2, badgenumber);

      int rows = ps.executeUpdate();
      System.out.println("saveNewAssociation - row inserted "+rows);

      return rows;

    }catch (SQLException e) {
      e.getMessage();
      throw  e;
    }
    finally {
      safeCloseConnection(conn);
    }
  }

  public ArrayList<User> getUserListAdmin() throws SQLException{
    checkInit();
    Connection conn = null;
    ArrayList<User> listUser = new ArrayList<>();
    try {
      conn = getConnection();

      Statement st = conn.createStatement();
      ResultSet rs = st.executeQuery(QUERY_USER_ADMIN);
      while (rs.next()) {
        User u = new User(rs.getString("iduser"), rs.getString("name"), rs.getString("surname"), rs.getString("username"));
        listUser.add(u);
      }
      rs.close();

    } finally {
      safeCloseConnection(conn);
    }
    return listUser;

  }

  //cancellazione associazione insegnante corso amministratore
  public int deleteAssociationAdmin(TeacherCourse tc) throws  SQLException {
    checkInit();
    Connection conn = null;
    try {
      conn = getConnection();
      conn.setAutoCommit(false);

      if (tc != null) {
        PreparedStatement ps = conn.prepareStatement(DELETE_ASSOCIATION_ADMIN);
        ps.setInt(1, tc.getId());

        int r = ps.executeUpdate();
        System.out.println("deleteAssociation--> " + tc.getId() + "row updated " + r);
      }

      PreparedStatement ps = conn.prepareStatement(DELETE_LESSON1_ADMIN);
      ps.setInt(1, Integer.parseInt(tc.getBadge()));
      ps.setString(2, tc.getCourseCode());

      int rows = ps.executeUpdate();
      conn.commit();
      System.out.println("Delete Association - row update " + rows);

      return rows;

    } catch (SQLException e) {
      System.out.println("Error " + e.getMessage() + " rolling back");
      conn.rollback();
      throw e;
    } finally {
      safeCloseConnection(conn);
    }
  }

    //cancellazione  insegnante  amministratore
    public int deleteTeacherAdmin (Teacher t) throws  SQLException {
      checkInit();
      Connection conn = null;
      try {
        conn = getConnection();
        conn.setAutoCommit(false);

        if(t!= null) {
          PreparedStatement ps = conn.prepareStatement(DELETE_TEACHER_ADMIN);
          ps.setString(1, t.getBadge());

          int r = ps.executeUpdate();
          System.out.println("deleteAssociation--> " + t.getBadge()+ "row updated "+r);
        }

        PreparedStatement ps = conn.prepareStatement(DELETE_LESSON2_ADMIN);
        ps.setString(1, t.getBadge());


        int rows = ps.executeUpdate();
        conn.commit();
        System.out.println("Delete Teacher - row update "+rows);

        return rows;

      }catch (SQLException e) {
        System.out.println("Error "+e.getMessage()+" rolling back");
        conn.rollback();
        throw  e;
      }
      finally {
        safeCloseConnection(conn);
      }
    }
  //cancellazione  corso amministratore
  public int deleteCourseAdmin (Course c) throws  SQLException {
    checkInit();
    Connection conn = null;
    try {
      conn = getConnection();
      conn.setAutoCommit(false);

      if(c!= null) {
        PreparedStatement ps = conn.prepareStatement(DELETE_COURSE_ADMIN);
        ps.setString(1, c.getCode());

        int r = ps.executeUpdate();
        System.out.println("deleteAssociation--> " + c.getCode()+ "row updated "+r);
      }

      PreparedStatement ps = conn.prepareStatement(DELETE_LESSON3_ADMIN);
      ps.setString(1, c.getCode());


      int rows = ps.executeUpdate();
      conn.commit();
      System.out.println("Delete Course - row update "+rows);

      return rows;

    }catch (SQLException e) {
      System.out.println("Error "+e.getMessage()+" rolling back");
      conn.rollback();
      throw  e;
    }
    finally {
      safeCloseConnection(conn);
    }
  }


}


