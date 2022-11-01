package de.pdv.demo.jose;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
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
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Base64;
import java.util.ResourceBundle;

import static com.nimbusds.jose.JWSAlgorithm.PS512;

@Log
@WebServlet(name = "verify", value = "/verify")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, //   2MB
        maxFileSize = 1024 * 1024 * 10,      //  10MB
        maxRequestSize = 1024 * 1024 * 15)   //  15MB
public class VerifyServlet extends HttpServlet {
    private static final ResourceBundle labels = ResourceBundle.getBundle("demo");
    private static final String JWS = labels.getString("data.jws");
    private static final String KEY_JWS = labels.getString("key.jws");


    public boolean verifySet(@NonNull InputStream jwkSetInputStream, @NonNull SignedJWT securityEventToken, @NonNull String keyId) {
        try {
            JWKSet jwkSet = JWKSet.load(jwkSetInputStream);
            JWK publicKey = jwkSet.getKeyByKeyId(keyId);
            if (!publicKey.getAlgorithm().toString().equalsIgnoreCase(JWSAlgorithm.PS512.toString())) {
                log.severe("The key specified for signature verification doesn't use/specify PS512 as algorithm.");
                return false;
            }
            JWSVerifier jwsVerifier = new RSASSAVerifier(publicKey.toRSAKey());
            return securityEventToken.verify(jwsVerifier);
        } catch (ParseException e) {
            log.severe(String.format("verify JWT failed, ParseException - %s", e.getMessage()));
            return false;
        } catch (IOException e) {
            log.severe(String.format("verify JWT failed, IOException - %s", e.getMessage()));
            return false;
        } catch (JOSEException e) {
            log.severe(String.format("verify JWT failed, JOSEException - %s", e.getMessage()));
            return false;
        }
    }


    public void processRequest(PrintWriter out) {
        out.println("<html lang=\"en\" data-theme=\"dark\">\n" + "<head>\n" + "    <meta charset=\"utf-8\">\n" + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" + "    <meta name=\"description\" content=\"Servlet demo for verify sets with Nimbus JOSE+JWT library\">\n" + "    <link rel=\"stylesheet\" href=\"webjars/pico/css/pico.min.css\">\n" + "    <title>Verify sets with Nimbus JOSE+JWT library</title>\n" + "</head>\n<body>\n" + "<main class=\"container\">");
        String message = "Verify demo!";
        out.println("<h1>" + message + "</h1>");
        out.println("<pre>");
        try {
            JWT jwt = JWTParser.parse(JWS);
            if (jwt instanceof SignedJWT) {
                SignedJWT jwsObject = (SignedJWT) jwt;
                InputStream is = getClass().getClassLoader().getResourceAsStream("jwks.json");
                if (verifySet(is, jwsObject, KEY_JWS)) {
                    out.println("Verified: TRUE\n");
                    out.println("Header (pretty):\n" + new JSONObject(jwsObject.getHeader().toJSONObject()).toString(4));
                    out.println("\nPayload (pretty):\n" + new JSONObject(jwsObject.getPayload().toJSONObject()).toString(4));
                    out.println("\nSignature:\n" + jwsObject.getSignature().toString());
                } else
                    out.println("Verified: FALSE");
            }
        } catch (ParseException e) {
            log.severe(String.format("parse JWT failed, ParseException - %s", e.getMessage()));
            out.println("Verified: FALSE");
        }
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
        String sEventToken = "";
        String keyId = "";
        InputStream jwkSet = null;
        try {
            for (Part part : request.getParts()) {
                if (part.getName().equalsIgnoreCase("securityEventToken")) {
                    sEventToken = IOUtils.toString(part.getInputStream(), StandardCharsets.UTF_8);
                }
                if (part.getName().equalsIgnoreCase("keyId")) {
                    keyId = IOUtils.toString(part.getInputStream(), StandardCharsets.UTF_8);
                }
                if (part.getName().equalsIgnoreCase("jwkSet")) {
                    jwkSet = part.getInputStream();
                }

            }
        } catch (ServletException | IOException e) {
            log.severe(String.format("Exception while getParts %s", e.getMessage()));
        }
        response.setCharacterEncoding("UTF-8");
        response.setContentType("");

        try {
            if (jwkSet == null) {
                log.warning("Missing parameter jwkSet");
                response.sendError(422, "Missing parameter jwkSet");
                return;
            }
            if (sEventToken.length() == 0) {
                log.warning("Missing parameter securityEventToken");
                response.sendError(422, "Missing parameter securityEventToken");
                return;
            }
            if (keyId.length() == 0) {
                log.warning("Missing parameter keyId");
                response.sendError(422, "Missing parameter keyId");
                return;
            }
            JWT jwt = JWTParser.parse(sEventToken);
            InputStream is = getClass().getClassLoader().getResourceAsStream("jwks.json");
            if (jwt instanceof SignedJWT) {
                SignedJWT jwsObject = (SignedJWT) jwt;
                if (!verifySet(is, jwsObject, keyId)) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Verify failed - see logs for details");
                    return;
                }
            }
            log.info("Process complete");
        } catch (IOException | ParseException e) {
            log.severe(String.format("doPost failed with %s", e.getMessage()));
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

}