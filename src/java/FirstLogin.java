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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author tA88
 */
public class FirstLogin extends HttpServlet {

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        HttpSession seas = request.getSession();

        String projectname = seas.getAttribute("projectname").toString();
        try {
            switch (changePassword(request)) {
                case -1:
                    out.write(MainServlet.changePasswordView(-1));
                    break;
                case 0:
                    out.write(MainServlet.mainView(projectname, true));
                    break;
                case 1:
                    out.write(MainServlet.changePasswordView(1));
                    break;
                case 2:
                    out.write(MainServlet.changePasswordView(2));
                    break;
                default:
                    break;
            }
        } finally {
            out.close();
        }
    }

    public static int changePassword(HttpServletRequest request) {
        Connection c = null;
        String user = request.getSession().getAttribute("user").toString();
        String oldPassword = request.getParameter("oldPassword");
        String newPassword = request.getParameter("newPassword");
        String validatePassword = request.getParameter("validatePassword");
        try {
            c = DBConnector.getConnection();
            PreparedStatement ps = c.prepareStatement("SELECT password FROM user WHERE email = ?");
            ps.setString(1, user);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getString(1).equals(Registrieren.md5(oldPassword))) {
                    if (newPassword.equals(validatePassword)) {
                        ps.close();
                        String sql = "";
                        try {
                            c.setAutoCommit(false);
                            sql = "UPDATE user SET password = ?, clearpw = ?, firstlogin = 1  WHERE email = ?";
                            ps = c.prepareStatement(sql);
                            ps.setString(1, Registrieren.md5(newPassword));
                            ps.setString(2, newPassword);
                            ps.setString(3, user);
                            ps.executeUpdate();
                            ps.close();
                            c.commit();
                        } catch (SQLException e) {
                            c.rollback();
                            throw new SQLException("Fehler beim Statement: " + sql);
                        } finally {
                            c.setAutoCommit(true);
                            c.close();
                        }
                        return 0;
                    } else {
                        return 1;
                    }
                } else {
                    return 2;
                }
            }
            c.close();
        } catch (MySQLException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
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
