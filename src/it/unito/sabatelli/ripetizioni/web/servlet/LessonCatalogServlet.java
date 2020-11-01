package it.unito.sabatelli.ripetizioni.web.servlet;

import com.google.gson.Gson;
import it.unito.sabatelli.ripetizioni.dao.Dao;
import it.unito.sabatelli.ripetizioni.model.CatalogItem;
import it.unito.sabatelli.ripetizioni.model.Course;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name="GetCatalog", urlPatterns = "/servlets/GetCatalog")
public class LessonCatalogServlet extends HttpServlet {
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws javax.servlet.ServletException, IOException {
    response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws javax.servlet.ServletException, IOException {
    processRequest(request, response);
  }

  private void processRequest(HttpServletRequest request, HttpServletResponse response) throws javax.servlet.ServletException, IOException {


    //setto il content Type
    response.setContentType("application/json");
    Gson gson = new Gson();

    try {

      Dao dao = (Dao) request.getServletContext().getAttribute(Dao.DAONAME);

      List<CatalogItem> list= dao.getCatalog();
      String json = gson.toJson(list);
      response.getWriter().write(json);
      response.setStatus(HttpServletResponse.SC_OK);

    }
    catch (SQLException e) {
      e.printStackTrace();
      response.getWriter().write("{message: \"Impossibile reperire i dati\"}");
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }


  }

}
