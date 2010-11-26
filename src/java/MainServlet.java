/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import db.DBConnector;
import exceptions.MySQLException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException, MySQLException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            Connection connection = DBConnector.getConnection();
            HttpSession seas = request.getSession();
            seas.setAttribute("user", request.getParameter("user"));
            seas.setAttribute("password", request.getParameter("password"));
            seas.setAttribute("status", getMyStatus(request.getParameter("user").toString()));

            String user = seas.getAttribute("user").toString();
            String password = seas.getAttribute("password").toString();
            String projectName = getProjectName(connection, user);
            seas.setAttribute("projectname", projectName);

            if (checkLogin(connection, user, password)) {
                PreparedStatement ps = connection.prepareStatement("SELECT firstlogin FROM user WHERE email=?");
                ps.setString(1, user);
                ResultSet rs = ps.executeQuery();
                rs.next();
                int firstLogin = rs.getInt(1);
                if (firstLogin == 0) {
                    out.write(firstLoginView(0));
                } else {
                    out.write(mainView(projectName));
                }
            } else {
                out.write("<a href='Login.html'>Falscher Benutzername oder falsches Passwort eingegeben!</a>");
            }
            connection.close();
        } finally {
            out.close();
        }
    }

    private boolean checkLogin(Connection c, String user, String password) {
        try {
            PreparedStatement ps = c.prepareStatement("SELECT password FROM user WHERE LOWER(email)=?");
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

    private String getProjectName(Connection c, String user) {
        try {
            PreparedStatement ps = c.prepareStatement("SELECT projectname FROM user WHERE email = ?");
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

    public static String mainView(String projectName) {
        StringBuilder sb = new StringBuilder(500);
        sb.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
        sb.append("<html>");
        sb.append("<head>");
        sb.append("<meta http-equiv=\"Content - Type\" content=\"text / html;charset = iso - 8859 - 1\">");
        sb.append("<title>Untitled Document</title>");
        sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"start.css\">");
        sb.append("<script src=\"jsXMLHttpRequestHandle.js\" type=\"text/javascript\"></script>");
        sb.append("</head>");
        sb.append("<body>");
        sb.append("<div id=\"frame\">");
        sb.append("<div id=\"title\">");
        sb.append(projectName);
        sb.append("</div>");
        sb.append("<div class=\"menuTopButton\">");
        sb.append("<a href=\"#\">&Uuml;berblick</a>");
        sb.append("</div>");
        sb.append("<div class=\"menuTopButton\">");
        sb.append("<a href=\"#\">Aufgaben</a>");
        sb.append("</div>");
        sb.append("<div class=\"menuTopButton\">");
        sb.append("<a href=\"#\">Zeiten</a>");
        sb.append("</div>");
        sb.append("<div class=\"menuTopButton\">");
        sb.append("<a href=\"#\">Profil</a>");
        sb.append("</div>");
        sb.append("<div class=\"menuTopButton\" onclick=\"showMembers()\">");
        sb.append("<a href=\"#\">Mitglieder</a>");
        sb.append("</div>");
        sb.append("<div class=\"menuTopButton\" onclick=\"logout()\">");
        sb.append("<a href=\"#\">Logout</a>");
        sb.append("</div>");
        sb.append("<div style=\"clear:both;\">");
        sb.append("</div>");
        sb.append("<div id=\"content\">");
        sb.append("Und hier kommt der Inhalt.");
        sb.append("</div>");
        sb.append("</div>");
        sb.append("</body>");
        sb.append("</html>");
        return sb.toString();
    }

    public static String firstLoginView(int falsePassword) {
        StringBuilder sb = new StringBuilder(500);
        sb.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
        sb.append("<html>");
        sb.append("<head>");
        sb.append("<title>FirstLogin</title>");
        sb.append("<meta http-equiv=\"Content - Type\" content=\"text / html;charset = iso - 8859 - 1\">");
        sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"start.css\">");
        sb.append("</head>");
        sb.append("<body>");
        sb.append("<div id=\"firstLogin\">");
        if (falsePassword == 0) {
            sb.append("<div id=\"textFirstLogin\">");
            sb.append("Herzlich Wilkommen,");
            sb.append("<br>");
            sb.append("da das Ihr erster Login ist ändern Sie bitte ihr Passwort.");
            sb.append("</div>");
        } else if (falsePassword == 1) {
            sb.append("<h1>Die neu eingebenen Passw&ouml;rter stimmen nicht überein!</h1>");
        } else {
            sb.append("<h1>Das eingegebene bisherige Passwort ist falsch!</h1>");
        }
        sb.append("<br>");
        sb.append("<form action=\"/Projektmanager/FirstLogin\" method=\"post\"");
        sb.append("<table>");
        sb.append("<tr>");
        sb.append("<th align=\"left\">");
        sb.append("Altes Passwort:");
        sb.append("</th>");
        sb.append("<th>");
        sb.append("<input name=\"oldPassword\" type=\"password\" />");
        sb.append("</th>");
        sb.append("</tr>");
        sb.append("<tr>");
        sb.append("<th align=\"left\">");
        sb.append("Neues Passwort:");
        sb.append("</th>");
        sb.append("<th>");
        sb.append("<input name=\"newPassword\" type=\"password\">");
        sb.append("<br>");
        sb.append("</th>");
        sb.append("</tr>");
        sb.append("<tr>");
        sb.append("<th align=\"left\">");
        sb.append("Neues Passwort bestätigen:");
        sb.append("</th>");
        sb.append("<th>");
        sb.append("<input name=\"validatePassword\" type=\"password\">");
        sb.append("<br>");
        sb.append("</th>");
        sb.append("</tr>");
        sb.append("</table>");
        sb.append("<input type=\"submit\" value=\"Speichern\" />");
        sb.append("</form>");
        sb.append("</div>");
        sb.append("</body>");
        sb.append("</html>");
        return sb.toString();
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
        try {
            processRequest(request, response);
        } catch (SQLException ex) {
            Logger.getLogger(MainServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MySQLException ex) {
            Logger.getLogger(MainServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
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
        try {
            processRequest(request, response);
        } catch (SQLException ex) {
            Logger.getLogger(MainServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MySQLException ex) {
            Logger.getLogger(MainServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private String getMyStatus(String userEmail) {
        String status = "";
        try {
            ResultSet rs = DBConnector.getConnection().createStatement().executeQuery("SELECT status FROM user WHERE email='" + userEmail + "'");
            if (rs.next()) {
                status = rs.getString(1);
            }
            rs.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return status;
    }
}
