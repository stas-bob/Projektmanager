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
                + "<table id=\"memberTable\" border=\"0\" style=\"border-collapse:collapse\">"
                + "<tr><td align=\"center\">Name</td><td align=\"center\">Vorname</td><td align=\"center\">Email</td><td align=\"center\">Status</td></tr>"
                + "<tr><td>&nbsp;</td></tr>";
        for (int i = 0; i < names.size(); i++) {
            htmlOutput += "<tr id=\"" + emails.get(i) + "\" onmouseover=\"fillColor(this, '#9f9fFF')\" onmouseout=\"fillColor(this, 'white')\" style=\"cursor:pointer\">"
                    + "<td style=\"border: 1px solid; padding-left: 10px; padding-right: 10px;\">" + names.get(i) + "</td>"
                    + "<td style=\"border: 1px solid; padding-left: 10px; padding-right: 10px;\">" + firstnames.get(i) + "</td>"
                    + "<td style=\"border: 1px solid; padding-left: 10px; padding-right: 10px;\">" + emails.get(i) + "</td>"
                    + "<td style=\"border: 1px solid; padding-left: 10px; padding-right: 10px;\">" + status.get(i) + "</td>"
                    + "<td><input type=\"button\" value=\"loeschen\"/ onclick=\"deleteUser('" + emails.get(i) + "')\"></td>"
                    + "</tr>";
        }
        htmlOutput += "<tr>"
                + "<td colspan=\"4\"><input type=\"button\" value=\"Neuen Benutzer anlegen\"/></td>"
                + "<tr>"
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
            PreparedStatement ps = c.prepareStatement("SELECT Name, Firstname, Email, Status FROM User WHERE Projectname=?");
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
            ResultSet rs = DBConnector.getConnection().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE).executeQuery("select ID from `User` where `EMail`='" + email + "'");
            if (rs.next()) {
                rs.deleteRow();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
