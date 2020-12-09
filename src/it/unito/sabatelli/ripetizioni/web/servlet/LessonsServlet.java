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
  /**
   * Resituisce l'elenco delle lezioni
   *
   * @param req
   * @param resp
   * @throws ServletException
   * @throws IOException
   */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    processRequestGet(req, resp);
  }

  /**
   * Modifica lo stato
   *
   * @param req
   * @param resp
   * @throws ServletException
   * @throws IOException
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    processRequestPost(req, resp);
  }

  protected void processRequestPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
      User user = (User) request.getSession().getAttribute("user");

      if(action.equalsIgnoreCase("modificastato")){
        String idLesson=request.getParameter("idLesson");
        String stateLesson=request.getParameter("stateLesson");

        Lesson lesson = dao.getLesson(idLesson);

        if(lesson == null) {
          gr.setResult(false);
          gr.setErrorOccurred("Lezione non esistente");
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          response.getWriter().write(gson.toJson(gr));
          return;

        }
        if(user.getRole().equalsIgnoreCase("student") && !lesson.getUser().getId().equalsIgnoreCase(user.getId())) {
          gr.setResult(false);
          gr.setErrorOccurred("Accesso non consentito");
          response.setStatus(HttpServletResponse.SC_FORBIDDEN);
          response.getWriter().write(gson.toJson(gr));
          return;
        }

        if(lesson.getState().getCode() > 1) {
          gr.setResult(false);
          gr.setErrorOccurred("Lo stato della lezione non è modificabile");
          response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
          response.getWriter().write(gson.toJson(gr));
          return;
        }

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
    } catch (Exception e) {
      e.printStackTrace();
      gr.setResult(false);
      gr.setErrorOccurred("Si è verificato un errore nell'aggiornamento");
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
    String json = gson.toJson(gr);
    System.out.println("Lessons -> Risultato POST -> "+json);
    response.getWriter().write(json);

  }

  private void processRequestGet (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
    response.setContentType("application/json");
    Gson gson = new Gson();
    GenericResponse gr = new GenericResponse();

    boolean listFormat = request.getParameter("list") != null;

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

      if(!user.getRole().equalsIgnoreCase("administrator") && !listFormat) {

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
      gr.setResult(false);
      gr.setErrorOccurred("Impossibile reperire i dati");
      response.getWriter().write(gson.toJson(gr));
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

  }
  private boolean validateRequest (HttpServletRequest r){
    return true;
  }
}

