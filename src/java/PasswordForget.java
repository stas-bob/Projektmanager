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
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        Connection c = null;
        String email = request.getParameter("email").toString();
        try {
            String genPW = (int) (Math.random() * 10000000) + "";
            String hashedPW = Registrieren.md5(genPW);
            System.out.println("new clear pw: " + genPW);
            System.out.println("new hashed pw: " + hashedPW);
            c = DBConnector.getConnection();
            PreparedStatement ps = c.prepareStatement("UPDATE user SET password = ?, firstlogin = 0  WHERE email = ?");
            ps.setString(1, hashedPW);
            ps.setString(2, email);
            ps.executeUpdate();
            ps = c.prepareStatement("SELECT name, firstname FROM user WHERE email = ?");
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            String name = "";
            String firstname = "";
            if (rs.next()) {
                name = rs.getString(1);
                firstname = rs.getString(2);
            }
            String text = createText(firstname, name, genPW);
            Registrieren.sendMail(email, "Ihr vergessenes Passwort", text);

            out.write("<html>");
            out.write("<head>");
            out.write("<title>Passwort vergessen</title>");
            out.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"start.css\" />");
            out.write("</head>");
            out.write("<body>");
            out.write("<div id=\"textPasswordForget\" />");
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
                .append("Ihr neues Passwort ist:\n-")
                .append(pw).append("-\n\n")
                .append("http://stud-i-pr2.htw-saarland.de:8080/Projektmanager/\n\n")
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
