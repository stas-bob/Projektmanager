/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import db.DBConnector;
import exceptions.MySQLException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author bline
 */
public class MainServlet extends HttpServlet {

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            HttpSession seas = request.getSession();
            seas.setAttribute("user", request.getParameter("benutzer"));
            seas.setAttribute("password", request.getParameter("passwort"));
            Connection c = DBConnector.getConnection();
            System.out.println(c.getMetaData() + " " + seas.getAttribute("user"));
            System.out.println(c.getMetaData() + " " + seas.getAttribute("password"));

            out.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
            out.write("<html>");
            out.write("<head>");
            out.write("<meta http-equiv=\"Content - Type\" content=\"text / html;charset = iso - 8859 - 1\">");
            out.write("<title>Untitled Document</title>");
            out.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"start.css\">");
            out.write("</head>");
            out.write("<body>");
            out.write("<div id=\"frame\">");
            out.write("<div id=\"title\">");
            out.write("Title des Projektes");
            out.write("</div>");
            out.write("<div class=\"menuTopButton\">");
            out.write("<a href=\"#\">&Uuml;berblick</a>");
            out.write("</div>");
            out.write("<div class=\"menuTopButton\">");
            out.write("<a href=\"#\">Aufgaben</a>");
            out.write("</div>");
            out.write("<div class=\"menuTopButton\">");
            out.write("<a href=\"#\">Zeiten</a>");
            out.write("</div>");
            out.write("<div class=\"menuTopButton\">");
            out.write("<a href=\"#\">Profil</a>");
            out.write("</div>");
            out.write("<div class=\"menuTopButton\">");
            out.write("<a href=\"#\">Logout</a>");
            out.write("</div>");
            out.write("<div style=\"clear:both;\">");
            out.write("</div>");
            out.write("<div id=\"Content\">");
            out.write("Und hier kommt der Inhalt.");
            out.write("</div>");
            out.write("<div>");
            out.write("</body>");
            out.write("</html>");


            //seas.invalidate(); //Logout
        } catch (SQLException ex) {
            Logger.getLogger(MainServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MySQLException ex) {
            Logger.getLogger(MainServlet.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
