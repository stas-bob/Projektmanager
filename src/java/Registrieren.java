/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Properties;
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
            seas.setAttribute("email", request.getParameter("email"));
            seas.setAttribute("projektname", request.getParameter("projektname"));
            System.out.println("HALLO1");
            sendMail("smtp.googlemail.com", "htw.projektmanager", new BufferedReader(new FileReader("/pwEmail.txt")).readLine(), "htw.projektmanager@googlemail.com", "altmeyer.thomas@googlemail.com", "Test", "Test123");
            System.out.println("HALLO2");
            try {
                out.println("<html>");
                out.println("<head>");
                out.println("<title>Servlet Registrieren</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("<h1>Servlet Regestrieren at " + request.getContextPath() + "</h1>");
                out.println("</body>");
                out.println("</html>");
            } finally {
                out.close();
            }

    }

    public void sendMail(String smtpHost, String username, String password, String senderAddress, String recipientsAddress, String subject, String text) {
        MailAuthenticator auth = new MailAuthenticator(username, password);

        Properties properties = new Properties();

        // Den Properties wird die ServerAdresse hinzugefügt
        properties.put("mail.smtp.host", smtpHost);

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
