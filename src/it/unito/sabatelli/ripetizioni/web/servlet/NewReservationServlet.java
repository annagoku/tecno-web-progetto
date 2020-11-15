package it.unito.sabatelli.ripetizioni.web.servlet;

import com.google.gson.Gson;
import it.unito.sabatelli.ripetizioni.dao.Dao;
import it.unito.sabatelli.ripetizioni.model.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name="NewReservation", urlPatterns = "/servlets/newReservation")
public class NewReservationServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    processRequest (request,response);
  }

  private void processRequest (HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
    response.setContentType("application/json");
    Gson gson = new Gson();
    GenericResponse gr= new GenericResponse();

    System.out.println("NewReservationServlet");

    try{
      if (!validateRequest(request)){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }

      User user = (User) request.getSession().getAttribute("user");
      Dao dao = (Dao) request.getServletContext().getAttribute(Dao.DAONAME);
      CatalogItem itemSelected= gson.fromJson(request.getParameter("infoCatalogItemSelected"),CatalogItem.class);

      String userIdSelected = request.getParameter("userId");

      if(userIdSelected != null && !user.getRole().equalsIgnoreCase("administrator")) {
        gr.setResult(false);
        gr.setErrorOccurred("Non si hanno i permessi per inserire una prenotazione");
        String json = gson.toJson(gr);
        response.getWriter().write(json);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }

      if(user.getRole().equalsIgnoreCase("student")) {
        userIdSelected = user.getId();
      }

      Lesson l= dao.checkLesson(userIdSelected, itemSelected.getDay().getDaycode(), itemSelected.getSlot().getId());

      if (l!= null && l.getState().getCode()==2){
        gr.setResult(false);
        gr.setErrorOccurred("Esiste già una lezione effettuata nello slot scelto");
        String json = gson.toJson(gr);
        response.getWriter().write(json);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }
      else if(user.getRole().equalsIgnoreCase("administrator") && l!= null && l.getState().getCode() <3){
        gr.setResult(false);
        gr.setErrorOccurred("Esiste già una lezione prenotata o effettuata nello slot scelto");
        String json = gson.toJson(gr);
        response.getWriter().write(json);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }


      dao.saveNewReservation(itemSelected, userIdSelected, l);
      gr.setResult(true);
      String json = gson.toJson(gr);
      response.getWriter().write(json);
      response.setStatus(HttpServletResponse.SC_OK);


    }catch (Exception e){
      e.printStackTrace();
      gr.setResult(false);
      gr.setErrorOccurred("Impossibile aggiornare i dati");
      String json = gson.toJson(gr);
      response.getWriter().write(json);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

  }
  private boolean validateRequest (HttpServletRequest r){
    return true;
  }
}
