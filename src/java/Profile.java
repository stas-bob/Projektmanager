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
        response.setContentType("application/xml");
        PrintWriter out = response.getWriter();
        HttpSession seas = request.getSession();
        
        try {
            StringBuilder htmlOutput = new StringBuilder(300);
            htmlOutput.append("<div id=profile>")
            .append("<table cellspacing=\"5\">")
            .append("<colgroup width=\"200\" />")
            .append("<tr align=\"left\">")
            .append("<th>Name:</th>")
            .append("<th>").append(seas.getAttribute("name")).append("</th>")
            .append("</tr>")
            .append("<tr align=\"left\">")
            .append("<th>Vorname:</th>")
            .append("<th>").append(seas.getAttribute("firstname")).append("</th>")
            .append("</tr>")
            .append("<tr align=\"left\">")
            .append("<th>E-Mail:</th>")
            .append("<th>").append(seas.getAttribute("user")).append("</th>")
            .append("</tr><tr><th>&#160;</th></tr>")
            .append("<tr align=\"left\">")
            .append("<th>Projekt:</th>")
            .append("<th>").append(seas.getAttribute("projectname")).append("</th>")
            .append("</tr>")
            .append("<tr align=\"left\">")
            .append("<th>Status:</th>")
            .append("<th>").append(seas.getAttribute("status")).append("</th>")
            .append("</tr>")
            .append("</table>")
            .append("<br><br>")
            .append("<form action=\"/Projektmanager/ChangePassword?firstLogin=0\" method=\"post\"")
            .append("<input type=\"submit\" value=\"Kennwort &auml;ndern\" />")
            .append("</form>")
            .append("</div>");
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
