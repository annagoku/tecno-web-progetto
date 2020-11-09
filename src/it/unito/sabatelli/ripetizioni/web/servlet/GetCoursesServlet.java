package it.unito.sabatelli.ripetizioni.web.servlet;

import com.google.gson.Gson;
import it.unito.sabatelli.ripetizioni.dao.Dao;
import it.unito.sabatelli.ripetizioni.model.Course;
import it.unito.sabatelli.ripetizioni.model.GenericResponse;
import it.unito.sabatelli.ripetizioni.model.User;
import org.apache.catalina.valves.rewrite.InternalRewriteMap;

import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import javax.servlet.http.*;

@WebServlet(name="GetCourses", urlPatterns = "/servlets/GetCourses")
public class GetCoursesServlet extends HttpServlet {
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws javax.servlet.ServletException, IOException {
    processRequestPost(request,response);
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws javax.servlet.ServletException, IOException {
    processRequestGet(request, response);
  }

  private void processRequestGet(HttpServletRequest request, HttpServletResponse response) throws javax.servlet.ServletException, IOException {
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

  private void processRequestPost(HttpServletRequest request, HttpServletResponse response) throws javax.servlet.ServletException, IOException{
    response.setContentType("application/json");
    Gson gson = new Gson();
    GenericResponse gresp= new GenericResponse();
    String regex = "^[a-zA-Z0-9]+$";
    Pattern pattern = Pattern.compile(regex);

    try {
      HttpSession s = request.getSession();
      User user = (User) s.getAttribute("user");

      if ( user== null || !user.getRole().equalsIgnoreCase("administrator")) {
        gresp.setResult(false);
        gresp.setErrorOccurred("Utente non autorizzato");
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        response.getWriter().write(gson.toJson(gresp));
        return;

      } else if (user.getRole().equalsIgnoreCase("administrator")) {
        String code = request.getParameter("newcode");
        String name = request.getParameter("newname");
        String image = request.getParameter("newimage");

        System.out.println("New Course-> code: "+code+" name: "+name+" icon: "+image);

        Dao dao = (Dao) request.getServletContext().getAttribute(Dao.DAONAME);

        if(code==null || name==null || !pattern.matcher(code).matches() || !pattern.matcher(name).matches() ) {
          gresp.setResult(false);
          gresp.setErrorOccurred("Compilare tutti i cambi obbligatori. Sono ammessi solo caratteri alfanumerici");
          response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
          response.getWriter().write(gson.toJson(gresp));
          return;
        } else if(!dao.checkNewCourse(code)){
          gresp.setResult(false);
          gresp.setErrorOccurred("Il corso è già presente nel database");
          response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
          response.getWriter().write(gson.toJson(gresp));
          return;
        } else{
          int row=dao.saveNewCourse(code.toUpperCase(), name,image);
          gresp.setResult(true);
          response.setStatus(HttpServletResponse.SC_OK);
          response.getWriter().write(gson.toJson(gresp));
          return;
        }
      }
      else {
        gresp.setResult(false);
        gresp.setErrorOccurred("Errore nel reperimento dei dati");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(gson.toJson(gresp));
        return;
      }
    }catch (Exception ex){
      ex.printStackTrace();
      gresp.setResult(false);
      gresp.setErrorOccurred("Errore "+ ex.getMessage());
      response.getWriter().write(gson.toJson(gresp));
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

  }
}
