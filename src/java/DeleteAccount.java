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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author tA88
 */
public class DeleteAccount extends HttpServlet {

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Connection c = null;
        HttpSession seas = request.getSession();


        int user_id = Integer.parseInt(seas.getAttribute("user_id").toString());
        String email = seas.getAttribute("user").toString();
        try {
            c = DBConnector.getConnection();
            try {
                c.setAutoCommit(false);

                PreparedStatement ps = c.prepareStatement("DELETE FROM time WHERE user_id = ?");
                ps.setInt(1, user_id);
                ps.executeUpdate();
                ps.close();

                ps = c.prepareStatement("DELETE FROM rel_module_user WHERE email = ?");
                ps.setString(1, email);
                ps.executeUpdate();
                ps.close();

                ps = c.prepareStatement("DELETE FROM user WHERE id = ?");
                ps.setInt(1, user_id);
                ps.executeUpdate();
                ps.close();

                c.commit();

                request.getSession().invalidate();
                out.write("<html>");
                out.write("<head>");
                out.write("<title>Account loeschen</title>");
                out.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"start.css\" />");
                out.write("</head>");
                out.write("<body>");
                out.write("<div id=\"textPasswordForget\" />");
                out.write("Ihr Account wurde gel&ouml;scht!");
                out.write("</div>");
                out.write("</body>");
                out.write("</html>");
            } catch (SQLException e) {
                e.printStackTrace();
                c.rollback();
            } finally {
                c.setAutoCommit(true);
                c.close();
            }
        } catch (MySQLException ex) {
            ex.printStackTrace();
        } catch (SQLException ex) {
            Logger.getLogger(Times.class.getName()).log(Level.SEVERE, null, ex);
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
}
