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
import org.omg.PortableServer.REQUEST_PROCESSING_POLICY_ID;

/**
 *
 * @author bline
 */
public class Members extends HttpServlet {
   
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
        HttpSession seas = request.getSession();

        if (request.getParameter("deleteEmail") != null) {
            String email = request.getParameter("deleteEmail");
            deleteUser(email);
        } else {
            if (request.getParameter("addName") != null) {
                String userName = request.getParameter("addName");
                String userEmail = request.getParameter("addEmail");
                if(new ValidateEmailServlet().validateEmail(userEmail).equals("0")) {
                    new Registrieren().activate(userName,
                        "empty",
                        userEmail,
                        request.getSession().getAttribute("projectname").toString(), true);
                } else {
                    out.write("1");
                }
            } else {
                if (request.getParameter("userDescription") != null) {
                    out.write(getUserDescription(request.getParameter("userDescription")));
                    return;
                } else {
                    if (request.getParameter("changeStatus") != null) {
                        out.write(setStatusOnDB(request.getParameter("changeStatus"), request.getParameter("email"), request.getSession().getAttribute("status").toString(), request.getSession().getAttribute("user").toString()));
                        return;
                    }
                }
            }
        }
        ArrayList<String> names, emails, status, firstnames;
        names = new ArrayList<String>();
        emails = new ArrayList<String>();
        status = new ArrayList<String>();
        firstnames = new ArrayList<String>();

        getMembers(seas.getAttribute("projectname").toString(), names, firstnames, emails, status);
        String htmlOutput = "<div id=\"userDescription\"></div>"
                + "<table border=\"0\" style=\"border-collapse:collapse\">"
                + "<tr><td align=\"center\">Name</td><td align=\"center\">Status</td></tr>"
                + "<tr><td>&nbsp;</td></tr>";
        for (int i = 0; i < names.size(); i++) {
            htmlOutput += "<tr id=\"" + emails.get(i) + "\" onmouseover=\"fillColor(this, '#9f9fFF')\" onmouseout=\"fillColor(this, 'white')\" onmousedown=\"fillColor(this, '#6c6ccc')\">"
                    + "<td style=\"border: 1px solid; padding-left: 10px;  padding-right: 10px; cursor:pointer;\" onclick=\"showUserDescription('" + emails.get(i) + "')\">" + names.get(i) + "</td>"
                    + "<td style=\"border: 1px solid; padding-left: 10px; padding-right: 10px;\">" + setStatus(status.get(i), emails.get(i)) + "</td>";
                    if (request.getSession().getAttribute("status").equals("PL")) {
                        if (!request.getSession().getAttribute("user").equals(emails.get(i))) {
                            htmlOutput += "<td><input type=\"button\" value=\"loeschen\"/ onclick=\"deleteUser('" + emails.get(i) + "')\"></td>";
                        }
                    }
            htmlOutput += "</tr>";
        }
        htmlOutput += "</table>"
                    + "<div id=\"addUserField\"></div>";
        if (request.getSession().getAttribute("status").equals("PL")) {
            htmlOutput += "<div><input type=\"button\" value=\"Neuen Benutzer anlegen\" onclick=\"addUser()\"/></div>";
        }
        String xmlResponse = "<root><htmlSeite><![CDATA[" + htmlOutput + "]]></htmlSeite><membersCount>" + names.size() + "</membersCount></root>";
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

    private void getMembers(String projectName, ArrayList<String> names, ArrayList<String> firstnames, ArrayList<String> emails, ArrayList<String> status) {
        try {
            Connection c = DBConnector.getConnection();
            PreparedStatement ps = c.prepareStatement("SELECT name, firstname, email, status FROM user WHERE projectname=?");
            ps.setString(1, projectName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                names.add(rs.getString(1));
                firstnames.add(rs.getString(2));
                emails.add(rs.getString(3));
                status.add(rs.getString(4));
            }
            ps.close();
            c.close();
        } catch (Exception ex) {
            Logger.getLogger(Members.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


   private void deleteUser(String email) {
        try {
            Connection c = null;
            String sql = "";
            try {
                c = DBConnector.getConnection();
                c.setAutoCommit(false);
                sql = "DELETE FROM user WHERE email = ?";
                PreparedStatement ps = c.prepareStatement(sql);
                ps.setString(1, email);
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

public String getUserDescription(String email) {
        try {
            Connection c = DBConnector.getConnection();
            PreparedStatement ps = c.prepareStatement("SELECT name, firstname, status FROM user where email = ?");
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String name = rs.getString(1);
                String firstname = rs.getString(2);
                String status = rs.getString(3);
                ps.close();
                c.close();
                return "Name: " + name +
                        "<br> Vorname: " + firstname +
                        "<br> E-Mail: <a href=\"mailto:" + email + "\">" + email + "</a>" +
                        "<br> Status: " + status;
            }
            ps.close();
            c.close();
            return null;
        } catch (MySQLException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
   }

    private String setStatusOnDB(String status, String email, String myStatus, String myEmail) {
        if (myStatus.equals("PL")) {
            if (!myEmail.equals(email)) {
                try {
                    Connection c = null;
                    try {
                        c = DBConnector.getConnection();
                        c.setAutoCommit(false);
                        PreparedStatement ps = c.prepareStatement("UPDATE user SET status = ? WHERE email = ?");
                        ps.setString(1, status);
                        ps.setString(2, email);
                        ps.executeUpdate();
                        ps.close();
                        c.commit();
                        return "&Auml;nderungen gespeichert";
                    } catch (SQLException e) {
                        c.rollback();
                        e.printStackTrace();
                    } finally {
                        c.setAutoCommit(true);
                        c.close();
                    }
                } catch (MySQLException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                return "Sie dürfen sich nicht selbst &auml;ndern";
            }
        } else {
            //TODO was passiert bei CPL ???
            return "Sie haben<br> keine Berechtigung<br> das zu tun!";
        }
        return "Fehler!";
    }

    private String setStatus(String status, String email) {
        String MEM = "<option onclick=\"changeStatus('MEM','" + email + "')\">MEM</option>";
        String CPL = "<option onclick=\"changeStatus('CPL','" + email + "')\">CPL</option>";
        String PL  = "<option onclick=\"changeStatus('PL','" + email + "')\">PL</option>";

        String select = "<select name=\"statusSelect\" size=\"1\">";
        if (status.equals("MEM")) {
            select += MEM + CPL + PL;
        } else {
            if (status.equals("CPL")) {
                select += CPL + MEM + PL;
            } else {
                select += PL + CPL + MEM;
            }
        }
        return select + "</select>";
    }
}
