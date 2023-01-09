package de.pdv.demo.jose;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.NonNull;
import lombok.extern.java.Log;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

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
@WebServlet(name = "sign", value = "/sign")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, //   2MB
        maxFileSize = 1024 * 1024 * 10,      //  10MB
        maxRequestSize = 1024 * 1024 * 15)   //  15MB
public class SignServlet extends HandlerServlet {
    private static final ResourceBundle labels = ResourceBundle.getBundle("demo");
    private static final String SIGN_HEADER = labels.getString("sign.header");
    private static final String SIGN_PAYLOAD = labels.getString("sign.payload");

    // Source: https://git.fitko.de/fit-connect/examples/-/blob/main/java/crypto/src/main/java/GenerateSignedToken.java
    // https://docs.fitko.de/fit-connect/docs/getting-started/event-log/set-validation
    public SignedJWT signSet(@NonNull InputStream jwkSetInputStream, @NonNull String sHeader, @NonNull String sPayload) {

        try {
            JWKSet localKeys = JWKSet.load(Objects.requireNonNull(jwkSetInputStream));
            JWSHeader header = JWSHeader.parse(sHeader);
            JWK key = localKeys.getKeyByKeyId(header.getKeyID());
            if (key != null) {
                JWSSigner signer = new RSASSASigner(key.toRSAKey());
                JWTClaimsSet claimsSet = JWTClaimsSet.parse(sPayload);
                SignedJWT signedJWT = new SignedJWT(header, claimsSet);

                signedJWT.sign(signer);
                return signedJWT;
            } else {
                log.warning("parse RSAKey for signing failed, missing key in getKeyByKeyId");
            }
        } catch (IOException | ParseException | JOSEException e) {
            log.severe(String.format("parse RSAKey for signing failed, %s - %s", e.getClass().getName(), e.getMessage()));
        }
        return null;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        log.info("doGet");
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("jwks2.json")) {
            out = response.getWriter();
            out.println("<html lang=\"en\" data-theme=\"dark\">\n" + "<head>\n" + "    <meta charset=\"utf-8\">\n" + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" + "    <meta name=\"description\" content=\"Servlet for sign sets with Nimbus JOSE+JWT library\">\n" + "    <link rel=\"stylesheet\" href=\"webjars/pico/css/pico.min.css\">\n" + "    <title>Sign sets with Nimbus JOSE+JWT library</title>\n" + "</head>\n<body>\n" + "<main class=\"container\">");
            String message = "Sign demo!";
            out.println("<h1>" + message + "</h1>");
            out.println("<a href=\"index.jsp\">Back</a>");
            out.println("<pre>");
            SignedJWT mySignedJWT = signSet(Objects.requireNonNull(is), SIGN_HEADER, SIGN_PAYLOAD);
            if (mySignedJWT != null) {
                out.println("\nSigned JWT:\n" + mySignedJWT.serialize());
                out.println("\nHeader (pretty):\n" + new JSONObject(mySignedJWT.getHeader().toJSONObject()).toString(4));
                out.println("\nPayload (pretty):\n" + new JSONObject(mySignedJWT.getPayload().toJSONObject()).toString(4));
                out.println("\nSignature:\n" + mySignedJWT.getSignature().toString());
            }
            out.println("</pre>");
            out.println("</main></body></html>");
        } catch (IOException e) {
            log.severe(String.format("getWriter failed, IOException - %s", e.getMessage()));
        } catch (JSONException e) {
            log.severe(String.format("pretty print of JSONObject failed, JSONException - %s", e.getMessage()));
        }
        log.info("Process complete");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        String header = "";
        String payload = "";
        InputStream jwkSet = null;
        try {
            for (Part part : request.getParts()) {
                if (part.getName().equalsIgnoreCase("header")) {
                    header = IOUtils.toString(part.getInputStream(), StandardCharsets.UTF_8);
                }
                if (part.getName().equalsIgnoreCase("payload")) {
                    payload = IOUtils.toString(part.getInputStream(), StandardCharsets.UTF_8);
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
            if (header.length() == 0) {
                handleWarning(response, 422, "Missing parameter header");
                return;
            }
            if (payload.length() == 0) {
                handleWarning(response, 422, "Missing parameter payload");
                return;
            }
            SignedJWT mySignedJWT = signSet(jwkSet, header, payload);
            if (mySignedJWT != null) response.getWriter().println(mySignedJWT.serialize());
            else handleWarning(response, HttpServletResponse.SC_BAD_REQUEST, "Sign failed - see logs for details");

            log.info("Process complete");
        } catch (IOException e) {
            log.severe(String.format("doPost failed with IOException %s", e.getMessage()));
            handleError(response, HttpServletResponse.SC_BAD_REQUEST, "Verification failed");
        }
    }
}