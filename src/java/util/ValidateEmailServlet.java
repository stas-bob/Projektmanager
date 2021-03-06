package util;

import db.DBConnector;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Überprüft ob die eingegeben E-Mail Addresse noch frei ist
 *
 * @author Thomas Altmeyer, Stanislaw Tartakowski
 */
public class ValidateEmailServlet extends HttpServlet {
   
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
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String emailName = request.getParameter("email");
        String status = validateEmail(emailName);
        out.write(status);
        out.close();
    } 

    /*
     * Überprüft die E-Mail Addresse
     *
     * @param emailName E-Mail die eingegebn wurde
     * @return Wenn Status = 0, dann E-Mail noch nicht vergeben, ansonsten schon vorhanden
     */
    public String validateEmail(String emailName) {
        String status = "0";
        try {
            Connection c = DBConnector.getConnection();
            PreparedStatement ps = c.prepareStatement("SELECT * FROM user WHERE email = ?");
            ps.setString(1, emailName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                status = "1";
            }
            ps.close();
            c.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return status;
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
