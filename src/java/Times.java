import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;
import db.DBConnector;
import exceptions.MySQLException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet für den Zeiten Tab. Benutzer kann neue Arbeitszeit eintragen, vorhandene Zeiten ansehen und loeschen
 *
 * @author Thomas Altmeyer, Stanislaw Tartakowski
 */
public class Times extends HttpServlet {
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();
        if (request.getCookies() == null) {
                out.write("<root><htmlSeite><![CDATA[Aktivieren Sie bitte <b>Cookies</b> in ihrem Webbrowser.]]></htmlSeite><status></status></root>");
                return;
            }
        HttpSession seas = request.getSession();
        Connection c = null;

        String user = request.getSession().getAttribute("user").toString();
        int user_id = Integer.parseInt(request.getSession().getAttribute("user_id").toString());
        String status = "";
        String input= "<modul></modul><date></date><start></start><end></end><description></description>";
        try {
            c = DBConnector.getConnection();

            if (request.getParameter("modul") != null) {
                String modulname = "";
                if (request.getParameter("modul").toString().equals("")) {
                    status = "Sie m&uuml;ssen ein Modul angeben";
                    input = getInput(request);
                } else {
                    modulname = request.getParameter("modul").toString();
                }

                Date date = getDate(request.getParameter("date").toString(), seas);
                if (date == null && status.equals("")) {
                    status = "Fehler beim Datum. Das Datum muss innerhalb des Projektes liegen und bitte verwenden Sie folgende Schreibweise: dd.mm.yyyy";
                }
                Time start = getTime(request.getParameter("start").toString());
                if (start == null && status.equals("")) {
                    status = "Fehler bei der Startzeit. Bitte verwenden Sie folgende Schreibweise: hh:mm";
                }
                Time end = getTime(request.getParameter("end").toString());
                if (end == null && status.equals("")) {
                    status = "Fehler bei der Endzeit. Bitte verwenden Sie folgende Schreibweise: hh:mm";
                }
                String description = request.getParameter("description").toString();
                if (status.equals("")) {
                    if (insertTime(c, user_id, modulname, date, start, end, description)) {
                        status = "Speichern erfolgreich";
                    } else {
                        status = "Zu diesem Zeitpunkt haben Sie bereits eine Zeit eingetragen!";
                        input = getInput(request);
                    }
                } else {
                    input = getInput(request);
                }
            } else if (request.getParameter("user_id") != null) {
                deleteTime(c, request.getParameter("user_id").toString(), request.getParameter("date").toString(), request.getParameter("start").toString());
                status = "Zeit erfolgreich geloescht";
            }
            try {
                
                ArrayList<String> moduls = getModules(c, user);
                StringBuilder htmlOutput = new StringBuilder(300);
                htmlOutput.append("<table>")
                        .append("<colgroup width=\"200\" />")
                        .append("<tr align=\"left\">")
                        .append("<td>Modul:</td>")
                        .append("<td><select id=\"modul\"size=1>");
                for (String modul : moduls) {
                    htmlOutput.append("<option>").append(modul).append("</option>");
                }
                htmlOutput.append("</select></td>")
                        .append("</tr>")
                        .append("<tr align=\"left\">")
                        .append("<td>Datum:</td>")
                        .append("<td><input type=\"text\" id=\"date\" name=\"date\" size=\"10\" maxlength=\"10\" />(dd.mm.yyyy)<input type=\"button\" value=\"Heute\" onclick=\"today('").append("date").append("')\"></td>")
                        .append("</tr>")
                        .append("<tr align=\"left\">")
                        .append("<td>Start:</td>")
                        .append("<td><input type=\"text\" id=\"start\" name=\"start\" size=\"5\" maxlength=\"5\" />(hh:mm)<input type=\"button\" value=\"Jetzt\" onclick=\"now('").append("start").append("')\"></td>")
                        .append("</tr>")
                        .append("<tr align=\"left\">")
                        .append("<td>Ende:</td>")
                        .append("<td><input type=\"text\" id=\"end\" name=\"end\" size=\"5\" maxlength=\"5\" />(hh:mm)<input type=\"button\" value=\"Jetzt\" onclick=\"now('").append("end").append("')\"></td>")
                        .append("</tr>")
                        .append("<tr align=\"left\">")
                        .append("<td>Beschreibung:</td>")
                        .append("<td><textarea id=\"description\" name=\"description\" cols=\"50\" rows=\"5\" maxlength=\"120\" onkeypress=\"ismaxlength(this)\"></textarea></td>")
                        .append("</tr>")
                        .append("<tr align=\"left\">")
                        .append("<td><input type=\"button\" value=\"Speichern\" onclick=\"saveTimes()\">")
                        .append("</table>")
                        .append("<br><br>")
                        .append("<br>");
                htmlOutput.append(getTimes(c, user_id));
                String xmlResponse = "<root><htmlSeite><![CDATA[" + htmlOutput.toString() + "]]></htmlSeite>"
                        + "<status><![CDATA[" + status +  "]]></status>";
                if (!input.equals("")) {
                    xmlResponse = xmlResponse + input;
                }
                xmlResponse = xmlResponse + "</root>";
                out.write(xmlResponse);
            } finally {
                c.close();
                out.close();
            }
        } catch (MySQLException ex) {
            ex.printStackTrace();
        } catch (SQLException ex) {
            Logger.getLogger(Times.class.getName()).log(Level.SEVERE, null, ex);
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

    /*
     * Bei fehlerhafter Eingabe für eine neue Zeit wird hier die Eingabe gespeichert, um bei einem Fehler dem Benutzer zu ersparen
     *  das er alles wieder neu eingeben muss.
     *
     * @param rquest Request des Servlet
     * @return String wird in die xmlResponse angefügt
     */
    private static String getInput(HttpServletRequest request) {
        return "<modul><![CDATA[" + request.getParameter("modul").toString() + "]]></modul>"
                        + "<date><![CDATA[" + request.getParameter("date").toString() + "]]></date>"
                        + "<start><![CDATA[" + request.getParameter("start").toString() + "]]></start>"
                        + "<end><![CDATA[" + request.getParameter("end").toString() + "]]></end>"
                        + "<description><![CDATA[" + request.getParameter("description").toString() + "]]></description>";
    }

    /*
     * Erstellt eine Liste mit alles Modulen, für die ein Benutzer Zeiten anlegen kann.
     *
     * @param c DB Connection
     * @param user Username des angemeldeten Benutzer
     * @return Liste mit Modulen
     */
    private static ArrayList<String> getModules(Connection c, String user) {
        ArrayList<String> result = new ArrayList<String>();
        try {
            PreparedStatement ps = c.prepareStatement("SELECT name FROM module WHERE id in (SELECT modulid FROM `rel_module_user` WHERE email = ?)");
            ps.setString(1, user);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(rs.getString("name"));
            }
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    /*
     * Foramtiert das Datum der Eingabe in das DB Format für Date und überprüft die Sinnhaftigkeit des Datums
     *
     * @param temp Datum das vom Benutzer eingegeben wurde
     * @param Session vom Benutzer
     * @return java.sql.Date Datum das eingegeben wurde
     */
    private static Date getDate(String temp, HttpSession seas) {
        int day = -1;
        int month = -1;
        int year = -1;
        Date date = null;

        try {
            day = Integer.parseInt(temp.substring(0, temp.indexOf(".")));
            if (day > 31 || day < 1) {
                return null;
            }
            temp = temp.substring(temp.indexOf(".") + 1);
            month = Integer.parseInt(temp.substring(0, temp.indexOf("."))) - 1;
            if (month > 11 || month < 0) {
                return null;
            }
            temp = temp.substring(temp.indexOf(".") + 1);
            year = Integer.parseInt(temp);
            if (year >= 2000) {
                year -= 1900;
            }
            if (year < 0) {
                return null;
            }

            date = new Date(year, month, day);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        String startProject = seas.getAttribute("startProject").toString();
        year = Integer.parseInt(startProject.substring(0, startProject.indexOf("-")));
        startProject = startProject.substring(startProject.indexOf("-") + 1);
        month = Integer.parseInt(startProject.substring(0, startProject.indexOf("-"))) - 1;
        startProject = startProject.substring(startProject.indexOf("-") + 1);
        day = Integer.parseInt(startProject);
        if (year >= 2000) {
            year -= 1900;
        }
        Date start = new Date(year, month, day);

        String endProject = seas.getAttribute("endProject").toString();
        year = Integer.parseInt(endProject.substring(0, endProject.indexOf("-")));
        endProject = endProject.substring(endProject.indexOf("-") + 1);
        month = Integer.parseInt(endProject.substring(0, endProject.indexOf("-"))) - 1;
        endProject = endProject.substring(endProject.indexOf("-") + 1);
        day = Integer.parseInt(endProject);
        if (year >= 2000) {
            year -= 1900;
        }
        Date end = new Date(year, month, day);

        java.util.Date today = new java.util.Date();
        if (today.compareTo(date) >= 0) {
            if (start.compareTo(date) <= 0  && end.compareTo(date) >= 0) {
                return date;
            }
        }
        return null;
    }

    /*
     * Formatiert die eingegeben zeit des Benutzers in das DB Format und überprüft sie gleichzeitig
     *
     * @param temp Zeit die vom Benutzer eingegeben wurde
     * @return java.sql.Time Zeit die eingegeben wurde
     */
    public static Time getTime(String temp) {
        int hour = -1;
        int minute = -1;
        Time time = null;
        try {
            hour = Integer.parseInt(temp.substring(0, temp.indexOf(":")));
            if (hour > 23 || hour < 0) {
                return null;
            }
            temp = temp.substring(temp.indexOf(":") + 1);
            minute = Integer.parseInt(temp);
            if (minute > 59 || minute < 0) {
                return null;
            }

            time = new Time(hour, minute, 0);
        } catch (Exception ex) {
            return null;
        }
        return time;
    }

    /*
     * Fügt eine neue Zeit für den Benutzer ein
     *
     * @param c DB Connection
     * @param user_id User-ID des Benutzers
     * @param modulname Name des Modules
     * @param date Datum
     * @param start Startzeit
     * @param end Endzeit
     * @param description Beschreibungstext
     * @return  true -> Wenn insert erfolgreich
     *          false -> Wenn insert nicht erfolgreich
     */
    private static boolean insertTime(Connection c, int user_id, String modulname, Date date, Time start, Time end, String description) {
        try {
            try {
                c.setAutoCommit(false);
                PreparedStatement ps = c.prepareStatement("INSERT INTO time (user_id, modulname, date, start, end, description) VALUES (?,?,?,?,?,?)");
                ps.setInt(1, user_id);
                ps.setString(2, modulname);
                ps.setDate(3, date);
                ps.setTime(4, start);
                ps.setTime(5, end);
                ps.setString(6, description);
                ps.executeUpdate();
                ps.close();
                c.commit();
                return true;
            } catch (MySQLIntegrityConstraintViolationException ex) {
                c.rollback();
                c.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /*
     * Gibt die bisherigen Zeiten des Benutzer zurück
     *
     * @param c DB Connection
     * @param user_id User-ID des Benutzers
     * @return String mit der Tabelle der bisherigen Zeiten
     */
    private static String getTimes(Connection c, int user_id) {
        try {
            StringBuilder sb = new StringBuilder(1000);
            Time start;
            Time end;
            Time duration;
            int hour;
            int minute;
            int totalHour = 0;
            int totalMinute = 0;

            sb.append("<table width=\"790\" border=1 style=\"border-collapse: collapse;\" cellpadding=\"3\">")
                    .append("<colgroup>")
                    .append("<col width=\"100\">")
                    .append("<col width=\"70\">")
                    .append("<col width=\"70\">")
                    .append("<col width=\"70\">")
                    .append("<col width=\"120\">")
                    .append("<col width=\"350\">")
                    .append("<col width=\"30\">")
                    .append("</colgroup>")
                    .append("<tr>")
                    .append("<td>Datum</td>")
                    .append("<td>Start</td>")
                    .append("<td>Ende</td>")
                    .append("<td>Dauer</td>")
                    .append("<td>Modul</td>")
                    .append("<td>Beschreibung</td>")
                    .append("</tr>");
            PreparedStatement ps = c.prepareStatement("SELECT date, start, end, modulname, description FROM time WHERE user_id = ? ORDER BY date DESC");
            ps.setInt(1, user_id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                start = rs.getTime("start");
                end = rs.getTime("end");
                hour = Math.abs(end.getHours() - start.getHours());
                minute = end.getMinutes() - start.getMinutes();
                totalHour = totalHour + hour;
                totalMinute = totalMinute + minute;
                duration = new Time(hour, minute, 0);
                sb.append("<tr>")
                        .append("<td>").append(rs.getDate("date")).append("</td>")
                        .append("<td>").append(start).append("</td>")
                        .append("<td>").append(end).append("</td>")
                        .append("<td>").append(duration).append("</td>")
                        .append("<td>").append(Modules.format(rs.getString("modulname"), 10)).append("</td>")
                        .append("<td align=\"left\">").append(Modules.format(rs.getString("description"), 25)).append("</td>")
                        .append("<td><input type=\"button\" value=\"X\" onclick=\"deleteTime('").append(user_id).append("','").append(rs.getDate("date")).append("','").append(start).append("')\"></td></tr>");
            }
            sb.append("<tr><td>&#160;</td><td>&#160;</td><td>&#160;</td><td>&#160;</td><td>&#160;</td><td>&#160;</td></tr>")
                    .append("<tr><td>Gesamt:</td>")
                    .append("<td>&#160;</td><td>&#160;</td>")
                    .append("<td>").append(new Time(totalHour, totalMinute, 0)).append("</td><td>&#160;</td><td>&#160;</td></tr>")
                    .append("</table>");
            if (totalHour != 0 || totalMinute != 0) {
                sb.append("<br>")
                        .append("<a href=\"/Projektmanager/DownloadServlet?file=").append(user_id).append(".csv\">Download Tabelle als .csv</a>");
            }
            return sb.toString();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return "";
    }

    /*
     * Löscht eine vorhande Zeit aus der Datenbank
     *
     * @param c DB Connection
     * @param user_id User-ID des Benutzers
     * @param date Datum
     * @param start Startzeit
     */
    private static void deleteTime(Connection c, String user_id, String date, String start) {
        try {
            try {
                c.setAutoCommit(false);
                PreparedStatement ps = c.prepareStatement("DELETE FROM time WHERE user_id = ? AND date = ? AND start = ?");
                ps.setString(1, user_id);
                ps.setString(2, date);
                ps.setString(3, start);
                ps.executeUpdate();
                ps.close();
                c.commit();
            } catch (MySQLIntegrityConstraintViolationException ex) {
                c.rollback();
                c.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
