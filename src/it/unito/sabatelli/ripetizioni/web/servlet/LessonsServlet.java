package it.unito.sabatelli.ripetizioni.web.servlet;

import com.google.gson.Gson;
import it.unito.sabatelli.ripetizioni.dao.Dao;
import it.unito.sabatelli.ripetizioni.model.GenericResponse;
import it.unito.sabatelli.ripetizioni.model.Lesson;
import it.unito.sabatelli.ripetizioni.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


@WebServlet(name="Lessons", urlPatterns = "/servlets/lessons")
public class LessonsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      response.setContentType("application/json");
      Gson gson = new Gson();
      GenericResponse gr= new GenericResponse();

      try {
        if (!validateRequest(request)){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }
      Dao dao = (Dao) request.getServletContext().getAttribute(Dao.DAONAME);
      String action = request.getParameter("action");

        if(action.equalsIgnoreCase("modificastato")){
          String idLesson=request.getParameter("idLesson");
          String stateLesson=request.getParameter("stateLesson");
            int result=dao.saveLessonState(Integer.parseInt(idLesson), Integer.parseInt(stateLesson));
            System.out.println("Righe modificate da saveLessonState ->"+ result);
            if (result!=1){
              gr.setResult(false);
              gr.setErrorOccurred("Si è verificato un errore nell'aggiornamento");
              response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            else{
              gr.setResult(true);
              response.setStatus(HttpServletResponse.SC_OK);
            }

        }
      } catch (SQLException e) {
        e.printStackTrace();
        gr.setResult(false);
        gr.setErrorOccurred("Si è verificato un errore nell'aggiornamento");
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
      String json = gson.toJson(gr);
      response.getWriter().write(json);

    }

    private void processRequest (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
      response.setContentType("application/json");
      Gson gson = new Gson();


      try {
        User user = (User) request.getSession().getAttribute("user");

        Dao dao = (Dao) request.getServletContext().getAttribute(Dao.DAONAME);

        List<Lesson> lessons= dao.loadLesson(user.getId(), user.getRole());
        ArrayList[][] matrixLesson = new ArrayList[4][5];
        for(int i=0; i< matrixLesson.length; i++) {
          for(int j=0; j<matrixLesson[i].length; j++) {
            matrixLesson[i][j] = new ArrayList();
          }
        }

        if(!user.getRole().equalsIgnoreCase("administrator")) {
          //todo leggere le dimensioni da DB


          for (Lesson l: lessons) {
            matrixLesson[l.getSlot().getId()-1][l.getDay().getDaycode()-1].add(l);
          }
          String json = gson.toJson(matrixLesson);
          response.getWriter().write(json);
          response.setStatus(HttpServletResponse.SC_OK);

        }
        else {
          String json = gson.toJson(lessons);
          response.getWriter().write(json);
          response.setStatus(HttpServletResponse.SC_OK);
        }


      }
      catch (SQLException e) {
        e.printStackTrace();
        response.getWriter().write("{message: \"Impossibile reperire i dati\"}");
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }

    }
    private boolean validateRequest (HttpServletRequest r){
      return true;
    }
  }

