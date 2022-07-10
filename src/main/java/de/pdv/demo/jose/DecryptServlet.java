package de.pdv.demo.jose;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.jwk.RSAKey;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "decrypt", value = "/decrypt")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, //   2MB
        maxFileSize = 1024 * 1024 * 20,      //  30MB
        maxRequestSize = 1024 * 1024 * 30)   //  50MB
public class DecryptServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(DecryptServlet.class.getName());
    private static final String SAVE_DIR = "uploadFiles";
    private static String sEncMetaData = "";
    private static String sEncKey = "";
    private String message;

    public void init() {
        message = "Decrypt demo!";
        ResourceBundle labels = ResourceBundle.getBundle("demo");
        sEncMetaData = labels.getString("data.enc");
        sEncKey = labels.getString("data.key");
    }

    public String decrypt(String keyStr, String encryptedStr) throws JOSEException, ParseException {
        RSAKey jwk = RSAKey.parse(keyStr);
        JWEObject jweObject = JWEObject.parse(encryptedStr);
        jweObject.decrypt(new RSADecrypter(jwk));
        return jweObject.getPayload().toString();
    }

    public void processRequest(PrintWriter out) {
        out.println("<html lang=\"en\" data-theme=\"dark\">\n" + "<head>\n" + "    <meta charset=\"utf-8\">\n" + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" + "    <meta name=\"description\" content=\"Servlet demo for decrypt json file with Nimbus JOSE+JWT library\">\n" + "    <link rel=\"stylesheet\" href=\"webjars/pico/css/pico.min.css\">\n" + "    <title>Decrypt json with Nimbus JOSE+JWT library</title>\n" + "</head>\n<body>\n" + "<main class=\"container\">");
        out.println("<h1>" + message + "</h1>");
        try {
            out.println("<pre>");
            String sDecrypted = decrypt(sEncKey, sEncMetaData);
            out.println("Decrypted:" + sDecrypted);
            out.println("</pre>");
            out.println("<pre>");
            out.println("Decrypted pretty:" + new JSONObject(sDecrypted).toString(4));
            out.println("</pre>");
        } catch (JOSEException | ParseException e) {
            throw new RuntimeException(e);
        }
        out.println("</main></body></html>");
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws IOException if a servlet-specific error occurs
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.log(Level.INFO, "doGet");
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        processRequest(response.getWriter());
        logger.log(Level.INFO, "Process complete");
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws IOException if a servlet-specific error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // gets absolute path of the web application
        String appPath = request.getServletContext().getRealPath("");
        // constructs path of the directory to save uploaded file
        String savePath = appPath + File.separator + SAVE_DIR;

        // creates the save directory if it does not exist
        File fileSaveDir = new File(savePath);
        if (!fileSaveDir.exists()) {
            if (!fileSaveDir.mkdir()) {
                logger.log(Level.WARNING, "mkdir failed");
            }
        }

        String privateKey = "";
        String encodedString = "";

        for (Part part : request.getParts()) {
            //String fileName = extractFileName(part);
            // refines the fileName in case it is an absolute path
            //fileName = new File(fileName).getName();

            if (part.getName().equalsIgnoreCase("privateKey")) {
                privateKey = IOUtils.toString(part.getInputStream(), StandardCharsets.UTF_8);
            }
            if (part.getName().equalsIgnoreCase("encodedString")) {
                encodedString = IOUtils.toString(part.getInputStream(), StandardCharsets.UTF_8);
            }
            // part.write(savePath + File.separator + fileName);
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // String contentType = request.getContentType();
        if (privateKey.length() == 0) {
            logger.log(Level.WARNING, "Missing parameter privateKey");
            response.sendError(422, "Missing parameter privateKey");
        } else if (encodedString.length() == 0) {
            logger.log(Level.WARNING, "Missing parameter encodedString");
            response.sendError(422, "Missing parameter encodedString");
        } else {
            String sDecrypted;
            try {
                sDecrypted = decrypt(privateKey, encodedString);
                response.getWriter().write(sDecrypted);
            } catch (JOSEException e) {
                logger.log(Level.SEVERE, "Decryption failed, JOSEException",e);
                response.sendError(500, "Decryption failed, JOSEException - see logs for details");
            } catch (ParseException e) {
                logger.log(Level.SEVERE, "Decryption failed, ParseException",e);
                response.sendError(500, "Decryption failed, ParseException - see logs for details");
            }
            logger.log(Level.INFO, "Process complete");
        }
    }

    /**
     * Demo servlet for Nimbus JOSE+JWT library
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Demo servlet for Nimbus JOSE+JWT library";
    }// </editor-fold>

    @Override
    public void destroy() {
    }
}