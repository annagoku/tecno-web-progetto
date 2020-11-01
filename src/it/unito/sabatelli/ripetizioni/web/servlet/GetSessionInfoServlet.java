package it.unito.sabatelli.ripetizioni.web.servlet;

import com.google.gson.Gson;
import it.unito.sabatelli.ripetizioni.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name="GetSessionInfo", urlPatterns = "/servlets/GetSessionInfo")
public class GetSessionInfoServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    processRequest(req, resp);

  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
  }

  private void processRequest (HttpServletRequest request, HttpServletResponse response) throws  IOException {
    response.setContentType("application/json");
    Gson gson = new Gson();

    HttpSession s=request.getSession();
    User connectedUser= (User) s.getAttribute("user");
    System.out.println("user logged "+connectedUser);

    String json = gson.toJson(connectedUser);
    response.getWriter().write(json);
    response.setStatus(HttpServletResponse.SC_OK);

  }

}
