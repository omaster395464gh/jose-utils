package de.pdv.demo.jose;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.NonNull;
import lombok.extern.java.Log;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Base64;
import java.util.ResourceBundle;

@Log
@WebServlet(name = "decrypt", value = "/decrypt")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, //   2MB
        maxFileSize = 1024 * 1024 * 50,      //  50MB
        maxRequestSize = 1024 * 1024 * 60)   //  60MB
public class DecryptServlet extends HttpServlet {
    private static final ResourceBundle labels = ResourceBundle.getBundle("demo");
    private static final String KEY = labels.getString("data.key");
    private static final String METADATA = labels.getString("data.enc");

    public Payload decryptPayload(@NonNull String keyStr, @NonNull String encryptedStr) {
        RSAKey jwk;
        try {
            jwk = RSAKey.parse(keyStr);
        } catch (ParseException e) {
            log.severe(String.format("parse RSAKey failed, ParseException - %s", e.getMessage()));
            return null;
        }
        JWEObject jweObject;
        try {
            jweObject = JWEObject.parse(encryptedStr);
        } catch (ParseException e) {
            log.severe(String.format("parse JWEObject failed, ParseException - %s", e.getMessage()));
            return null;
        }
        try {
            jweObject.decrypt(new RSADecrypter(jwk));
        } catch (JOSEException e) {
            log.severe(String.format("decrypt failed, JOSEException - %s", e.getMessage()));
            return null;
        }
        return jweObject.getPayload();
    }

    public void processRequest(PrintWriter out) {
        out.println("<html lang=\"en\" data-theme=\"dark\">\n" + "<head>\n" + "    <meta charset=\"utf-8\">\n" + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" + "    <meta name=\"description\" content=\"Servlet for decrypt json file with Nimbus JOSE+JWT library\">\n" + "    <link rel=\"stylesheet\" href=\"webjars/pico/css/pico.min.css\">\n" + "    <title>Decrypt json with Nimbus JOSE+JWT library</title>\n" + "</head>\n<body>\n" + "<main class=\"container\">");
        String message = "Decrypt demo!";
        out.println("<h1>" + message + "</h1>");
        out.println("<pre>");
        String sDecrypted = decryptPayload(KEY, METADATA).toString();
        out.println("Decrypted:" + sDecrypted);
        out.println("</pre>");
        out.println("<pre>");
        out.println("Decrypted pretty:" + new JSONObject(sDecrypted).toString(4));
        out.println("</pre>");
        out.println("</main></body></html>");
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        log.info("doGet");
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        try {
            processRequest(response.getWriter());
        } catch (IOException e) {
            log.severe(String.format("getWriter failed with IOException %s", e.getMessage()));
            return;
        }
        log.info("Process complete");
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        String privateKey = "";
        String encodedString = "";
        String resultAsBase64 = "";
        try {
            for (Part part : request.getParts()) {
                if (part.getName().equalsIgnoreCase("privateKey")) {
                    privateKey = IOUtils.toString(part.getInputStream(), StandardCharsets.UTF_8);
                }
                if (part.getName().equalsIgnoreCase("encodedString")) {
                    encodedString = IOUtils.toString(part.getInputStream(), StandardCharsets.UTF_8);
                }
                if (part.getName().equalsIgnoreCase("resultAsBase64")) {
                    resultAsBase64 = IOUtils.toString(part.getInputStream(), StandardCharsets.UTF_8);
                }
            }
        } catch (ServletException | IOException e) {
            log.severe(String.format("Exception while getParts %s", e.getMessage()));
        }

        response.setHeader("Content-Disposition", "attachment; filename=\"result.txt\"");
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("UTF-8");

        try {
            if (privateKey.length() == 0) {
                log.warning("Missing parameter privateKey");
                response.sendError(422, "Missing parameter privateKey");
                return;
            }
            if (encodedString.length() == 0) {
                log.warning("Missing parameter encodedString");
                response.sendError(422, "Missing parameter encodedString");
                return;
            }
            Payload pDecrypted = decryptPayload(privateKey, encodedString);
            if (pDecrypted == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Decryption failed - see logs for details");
                return;
            }
            // start processing
            if (resultAsBase64.equalsIgnoreCase("on")) {
                response.getWriter().write(Base64.getEncoder().encodeToString(pDecrypted.toBytes()));
            } else {
                response.getOutputStream().write(pDecrypted.toBytes());
            }
            log.info("Process complete");
        } catch (IOException e) {
            log.severe(String.format("doPost failed with IOException %s", e.getMessage()));
        }
    }

    /**
     * Servlet for Nimbus JOSE+JWT library
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Servlet tools for Nimbus JOSE+JWT library";
    }// </editor-fold>

}