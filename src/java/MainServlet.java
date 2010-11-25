/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import db.DBConnector;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
            seas.setAttribute("user", request.getParameter("user"));
            seas.setAttribute("password", request.getParameter("password"));

            String user = seas.getAttribute("user").toString();
            String password = seas.getAttribute("password").toString();
            String projectName = getProjectName(user);
            seas.setAttribute("projectname", projectName);

            if (checkLogin(user, password)) {
                out.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
                out.write("<html>");
                out.write("<head>");
                out.write("<meta http-equiv=\"Content - Type\" content=\"text / html;charset = iso - 8859 - 1\">");
                out.write("<title>Untitled Document</title>");
                out.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"start.css\">");
                out.write("<script src=\"jsXMLHttpRequestHandle.js\" type=\"text/javascript\"></script>");
                out.write("</head>");
                out.write("<body>");
                out.write("<div id=\"frame\">");
                out.write("<div id=\"title\">");
                out.write(projectName);
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
                out.write("<div class=\"menuTopButton\" onclick=\"showMembers()\">");
                out.write("<a href=\"#\">Mitglieder</a>");
                out.write("</div>");
                out.write("<div class=\"menuTopButton\">");
                out.write("<a href=\"#\">Logout</a>");
                out.write("</div>");
                out.write("<div style=\"clear:both;\">");
                out.write("</div>");
                out.write("<div id=\"content\">");
                out.write("Und hier kommt der Inhalt.");
                out.write("</div>");
                out.write("</div>");
                out.write("</body>");
                out.write("</html>");
            } else {
                out.write("<a href='Login.html'>Falscher Benutzername oder falsches Passwort eingegeben!</a>");
            }
        } finally {
            out.close();
        }
    }

    private boolean checkLogin(String user, String password) {
        try {
            Connection c = DBConnector.getConnection();
            PreparedStatement ps = c.prepareStatement("SELECT password FROM User WHERE LOWER(email)=?");
            user = user.toLowerCase();
            ps.setString(1, user);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getString(1).equals(password)) {
                    ps.close();
                    return true;
                }
            }
            ps.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private String getProjectName(String user) {
        try {
            Connection c = DBConnector.getConnection();
            PreparedStatement ps = c.prepareStatement("select Projectname from `User` where `EMail`=?");
            ps.setString(1, user);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String projectname = rs.getString(1);
                ps.close();
                return projectname;
            }
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
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
