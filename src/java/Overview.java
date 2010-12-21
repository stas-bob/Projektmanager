/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import db.DBConnector;
import java.sql.*;
import exceptions.MySQLException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author tA88
 */
public class Overview extends HttpServlet {
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("application/xml");
        PrintWriter out = response.getWriter();
        try {
            Connection c = DBConnector.getConnection();
            int modulesCount = getModulesCount(c, request.getSession().getAttribute("projectname").toString());
            int doneCount = getModulesDoneCount(c, request.getSession().getAttribute("projectname").toString());
            int divWidth = 795;
            int progress = 0;
            if (modulesCount > 0) {
                progress = (divWidth*doneCount)/modulesCount;
            }
            GregorianCalendar projectEndDate = Modules.getDateFromString(request.getSession().getAttribute("endProject").toString());
            projectEndDate.set(GregorianCalendar.MONTH, (projectEndDate.get(GregorianCalendar.MONTH) + 11) % 12);
           // projectEndDate.set(GregorianCalendar.YEAR, (projectEndDate.get(GregorianCalendar.YEAR) - 1));

            long daysLeft = (projectEndDate.getTime().getTime() - new GregorianCalendar().getTime().getTime())/(1000*60*60*24);

            ArrayList<Time> hours = new ArrayList<Time>();
            ArrayList<String> names = new ArrayList<String>();
            ArrayList<Long> progresses = new ArrayList<Long>();
            int width = 100;
            fillTimeSpent(hours, names, progresses, width, request.getSession().getAttribute("projectname").toString(), c);

            String htmlOutput = "<div style=\"margin-top:20px;\">" + getProgressBar(100, divWidth, progress, "Fortschritt ")
                              + "</div>"
                              + "<br>"
                              + "<br>"
                              + "<div>"
                                + "Projektende ist am: " + projectEndDate.getTime().getDate() + "." + (projectEndDate.getTime().getMonth() + 1) + "." + (projectEndDate.getTime().getYear() + 1900)
                                + "<br>Es bleiben nur noch <span  style=\"font-weight:bold; color:" + (daysLeft < 50 ? "red" : "green") + "\">" + daysLeft + "</span> Tage."
                              + "</div>"
                              + "<br>"
                              + "<table cellpadding=\"10\" border=\"1\" style=\"border-collapse:collapse;\">"
                              + "<tr>"
                                + "<td align=\"center\" colspan=\"3\">Überblick der Arbeitszeiten</td>"
                              + "</tr>";
                                for (int i = 0; i < hours.size(); i++) {
                                    if (progresses.size() > 0) {
                                        hours.get(i).setHours(hours.get(i).getHours() - 1);
                                        htmlOutput +=  "<tr>"
                                                        + "<td>" + names.get(i) + "</td><td>" + hours.get(i) + "</td><td>" + getProgressBar(15, width, progresses.get(i), "") + "</td>"
                                                     + "</tr>";
                                    }
                                }
                              htmlOutput += "</table>";

            String xmlResponse = "<root><htmlSeite><![CDATA[" + htmlOutput + "]]></htmlSeite></root>";
            out.write(xmlResponse);
        } catch (SQLException ex) {
            Logger.getLogger(Overview.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MySQLException ex) {
            Logger.getLogger(Overview.class.getName()).log(Level.SEVERE, null, ex);
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

    private int getModulesCount(Connection c, String projectName) {
        try {
            PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM module WHERE projectname=?");
            ps.setString(1, projectName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Overview.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    private int getModulesDoneCount(Connection c, String projectName) {
        try {
            PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM module WHERE projectname=? AND status='closed'");
            ps.setString(1, projectName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Overview.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    public static String getProgressBar(int height, int width, long progress, String text) {
        return "<div style=\"border:1px dashed; width:" + width + "px; height:" + height + "px; position:relative; background-color: ##EEEEFF; margin-top:0px\">"
                  + "<div style=\"border:1px solid blue; width:" + progress + "px; height:100%; margin-top: -1px; margin-left: -1px; color:white; background-color: LightSteelBlue;\">"
                    + "<div style=\"margin-left:30%; margin-top:" + (height/2 - 10) + "px; position:absolute; text-shadow: 2px 2px 0 #AAAAAA; color:black\">" + text + (int)((float)progress*100/width) + "%</div>"
                  + "</div>"
              + "</div>";
    }

    private void fillTimeSpent(ArrayList<Time> hours, ArrayList<String> names, ArrayList<Long> progress, int width, String projectName, Connection c) {
        try {
            PreparedStatement ps = c.prepareStatement("SELECT name, id FROM `user` WHERE projectname=?");
            ps.setString(1, projectName);
            ResultSet rs = ps.executeQuery();
            long sumTimeSpent = 0;
            while (rs.next()) {
                PreparedStatement ps2 = c.prepareStatement("SELECT start, end FROM `time` WHERE user_id=?");
                ps2.setInt(1, rs.getInt(2));
                ResultSet rs2 = ps2.executeQuery();
                long timeSpent = 0L;
                while (rs2.next()) {
                    Time start = rs2.getTime(1);
                    Time end = rs2.getTime(2);
                    timeSpent += end.getTime() - start.getTime();

                }

                sumTimeSpent += timeSpent;
                
                hours.add(new Time(timeSpent));

                names.add(rs.getString(1));
            }
            for (int i = 0; i < hours.size(); i++) {
                if (hours.get(i).getTime() == -3600000 || sumTimeSpent == 0) {
                    progress.add(0L);
                } else {
                    progress.add(width*hours.get(i).getTime()/sumTimeSpent);
                }
            }
            ps.close();
        } catch (SQLException ex) {
            Logger.getLogger(Modules.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
