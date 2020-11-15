package it.unito.sabatelli.ripetizioni.web.servlet;

import com.google.gson.Gson;
import it.unito.sabatelli.ripetizioni.dao.Dao;
import it.unito.sabatelli.ripetizioni.model.GenericResponse;
import it.unito.sabatelli.ripetizioni.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name="GetUserList", urlPatterns = "/servlets/GetUserList")
public class GetUserListServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    processRequestGet(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
  }

  private  void processRequestGet (HttpServletRequest request, HttpServletResponse response) throws javax.servlet.ServletException, IOException {
    response.setContentType("application/json");
    Gson gson = new Gson();
    GenericResponse gr = new GenericResponse();
    User user = (User) request.getSession().getAttribute("user");

    try {
      if (!user.getRole().equalsIgnoreCase("administrator")) {
        gr.setResult(false);
        gr.setErrorOccurred("Utente non autorizzato");
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        response.getWriter().write(gson.toJson(gr));
      } else {
        Dao dao = (Dao) request.getServletContext().getAttribute(Dao.DAONAME);
        List<User> listUser=dao.getUserListAdmin();
        gr.setResult(true);
        response.getWriter().write(gson.toJson(listUser));

      }
    } catch (Exception e) {
      e.printStackTrace();
      gr.setResult(false);
      gr.setErrorOccurred(e.getMessage());
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(gson.toJson(gr));
    }
  }
}
