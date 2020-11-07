package it.unito.sabatelli.ripetizioni.web.servlet;

import com.google.gson.Gson;
import it.unito.sabatelli.ripetizioni.dao.Dao;
import it.unito.sabatelli.ripetizioni.model.Course;
import it.unito.sabatelli.ripetizioni.model.GenericResponse;
import it.unito.sabatelli.ripetizioni.model.Teacher;
import it.unito.sabatelli.ripetizioni.model.User;

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
      GenericResponse gr =new GenericResponse();


      try {
        Dao dao = (Dao) request.getServletContext().getAttribute(Dao.DAONAME);
        String filter=request.getParameter("filter");
        User user = (User) request.getSession().getAttribute("user");

        if (filter==null){
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          gr.setResult(false);
          gr.setErrorOccurred("Manca parametro filter");
          response.getWriter().write(gson.toJson(gr));
          return;
        }

        List<Teacher> teachers= null;

        if("admin".equals(filter)) {
          if (user == null || !user.getRole().equalsIgnoreCase("administrator")) {
            // l'utente non è autorizzato
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            gr = new GenericResponse();
            gr.setErrorOccurred("Utente nullo o non autorizzato");
            gr.setResult(false);
            response.getWriter().write(gson.toJson(gr));
            return;
          }
          teachers = dao.getTeacherDB(true);
        }else {
          teachers = dao.getTeacherDB(false);
        }

        String json = gson.toJson(teachers);
        response.getWriter().write(json);
        gr.setResult(true);
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

