package it.unito.sabatelli.ripetizioni.web.servlet;

import com.google.gson.Gson;
import it.unito.sabatelli.ripetizioni.model.GenericResponse;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet(name = "Controller", urlPatterns = {"","/public/*", "/private/*"})
public class ControllerServlet extends HttpServlet {

  static final String REGEX = "(.*);jsessionid=([A-Za-z0-9]+)";
  static final Pattern PATTERN = Pattern.compile(REGEX);

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    process(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    process(req, resp);

  }

  private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String method = request.getMethod();
    String fullpath = getPath(request);
    System.out.println("Controller "+method+" "+fullpath);
    Matcher m = PATTERN.matcher(fullpath);
    HttpSession session = request.getSession();
    String sessionId = session.getId();
    String clientSessionId = null;
    String path = fullpath;

    // controlla che il path contenga il sessionID
    if(m.matches()) {
      clientSessionId = m.group(2);
      path = m.group(1);
    }

    System.out.println("Controller clientSessionID -> "+clientSessionId);
    //Se gli id non coincidono la sessione è invalidata
    if(clientSessionId != null && !clientSessionId.equalsIgnoreCase(sessionId)) {
      invalidate(request, response);
      return;
    }

    //Home
    if("/".equals(path)){

      if(clientSessionId == null && session.isNew()) {
        String encodedURL = getServletContext().getContextPath()+path+";jsessionid="+sessionId;
        System.out.println("encodedUrl "+encodedURL);
        response.sendRedirect(encodedURL);
      }
      else {
        getServletContext().getRequestDispatcher("/pages/index.html").forward(request, response);
      }

    }
    else if(path.startsWith("/public")) {
      switch (path){
        case "/public/courses":
          request.getServletContext().getNamedDispatcher("GetCourses").forward(request, response);
          return;
        case "/public/teachers":
          request.getServletContext().getNamedDispatcher("GetTeachers").forward(request, response);
          return;
        case "/public/catalog":
          request.getServletContext().getNamedDispatcher("GetCatalog").forward(request, response);
          return;
        case "/public/login":
          request.getServletContext().getNamedDispatcher("CheckLogin").forward(request, response);
          return;
      }
    }
    else if(path.startsWith("/private")) {
      if(clientSessionId == null) {
        invalidate(request, response);
        return;
      }
      switch (path){
        case "/private/":
          getServletContext().getRequestDispatcher("/pages/private/areariservata.html").forward(request, response);
          return;
        case "/private/userlog":
          System.out.println("verso session INFO");
          request.getServletContext().getNamedDispatcher("GetSessionInfo").forward(request, response);
          return;
        case "/private/lessons":
          request.getServletContext().getNamedDispatcher("Lessons").forward(request, response);
          return;
        case "/private/catalog":
          request.getServletContext().getNamedDispatcher("GetCatalog").forward(request, response);
          return;
        case "/private/courseavailability":
          request.getServletContext().getNamedDispatcher("CourseAvailability").forward(request, response);
          return;
        case "/private/logout":
          request.getServletContext().getNamedDispatcher("Logout").forward(request, response);
          return;

        case "/private/newreservation":
          request.getServletContext().getNamedDispatcher("NewReservation").forward(request, response);
          return;

        case "/private/associationsadmin":
          request.getServletContext().getNamedDispatcher("GetTeacherCourse").forward(request, response);
          return;

        case "/private/courselist":
          request.getServletContext().getNamedDispatcher("GetNewCourseForTeacher").forward(request, response);
          return;

        case "/private/userlist":
          request.getServletContext().getNamedDispatcher("GetUserList").forward(request, response);
          return;

        case "/private/deleteassociation":
          request.getServletContext().getNamedDispatcher("DeleteAssociationAdmin").forward(request, response);
          return;

        case "/private/deleteteacher":
          request.getServletContext().getNamedDispatcher("DeleteTeacherAdmin").forward(request, response);
          return;

        case "/private/deletecourse":
          request.getServletContext().getNamedDispatcher("DeleteCourseAdmin").forward(request, response);
          return;

        case "/private/deletereservation":
          request.getServletContext().getNamedDispatcher("DeleteReservationAdmin").forward(request, response);
          return;

      }
    }
    else {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().println("Risorsa non trovata");


    }




  }


  private void invalidate(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    String acceptedContentTypes = request.getHeader("Accept");
    boolean jsonContentType = "application/json".equalsIgnoreCase(acceptedContentTypes);
    System.out.println("jsonContentType -> "+jsonContentType);
    System.out.println("INVALIDO LA SESSIONE -> "+request.getSession().getId());
    request.getSession().invalidate();
    handleLogOutResponseCookie(request, response);
    if(jsonContentType) {
      GenericResponse gr = new GenericResponse();
      gr.setResult(false);
      gr.setErrorOccurred("Sessione non valida");
      response.setStatus(440);
      response.getWriter().write(new Gson().toJson(gr));
      return;
    }
    else {
      response.sendRedirect(request.getContextPath()+"/pages/invalid.html");
    }
  }

  private void handleLogOutResponseCookie(HttpServletRequest request, HttpServletResponse response) {
    Cookie[] cookies = request.getCookies();
    if(cookies != null) {
      for (Cookie cookie : cookies) {
        cookie.setMaxAge(0);
        cookie.setValue(null);
        cookie.setPath("/");
        response.addCookie(cookie);
      }
    }
  }
  //Estrazione path senza context root
  static String getPath(HttpServletRequest request) {
    return request.getRequestURI().substring(request.getContextPath().length()).toLowerCase();
  }
}
