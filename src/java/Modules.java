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
import java.sql.Statement;
import java.util.ArrayList;
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
        response.setContentType("application/xml");
        PrintWriter out = response.getWriter();

        if (request.getParameter("description") != null) {
            ArrayList<String> members = getMembers(request.getParameter("membersToAdd"), request.getSession().getAttribute("projectname").toString());
            if (validateModuleName(request.getParameter("name").toString(),request.getSession().getAttribute("projectname").toString())) {
                saveMouleToDB(members, request.getParameter("name").toString(), request.getParameter("description").toString(), request.getParameter("startDate").toString(), request.getParameter("endDate").toString(), request.getParameter("prio").toString(), request.getSession().getAttribute("projectname").toString());
                if (members.contains(request.getSession().getAttribute("user").toString())) {
                    int modulid = getModulId(request.getSession().getAttribute("projectname").toString(), request.getParameter("name").toString());
                    ((ArrayList<Integer>)request.getSession().getAttribute("modules")).add(modulid);
                }
            } else {
                String xmlResponse = "<root><htmlSeite><![CDATA[Der Name " + request.getParameter("name").toString() + " ist in diesem Projekt bereits vorhanden]]></htmlSeite><modulesCount>0</modulesCount><error>1</error></root>";
                out.write(xmlResponse);
                return;
            }
        } else {
            if(request.getParameter("addModule") != null) {
                String htmlOutput =
                        "<table border=\"0\">" +
                            "<tbody><tr><td>Name:</td><td><input id=\"name\" size=\"40\" maxlength=\"40\" type=\"text\"></td></tr>" +
                                "<tr><td valign=\"top\">Beschreibung:</td><td><textarea id=\"description\" cols=\"30\" rows=\"6\" maxlength=\"400\" onkeypress=\"ismaxlength(this)\"></textarea></td></tr>" +
                                "<tr><td>Priorität:</td><td><select size=\"1\" id=\"prio\"><option>1</option><option>2</option><option>3</option></select><div style=\"background-image:url(grafik/question_mark.png); width:25px; height:22px; position:absolute; margin-left: 40px; margin-top: -22px;\" onclick=\"showHint(this, '1 is die höchste prio')\"></div></td></tr>" +
                                "<tr><td>Start:</td><td>Tag: <input id=\"startDay\" typ=\"text\" size=\"1\" maxlength=\"2\">Monat: <input id=\"startMonth\" typ=\"text\" size=\"1\" maxlength=\"2\">Jahr: <input id=\"startYear\" typ=\"text\" size=\"4\" maxlength=\"4\"></td></tr>" +
                                "<tr><td>Ende:</td><td>Tag: <input id=\"endDay\" typ=\"text\" size=\"1\" maxlength=\"2\">Monat: <input id=\"endMonth\" typ=\"text\" size=\"1\" maxlength=\"2\">Jahr: <input id=\"endYear\" typ=\"text\" size=\"4\" maxlength=\"4\"></td></tr>" +
                                "<tr><td>Mitglied zuweisen:</td><td><select id=\"selectMember\">" + getAllMembers(request.getSession().getAttribute("projectname").toString()) + "</select><input value=\"einfügen\" type=\"button\" onclick=\"addMemberToModuleBox()\"/><input value=\"loeschen\" type=\"button\" onclick=\"removeMemberFromModuleBox()\"/></td></tr>" +
                            "</tbody>" +
                        "</table>" +
                        "<div id=\"membersInModuleBox\" style=\"position:absolute; border: 1px dashed; margin-left: 50px; width: 300px; height: 120px; display:none\"></div><button onclick=\"saveModule()\" style=\"margin-left: 354px; font: 12px Arial; padding-top:1px; padding-left:0px; padding-right:0px; position: absolute; margin-top: -27px;\">speichern</button>";
                out.write(htmlOutput);
                return;
            } else {
                if (request.getParameter("addToModule") != null) {
                    addMeToModule(request.getSession().getAttribute("user").toString(), request.getParameter("addToModule").toString());
                    ((ArrayList<Integer>)request.getSession().getAttribute("modules")).add(Integer.parseInt(request.getParameter("addToModule").toString()));
                } else {
                    if (request.getParameter("moduleDescriptionId") != null) {
                        out.write(getModuleDescription(request.getParameter("moduleDescriptionId"), request.getSession().getAttribute("status").toString()));
                        return;
                    } else {
                        if (request.getParameter("changeStatus") != null) {
                            out.write(setModuleStatusOnDB(request.getParameter("changeStatus"), request.getParameter("id")));
                            return;
                        } else {
                            if (request.getParameter("deleteModule") != null) {
                                deleteModule(request.getParameter("deleteModule").toString());
                            } else {
                                if (request.getParameter("removeFromModule") != null) {
                                    removeMeFromModule(request.getSession().getAttribute("user").toString(), Integer.parseInt(request.getParameter("removeFromModule").toString()));
                                    ((ArrayList<Integer>)request.getSession().getAttribute("modules")).remove((Integer)Integer.parseInt(request.getParameter("removeFromModule")));
                                }
                            }
                        }
                    }
                }
            }
        }


        ArrayList<String> names, status;
        names = new ArrayList<String>();
        status = new ArrayList<String>();
        ArrayList<Integer> ids = new ArrayList<Integer>();

        getModules(request.getSession().getAttribute("projectname").toString(), names, status, ids);
        String htmlOutput = "<table border=\"0\" style=\"border-collapse:collapse; position:absolute;\" >"
                + "<tr><td align=\"center\">Name</td><td align=\"center\">Status</td></tr>";
        for (int i = 0; i < names.size(); i++) {
            htmlOutput += "<tr onmouseover=\"fillColor(this, '#9f9fFF')\" onmouseout=\"fillColor(this, 'white')\" onmousedown=\"fillColor(this, '#6c6ccc')\">"
                    + "<td style=\"border: 1px solid; padding-left: 10px; padding-right: 10px; cursor:pointer;\" onclick=\"showModuleDescription(" + ids.get(i) + ")\">" + names.get(i) + "</td>"
                    + "<td style=\"border: 1px solid; padding-left: 10px; padding-right: 10px;\">" + setStatus(status.get(i),ids.get(i)) + "</td>";
            if (!((ArrayList<Integer>)request.getSession().getAttribute("modules")).contains(ids.get(i))) {
                htmlOutput += "<td><input type=\"button\" value=\"Teilnehmen\" onclick=\"addMeToModule(" + ids.get(i) + ")\"/></td>";
            } else {
                htmlOutput += "<td><input type=\"button\" value=\"Aufh&ouml;ren\" onclick=\"removeMeFromModule(" + ids.get(i) + ")\"/></td>";
            }
            htmlOutput += "</tr>";
        }
        if (request.getSession().getAttribute("status").equals("PL")) {
            htmlOutput += "<tr><td colspan=\"2\"><input type=\"button\" value=\"Neue Aufgabe anlegen\" onclick=\"addModule()\"/></td></tr>";
        }
        htmlOutput += "</table>";
        htmlOutput += "<div id=\"addModule\"></div>";
        htmlOutput += "<div id=\"statusBox\" style=\"height:50px; width:430px; position:absolute; margin-left:309px; margin-top:420px;\"></div>";
        String xmlResponse = "<root><htmlSeite><![CDATA[" + htmlOutput + "]]></htmlSeite><modulesCount>" + names.size() + "</modulesCount><error>0</error></root>";
        out.write(xmlResponse);
        out.close();
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

    private void getModules(String projectName, ArrayList<String> names, ArrayList<String> status, ArrayList<Integer> ids) {
        try {
            Connection c = DBConnector.getConnection();
            PreparedStatement ps = c.prepareStatement("SELECT name, status, id FROM module WHERE projectname=?");
            ps.setString(1, projectName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                names.add(rs.getString(1));
                status.add(rs.getString(2));
                ids.add(rs.getInt(3));
            }
            ps.close();
            c.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String getModuleDescription(String id, String status) {
        try {
            Statement s = DBConnector.getConnection().createStatement();
            ResultSet rs = s.executeQuery("SELECT name, prio, description, start, end FROM module WHERE id='" + id + "'");
            if (rs.next()) {
                String description = rs.getString(3);
                String name = rs.getString(1);
                String prio = rs.getString(2);
                Date start = rs.getDate(4);
                Date end = rs.getDate(5);
                String members = "";
                rs = s.executeQuery("SELECT name FROM rel_module_user, user WHERE user.email = rel_module_user.email AND modulid='" + id + "'");
                rs.last();
                int memberCount = rs.getRow();
                rs.beforeFirst();
                while (rs.next()) {
                    members += "[" + rs.getString(1) + "]";
                }

                String htmlOutput = "<table border=\"1\" style=\"border-collapse:collapse\">"
                        + "<tr onmouseover=\"fillColor(this, '#fbf52d')\" onmouseout=\"fillColor(this, 'white')\" onmousedown=\"fillColor(this, '#c8c20a')\"><td>Name: </td><td>" + name +"</td></tr>"
                        + "<tr onmouseover=\"fillColor(this, '#fbf52d')\" onmouseout=\"fillColor(this, 'white')\" onmousedown=\"fillColor(this, '#c8c20a')\"><td>Prio: </td><td>" + prio + "</td></tr>"
                        + "<tr onmouseover=\"fillColor(this, '#fbf52d')\" onmouseout=\"fillColor(this, 'white')\" onmousedown=\"fillColor(this, '#c8c20a')\"><td>Start: </td><td>" + start + "</td></tr>"
                        + "<tr onmouseover=\"fillColor(this, '#fbf52d')\" onmouseout=\"fillColor(this, 'white')\" onmousedown=\"fillColor(this, '#c8c20a')\"><td>Ende: </td><td>" + end + "</td></tr>"
                        + "<tr onmouseover=\"fillColor(this, '#fbf52d')\" onmouseout=\"fillColor(this, 'white')\" onmousedown=\"fillColor(this, '#c8c20a')\"><td>Mitgliederzahl: </td><td>" + memberCount + "</td></tr>"
                        + "<tr onmouseover=\"fillColor(this, '#fbf52d')\" onmouseout=\"fillColor(this, 'white')\" onmousedown=\"fillColor(this, '#c8c20a')\"><td>Mitglieder: </td><td>" + members + "</td></tr>"
                        + "<tr onmouseover=\"fillColor(this, '#fbf52d')\" onmouseout=\"fillColor(this, 'white')\" onmousedown=\"fillColor(this, '#c8c20a')\"><td>Beschreibung: </td><td>" + description + "</td></tr>";
                        if (status.equals("PL")) {
                            htmlOutput += "<tr onmouseover=\"fillColor(this, '#fbf52d')\" onmouseout=\"fillColor(this, 'white')\" onmousedown=\"fillColor(this, '#6c6ccc')\"><td colspan=\"2\" align=\"center\"><input type=\"button\" value=\"loeschen\" onclick=\"deleteModule(" + id + ")\"/></td></tr>";
                        }
                        htmlOutput += "</table>";
                return htmlOutput;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private String getAllMembers(String projectName) {
        try {
            String result = "";
            ResultSet rs = DBConnector.getConnection().createStatement().executeQuery("SELECT name FROM user WHERE projectname='" + projectName + "'");
            while (rs.next()) {
                result += "<option>" + rs.getString(1) + "</option>";
            }
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private ArrayList<String> getMembers(String string, String projectName) {
        ArrayList<String> members = new ArrayList<String>();
        try {
            Statement s = DBConnector.getConnection().createStatement();
            for (int i = 0; i < string.length(); i++) {
                if (string.charAt(i) == '[') {
                    String tmpString = "";
                    i++;
                    while (string.charAt(i) != ']') {
                        tmpString += string.charAt(i);
                        i++;
                    }
                    ResultSet rs = s.executeQuery("SELECT email FROM user WHERE name='" + tmpString + "' AND projectname='" + projectName + "'");
                    if (rs.next()) {
                        members.add(rs.getString(1));
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return members;
    }

    private void saveMouleToDB(ArrayList<String> members, String name, String description, String start, String end, String prio, String projectName) {
        try {
            Statement s = DBConnector.getConnection().createStatement();
            s.executeUpdate("INSERT INTO module (name,start,end,prio,status,description,projectname) VALUES ('" + name + "','" + start + "','" + end + "','" + prio + "','open','" + description + "','" + projectName + "')");
            for (String member: members) {
                s.executeUpdate("INSERT INTO rel_module_user (modulid, email) VALUES ((SELECT id FROM module WHERE name='" + name + "' AND projectname='" + projectName + "'),'" + member + "')");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean validateModuleName(String name, String projectName) {
        try {
            ResultSet rs = DBConnector.getConnection().createStatement().executeQuery("SELECT * FROM module WHERE name='" + name + "' AND projectname='" + projectName + "'");
            if (rs.next()) {
                return false;
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private void addMeToModule(String email, String modulid) {
        try {
            DBConnector.getConnection().createStatement().executeUpdate("INSERT INTO rel_module_user (modulid, email) VALUES ('" + modulid + "','" + email + "')");
        } catch (Exception ex) {
            Logger.getLogger(Modules.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String setStatus(String status, int id) {
        String open = "<option onclick=\"changeModuleStatus('open','" + id + "')\">open</option>";
        String closed = "<option onclick=\"changeModuleStatus('closed','" + id + "')\">closed</option>";

        String select = "<select name=\"statusSelect\" size=\"1\">";
        select += status.equals("open") ? open + closed : closed + open;
        return select + "</select>";
    }

    public String setModuleStatusOnDB(String status, String id) {
        try {
            DBConnector.getConnection().createStatement().executeUpdate("UPDATE module SET status='" + status + "' WHERE id='" + id + "'");
            return "Aenderung gespeichert.";
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private void deleteModule(String id) {
        try {
            Connection c = null;
            String sql = "";
            try {
                c = DBConnector.getConnection();
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
                c.commit();
            } catch (MySQLException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                c.rollback();
                throw new SQLException("Fehler beim Statement: " + sql);
            } finally {
                c.setAutoCommit(true);
                c.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getModulId(String projectName, String name) {
        try {
            ResultSet rs = DBConnector.getConnection().createStatement().executeQuery("SELECT id FROM module WHERE name='" + name + "' AND projectname='" + projectName + "'");
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    private void removeMeFromModule(String email, int modulid) {
        try {
            DBConnector.getConnection().createStatement().executeUpdate("DELETE FROM rel_module_user WHERE modulid='" + modulid + "' AND email='" + email + "'");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}
