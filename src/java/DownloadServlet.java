
import db.DBConnector;
import exceptions.MySQLException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

 /**
  * Servlet Class
  *
  * @web.servlet name="downloadServlet" display-name="Simple DownloadServlet"
  *           description="Simple Servlet for Streaming Files to the Clients
  *           Browser"
  * @web.servlet-mapping url-pattern="/download"
  */
 public class DownloadServlet extends HttpServlet {
    static final int BUFFER_SIZE = 16384;

    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        Connection c = null;
        try {
            c = DBConnector.getConnection();
            int user_id = Integer.parseInt(request.getParameter("file").toString().substring(0, request.getParameter("file").toString().indexOf(".")));
            downloadTimes(user_id, c);
        } catch (MySQLException ex) {
            ex.printStackTrace();
        } catch (SQLException ex) {
            Logger.getLogger(Times.class.getName()).log(Level.SEVERE, null, ex);
        }
        File file = getFileToDownload(request.getParameter("file"));
        prepareResponseFor(response, file);
        streamFileTo(response, file);

    }

    private static void downloadTimes(int user_id, Connection c) {
        StringBuilder sb = new StringBuilder(1000);
        try {
            Time start;
            Time end;
            Time duration;
            int hour;
            int minute;
            int totalHour = 0;
            int totalMinute = 0;

            sb.append("Datum;Start;Ende;Dauer;Modul;Beschreibung\n");
            PreparedStatement ps = c.prepareStatement("SELECT date, start, end, modulname, description FROM time WHERE user_id = ? ORDER BY date DESC");
            ps.setInt(1, user_id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                start = rs.getTime("start");
                end = rs.getTime("end");
                hour = end.getHours() - start.getHours();
                minute = end.getMinutes() - start.getMinutes();
                totalHour = totalHour + hour;
                totalMinute = totalMinute + minute;
                duration = new Time(hour, minute, 0);
                sb.append(rs.getDate("date")).append(";")
                        .append(start).append(";")
                        .append(end).append(";")
                        .append(duration).append(";")
                        .append(rs.getString("modulname")).append(";")
                        .append(rs.getString("description")).append(";\n");
            }
            sb.append("\nGesamt:;;").append(new Time(totalHour, totalMinute, 0)).append(";");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }


        File ausgabedatei;
        FileWriter fw;
        BufferedWriter bw;
        try {
            ausgabedatei = new File(user_id + ".csv");
            fw = new FileWriter(ausgabedatei);
            bw = new BufferedWriter(fw);
            bw.write(sb.toString());
            bw.close();
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            System.out.println("Aufruf mit: java SchreibeDatei name");
            System.out.println("erzeugt eine Datei name.html");
        } catch (IOException ioe) {
            System.out.println("Habe gefangen: "+ioe);
        }
    }
        
    private File getFileToDownload(String file) {
        return new File(file);
    }

    private void streamFileTo(HttpServletResponse response, File file)
            throws IOException, FileNotFoundException {
        OutputStream os = response.getOutputStream();
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead = 0;
        while ((bytesRead = fis.read(buffer)) > 0) {
            os.write(buffer, 0, bytesRead);
        }
        os.flush();
        fis.close();
    }

    private void prepareResponseFor(HttpServletResponse response, File file) {
        StringBuilder type = new StringBuilder("attachment; filename=");
        type.append(file.getName());
        response.setContentLength((int) file.length());
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", type.toString());
    }
 }