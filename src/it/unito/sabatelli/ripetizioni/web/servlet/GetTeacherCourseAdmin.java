package it.unito.sabatelli.ripetizioni.web.servlet;

import com.google.gson.Gson;
import it.unito.sabatelli.ripetizioni.dao.Dao;
import it.unito.sabatelli.ripetizioni.model.GenericResponse;
import it.unito.sabatelli.ripetizioni.model.Teacher;
import it.unito.sabatelli.ripetizioni.model.TeacherCourse;
import it.unito.sabatelli.ripetizioni.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name="GetTeacherCourse", urlPatterns = "/servlets/GetTeacherCourse")
public class GetTeacherCourseAdmin extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    processRequest (req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
  }

  public void processRequest (HttpServletRequest request, HttpServletResponse response)throws javax.servlet.ServletException, IOException {
    response.setContentType("application/json");
    Gson gson = new Gson();
    GenericResponse gr =new GenericResponse();


    try {
      Dao dao = (Dao) request.getServletContext().getAttribute(Dao.DAONAME);
      User user = (User) request.getSession().getAttribute("user");

      List<TeacherCourse> listTC= null;


        if (user == null || !user.getRole().equalsIgnoreCase("administrator")) {
          // l'utente non è autorizzato
          response.setStatus(HttpServletResponse.SC_FORBIDDEN);
          gr = new GenericResponse();
          gr.setErrorOccurred("Utente nullo o non autorizzato");
          gr.setResult(false);
          response.getWriter().write(gson.toJson(gr));
          return;
        }else {
          listTC = dao.getTeacherCourseList();
        }
      String json = gson.toJson(listTC);
      response.getWriter().write(json);
      gr.setResult(true);
      response.setStatus(HttpServletResponse.SC_OK);
    }catch (SQLException e) {
      e.printStackTrace();
      gr.setResult(false);
      gr.setErrorOccurred("Impossibile reperire i dati");
      response.getWriter().write(gson.toJson(gr));
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }
}
