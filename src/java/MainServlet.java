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
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.swing.JFrame;

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
            seas.setAttribute("user_id", getUserId(request.getParameter("user"), connection));
            seas.setAttribute("password", request.getParameter("password"));
            seas.setAttribute("startProject", getStartProject(Integer.parseInt(seas.getAttribute("user_id").toString()), connection));
            seas.setAttribute("endProject", getEndProject(Integer.parseInt(seas.getAttribute("user_id").toString()), connection));
            seas.setAttribute("status", getMyStatus(request.getParameter("user"), connection));
            seas.setAttribute("modules", getMyModules(request.getParameter("user"), connection));
            seas.setAttribute("name", getName(request.getParameter("user"), connection));
            seas.setAttribute("firstname", getFirstname(request.getParameter("user"), connection));

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
                    out.write(changePasswordView(0));
                } else {
                    out.write(mainView(projectName, false));
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

    public static String mainView(String projectName, boolean passwordChange) {
        StringBuilder sb = new StringBuilder(500);
        sb.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
        sb.append("<html>");
        sb.append("<head>");
        sb.append("<title>Projektmanager</title>");
        sb.append("<meta http-equiv=\"Content - Type\" content=\"text / html;charset = iso - 8859 - 1\">");
        sb.append("<title>Untitled Document</title>");
        sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"start.css\"></link>");
        sb.append("<script src=\"jsXMLHttpRequestHandle.js\" type=\"text/javascript\"></script>");
        sb.append("</head>");
        sb.append("<body onLoad=\"showOverview()\">");
        sb.append("<div id=\"frame\">");
        sb.append("<div id=\"title\">");
        sb.append(projectName);
        sb.append("</div>");
        sb.append("<div class=\"menuTopButton\" onclick=\"showOverview()\">");
        sb.append("<a href=\"#\">&Uuml;berblick</a>");
        sb.append("</div>");
        sb.append("<div class=\"menuTopButton\" onclick=\"showModules()\">");
        sb.append("<a href=\"#\">Aufgaben</a>");
        sb.append("</div>");
        sb.append("<div class=\"menuTopButton\" onclick=\"showTimes()\">");
        sb.append("<a href=\"#\">Zeiten</a>");
        sb.append("</div>");
        sb.append("<div class=\"menuTopButton\" onclick=\"showProfile()\">");
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
        if (passwordChange) {
            sb.append("Ihr Kennwort wurde erfolgreich ge&auml;ndert.");
        }
        sb.append("</div>");
        sb.append("<div id=\"statusBox\" style=\"text-align: center; border: 1px solid; margin-left: 0px; width: 820px; position: relative; height: 25px;\"></div>");
        sb.append("</div>");
        sb.append("</body>");
        sb.append("</html>");
        return sb.toString();
    }

    public static String changePasswordView(int falsePassword) {
        StringBuilder sb = new StringBuilder(500);
        sb.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
        sb.append("<html>");
        sb.append("<head>");
        sb.append("<title>First Login</title>");
        sb.append("<meta http-equiv=\"Content - Type\" content=\"text / html;charset = iso - 8859 - 1\">");
        sb.append("<title>Untitled Document</title>");
        sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"start.css\">");
        sb.append("</head>");
        sb.append("<body>");
        if (falsePassword == 0) {
            sb.append("Herzlich Wilkommen,");
            sb.append("<br>");
            sb.append("da das Ihr erster Login ist, aendern Sie bitte ihr Passwort.");
        } else if (falsePassword == 1) {
            sb.append("Die neu eingebenen Passw&ouml;rter stimmen nicht ueberein!");
        } else if (falsePassword == 2) {
            sb.append("Das eingegebene bisherige Passwort ist falsch!");
        } else {
            sb.append("Fehler bei der &Auml;nderung ihres Passwortes.");
        }
        sb.append("<br>");
        sb.append("<form action=\"/Projektmanager/FirstLogin\" method=\"post\" >");
        sb.append(changePasswordArea());
        sb.append("<input type=\"submit\" value=\"Speichern\" />");
        sb.append("</form>");
        sb.append("</body>");
        sb.append("</html>");
        return sb.toString();
    }

    public static String changePasswordArea() {
        StringBuilder sb = new StringBuilder(200);
        sb.append("<table>");
        sb.append("<tr>");
        sb.append("<td align=\"left\">");
        sb.append("Altes Passwort:");
        sb.append("</td>");
        sb.append("<td>");
        sb.append("<input id=\"oldPassword\" name=\"oldPassword\" type=\"password\" />");
        sb.append("</td>");
        sb.append("</tr>");
        sb.append("<tr>");
        sb.append("<td align=\"left\">");
        sb.append("Neues Passwort:");
        sb.append("</td>");
        sb.append("<td>");
        sb.append("<input id=\"newPassword\" name=\"newPassword\" type=\"password\">");
        sb.append("<br>");
        sb.append("</td>");
        sb.append("</tr>");
        sb.append("<tr>");
        sb.append("<td align=\"left\">");
        sb.append("Neues Passwort best&auml;tigen:");
        sb.append("</td>");
        sb.append("<td>");
        sb.append("<input id=\"validatePassword\" name=\"validatePassword\" type=\"password\">");
        sb.append("<br>");
        sb.append("</td>");
        sb.append("</tr>");
        sb.append("</table>");
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

    private String getMyStatus(String email, Connection c) {
        String status = "";
        try {
            PreparedStatement ps = c.prepareStatement("SELECT status FROM user WHERE email = ?");
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                status = rs.getString(1);
            }
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return status;
    }

    private ArrayList<Integer> getMyModules(String email, Connection c) {
        try {
            PreparedStatement ps = c.prepareStatement("SELECT modulid FROM rel_module_user WHERE email = ?");
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            ArrayList<Integer> modules = new ArrayList<Integer>();
            while (rs.next()) {
                modules.add(rs.getInt(1));
            }
            ps.close();
            return modules;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private String getName(String email, Connection c) {
        String name = "";
        try {
            PreparedStatement ps = c.prepareStatement("SELECT name FROM user WHERE email = ?");
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                name = rs.getString(1);
            }
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return name;
    }

    private String getFirstname(String email, Connection c) {
        String firstname = "";
        try {
            PreparedStatement ps = c.prepareStatement("SELECT firstname FROM user WHERE email = ?");
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                firstname = rs.getString(1);
            }
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return firstname;
    }

    private int getUserId(String email, Connection c) {
        int user_id = -1;
        try {
            PreparedStatement ps = c.prepareStatement("SELECT id FROM user WHERE email = ?");
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                user_id = rs.getInt(1);
            }
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return user_id;
    }

    private static String getStartProject(int user_id, Connection c) {
        String date = "";
        try {
            PreparedStatement ps = c.prepareStatement("SELECT start FROM project WHERE name = (SELECT projectname FROM user WHERE id = ?)");
            ps.setInt(1, user_id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                date = rs.getString(1);
            }
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return date;
    }

    private static String getEndProject(int user_id, Connection c) {
        String date = "";
        try {
            PreparedStatement ps = c.prepareStatement("SELECT end FROM project WHERE name = (SELECT projectname FROM user WHERE id = ?)");
            ps.setInt(1, user_id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                date = rs.getString(1);
            }
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return date;
    }
}

