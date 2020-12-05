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
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet (name="CheckLogin",  urlPatterns = "/servlets/CheckLogin")

public class LoginServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    processRequest ( resp, req);
  }

  private void processRequest (HttpServletResponse response, HttpServletRequest request) throws IOException, ServletException {
    GenericResponse genericResponseJson = new GenericResponse();
    Gson gson = new Gson();
    response.setContentType("application/json");
    try {
      Dao dao = (Dao) request.getServletContext().getAttribute(Dao.DAONAME);

      request.getSession().removeAttribute("user");
      genericResponseJson.setSessionId(request.getSession().getId());
      boolean found = false;
      String username=request.getParameter("user");
      String password=request.getParameter("psw");
      User userFound=dao.checkUserDB(username, password);
      if (userFound!=null){
        found=true;
      }
      if (found) {
        HttpSession s= request.getSession();
        s.setAttribute("user", userFound);
        System.out.println("Login -> session attribute user -> " + s.getAttribute("user"));
        genericResponseJson.setResult(true);
        response.getWriter().write(gson.toJson(genericResponseJson));
        response.setStatus(HttpServletResponse.SC_OK);
      }

      else{
        String s="*Username o password errati";
        genericResponseJson.setResult(false);
        genericResponseJson.setErrorOccurred(s);
        //String s="*Username o password errati";
        String json = gson.toJson(genericResponseJson);
        response.getWriter().write(json);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      }

    }
    catch (SQLException e) {
      e.printStackTrace();
      genericResponseJson.setResult(false);
      genericResponseJson.setErrorOccurred("Si è verificato un errore");
      response.getWriter().write(gson.toJson(genericResponseJson));
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

  }



}

