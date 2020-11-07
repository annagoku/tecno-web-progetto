package it.unito.sabatelli.ripetizioni.web.servlet;

import com.google.gson.Gson;
import it.unito.sabatelli.ripetizioni.dao.Dao;
import it.unito.sabatelli.ripetizioni.model.Course;
import it.unito.sabatelli.ripetizioni.model.GenericResponse;
import it.unito.sabatelli.ripetizioni.model.User;

import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.*;

@WebServlet(name="GetCourses", urlPatterns = "/servlets/GetCourses")
public class GetCoursesServlet extends HttpServlet {
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws javax.servlet.ServletException, IOException {
    response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws javax.servlet.ServletException, IOException {
    processRequest(request, response);
  }

  private void processRequest(HttpServletRequest request, HttpServletResponse response) throws javax.servlet.ServletException, IOException {
    //TODO reperire i corsi

    //setto il content Type
    response.setContentType("application/json");
    Gson gson = new Gson();
    GenericResponse gr= new GenericResponse();

    try {
      String filter=request.getParameter("filter");

      User user = (User) request.getSession().getAttribute("user");

      if (filter==null){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        gr.setResult(false);
        gr.setErrorOccurred("Manca parametro filter");
        response.getWriter().write(gson.toJson(gr));
        return;
      }
      Dao dao = (Dao) request.getServletContext().getAttribute(Dao.DAONAME);
      List<Course> courses = null;
      if("admin".equals(filter)) {
        if(user == null || !user.getRole().equalsIgnoreCase("administrator")) {
          // l'utente non è autorizzato
          response.setStatus(HttpServletResponse.SC_FORBIDDEN);
          gr = new GenericResponse();
          gr.setErrorOccurred("Utente nullo o non autorizzato");
          gr.setResult(false);
          response.getWriter().write(gson.toJson(gr));
          return;
        }

        courses= dao.getCourseDB(true);

      }
      else {
        courses= dao.getCourseDB(false);
      }



      String json = gson.toJson(courses);
      response.getWriter().write(json);
      response.setStatus(HttpServletResponse.SC_OK);

    }
    catch (SQLException e) {
      e.printStackTrace();
      gr.setResult(false);
      gr.setErrorOccurred("Impossibile reperire i dati");
      response.getWriter().write(gson.toJson(gr));
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }


  }
}
