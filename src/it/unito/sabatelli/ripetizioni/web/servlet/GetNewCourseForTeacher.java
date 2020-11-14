package it.unito.sabatelli.ripetizioni.web.servlet;

import com.google.gson.Gson;
import it.unito.sabatelli.ripetizioni.dao.Dao;
import it.unito.sabatelli.ripetizioni.model.Course;
import it.unito.sabatelli.ripetizioni.model.GenericResponse;
import it.unito.sabatelli.ripetizioni.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name="GetNewCourseForTeacher", urlPatterns = "/servlets/GetNewCourseForTeacher")
public class GetNewCourseForTeacher extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    processRequestGet(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    processRequestPost(req, resp);
  }

  private void processRequestGet(HttpServletRequest request, HttpServletResponse response)throws javax.servlet.ServletException, IOException{
    response.setContentType("application/json");
    Gson gson = new Gson();
    GenericResponse gr= new GenericResponse();
    Dao dao = (Dao) request.getServletContext().getAttribute(Dao.DAONAME);
    List<Course> courseForTeacher = null;

    try{
      User user = (User) request.getSession().getAttribute("user");

      if(user == null || !user.getRole().equalsIgnoreCase("administrator")) {
        // l'utente non è autorizzato
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        gr = new GenericResponse();
        gr.setErrorOccurred("Utente nullo o non autorizzato");
        gr.setResult(false);
        response.getWriter().write(gson.toJson(gr));
        return;
      }

      if (user.getRole().equalsIgnoreCase("administrator")) {
        String badge = request.getParameter("badge");

        if (badge != null) {
          gr.setResult(true);
          courseForTeacher = dao.findNewCourseForTeacher(badge);
          gr.setResult(true);
          response.setStatus(HttpServletResponse.SC_OK);
          response.getWriter().write(gson.toJson(courseForTeacher));


        } else {
          gr.setResult(false);
          gr.setErrorOccurred("Selezionare un insegnante");
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          response.getWriter().write(gson.toJson(gr));
          return;
        }
      }else{
        gr.setResult(false);
        gr.setErrorOccurred("Errore nel reperimento dei dati");
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.getWriter().write(gson.toJson(gr));
        return;
      }
    }catch (Exception e){
      e.printStackTrace();
      gr.setResult(false);
      gr.setErrorOccurred(e.getMessage());
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().write(gson.toJson(gr));
    }
  }

  private void processRequestPost(HttpServletRequest request, HttpServletResponse response) throws javax.servlet.ServletException, IOException{
    response.setContentType("application/json");
    Gson gson = new Gson();
    GenericResponse gresp= new GenericResponse();
    Dao dao = (Dao) request.getServletContext().getAttribute(Dao.DAONAME);

    try{
      User user = (User) request.getSession().getAttribute("user");

      if(user == null || !user.getRole().equalsIgnoreCase("administrator")) {
        // l'utente non è autorizzato
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        gresp = new GenericResponse();
        gresp.setErrorOccurred("Utente nullo o non autorizzato");
        gresp.setResult(false);
        response.getWriter().write(gson.toJson(gresp));
        return;
      }
      if (user.getRole().equalsIgnoreCase("administrator")) {
        String badge=request.getParameter("badgeNumber");
        String codCourse=request.getParameter("codCourse");

        if (badge==null || codCourse==null){
          gresp.setResult(false);
          gresp.setErrorOccurred("Selezionare un insegnante e una materia tra quelli proposti");
          response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
          response.getWriter().write(gson.toJson(gresp));
          return;
        }
        else{
          int row=dao.saveNewAssociation(codCourse,badge);
          gresp.setResult(true);
          response.setStatus(HttpServletResponse.SC_OK);
          response.getWriter().write(gson.toJson(gresp));
        }
      }else {
        gresp.setResult(false);
        gresp.setErrorOccurred("Errore nel reperimento dei dati");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(gson.toJson(gresp));
        return;
      }
    }catch (Exception ex){
      ex.printStackTrace();
      gresp.setResult(false);
      gresp.setErrorOccurred(ex.getMessage());
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().write(gson.toJson(gresp));
    }

    }
}
