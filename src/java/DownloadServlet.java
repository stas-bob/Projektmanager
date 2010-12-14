 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.OutputStream;

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
        File file = getFileToDownload(request.getParameter("file"));
        prepareResponseFor(response, file);
        streamFileTo(response, file);
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