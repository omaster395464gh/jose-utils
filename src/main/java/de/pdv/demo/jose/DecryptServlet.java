package de.pdv.demo.jose;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.jwk.RSAKey;

import java.io.*;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

@WebServlet(name = "decrypt", value = "/decrypt")
public class DecryptServlet extends HttpServlet {
    private String message;
    private static final Logger logger = Logger.getLogger(DecryptServlet.class.getName());


    public void init() {
        message = "Decrypt demo!";
    }

    public String encrypt() throws JOSEException {
        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.HS256),
        new Payload(message));
        byte[] sharedKey = new byte[32];
        new SecureRandom().nextBytes(sharedKey);
       // Apply the HMAC to the JWS object
        jwsObject.sign(new MACSigner(sharedKey));
        return jwsObject.serialize();
    }

    public String decrypt(String keyStr, String encryptedStr) throws JOSEException, ParseException {

        //String keyStr = new String(existingPrivateKey.readAllBytes());
        RSAKey jwk = RSAKey.parse(keyStr);
        //String encryptedStr = new String(jweString.readAllBytes());
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
            out.println("Data:" + message);
            String sEncrypted = encrypt();
            out.println("Encrypted:" + sEncrypted);
            //String sDecrypted = decrypt("key",sEncrypted);
            //out.println("Decrypted:" + sDecrypted);
            out.println("</pre>");
        } catch (JOSEException e) {
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
        response.setContentType("text/html");
        processRequest(response.getWriter());
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