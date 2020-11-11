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
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;


@WebServlet(name="GetTeachers", urlPatterns = "/servlets/GetTeachers")
public class GetTeacherServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      processRequestGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      processRequestPost(req, resp);
    }

    private void processRequestGet (HttpServletRequest request, HttpServletResponse response) throws javax.servlet.ServletException, IOException{
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

    private void processRequestPost (HttpServletRequest request, HttpServletResponse response)throws javax.servlet.ServletException, IOException{
      response.setContentType("application/json");
      Gson gson = new Gson();
      GenericResponse gresp= new GenericResponse();
      String regexAlphabetic = "^[a-zA-Z ]+$";
      String regexNumeric = "^[0-9]+$";
      Pattern patternAlphabetic = Pattern.compile(regexAlphabetic);
      Pattern patternNumeric = Pattern.compile(regexNumeric);

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
          String badge = request.getParameter("newbadge");
          String name = request.getParameter("newname");
          String surname = request.getParameter("newsurname");
          String avatar = request.getParameter("newavatar");

          System.out.println("New Teacher-> badge: "+badge+" name: "+name+ "surname:"+surname+ "avatar:"+avatar);

          Dao dao = (Dao) request.getServletContext().getAttribute(Dao.DAONAME);

          if(badge==null || name==null || surname==null|| !patternNumeric.matcher(badge).matches() || !patternAlphabetic.matcher(name).matches()
                  ||!patternAlphabetic.matcher(surname).matches() ) {
            gresp.setResult(false);
            gresp.setErrorOccurred("Compilare tutti i cambi obbligatori. Sono ammessi solo caratteri alfanumerici");
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
            response.getWriter().write(gson.toJson(gresp));
            return;
          } else if(!dao.checkNewTeacher(badge)){
            gresp.setResult(false);
            gresp.setErrorOccurred("Il numero di badge è già presente nel database");
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
            response.getWriter().write(gson.toJson(gresp));
            return;
          } else{
            if (avatar==null) avatar="assets/img/soldier.png";
            int row=dao.saveNewTeacher(badge, name, surname, avatar);
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

