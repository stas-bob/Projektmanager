/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import db.DBConnector;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
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
public class Registrieren extends HttpServlet {

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

        String smtp = "mail.gmx.net";
        String fromEmail = "htw-projektmanager@gmx.de";
        String pw = new BufferedReader(new FileReader("/pw.txt")).readLine();
        String toEmail = request.getParameter("email");
        String subject = "Anmeldung fuer das Projekt " + request.getParameter("projectname");
        String genPW = (int) (Math.random() * 10000000) + "";
        String text = createText(request.getParameter("firstname"),
                request.getParameter("name"),
                request.getParameter("projectname"),
                toEmail,
                genPW);

        if (validateProject(request.getParameter("projectname"), request.getParameter("email"))) {
            activate(request.getParameter("name"),
                    request.getParameter("firstname"),
                    request.getParameter("email"),
                    request.getParameter("projectname"),
                    genPW);
            sendMail(smtp, fromEmail, pw, fromEmail, toEmail, subject, text);
            out.write("0");
        } else {
            out.write("1");
        }
        out.close();
    }

    public void sendMail(String smtpHost, String username, String password, String senderAddress, String recipientsAddress, String subject, String text) {
        MailAuthenticator auth = new MailAuthenticator(username, password);

        Properties properties = System.getProperties();
        properties.put("mail.smtp.host", smtpHost);
        properties.put("mail.smtp.port", 587);
        properties.put("mail.smtp.auth", "true");

        Session session = Session.getInstance(properties, auth);
        try {
            Message msg = new MimeMessage(session);

            msg.setFrom(new InternetAddress(senderAddress));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(
                    recipientsAddress, false));

            msg.setSubject(subject);
            msg.setText(text);

            msg.setHeader("Test", "Test");
            msg.setSentDate(new Date());

            Transport.send(msg);

        } catch (Exception e) {
            e.printStackTrace();
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

    private String createText(String firstname, String name, String projectname, String email, String pw) {
        StringBuilder sb = new StringBuilder(200);
        sb.append("Hallo ").append(firstname).append(" ").append(name).append(",\n\n")
                .append("die Anmeldung fuer das Projekt ").append(projectname).append(" war erfolgreich.\n\n")
                .append("Benutzername: ").append(email).append("\n")
                .append("Passwort: ").append(pw).append("\n\n")
                .append("Ihr Entwickler Team");
        return sb.toString();
    }

    private void activate(String name, String firstname, String email, String projectName, String password) {
        try {
            Connection c = DBConnector.getConnection();
            PreparedStatement ps = c.prepareStatement("insert into `Project` (Name) values (?)");
            ps.setString(1, projectName);
            ps.executeUpdate();
            ps.close();

            ps = c.prepareStatement("insert into `User` (Name, Firstname, Email, Projectname, Password, Status)"
                    + " values (?,?,?,?,?,'PL')");
            ps.setString(1, name);
            ps.setString(2, firstname);
            ps.setString(3, email);
            ps.setString(4, projectName);
            ps.setString(5, password);
            ps.executeUpdate();
            ps.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.getLogger(Registrieren.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean validateProject(String projectName, String email) {
        try {
            if (projectName.trim().isEmpty() || email.trim().isEmpty()) {
                return false;
            }
            Connection c = DBConnector.getConnection();
            PreparedStatement ps1 = c.prepareStatement("select * from `Project` where `Name`=?");
            ps1.setString(1, projectName);
            ResultSet rsPName = ps1.executeQuery();

            PreparedStatement ps2 = c.prepareStatement("select * from `User` where `Email`=?");
            ps2.setString(1, email);
            ResultSet rsEmail = ps2.executeQuery();
            if (rsPName.next() || rsEmail.next()) {
                ps1.close();
                ps2.close();
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
}

class MailAuthenticator extends Authenticator {

    /**
     * Ein String, der den Usernamen nach der Erzeugung eines
     * Objektes<br>
     * dieser Klasse enthalten wird.
     */
    private final String user;
    /**
     * Ein String, der das Passwort nach der Erzeugung eines
     * Objektes<br>
     * dieser Klasse enthalten wird.
     */
    private final String password;

    /**
     * Der Konstruktor erzeugt ein MailAuthenticator Objekt<br>
     * aus den beiden Parametern user und passwort.
     *
     * @param user
     *            String, der Username fuer den Mailaccount.
     * @param password
     *            String, das Passwort fuer den Mailaccount.
     */
    public MailAuthenticator(String user, String password) {
        this.user = user;
        this.password = password;
    }

    /**
     * Diese Methode gibt ein neues PasswortAuthentication
     * Objekt zurueck.
     *
     * @see javax.mail.Authenticator#getPasswordAuthentication()
     */
    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(this.user, this.password);
    }
}
