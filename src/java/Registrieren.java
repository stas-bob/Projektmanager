/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import db.DBConnector;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
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
import javax.servlet.http.HttpSession;
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
            HttpSession seas = request.getSession();
            seas.setAttribute("projektname", request.getParameter("projektname"));
            seas.setAttribute("name", request.getParameter("name"));
            seas.setAttribute("vorname", request.getParameter("vorname"));
            seas.setAttribute("email", request.getParameter("email"));

            String smtp = "mail.gmx.net";
            String fromEmail = "htw-projektmanager@gmx.de";
            String pw = new BufferedReader(new FileReader("/pwEmail.txt")).readLine();
            String toEmail = seas.getAttribute("email").toString();
            String subject = "Anmeldung fuer das Projekt " + seas.getAttribute("projektname").toString();
            String genPW = (int)(Math.random()*10000000) + "";
            String text = createText(seas, genPW);


            if(validateProject(seas.getAttribute("projektname").toString(), seas.getAttribute("email").toString())) {
               activate(seas.getAttribute("name").toString(),
                       seas.getAttribute("vorname").toString(),
                       seas.getAttribute("email").toString(),
                       seas.getAttribute("projektname").toString(),
                       genPW);
                sendMail(smtp, fromEmail, pw, fromEmail, toEmail, subject, text);
                out.write(" backToLogin ");
            } else {
                out.write("<font color=\"#990000\">Die Eingabe ist nicht zufriedenstellend</font>");
            }
            out.close();

    }
    
    public void sendMail(String smtpHost, String username, String password, String senderAddress, String recipientsAddress, String subject, String text) {
        MailAuthenticator auth = new MailAuthenticator(username, password);

        Properties properties = new Properties();

        // Den Properties wird die ServerAdresse hinzugefügt
        properties.put("mail.smtp.host", smtpHost);
        properties.put("mail.smtp.port", 587);

        // !!Wichtig!! Falls der SMTP-Server eine Authentifizierung
        // verlangt
        // muss an dieser Stelle die Property auf "true" gesetzt
        // werden
        properties.put("mail.smtp.auth", "true");

        // Hier wird mit den Properties und dem implements Contructor
        // erzeugten
        // MailAuthenticator eine Session erzeugt
        Session session = Session.getInstance(properties, auth);

        try {
            // Eine neue Message erzeugen
            Message msg = new MimeMessage(session);

            // Hier werden die Absender- und Empfängeradressen gesetzt
            msg.setFrom(new InternetAddress(senderAddress));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(
                    recipientsAddress, false));

            // Der Betreff und Body der Message werden gesetzt
            msg.setSubject(subject);
            msg.setText(text);

            // Hier lassen sich HEADER-Informationen hinzufügen
            msg.setHeader("Test", "Test");
            msg.setSentDate(new Date());

            // Zum Schluss wird die Mail natürlich noch verschickt
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

    private String createText(HttpSession seas, String pw) {
        StringBuilder sb = new StringBuilder(100);
        sb.append("Hallo ").append(seas.getAttribute("vorname").toString()).append(" ").append(seas.getAttribute("name").toString()).append(",\n\n")
          .append("die Anmeldung fuer das Projekt ").append(seas.getAttribute("projektname").toString()).append(" war erfolgreich.\n\n")
          .append("Ihr Passwort ist: ").append(pw).append("\n\n")
          .append("Ihr Entwickler Team");
        return sb.toString();
    }

    private void activate(String name, String surename, String email, String projectName, String password) {
        try {
            DBConnector.getConnection().createStatement().executeUpdate("insert into `Projekt` (Name) values ('" + projectName + "');");
            DBConnector.getConnection().createStatement().executeUpdate("insert into `Benutzer` (Name, Vorname, Email, projektname, passwort, status) values ('" + name + "','" + surename + "','" + email + "','" + projectName + "','" + password + "','PL');");
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.getLogger(Registrieren.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean validateProject(String projectName, String email) {
        try {
            if (projectName.trim().isEmpty() || email.trim().isEmpty()) return false;
            ResultSet rsPName = DBConnector.getConnection().createStatement().executeQuery("select * from `Projekt` where `Name`='" + projectName + "'");
            ResultSet rsEmail = DBConnector.getConnection().createStatement().executeQuery("select * from `Benutzer` where `Email`='" + email + "'");
            if ( rsPName.next() || rsEmail.next()) return false;
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
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(this.user, this.password);
    }
}
