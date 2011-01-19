package util;

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
import servlet.Modules;
import servlet.Times;

/**
 * Loescht einen Benutzer Account
 *
 * @author Thomas Altmeyer, Stanislaw Tartakowski
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
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Connection c = null;
        HttpSession seas = request.getSession();


        int user_id = Integer.parseInt(seas.getAttribute("user_id").toString());
        String email = seas.getAttribute("user").toString();
        String projectName = seas.getAttribute("projectname").toString();
        String status = seas.getAttribute("status").toString();
        try {
            c = DBConnector.getConnection();
            try {
              //  c.setAutoCommit(false);

                PreparedStatement ps = null;
                ps = c.prepareStatement("SELECT COUNT(*) FROM user");
                ResultSet rs = ps.executeQuery();
                int anzahl = 0;
                if (rs.next()) {
                    anzahl = rs.getInt(1);
                }
                rs.close();
                ps.close();
                
                if (anzahl > 1) {
                    if (status.equals("PL")) {
                        ps = c.prepareStatement("SELECT id FROM user WHERE status=?");
                        ps.setString(1, status);
                        rs = ps.executeQuery();
                        boolean secondPLFound = false;
                        while (rs.next()) {
                            if (rs.getInt(1) != user_id) {
                                secondPLFound = true;
                            }
                        }
                        if (!secondPLFound) {
                            out.write("<root><htmlSeite><![CDATA[]]></htmlSeite><message><![CDATA[Ernenen sie erst einen anderen zum Projektleiter]]></message></root>");
                            return;
                        }

                    }
                }

                ps = c.prepareStatement("DELETE FROM time WHERE user_id = ?");
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
                anzahl--;

                if (anzahl == 0) {
                    deleteProject(user_id, projectName, c);
                }


               // c.commit();

                request.getSession().invalidate();
                out.write("<root><htmlSeite><![CDATA[<html><head><title>Account l&ouml;schen</title><link rel=\"stylesheet\" type=\"text/css\" href=\"start.css\" /></head>"
                        + "<body><div id=\"textPasswordForget\" />Ihr Account wurde gel&ouml;scht!</div></body></html>]]></htmlSeite><message> </message></root>");
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

    /*
     * Loescht ein Projekt
     *
     * @param user_id User-ID des Benutzers
     * @param projectName Projektname des Projektes
     * @param c DB Connection
     */
    public static void deleteProject(int user_id, String projectName, Connection c) {
        try {
            PreparedStatement ps = c.prepareStatement("DELETE FROM project WHERE name = ?");
            ps.setString(1, projectName);
            ps.executeUpdate();
            ps.close();
            ps = c.prepareStatement("SELECT id FROM module WHERE projectname = ?");
            ps.setString(1, projectName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Modules.deleteModule(rs.getString(1), c);
            }
            ps.close();
            ps = c.prepareStatement("DELETE FROM time WHERE user_id = ?");
            ps.setInt(1, user_id);
            ps.executeUpdate();
            ps.close();
            ps = c.prepareStatement("DELETE FROM user WHERE id = ?");
            ps.setInt(1, user_id);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            Logger.getLogger(DeleteAccount.class.getName()).log(Level.SEVERE, null, ex);
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
