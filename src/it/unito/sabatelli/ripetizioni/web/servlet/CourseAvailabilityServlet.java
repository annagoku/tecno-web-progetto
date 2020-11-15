package it.unito.sabatelli.ripetizioni.web.servlet;

import com.google.gson.Gson;
import it.unito.sabatelli.ripetizioni.dao.Dao;
import it.unito.sabatelli.ripetizioni.model.CatalogItem;
import it.unito.sabatelli.ripetizioni.model.Course;
import it.unito.sabatelli.ripetizioni.model.GenericResponse;
import it.unito.sabatelli.ripetizioni.model.User;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name="CourseAvailability", urlPatterns = "/servlets/CourseAvailability")
public class CourseAvailabilityServlet extends HttpServlet {

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws javax.servlet.ServletException, IOException {
    response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws javax.servlet.ServletException, IOException {
    processRequest(request, response);
  }

  private void processRequest(HttpServletRequest request, HttpServletResponse response) throws javax.servlet.ServletException, IOException {
    //TODO reperire i corsi

    //setto il content Type
    response.setContentType("application/json");
    Gson gson = new Gson();
    GenericResponse gr= new GenericResponse();
    ArrayList[][] matrixCourse=new ArrayList[4][5];

    for(int i=0; i< matrixCourse.length; i++) {
      for(int j=0; j<matrixCourse[i].length; j++) {
        matrixCourse[i][j] = new ArrayList();
      }
    }

    try {
      String coursecode=request.getParameter("courseCode");

      if (coursecode==null){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        gr.setResult(false);
        gr.setErrorOccurred("Manca parametro courseCode");
        response.getWriter().write(gson.toJson(gr));
        return;
      }

      Dao dao = (Dao) request.getServletContext().getAttribute(Dao.DAONAME);

      List<CatalogItem> list= dao.getCatalogFiltered(coursecode);


      for (CatalogItem item: list){
        matrixCourse[item.getSlot().getId()-1][item.getDay().getDaycode()-1].add(item);
      }

      String json = gson.toJson(matrixCourse);
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
