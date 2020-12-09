package it.unito.sabatelli.ripetizioni.web.servlet;

import com.google.gson.Gson;
import it.unito.sabatelli.ripetizioni.model.GenericResponse;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet(name="Logout", urlPatterns = "/servlets/logout")
public class LogoutServlet extends HttpServlet {
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
    GenericResponse gr= new GenericResponse();
    System.out.println("entro nella get");

    HttpSession s=request.getSession();
    s.removeAttribute("user");
    s.invalidate();
    handleLogOutResponseCookie(request, response);
    System.out.println("annullo i cookie");

    if(request.getSession(false)==null){
      gr.setResult(true);
      response.getWriter().write(gson.toJson(gr));
      response.setStatus(HttpServletResponse.SC_OK);
    }
    else{
      gr.setResult(false);
      gr.setErrorOccurred("Impossibile eseguire il logout");
      response.getWriter().write(gson.toJson(gr));
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private void handleLogOutResponseCookie(HttpServletRequest request, HttpServletResponse response) {
    Cookie[] cookies = request.getCookies();
    if(cookies!=null) {
      for (Cookie cookie : cookies) {
        cookie.setMaxAge(0);
        cookie.setValue(null);
        cookie.setPath("/");
        response.addCookie(cookie);
      }
    }
  }
}
