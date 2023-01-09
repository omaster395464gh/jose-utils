package de.pdv.demo.jose;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSVerifier;
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
import org.json.JSONStringer;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Objects;
import java.util.ResourceBundle;

@Log
@WebServlet(name = "verify", value = "/verify")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, //   2MB
        maxFileSize = 1024 * 1024 * 10,      //  10MB
        maxRequestSize = 1024 * 1024 * 15)   //  15MB
public class VerifyServlet extends HandlerServlet {
    private static final ResourceBundle labels = ResourceBundle.getBundle("demo");
    private static final String JWS = labels.getString("data.jws");
    private static final String KEY_JWS = labels.getString("key.jws");

    private static final String VERIFIED_FALSE = "Verified: FALSE\n";
    private static final String VERIFIED_TRUE = "Verified: TRUE\n";
    private static final String VERIFIED_JSON = new JSONStringer().object().key("signature").value("verified").endObject().toString();


    public boolean verifySet(@NonNull InputStream jwkSetInputStream, @NonNull SignedJWT securityEventToken, @NonNull String keyId) {
        try {
            JWKSet jwkSet = JWKSet.load(jwkSetInputStream);
            JWK publicKey = jwkSet.getKeyByKeyId(keyId);
            if (publicKey == null) {
                log.severe("The key specified for signature verification does not match any public key.");
                return false;
            }

            if (!publicKey.getAlgorithm().toString().equalsIgnoreCase(JWSAlgorithm.PS512.toString())) {
                log.severe("The key specified for signature verification doesn't use/specify PS512 as algorithm.");
                return false;
            }
            JWSVerifier jwsVerifier = new RSASSAVerifier(publicKey.toRSAKey());
            return securityEventToken.verify(jwsVerifier);
        } catch (ParseException | IOException | JOSEException e) {
            log.severe(String.format("verify JWT failed, %s - %s", e.getClass().getName(), e.getMessage()));
            return false;
        }
    }

    public void processRequest(@NonNull PrintWriter out) {
        out.println("<html lang=\"en\" data-theme=\"dark\">\n" + "<head>\n" + "    <meta charset=\"utf-8\">\n" + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" + "    <meta name=\"description\" content=\"Servlet for verify sets with Nimbus JOSE+JWT library\">\n" + "    <link rel=\"stylesheet\" href=\"webjars/pico/css/pico.min.css\">\n" + "    <title>Verify sets with Nimbus JOSE+JWT library</title>\n" + "</head>\n<body>\n" + "<main class=\"container\">");
        String message = "Verify demo!";
        out.println("<h1>" + message + "</h1>");
        out.println("<a href=\"index.jsp\">Back</a>");
        out.println("<pre>");
        try {
            JWT jwt = JWTParser.parse(JWS);
            if (jwt instanceof SignedJWT) {
                processRequestForSignedJWT(out, jwt);
            }
        } catch (ParseException e) {
            log.severe(String.format("parse JWT failed, ParseException - %s", e.getMessage()));
            out.println(VERIFIED_FALSE);
        }
        out.println("</pre>");
        out.println("</main></body></html>");
    }

    public void processRequestForSignedJWT(@NonNull PrintWriter out, @NonNull JWT jwt) {
        SignedJWT jwsObject = (SignedJWT) jwt;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("jwks.json")) {
            if (verifySet(Objects.requireNonNull(is), jwsObject, KEY_JWS)) {
                out.println(VERIFIED_TRUE);
                out.println("Header (pretty):\n" + new JSONObject(jwsObject.getHeader().toJSONObject()).toString(4));
                out.println("\nPayload (pretty):\n" + new JSONObject(jwsObject.getPayload().toJSONObject()).toString(4));
                out.println("\nSignature:\n" + jwsObject.getSignature().toString());
            } else {
                out.println(VERIFIED_FALSE);
            }
        } catch (IOException e) {
            log.severe(String.format("parse JWT failed, IOException - %s", e.getMessage()));
            out.println(VERIFIED_FALSE);
        }
    }

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
        response.setContentType("application/json");

        try {
            if (jwkSet == null) {
                handleWarning(response, 422, "Missing parameter jwkSet");
                return;
            }
            if (sEventToken.length() == 0) {
                handleWarning(response, 422, "Missing parameter securityEventToken");
                return;
            }
            if (keyId.length() == 0) {
                handleWarning(response, 422, "Missing parameter keyId");
                return;
            }
            JWT jwt = JWTParser.parse(sEventToken);
            if (jwt instanceof SignedJWT) {
                SignedJWT jwsObject = (SignedJWT) jwt;
                boolean bResult = verifySet(jwkSet, jwsObject, keyId);
                processPostForSignedJWT(response, bResult);
                if (!bResult) return;
            }
            log.info("Process complete");
        } catch (ParseException e) {
            log.severe(String.format("doPost failed with ParseException %s", e.getMessage()));
            handleError(response, HttpServletResponse.SC_BAD_REQUEST, "Verification failed");
        }
    }

    protected void processPostForSignedJWT(@NonNull HttpServletResponse response, @NonNull boolean bResult) {
        if (bResult) {
            try {
                response.getWriter().println(VERIFIED_JSON);
            } catch (IOException e) {
                log.severe(String.format("getWriter failed with IOException %s", e.getMessage()));
            }
        } else {
            handleWarning(response, HttpServletResponse.SC_BAD_REQUEST, "Verification returned false - see logs for details");
        }
    }


}