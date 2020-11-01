package it.unito.sabatelli.ripetizioni.web.servlet;

import com.google.gson.Gson;
import it.unito.sabatelli.ripetizioni.dao.Dao;
import it.unito.sabatelli.ripetizioni.model.Course;
import it.unito.sabatelli.ripetizioni.model.Teacher;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;


@WebServlet(name="GetTeachers", urlPatterns = "/servlets/GetTeachers")
public class GetTeacherServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    private void processRequest (HttpServletRequest request, HttpServletResponse response) throws javax.servlet.ServletException, IOException{
      response.setContentType("application/json");
      Gson gson = new Gson();


      try {
        Dao dao = (Dao) request.getServletContext().getAttribute(Dao.DAONAME);

        List<Teacher> teachers= dao.getTeacherDB();
        String json = gson.toJson(teachers);
        response.getWriter().write(json);
        response.setStatus(HttpServletResponse.SC_OK);

      }
      catch (SQLException e) {
        e.printStackTrace();
        response.getWriter().write("{message: \"Impossibile reperire i dati\"}");
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }

    }
  }

