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
                System.out.println(userEmail);
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
                }
            }
        }
        ArrayList<String> names, emails, status, firstnames;
        names = new ArrayList<String>();
        emails = new ArrayList<String>();
        status = new ArrayList<String>();
        firstnames = new ArrayList<String>();

        getMembers(seas.getAttribute("projectname").toString(), names, firstnames, emails, status);

        String htmlOutput = "<html>"
                + "<head>"
                + "<link rel=\"stylesheet\" type=\"text/css\" href=\"start.css\"></link>"
                + "</head>"
                + "<body>"
                + "<div id=\"userDescription\"></div>"
                + "<table border=\"0\" style=\"border-collapse:collapse\">"
                + "<tr><td align=\"center\">Name</td><td align=\"center\">Status</td></tr>"
                + "<tr><td>&nbsp;</td></tr>";
        for (int i = 0; i < names.size(); i++) {
            htmlOutput += "<tr id=\"" + emails.get(i) + "\" onmouseover=\"fillColor(this, '#9f9fFF')\" onmouseout=\"fillColor(this, 'white')\" style=\"cursor:pointer\" onclick=\"showUserDescription('" + emails.get(i) + "')\">"
                    + "<td style=\"border: 1px solid; padding-left: 10px; padding-right: 10px;\">" + names.get(i) + "</td>"
                    + "<td style=\"border: 1px solid; padding-left: 10px; padding-right: 10px;\">" + status.get(i) + "</td>"
                    + "<td><input type=\"button\" value=\"loeschen\"/ onclick=\"deleteUser('" + emails.get(i) + "')\"></td>"
                    + "</tr>";
        }
        htmlOutput += "<tr>"
                + "<td colspan=\"4\" id=\"addUserField\"></td>"
                + "</tr>"
                + "<tr>"
                + "<td colspan=\"4\"><input type=\"button\" value=\"Neuen Benutzer anlegen\" onclick=\"addUser()\"/></td>"
                + "</tr>"
                + "</table>"
                + "</body>"
                + "</html>";
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
            rs.close();
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
            ResultSet rs = DBConnector.getConnection().createStatement().executeQuery("SELECT name, firstname, status from user where email='" + email + "'");
            if (rs.next()) {
                return "Name: " + rs.getString(1) + "<br> Vorname: " + rs.getString(2) + "<br> E-Mail: " + email + "<br> Status: " + rs.getString(3);
            }
            rs.close();
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
   }
}
