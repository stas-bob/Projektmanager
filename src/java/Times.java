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
        try {
            try {
                c = DBConnector.getConnection();
                ArrayList<String> moduls = getModules(c, user);
                StringBuilder htmlOutput = new StringBuilder(300);
                htmlOutput.append("<table>")
                        .append("<colgroup width=\"200\" />")
                        .append("<tr align=\"left\">")
                        .append("<th>Modul:</th>")
                        .append("<th><select size=1>");
                for (String modul : moduls) {
                    htmlOutput.append("<option>").append(modul).append("</option>");
                }
                htmlOutput.append("</select></th>")
                        .append("</tr>")
                        .append("<tr align=\"left\">")
                        .append("<th>Datum:</th>")
                        .append("<th><input type=\"text\" name=\"date\" size=\"10\" maxlength=\"10\" />(dd:mm:yyyy)</th>")
                        .append("</tr>")
                        .append("<tr align=\"left\">")
                        .append("<th>Start:</th>")
                        .append("<th><input type=\"text\" name=\"start\" size=\"5\" maxlength=\"5\" />(hh:mm)</th>")
                        .append("</tr>")
                        .append("<tr align=\"left\">")
                        .append("<th>Ende:</th>")
                        .append("<th><input type=\"text\" name=\"end\" size=\"5\" maxlength=\"5\" />(hh:mm)</th>")
                        .append("</tr>")
                        .append("<tr align=\"left\">")
                        .append("<th>Dauer:</th>")
                        .append("<th><input type=\"text\" name=\"duration\" id=\"duration\" size=\"5\" maxlength=\"5\" readonly /></th>")
                        .append("</tr>")
                        .append("<tr align=\"left\">")
                        .append("<th>Beschreibung:</th>")
                        .append("<th><textarea name=\"description\" cols=\"50\" rows=\"5\"></textarea></th>")
                        .append("</tr>")
                        .append("<tr align=\"left\">")
                        .append("<th><input type=\"button\" value=\"Speichern\" onclick=\"\"")
                        .append("</table>");
                String xmlResponse = "<root><htmlSeite><![CDATA[" + htmlOutput.toString() + "]]></htmlSeite></root>";
                out.write(xmlResponse);
            } catch (MySQLException ex) {
            } finally {
                c.close();
                out.close();
            }
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
}
