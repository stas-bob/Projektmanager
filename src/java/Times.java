/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


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

/**
 *
 * @author tA88
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
        response.setContentType("application/xml");
        PrintWriter out = response.getWriter();
        Connection c = null;

        String user = request.getSession().getAttribute("user").toString();
        int user_id = Integer.parseInt(request.getSession().getAttribute("user_id").toString());
        String status = "";
        try {
            c = DBConnector.getConnection();

            if (request.getParameter("modul") != null) {
                int modul_id = -1;
                if (request.getParameter("modul").toString().equals("")) {
                    status = "Sie müssen ein Modul angeben";
                } else {
                    modul_id = getModulId(c, request.getParameter("modul").toString());
                }

                Date date = getDate(request.getParameter("date").toString());
                if (date == null && status.equals("")) {
                    status = "Fehler beim Datum. Bitte verwenden Sie folgende Schreibweise: dd.mm.yyyy";
                }
                Time start = getTime(request.getParameter("start").toString());
                if (start == null && status.equals("")) {
                    status = "Fehler bei der Startzeit. Bitte verwenden Sie folgende Schreibweise: hh:mm";
                }
                Time end = getTime(request.getParameter("end").toString());
                if (end == null && status.equals("")) {
                    status = "Fehler bei der Endzeit. Bitte verwenden Sie folgende Schreibweise: hh:mm";
                }
                if (end != null && start != null && status.equals("")) {
                    if (end.getTime() < start.getTime()) {
                        status = "Endzeit muss später sein als die Startzeit";
                    }
                }
                String description = request.getParameter("description").toString();
                if (status.equals("")) {
                    insertTime(c, user_id, modul_id, date, start, end, description);
                    status = "Speichern erfolgreich";
                }
            }
            try {
                
                ArrayList<String> moduls = getModules(c, user);
                StringBuilder htmlOutput = new StringBuilder(300);
                htmlOutput.append("<table>")
                        .append("<colgroup width=\"200\" />")
                        .append("<tr align=\"left\">")
                        .append("<th>Modul:</th>")
                        .append("<th><select id=\"modul\"size=1>")
                        .append("<option></option>");
                for (String modul : moduls) {
                    htmlOutput.append("<option>").append(modul).append("</option>");
                }
                htmlOutput.append("</select></th>")
                        .append("</tr>")
                        .append("<tr align=\"left\">")
                        .append("<th>Datum:</th>")
                        .append("<th><input type=\"text\" id=\"date\" name=\"date\" size=\"10\" maxlength=\"10\" />(dd.mm.yyyy)</th>")
                        .append("</tr>")
                        .append("<tr align=\"left\">")
                        .append("<th>Start:</th>")
                        .append("<th><input type=\"text\" id=\"start\" name=\"start\" size=\"5\" maxlength=\"5\" />(hh:mm)</th>")
                        .append("</tr>")
                        .append("<tr align=\"left\">")
                        .append("<th>Ende:</th>")
                        .append("<th><input type=\"text\" id=\"end\" name=\"end\" size=\"5\" maxlength=\"5\" />(hh:mm)</th>")
                        .append("</tr>")
                        //.append("<tr align=\"left\">")
                        //.append("<th>Dauer:</th>")
                        //.append("<th><input type=\"text\" id=\"duration\" name=\"duration\" id=\"duration\" size=\"5\" maxlength=\"5\" readonly /></th>")
                        //.append("</tr>")
                        .append("<tr align=\"left\">")
                        .append("<th>Beschreibung:</th>")
                        .append("<th><textarea id=\"description\" name=\"description\" cols=\"50\" rows=\"5\" maxlength=\"250\" ></textarea></th>")
                        .append("</tr>")
                        .append("<tr align=\"left\">")
                        .append("<th><input type=\"button\" value=\"Speichern\" onclick=\"saveTimes()\"")
                        .append("</table>")
                        .append("<br><br>")
                        .append("<br>")
                        .append("<table border=1>")
                        .append("<colgroup>")
                        .append("<col width=\"70\">")
                        .append("<col width=\"70\">")
                        .append("<col width=\"70\">")
                        .append("<col width=\"70\">")
                        .append("<col width=\"150\">")
                        .append("<col width=\"400\">")
                        .append("</colgroup>")
                        .append("<tr>")
                        .append("<th>Datum</th>")
                        .append("<th>Start</th>")
                        .append("<th>Ende</th>")
                        .append("<th>Dauer</th>")
                        .append("<th>Modul</th>")
                        .append("<th>Beschreibung</th>")
                        .append("</tr>");
                htmlOutput.append(getTimes(c, user_id));
                htmlOutput.append("</table>");
                String xmlResponse = "<root><htmlSeite><![CDATA[" + htmlOutput.toString() + "]]></htmlSeite><status>" + status +  "</status></root>";
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

    private static int getModulId(Connection c, String modulname) {
        int result = -1;
        try {
            PreparedStatement ps = c.prepareStatement("SELECT id FROM module WHERE name = ?");
            ps.setString(1, modulname);
            ResultSet rs = ps.executeQuery();
            rs.next();
            result = rs.getInt(1);
            ps.close();
            return result;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    private static String getModulName(Connection c, int modul_id) {
        String result = "";
        try {
            PreparedStatement ps = c.prepareStatement("SELECT name FROM module WHERE id = ?");
            ps.setInt(1, modul_id);
            ResultSet rs = ps.executeQuery();
            rs.next();
            result = rs.getString(1);
            ps.close();
            return result;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    private static Date getDate(String temp) {
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
            if (month > 12 || month < 1) {
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
            return null;
        }
        return date;
    }

    private static Time getTime(String temp) {
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

    private static void insertTime(Connection c, int user_id, int modul_id, Date date, Time start, Time end, String description) {
        try {
            PreparedStatement ps = c.prepareStatement("INSERT INTO time (user_id, modul_id, date, start, end, description) VALUES (?,?,?,?,?,?)");
            ps.setInt(1, user_id);
            ps.setInt(2, modul_id);
            ps.setDate(3, date);
            ps.setTime(4, start);
            ps.setTime(5, end);
            ps.setString(6, description);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

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
            
            PreparedStatement ps = c.prepareStatement("SELECT date, start, end, modul_id, description FROM time WHERE user_id = ? ORDER BY date DESC");
            ps.setInt(1, user_id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                start = rs.getTime("start");
                end = rs.getTime("end");
                hour = end.getHours() - start.getHours();
                minute = end.getMinutes() - start.getMinutes();
                totalHour = totalHour + hour;
                totalMinute = totalMinute + minute;
                duration = new Time(hour, minute, 0);
                sb.append("<tr>")
                        .append("<th>").append(rs.getDate("date")).append("</th>")
                        .append("<th>").append(start).append("</th>")
                        .append("<th>").append(end).append("</th>")
                        .append("<th>").append(duration).append("</th>")
                        .append("<th>").append(getModulName(c, rs.getInt(4))).append("</th>")
                        .append("<th align=\"left\">").append(rs.getString("description")).append("</th></tr>");
            }
            sb.append("<tr><th>&#160;</th><th>&#160;</th><th>&#160;</th><th>&#160;</th><th>&#160;</th><th>&#160;</th></tr>")
                    .append("<tr><th>Gesamt:</th>")
                    .append("<th>&#160;</th><th>&#160;</th>")
                    .append("<th>").append(new Time(totalHour, totalMinute, 0)).append("</th><th>&#160;</th><th>&#160;</th></tr>");
            System.out.println(new Time(totalHour, totalMinute, 0));
            return sb.toString();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return "";
    }
}
