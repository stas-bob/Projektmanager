package main;

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

/**
 * Servlet, das die Benutzeroberfläsche bei erfolgreichem Login anlegt
 *
 * @author Thomas Altmeyer, Stanislaw Tartakowski
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
        request.setCharacterEncoding("UTF-8");
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
                if (firstLogin == 0) { //Prüfung ob der benutzer sich zum ersten Mal einloggt
                    out.write(changePasswordView(0)); //Wenn ja dann muss er das Passwort aendern
                } else {
                    out.write(mainView(projectName, false)); //Wenn nicht wird direkt zum Overview weitergeleitet
                }
            } else {
                out.write("<a href='Login.html'>Falscher Benutzername oder falsches Passwort eingegeben!</a>");
            }
            connection.close();
        } finally {
            out.close();
        }
    }

    /*
     *
     *
     * @param str1
     * @param str2
     * @return
     */
    public boolean equals(String str1, String str2) {
        return str1.replace("0", "").equals(str2.replace("0", ""));
    }

    /*
     * Überprüt die Login Daten
     *
     * @param c DB Connection
     * @param user Bentuzername(E-Mail) des Benutzer
     * @param password Password das vom User eingegeben wurde
     * @return  true -> wenn Login erfolgreich
     *          false-> wenn Login fehlgeschlagen
     */
    private boolean checkLogin(Connection c, String user, String password) {
        try {
            PreparedStatement ps = c.prepareStatement("SELECT password FROM user WHERE LOWER(email)=?");
            user = user.toLowerCase();
            ps.setString(1, user);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if (equals(rs.getString(1), password)) {
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

    /*
     * Gibt den Projektnamen zurück für das Projekt des Benutzers
     *
     * @param c DB Connection
     * @param user Bentuzername(E-Mail) des Benutzer
     * @return Projektname des Projektes
     */
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

    /*
     * Servlet, das die Benutzoberfläsche des mit den verschieden Tabs erzeugt
     *
     * @param projectName Projektname des Projektes
     * @param passwordChange true -> wird kurz angezeigt das Aenderung des Passsowrtes erfolgreich
     *                       false-> wird direkt zum Overview weitergeleitet
     * @return HTML Text für das Servlet
     */
    public static String mainView(String projectName, boolean passwordChange) {
        StringBuilder sb = new StringBuilder(500);
        sb.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
        sb.append("<html>");
        sb.append("<head>");
        sb.append("<title>Projektmanager</title>");
        sb.append("<meta http-equiv=\"Content - Type\" content=\"text / html;charset = iso - 8859 - 1\"/>");
        sb.append("<title>Untitled Document</title>");
        sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"start.css\"></link>");
        sb.append("<script src=\"jsXMLHttpRequestHandle.js\" type=\"text/javascript\"></script>");
        sb.append("<script language=\"javascript\" src=\"md5.js\"></script>");
        sb.append("<script language=\"javascript\">");
        sb.append("function transformPassword() {");
        sb.append("str = document.getElementById(\"newPasswordClear\").value;");
        sb.append("document.getElementById(\"newPassword\").value = MD5(str);");
        sb.append("str = document.getElementById(\"oldPasswordClear\").value;");
        sb.append("document.getElementById(\"oldPassword\").value = MD5(str);");
        sb.append("str = document.getElementById(\"validatePasswordClear\").value;");
        sb.append("document.getElementById(\"validatePassword\").value = MD5(str);");
        sb.append("}");
        sb.append("</script>");
        sb.append("</head>");
        sb.append("<body onLoad=\"showOverview()\">");
        sb.append("<div id=\"frame\">");
        sb.append("<div id=\"selected_tab\">");
        sb.append("</div>");
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
        sb.append("<div id=\"statusBox\" style=\"text-align: center; border: 1px solid; margin-left: 0px; width: 825px; position: relative; height: 25px;\"></div>");
        sb.append("</div>");
        sb.append("</body>");
        sb.append("</html>");
        return sb.toString();
    }

    /*
     * Servlet, in dem der Benutzer sein Passwort aendern muss bei erster Anmeldung
     *
     * @param falsePassword Meldung die ausgegeben wird, entweder Fehler oder Willkommensmeldung
     * @return HTML Text für das Servlet
     */
    public static String changePasswordView(int falsePassword) {
        StringBuilder sb = new StringBuilder(500);
        sb.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
        sb.append("<html>");
        sb.append("<head>");
        sb.append("<title>First Login</title>");
        sb.append("<meta http-equiv=\"Content - Type\" content=\"text / html;charset = iso - 8859 - 1\"></meta>");
        sb.append("<script language=\"javascript\" src=\"md5.js\"></script>");
        sb.append("<script language=\"javascript\">");
        sb.append("function transformPassword() {");
        sb.append("str = document.chpw.newPasswordClear.value;");
        sb.append("document.chpw.newPassword.value = MD5(str);");
        sb.append("str = document.chpw.oldPasswordClear.value;");
        sb.append("document.chpw.oldPassword.value = MD5(str);");
        sb.append("str = document.chpw.validatePasswordClear.value;");
        sb.append("document.chpw.validatePassword.value = MD5(str);");
        sb.append("}");
        sb.append("</script>");
        sb.append("<title>Untitled Document</title>");
        sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"start.css\"></link>");
        sb.append("</head>");
        sb.append("<body>");
        if (falsePassword == 0) {
            sb.append("Herzlich Wilkommen,");
            sb.append("<br/>");
            sb.append("da das Ihr erster Login ist, &auml;ndern Sie bitte ihr Passwort.");
        } else if (falsePassword == 1) {
            sb.append("Die neu eingebenen Passwoerter stimmen nicht ueberein!");
        } else if (falsePassword == 2) {
            sb.append("Das eingegebene bisherige Passwort ist falsch!");
        } else {
            sb.append("Fehler bei der &Auml;nderung ihres Passwortes.");
        }
        sb.append("<br/>");
        sb.append("<form action=\"/Projektmanager/FirstLogin\" method=\"post\" name=\"chpw\">");
        sb.append(changePasswordArea());
        sb.append("<input type=\"submit\" value=\"Speichern\" onclick=\"transformPassword()\"/>");
        sb.append("</form>");
        sb.append("</body>");
        sb.append("</html>");
        return sb.toString();
    }

    /*
     * Erezugt HTML Text für die Aenderung des Passwortes
     *
     * @return HTML Text für das Servlet
     */
    public static String changePasswordArea() {
        StringBuilder sb = new StringBuilder(200);
        sb.append("<table>");
        sb.append("<tr>");
        sb.append("<td align=\"left\">");
        sb.append("Altes Passwort:");
        sb.append("</td>");
        sb.append("<td>");
        sb.append("<input id=\"oldPasswordClear\" name=\"oldPasswordClear\" type=\"password\" />");
        sb.append("<input id=\"oldPassword\" name=\"oldPassword\" type=\"hidden\"/>");
        sb.append("</td>");
        sb.append("</tr>");
        sb.append("<tr>");
        sb.append("<td align=\"left\">");
        sb.append("Neues Passwort:");
        sb.append("</td>");
        sb.append("<td>");
        sb.append("<input id=\"newPasswordClear\" name=\"newPasswordClear\" type=\"password\"/>");
        sb.append("<input id=\"newPassword\" name=\"newPassword\" type=\"hidden\"/>");
        sb.append("<br></br>");
        sb.append("</td>");
        sb.append("</tr>");
        sb.append("<tr>");
        sb.append("<td align=\"left\">");
        sb.append("Neues Passwort best&auml;tigen:");
        sb.append("</td>");
        sb.append("<td>");
        sb.append("<input id=\"validatePasswordClear\" name=\"validatePasswordClear\" type=\"password\"/>");
        sb.append("<input id=\"validatePassword\" name=\"validatePassword\" type=\"hidden\"/>");
        sb.append("<br/>");
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

    /*
     * Gibt den Status den Benutzers entweder PL(Projektleiter) oder MEM(Mitglied)
     *
     * @param email E-Mail des Benutzers
     * @param c DB Connection
     * @return String, mit dem Status
     */
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

    /*
     * Gibt die Module für die der Benutzer angemeldet ist
     *
     * @param email E-Mail des Benutzers
     * @param c DB Connection
     * @return Liste, mit den Modulen
     */
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

    /*
     * Gibt den Namen des Benutzers
     *
     * @param email E-Mail des Benutzers
     * @param c DB Connection
     * @return String, mit dem Namen
     */
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

    /*
     * Gibt den Vornamen des Benutzers
     *
     * @param email E-Mail des Benutzers
     * @param c DB Connection
     * @return String, mit dem Vornamen
     */
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

    /*
     * Gibt die Benutzer-ID des Benutzers
     *
     * @param c DB Connection
     * @param email E-Mail des Benutzers
     * @return Integer, mit der User-ID
     */
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

    /*
     * Gibt das Startdatum des Projektes
     *
     * @param c DB Connection
     * @param user_id Benutzer-ID des Benutzer
     * @return String, mit Datum
     */
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

    /*
     * Gibt das Enddatum des Projektes
     *
     * @param c DB Connection
     * @param user_id Benutzer-ID des Benutzer
     * @return String, mit Datum
     */
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

