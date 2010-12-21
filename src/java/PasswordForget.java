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
import java.util.Properties;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author tA88
 */
public class PasswordForget extends HttpServlet {
   
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
        String email = request.getParameter("email").toString();
        try {
            c = DBConnector.getConnection();
            PreparedStatement ps = c.prepareStatement("SELECT name, firstname, password FROM user WHERE email = ?");
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            rs.next();
            String name = rs.getString("name");
            String firstname = rs.getString("firstname");
            String pw = rs.getString("password");
            String text = createText(firstname, name, pw);
            Registrieren.sendMail(email, "Ihr vergessenes Passwort", text);

            out.write("<html>");
            out.write("<head>");
            out.write("<title>Logout</title>");
            out.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"start.css\" />");
            out.write("</head>");
            out.write("<body>");
            out.write("<div id=\"textLogout\" />");
            out.write("Ihr Passwort wurde an die von Ihnen eingegeben E-Mail geschickt!");
            out.write("<br>");
            out.write("<a href='Login.html'>Zur&uuml;ck zum Login</a>");
            out.write("</div>");
            out.write("</body>");
            out.write("</html>");
        } catch (MySQLException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally { 
            out.close();
        }
    }

    private String createText(String firstname, String name, String pw) {
        StringBuilder sb = new StringBuilder(200);
        sb.append("Hallo ").append(firstname).append(" ").append(name).append(",\n\n")
                .append("Ihr Passwort ist:\n")
                .append(pw).append("\n\n")
                .append("Ihr Entwickler Team");
        return sb.toString();
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
