/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import db.DBConnector;
import exceptions.MySQLException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
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

        if (validateProject(request.getParameter("projectname"), request.getParameter("email"))) {
            Date startProject = getDate(request.getParameter("startProject"));
            Date endProject = getDate(request.getParameter("endProject"));
            activate(request.getParameter("name"),
                    request.getParameter("firstname"),
                    request.getParameter("email"),
                    startProject,
                    endProject,
                    request.getParameter("projectname"), false);    //false == projektleiter dh. es wird ein projekt angelegt
            out.write("0");
        } else {
            out.write("1");
        }
        out.close();
    }

    public void activate(String name, String firstname, String toEmail, Date startProject, Date endProject, String projectName, boolean member) {
        try {
            
            String subject = "Anmeldung fuer das Projekt " + projectName;
            String genPW = (int) (Math.random() * 10000000) + "";
            String text = createText(firstname,
            name,
            projectName,
            toEmail,
            genPW);

            Connection c = DBConnector.getConnection();
            try {
                c.setAutoCommit(false);
                PreparedStatement ps = null;
                String status = "MEM";
                if (!member) {
                    ps = c.prepareStatement("INSERT INTO project (name, start, end) VALUES (?,?,?)");
                    ps.setString(1, projectName);
                    ps.setDate(2, startProject);
                    ps.setDate(3, endProject);
                    ps.executeUpdate();
                    ps.close();
                    status = "PL";
                }

                ps = c.prepareStatement("INSERT INTO user (name, firstname, email, projectname, password, status)"
                        + " VALUES (?,?,?,?,?,?)");
                ps.setString(1, name);
                ps.setString(2, firstname);
                ps.setString(3, toEmail);
                ps.setString(4, projectName);
                ps.setString(5, genPW);
                ps.setString(6, status);
                ps.executeUpdate();
                ps.close();
                c.commit();
                sendMail(toEmail, subject, text);
            } catch (SQLException e) {
                c.rollback();
                e.printStackTrace();
            } finally {
                c.setAutoCommit(true);
                c.close();
            }
        } catch (MySQLException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void sendMail(String recipientsAddress, String subject, String text) {
        String smtpHost = "mail.gmx.net";
        String username = "htw-projektmanager@gmx.de";
        String password = "";
        try {
            password = new BufferedReader(new FileReader("/pw.txt")).readLine();
        } catch (IOException ex) {
            Logger.getLogger(Registrieren.class.getName()).log(Level.SEVERE, null, ex);
        }
        MailAuthenticator auth = new MailAuthenticator(username, password);

        Properties properties = System.getProperties();
        properties.put("mail.smtp.host", smtpHost);
        properties.put("mail.smtp.port", 587);
        properties.put("mail.smtp.auth", "true");

        Session session = Session.getInstance(properties, auth);
        try {
            Message msg = new MimeMessage(session);

            msg.setFrom(new InternetAddress(username));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(
                    recipientsAddress, false));

            msg.setSubject(subject);
            msg.setText(text);

            msg.setHeader("Test", "Test");
            msg.setSentDate(new java.util.Date());

            Transport.send(msg);
        } catch (MessagingException ex) {
            System.out.println("Fehler in sendMail(...)");
        }
    }

    private String createText(String firstname, String name, String projectname, String email, String pw) {
        StringBuilder sb = new StringBuilder(200);
        sb.append("Hallo ").append(firstname).append(" ").append(name).append(",\n\n")
                .append("die Anmeldung fuer das Projekt ").append(projectname).append(" war erfolgreich.\n\n")
                .append("Benutzername: ").append(email).append("\n")
                .append("Passwort: ").append(pw).append("\n\n")
                .append("Ihr Entwickler Team");
        return sb.toString();
    }

    private boolean validateProject(String projectName, String email) {
        try {
            if (projectName.trim().isEmpty() || email.trim().isEmpty()) {
                return false;
            }
            Connection c = DBConnector.getConnection();
            PreparedStatement ps1 = c.prepareStatement("SELECT * FROM project WHERE name = ?");
            ps1.setString(1, projectName);
            ResultSet rsPName = ps1.executeQuery();

            PreparedStatement ps2 = c.prepareStatement("SELECT * FROM user WHERE email = ?");
            ps2.setString(1, email);
            ResultSet rsEmail = ps2.executeQuery();
            if (rsPName.next() || rsEmail.next()) {
                ps1.close();
                ps2.close();
                return false;
            }
            c.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    private static Date getDate(String temp) {
        int day = -1;
        int month = -1;
        int year = -1;
        Date date = null;

            System.out.println("HALLO1");
            day = Integer.parseInt(temp.substring(0, temp.indexOf(".")));
            temp = temp.substring(temp.indexOf(".") + 1);
            month = Integer.parseInt(temp.substring(0, temp.indexOf("."))) - 1;
            temp = temp.substring(temp.indexOf(".") + 1);
            year = Integer.parseInt(temp);
            if (year >= 2000) {
                year -= 1900;
            }
            System.out.println("HALLO2");
            date = new Date(year, month, day);
            System.out.println(date.toString());

        return date;
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
