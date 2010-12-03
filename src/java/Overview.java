/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import db.DBConnector;
import java.sql.*;
import exceptions.MySQLException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
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
public class Overview extends HttpServlet {
   
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
        try {
            Connection c = DBConnector.getConnection();
            int modulesCount = getModulesCount(c, request.getSession().getAttribute("projectname").toString());
            int doneCount = getModulesDoneCount(c, request.getSession().getAttribute("projectname").toString());
            int divWidth = 200;
            int progress = (divWidth*doneCount)/modulesCount;
            String htmlOutput = "<div style=\"border:1px dashed; width:" + divWidth + "px; height:100px; position:absolute; background-color: ##EEEEFF;\">"
                                  + "<div style=\"margin-left:30%; margin-top:35px; position:absolute; text-shadow: 2px 2px 0 #AAAAAA;\">Fortschritt " + (int)((float)progress*100/divWidth) + "%</div>"
                                  + "<div style=\"border:1px solid blue; width:" + progress + "px; height:100px; margin-top: -1px; margin-left: -1px; color:white; background-color: LightSteelBlue;\"></div>"
                              + "</div>";

            String xmlResponse = "<root><htmlSeite><![CDATA[" + htmlOutput + "]]></htmlSeite></root>";
            out.write(xmlResponse);
        } catch (SQLException ex) {
            Logger.getLogger(Overview.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MySQLException ex) {
            Logger.getLogger(Overview.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            out.close();
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

    private int getModulesCount(Connection c, String projectName) {
        try {
            PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM module WHERE projectname=?");
            ps.setString(1, projectName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Overview.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    private int getModulesDoneCount(Connection c, String projectName) {
        try {
            PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM module WHERE projectname=? AND status='closed'");
            ps.setString(1, projectName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Overview.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

}
