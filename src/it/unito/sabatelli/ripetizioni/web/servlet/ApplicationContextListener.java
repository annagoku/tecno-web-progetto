package it.unito.sabatelli.ripetizioni.web.servlet;

import it.unito.sabatelli.ripetizioni.dao.Dao;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.SQLException;


@WebListener
public class ApplicationContextListener implements ServletContextListener {

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    System.out.println("------------------- Ripetizioni - START -------------------");

    String url = sce.getServletContext().getInitParameter("DB_URL");
    String user = sce.getServletContext().getInitParameter("DB_USER");
    String pwd = sce.getServletContext().getInitParameter("DB_PWD");

    System.out.println("DB URL  -> "+url);
    System.out.println("DB USER -> "+user);

    Dao dao = new Dao();
    try {
      dao.init(url, user, pwd);
    } catch (SQLException e) {
      e.printStackTrace();

    }
    sce.getServletContext().setAttribute(Dao.DAONAME, dao);
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    System.out.println("------------------- Ripetizioni - END -------------------");
  }
}
