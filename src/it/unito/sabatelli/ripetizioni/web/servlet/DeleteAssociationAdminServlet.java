package it.unito.sabatelli.ripetizioni.web.servlet;

import com.google.gson.Gson;
import it.unito.sabatelli.ripetizioni.dao.Dao;
import it.unito.sabatelli.ripetizioni.model.CatalogItem;
import it.unito.sabatelli.ripetizioni.model.GenericResponse;
import it.unito.sabatelli.ripetizioni.model.TeacherCourse;
import it.unito.sabatelli.ripetizioni.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name="DeleteAssociationAdmin", urlPatterns = "/servlets/DeleteAssociationAdmin")
public class DeleteAssociationAdminServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    processRequestPost(req, resp);
  }

  public void processRequestPost (HttpServletRequest request, HttpServletResponse response) throws javax.servlet.ServletException, IOException {
    response.setContentType("application/json");
    Gson gson = new Gson();
    GenericResponse gresp= new GenericResponse();
    User user= (User) request.getSession().getAttribute("user");



    try{
      if(!user.getRole().equalsIgnoreCase("administrator")){
        gresp.setResult(false);
        gresp.setErrorOccurred("Operazione non atorizzata");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(gson.toJson(gresp));
        return;
      }else{
        Dao dao = (Dao) request.getServletContext().getAttribute(Dao.DAONAME);
        TeacherCourse tc=gson.fromJson(request.getParameter("associationToDelete"), TeacherCourse.class);
        if (tc!=null){
          int row=dao.deleteAssociationAdmin(tc);
          gresp.setResult(true);
          response.setStatus(HttpServletResponse.SC_OK);
          response.getWriter().write(gson.toJson(gresp));
          return;

        }else{
          gresp.setResult(false);
          gresp.setErrorOccurred("Non è possibile accedere ai dati");
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          response.getWriter().write(gson.toJson(gresp));
          return;

        }

      }

    }catch (Exception e){
      e.printStackTrace();
      gresp.setResult(false);
      gresp.setErrorOccurred("Errore nell'accesso ai dati" + e.getMessage());
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response.getWriter().write(gson.toJson(gresp));
      return;

    }

  }

}
