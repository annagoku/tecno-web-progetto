package it.unito.sabatelli.ripetizioni.web.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "Controller", urlPatterns = {"","/public/*", "/private/*"})
public class ControllerServlet extends HttpServlet {
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
    String path = getPath(request);
    System.out.println("Controller "+method+" "+path);

    //Home
    if("/".equals(path)){
        getServletContext().getRequestDispatcher("/pages/index.html").forward(request, response);

    }
    else if(path.startsWith("/public")) {
      switch (path){
        case "/public/courses":
          request.getServletContext().getNamedDispatcher("GetCourses").forward(request, response);
          return;
        case "/public/teachers":
          request.getServletContext().getNamedDispatcher("GetTeachers").forward(request, response);
          return;
        case "/public/login":
          request.getServletContext().getNamedDispatcher("CheckLogin").forward(request, response);
          return;
      }
    }
    else if(path.startsWith("/private")) {
      switch (path){
        case "/private/":
          request.getServletContext().getRequestDispatcher("/pages/private/areariservata.html").forward(request, response);
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
          request.getServletContext().getNamedDispatcher("ChangeStateAssociationAdmin").forward(request, response);
          return;

      }
    }
    else {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().println("Risorsa non trovata");


    }




  }


  static String getPath(HttpServletRequest request) {
    return request.getRequestURI().substring(request.getContextPath().length()).toLowerCase();
  }
}
