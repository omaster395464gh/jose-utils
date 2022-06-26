package de.pdv.demo.jose;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.jwk.RSAKey;
import org.json.JSONObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "decrypt", value = "/decrypt")
public class DecryptServlet extends HttpServlet {
    private String message;

    private static String  sEncMetaData = "";
    private static String  sEncKey = "";
    private static final Logger logger = Logger.getLogger(DecryptServlet.class.getName());

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


    public void processRequest (PrintWriter out)
    {
        out.println("<html lang=\"en\" data-theme=\"dark\">\n" +
                "<head>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                "    <meta name=\"description\" content=\"Servlet demo for decrypt json file with Nimbus JOSE+JWT library\">\n" +
                "    <link rel=\"stylesheet\" href=\"webjars/pico/css/pico.min.css\">\n" +
                "    <title>Decrypt json with Nimbus JOSE+JWT library</title>\n" +
                "</head>\n<body>\n" +
                "<main class=\"container\">");
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
        logger.log(Level.INFO,"doGet");
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        processRequest(response.getWriter());
        logger.log(Level.INFO,"Process complete");
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws IOException if a servlet-specific error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        //ServletContext context = getServletContext();
        logger.log(Level.INFO,"doPost");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // String contentType = request.getContentType();
        if ( (request.getParameter("privateKey") != null) && (request.getParameter("encodedString") != null) )
        {
            String privateKey = request.getParameter("privateKey");
            String encodedString = request.getParameter("encodedString");
            String sDecrypted;
            try {
                sDecrypted = decrypt(privateKey, encodedString);
                response.getWriter().write(sDecrypted);
            } catch (JOSEException e) {
                logger.log(Level.SEVERE,"Decryption failed, JOSEException %s ", Arrays.toString(e.getStackTrace()));
                response.sendError(500 ,"Decryption failed, JOSEException - see logs for details");
            } catch (ParseException e) {
                logger.log(Level.SEVERE,"Decryption failed, ParseException %s", Arrays.toString(e.getStackTrace()));
                response.sendError(500 ,"Decryption failed, ParseException - see logs for details");
            }
            // processRequest(response.getWriter());
        } else {
            response.sendError(422 ,"Missing parameter");
        }
        logger.log(Level.INFO,"Process complete");
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