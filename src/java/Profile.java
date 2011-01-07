/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author tA88
 */
public class Profile extends HttpServlet {
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    public void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();
        HttpSession seas = request.getSession();
        
        try {
            if (request.getCookies() == null) {
                out.write("<root><htmlSeite><![CDATA[Aktivieren Sie bitte <b>Cookies</b> in ihrem Webbrowser.]]></htmlSeite></root>");
                return;
            }
            StringBuilder htmlOutput = new StringBuilder(300);
            htmlOutput.append("<div id=profile>")
                .append("<table cellspacing=\"5\">")
                .append("<colgroup width=\"200\" />")
                .append("<tr align=\"left\">")
                .append("<td>Name:</td>")
                .append("<td>").append(seas.getAttribute("name")).append("</td>")
                .append("</tr>")
                .append("<tr align=\"left\">")
                .append("<td>Vorname:</td>")
                .append("<td>").append(seas.getAttribute("firstname")).append("</td>")
                .append("</tr>")
                .append("<tr align=\"left\">")
                .append("<td>E-Mail:</td>")
                .append("<td>").append(seas.getAttribute("user")).append("</td>")
                .append("</tr><tr><td>&#160;</td></tr>")
                .append("<tr align=\"left\">")
                .append("<td>Projekt:</td>")
                .append("<td>").append(seas.getAttribute("projectname")).append("</td>")
                .append("</tr>")
                .append("<tr align=\"left\">")
                .append("<td>Status:</td>");
            if (seas.getAttribute("status").equals("PL")) {
                htmlOutput.append("<td>Projektleiter</td>");
            } else if (seas.getAttribute("status").equals("CPL")) {
                htmlOutput.append("<td>Co- Projektleiter</td>");
            } else {
                htmlOutput.append("<td>Mitglied</td>");
            }
            htmlOutput.append("</tr>")
                .append("</table>")
                .append("<br><br>")
                .append("<div id=\"changePasswordText\">Passwort &auml;ndern:</div>")
                .append(MainServlet.changePasswordArea())
                .append("<table>")
                .append("<tr>")
                .append("<td><input type=\"button\" value=\"&Auml;ndern\" onclick=\"transformPassword();changePassword()\" /></td>")
                .append("</tr>")
                .append("</table>")
                .append("</div>")
                .append("<br><br>")
                .append("<input type=\"submit\" value=\"Account l&ouml;schen\" onclick=\"deleteAccount()\" />");

            String xmlResponse = "<root><htmlSeite><![CDATA[" + htmlOutput.toString() + "]]></htmlSeite></root>";
            out.write(xmlResponse);
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
