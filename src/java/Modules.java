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
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author bline
 */
public class Modules extends HttpServlet {
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        try {
            request.setCharacterEncoding("UTF-8");
            response.setContentType("application/xml;charset=UTF-8");
            PrintWriter out = response.getWriter();
            if (request.getCookies() == null) {
                out.write("<root><htmlSeite><![CDATA[Aktivieren Sie bitte <b>Cookies</b> in ihrem Webbrowser.]]></htmlSeite><modulesCount>0</modulesCount><error>0</error><errorMsg> </errorMsg></root>");
                return;
            }
            Connection c = DBConnector.getConnection();
            int error = 0;
            String errorMsg = " ";
            if (request.getParameter("description") != null) {
                ArrayList<String> members = getMembers(request.getParameter("membersToAdd"), request.getSession().getAttribute("projectname").toString(), c);
                if (validateModuleName(request.getParameter("name").toString(), request.getSession().getAttribute("projectname").toString(), c)) {
                    error = saveMouleToDB(members, request.getParameter("name"), request.getParameter("description"), request.getParameter("startDate"), request.getParameter("endDate"), request.getParameter("prio"), request.getSession().getAttribute("projectname").toString(), request.getSession().getAttribute("startProject").toString(), request.getSession().getAttribute("endProject").toString(), c);
                    errorMsg = "Datum ausserhalb projektzeit oder datum nicht korrekt";
                    if (members.contains(request.getSession().getAttribute("user").toString())) {
                        int modulid = getModulId(request.getSession().getAttribute("projectname").toString(), request.getParameter("name"), c);
                        ((ArrayList<Integer>) request.getSession().getAttribute("modules")).add(modulid);
                    }
                } else {
                    String xmlResponse = "<root><htmlSeite><![CDATA[...]]></htmlSeite><modulesCount>0</modulesCount><error>1</error><errorMsg>Der Name " + request.getParameter("name").toString() + " ist in diesem Projekt bereits vorhanden</errorMsg></root>";
                    out.write(xmlResponse);
                    c.close();
                    return;
                }
            } else {
                if (request.getParameter("addModule") != null) {
                    String htmlOutput = "<table border=\"0\">" + "<tbody><tr><td>Name:</td><td><input id=\"name\" size=\"40\" maxlength=\"40\" type=\"text\"></td></tr>" + 
                            "<tr><td valign=\"top\">Beschreibung:</td><td><textarea id=\"description\" cols=\"30\" rows=\"6\" maxlength=\"400\" onkeypress=\"ismaxlength(this)\"></textarea></td></tr>" +
                            "<tr><td>Priorit&auml;t:</td><td><select size=\"1\" id=\"prio\"><option>1</option><option>2</option><option>3</option></select><div style=\"background-image:url(grafik/question_mark.png); background-repeat:no-repeat; width:25px; height:22px; position:absolute; margin-left: 40px; margin-top: -22px;\" onclick=\"showHint(this, '1 is die h&ouml;chste prio')\"></div></td></tr>" +
                            "<tr><td>Start:</td><td>Tag: <input id=\"startDay\" typ=\"text\" size=\"1\" maxlength=\"2\">Monat: <input id=\"startMonth\" typ=\"text\" size=\"1\" maxlength=\"2\">Jahr: <input id=\"startYear\" typ=\"text\" size=\"4\" maxlength=\"4\"></td></tr>" +
                            "<tr><td>Ende:</td><td>Tag: <input id=\"endDay\" typ=\"text\" size=\"1\" maxlength=\"2\">Monat: <input id=\"endMonth\" typ=\"text\" size=\"1\" maxlength=\"2\">Jahr: <input id=\"endYear\" typ=\"text\" size=\"4\" maxlength=\"4\"></td></tr>" +
                            "<tr><td>Mitglied zuweisen:</td><td><select id=\"selectMember\">" + getAllMembers(request.getSession().getAttribute("projectname").toString(), c) + "</select><input value=\"zuweisen\" type=\"button\" onclick=\"addMemberToModuleBox()\"/><input value=\"entfernen\" type=\"button\" onclick=\"removeMemberFromModuleBox()\"/><input type=\"button\" value=\"anlegen\" onclick=\"saveModule()\"/></td></tr>" +
                            "</tbody>" +
                            "</table>" +
                            "<div id=\"membersInModuleBox\" style=\"position:absolute; border: 1px dashed; margin-left: 50px; width: 300px; height: 120px; display:none\"></div>";
                    out.write(htmlOutput);
                    c.close();
                    return;
                } else {
                    if (request.getParameter("addToModule") != null) {
                        addMeToModule(request.getSession().getAttribute("user").toString(), request.getParameter("addToModule").toString(), c);
                        ((ArrayList<Integer>) request.getSession().getAttribute("modules")).add(Integer.parseInt(request.getParameter("addToModule")));
                    } else {
                        if (request.getParameter("moduleDescriptionId") != null) {
                            out.write(getModuleDescription(request.getParameter("moduleDescriptionId"), request.getSession().getAttribute("status").toString(), request.getSession().getAttribute("user").toString(), c));
                            c.close();
                            return;
                        } else {
                            if (request.getParameter("changeStatus") != null) {
                                error = setModuleStatusOnDB(request.getParameter("changeStatus"), request.getParameter("id"), c);
                                if (error == 1) throw new NullPointerException("db error");
                                errorMsg = "<![CDATA[&Auml;nderung gespeichert.]]>";
                                error = -1;
                            } else {
                                if (request.getParameter("deleteModule") != null) {
                                    deleteModule(request.getParameter("deleteModule"), c);
                                } else {
                                    if (request.getParameter("removeFromModule") != null) {
                                        removeMeFromModule(request.getSession().getAttribute("user").toString(), Integer.parseInt(request.getParameter("removeFromModule")), c);
                                        ((ArrayList<Integer>) request.getSession().getAttribute("modules")).remove((Integer) Integer.parseInt(request.getParameter("removeFromModule")));
                                    } else {
                                        if (request.getParameter("saveMessage") != null) {
                                            saveMessageToDB(request.getParameter("saveMessage"), request.getParameter("id"), request.getSession().getAttribute("name").toString(), request.getSession().getAttribute("user").toString(), c);
                                            out.write(getModuleDescription(request.getParameter("id"), request.getSession().getAttribute("status").toString(), request.getSession().getAttribute("user").toString(), c));
                                            c.close();
                                            return;
                                        } else {
                                            if (request.getParameter("deleteMessageId") != null) {
                                                deleteMessage(request.getParameter("deleteMessageId"), request.getParameter("modulid"), c);
                                                out.write(getModuleDescription(request.getParameter("modulid"), request.getSession().getAttribute("status").toString(), request.getSession().getAttribute("user").toString(), c));
                                                c.close();
                                                return;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            ArrayList<String> names;
            ArrayList<String> status;
            names = new ArrayList<String>();
            status = new ArrayList<String>();
            ArrayList<Integer> ids = new ArrayList<Integer>();
            getModules(request.getSession().getAttribute("projectname").toString(), names, status, ids, c);
            String htmlOutput = "<table><tr><td valign=\"top\"><table border=\"0\" style=\"border-collapse:collapse;\" >" + "<tr><td align=\"center\">Name</td><td align=\"center\">Status</td></tr>";
            for (int i = 0; i < names.size(); i++) {
                htmlOutput += "<tr onmouseover=\"fillColor(this, '#9f9fFF')\" onmouseout=\"fillColor(this, 'white')\" onmousedown=\"fillColor(this, '#6c6ccc')\">" +
                                "<td style=\"border: 1px solid; padding-left: 10px; padding-right: 10px; cursor:pointer;\" onclick=\"showModuleDescription(" + ids.get(i) + ")\"><font size=\"1\">" + format(names.get(i), 10) + "</font></td>" +
                                "<td style=\"border: 1px solid; padding-left: 10px; padding-right: 10px;\">" + setStatus(status.get(i), ids.get(i)) + "</td>";
                if (!((ArrayList<Integer>) request.getSession().getAttribute("modules")).contains(ids.get(i))) {
                    if (status.get(i).equals("open")) {
                        htmlOutput += "<td><input type=\"button\" value=\"Teilnehmen\" onclick=\"addMeToModule(" + ids.get(i) + ")\"/></td>";
                    }
                } else {
                    htmlOutput += "<td><input type=\"button\" value=\"Aufh&ouml;ren\" onclick=\"removeMeFromModule(" + ids.get(i) + ")\"/></td>";
                }
                htmlOutput += "</tr>";
            }
            if (request.getSession().getAttribute("status").equals("PL")) {
                htmlOutput += "<tr><td colspan=\"2\"><input type=\"button\" value=\"Neue Aufgabe anlegen\" onclick=\"addModule()\"/></td></tr>";
            }
            htmlOutput += "</table></td><td>";
            htmlOutput += "<div id=\"addModule\"></div></td></tr></table>";
            String xmlResponse = "<root><htmlSeite><![CDATA[" + htmlOutput + "]]></htmlSeite><modulesCount>" + names.size() + "</modulesCount><error>" + error + "</error><errorMsg>" + errorMsg + "</errorMsg></root>";
            out.write(xmlResponse);
            out.close();
            c.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            Logger.getLogger(Modules.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MySQLException ex) {
            ex.printStackTrace();
            Logger.getLogger(Modules.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException nex) {
            nex.printStackTrace();
            request.getSession().invalidate();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Logout");
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

    public static void getModules(String projectName, ArrayList<String> names, ArrayList<String> status, ArrayList<Integer> ids, Connection c) {
        try {
            PreparedStatement ps = c.prepareStatement("SELECT name, status, id FROM module WHERE projectname = ?");
            ps.setString(1, projectName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                names.add(rs.getString(1));
                status.add(rs.getString(2));
                ids.add(rs.getInt(3));
            }
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private String getModuleDescription(String id, String status, String myEmail, Connection c) {
        try {
            PreparedStatement ps = c.prepareStatement("SELECT name, prio, description, start, end FROM module WHERE id = ?");
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String description = rs.getString(3);
                String name = rs.getString(1);
                System.out.println("ddd" + name);
                String prio = rs.getString(2);
                Date start = rs.getDate(4);
                Date end = rs.getDate(5);

                ps.close();
                String members = "";
                ps = c.prepareStatement("SELECT name FROM rel_module_user, user WHERE user.email = rel_module_user.email AND modulid = ?");
                ps.setString(1, id);
                rs = ps.executeQuery();
                rs.last();
                int memberCount = rs.getRow();
                rs.beforeFirst();
                while (rs.next()) {
                    members += "[" + rs.getString(1) + "]";
                }
                ps.close();

                ArrayList<String> messages = new ArrayList<String>();
                ArrayList<String> username = new ArrayList<String>();
                ArrayList<String> email = new ArrayList<String>();
                ArrayList<Integer> messageIds = new ArrayList<Integer>();
                ps = c.prepareStatement("SELECT message, username, email, messageid FROM rel_module_message WHERE modulid = ? ORDER BY messageid");
                ps.setString(1, id);
                rs = ps.executeQuery();
                while (rs.next()) {
                    messages.add(rs.getString(1));
                    username.add(rs.getString(2));
                    email.add(rs.getString(3));
                    messageIds.add(rs.getInt(4));
                }
                ps.close();


                String htmlOutput = "<table border=\"1\" style=\"border-collapse:collapse\">"
                        + "<tr onmouseover=\"fillColor(this, '#fbf52d')\" onmouseout=\"fillColor(this, 'white')\" onmousedown=\"fillColor(this, '#c8c20a')\"><td>Name: </td><td>" + format(name, 30) +"</td></tr>"
                        + "<tr onmouseover=\"fillColor(this, '#fbf52d')\" onmouseout=\"fillColor(this, 'white')\" onmousedown=\"fillColor(this, '#c8c20a')\"><td>Prio: </td><td>" + prio + "</td></tr>"
                        + "<tr onmouseover=\"fillColor(this, '#fbf52d')\" onmouseout=\"fillColor(this, 'white')\" onmousedown=\"fillColor(this, '#c8c20a')\"><td>Start: </td><td>" + start + "</td></tr>"
                        + "<tr onmouseover=\"fillColor(this, '#fbf52d')\" onmouseout=\"fillColor(this, 'white')\" onmousedown=\"fillColor(this, '#c8c20a')\"><td>Ende: </td><td>" + end + "</td></tr>"
                        + "<tr onmouseover=\"fillColor(this, '#fbf52d')\" onmouseout=\"fillColor(this, 'white')\" onmousedown=\"fillColor(this, '#c8c20a')\"><td>Mitgliederzahl: </td><td>" + memberCount + "</td></tr>"
                        + "<tr onmouseover=\"fillColor(this, '#fbf52d')\" onmouseout=\"fillColor(this, 'white')\" onmousedown=\"fillColor(this, '#c8c20a')\"><td>Mitglieder: </td><td>" + members + "</td></tr>"
                        + "<tr onmouseover=\"fillColor(this, '#fbf52d')\" onmouseout=\"fillColor(this, 'white')\" onmousedown=\"fillColor(this, '#c8c20a')\"><td>Beschreibung: </td><td>" + format(description, 30) + "</td></tr>";
                        if (status.equals("PL")) {
                            htmlOutput += "<tr onmouseover=\"fillColor(this, '#fbf52d')\" onmouseout=\"fillColor(this, 'white')\" onmousedown=\"fillColor(this, '#6c6ccc')\"><td colspan=\"2\" align=\"center\"><input type=\"button\" value=\"l&ouml;schen\" onclick=\"deleteModule(" + id + ")\"/></td></tr>";
                        }
                htmlOutput += "</table>"
                            + "<table border=\"0\" style=\"border-collapse: collapse; margin-top: 10px; width: 480px;\">";
                for (int i = 0; i < messages.size(); i++) {
                    htmlOutput += "<tr style=\"border: 1px solid;\"><td><b>" + username.get(i) + "</b> schrieb </td>";
                    if (email.get(i).equals(myEmail)) {
                        htmlOutput += "<td align=\"right\"><button onclick=\"deleteMessage('" + id + "','" + messageIds.get(i) + "')\">l&ouml;schen</button></td></tr>";
                    }
                    htmlOutput += "<tr style=\"border: 1px solid;\"><td colspan=\"2\"><div style=\"height: 100px; overflow: auto;\">" + format(messages.get(i), 35) + "</div></td></tr>"
                    + "<tr><td height=\"10\"></td></tr>";
                }
                htmlOutput += "<tr style=\"border: 1px solid;\"><td colspan=\"2\"><font style=\"color:blue\">Schreiben Sie einen Kommentar</font></td></tr>"
                            + "<tr style=\"border: 1px solid;\"><td colspan=\"2\"><textarea id=\"messageArea\" cols=\"56\" rows=\"5\" maxlength=\"210\" onkeypress=\"ismaxlength(this)\"></textarea></td></tr>"
                            + "<tr align=\"right\"><td colspan=\"2\"><button onclick=\"saveMessage(" + id + ")\">absenden</button></td></tr>"
                            + "</table>";
                return "<root><htmlSeite><![CDATA[" + htmlOutput + "]]></htmlSeite><modulesCount>" + messages.size() + "</modulesCount><error>0</error><errorMsg> </errorMsg></root>";
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private String getAllMembers(String projectName, Connection c) {
        try {
            String result = "";
            PreparedStatement ps = c.prepareStatement("SELECT name FROM user WHERE projectname = ?");
            ps.setString(1, projectName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result += "<option>" + rs.getString(1) + "</option>";
            }
            ps.close();
            return result;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private ArrayList<String> getMembers(String membersString, String projectName, Connection c) {
        ArrayList<String> members = new ArrayList<String>();
        try {

            PreparedStatement ps = c.prepareStatement("SELECT email FROM user WHERE name = ? AND projectname = ?");
            ps.setString(2, projectName);
            for (int i = 0; i < membersString.length(); i++) {
                if (membersString.charAt(i) == '[') {
                    String tmpString = "";
                    i++;
                    while (membersString.charAt(i) != ']') {
                        tmpString += membersString.charAt(i);
                        i++;
                    }
                    ps.setString(1, tmpString);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        members.add(rs.getString(1));
                    }
                }
            }
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return members;
    }

    private int saveMouleToDB(ArrayList<String> members, String name, String description, String start, String end, String prio, String projectName, String projectStart, String projectEnd, Connection c) {
        try {
            GregorianCalendar moduleStartDate = getDateFromString(start);
            GregorianCalendar moduleEndDate = getDateFromString(end);
            GregorianCalendar projectStartDate = getDateFromString(projectStart);
            GregorianCalendar projectEndDate = getDateFromString(projectEnd);

            if (moduleStartDate == null || moduleEndDate == null || projectStartDate == null || projectEndDate == null) {
                return 1;
            }
            if (moduleStartDate.before(projectStartDate) || moduleEndDate.after(projectEndDate)) {
                return 1;
            }
            PreparedStatement ps = c.prepareStatement("INSERT INTO module (name,start,end,prio,status,description,projectname) VALUES (?,?,?,?,'open',?,?)");
            System.out.println("name save " + name);
            ps.setString(1, name);
            ps.setString(2, start);
            ps.setString(3, end);
            ps.setString(4, prio);
            ps.setString(5, description);
            ps.setString(6, projectName);
            ps.executeUpdate();
            ps.close();

            ps = c.prepareStatement("INSERT INTO rel_module_user (modulid, email) VALUES ((SELECT id FROM module WHERE name = ? AND projectname = ?), ?)");
            ps.setString(1, name);
            ps.setString(2, projectName);
            for (String member: members) {
                ps.setString(3, member);
                ps.executeUpdate();
            }
            ps.close();
            return 0;
        } catch (SQLException ex) {
            return 1;
        }
    }

    private boolean validateModuleName(String name, String projectName, Connection c) {
        try {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM module WHERE name = ? AND projectname = ?");
            ps.setString(1, name);
            ps.setString(2, projectName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ps.close();
                return false;
            }
            ps.close();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private void addMeToModule(String email, String modulid, Connection c) {
        try {
            PreparedStatement ps = c.prepareStatement("INSERT INTO rel_module_user (modulid, email) VALUES (?,?)");
            ps.setString(1, modulid);
            ps.setString(2, email);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            Logger.getLogger(Modules.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String setStatus(String status, int id) {
        String open = "<option>open</option>";
        String closed = "<option>closed</option>";

        String select = "<select name=\"statusSelect\" size=\"1\" onchange=\"changeModuleStatus(this.options[this.selectedIndex].value,'" + id + "')\">";
        select += status.equals("open") ? open + closed : closed + open;
        return select + "</select>";
    }

    public int setModuleStatusOnDB(String status, String id, Connection c) {
        try {
            PreparedStatement ps = c.prepareStatement("UPDATE module SET status = ? WHERE id = ?");
            ps.setString(1, status);
            ps.setString(2, id);
            ps.executeUpdate();
            ps.close();
            return 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 1;
    }

    public static void deleteModule(String id, Connection c) {
        try {
            String sql = "";
            try {
                c.setAutoCommit(false);
                sql = "DELETE FROM module WHERE id = ?";
                PreparedStatement ps = c.prepareStatement(sql);
                ps.setString(1, id);
                ps.executeUpdate();
                ps.close();
                sql = "DELETE FROM rel_module_user WHERE modulid = ?";
                ps = c.prepareStatement(sql);
                ps.setString(1, id);
                ps.executeUpdate();
                ps.close();
                sql = "DELETE FROM rel_module_message WHERE modulid = ?";
                ps = c.prepareStatement(sql);
                ps.setString(1, id);
                ps.executeUpdate();
                ps.close();
                c.commit();
            } catch (SQLException e) {
                e.printStackTrace();
                c.rollback();
                throw new SQLException("Fehler beim Statement: " + sql);
            } finally {
                c.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getModulId(String projectName, String name, Connection c) {
        try {
            PreparedStatement ps = c.prepareStatement("SELECT id FROM module WHERE name = ? AND projectname = ?");
            ps.setString(1, name);
            ps.setString(2, projectName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int result = rs.getInt(1);
                ps.close();
                return result;
            }
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    private void removeMeFromModule(String email, int modulid, Connection c) {
        try {
            PreparedStatement ps = c.prepareStatement("DELETE FROM rel_module_user WHERE modulid = ? AND email = ?");
            ps.setInt(1, modulid);
            ps.setString(2, email);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void saveMessageToDB(String message, String id, String username, String myEmail, Connection c) {
        try {
            int freeID = getFreeMessageID(id, c);
            PreparedStatement ps = c.prepareStatement("INSERT INTO rel_module_message (modulid, username, message, email, messageid) VALUES (?,?,?,?,?)");
            ps.setString(1, id);
            ps.setString(2, username);
            ps.setString(3, message);
            ps.setString(4, myEmail);
            ps.setInt(5, freeID);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private int getFreeMessageID(String modulid, Connection c) {
        try {
            int maxid = 0;
            PreparedStatement ps = c.prepareStatement("SELECT MAX(messageid) FROM rel_module_message WHERE modulid = ?");
            ps.setString(1, modulid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                maxid = rs.getInt(1);
            }
            ps.close();
            return maxid+1;
        } catch (SQLException ex) {
            Logger.getLogger(Modules.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    private void deleteMessage(String messageid, String modulid, Connection c) {
        try {
            PreparedStatement ps = c.prepareStatement("DELETE FROM rel_module_message WHERE modulid = ? AND messageid = ?");
            ps.setString(1, modulid);
            ps.setString(2, messageid);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static GregorianCalendar getDateFromString(String str) {
        int year = Integer.parseInt(str.substring(0, str.indexOf("-")));
        str = str.substring(str.indexOf("-") + 1);
        int month = Integer.parseInt(str.substring(0, str.indexOf("-")));
        str = str.substring(str.indexOf("-") + 1);
        int day = Integer.parseInt(str);
        str = str.substring(str.indexOf("-") + 1);
        if (year >= 2010) {
            if (month > 0 && month < 13) {
                if (day > 0 && (month == 1 && day < 32) ||
                               (month == 2 && day < 29) ||
                               (month == 3 && day < 32) ||
                               (month == 4 && day < 31) ||
                               (month == 5 && day < 32) ||
                               (month == 6 && day < 31) ||
                               (month == 7 && day < 32) ||
                               (month == 8 && day < 32) ||
                               (month == 9 && day < 31) ||
                               (month == 10 && day < 32) ||
                               (month == 11 && day < 31) ||
                               (month == 12 && day < 32)) {
                    System.out.println("tag:" + day + "monat:" + month + "jahr:" + year);
                    return new GregorianCalendar(year, month, day);
                }
            }
        }
        return null;
    }

    public static String format(String string, int number) {
        System.out.println(string);
        int br = 0, i = 1;
        while (i < string.length() - br) {
            if (i % number == 0) {
                if (string.charAt(i + br) != ' ') {
                    string = string.substring(0, i + br) + "-" + string.substring(i + br);
                    br++;
                }
                string = string.substring(0, i + br) + "<br>" + string.substring(i + br);
                br += 4;
            }
            i++;
        }
        return string;
    }
}
