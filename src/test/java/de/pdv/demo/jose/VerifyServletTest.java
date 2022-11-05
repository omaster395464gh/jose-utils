package de.pdv.demo.jose;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jwt.*;
import lombok.extern.java.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;


@Log
class VerifyServletTest extends Mockito {

    static final String KEY_JWS = "key.jws";
    static final String JWS = "data.jws";
    static final String JWS_FAIL = "data.jws_fail";
    static final String MISSING_KEY = "Missing parameter privateKey";
    @Spy
    private VerifyServlet servlet;
    @Mock
    private ServletConfig servletConfig;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private PrintWriter printWriter;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void assertThatNoMethodHasBeenCalled() {
        when(servlet.getServletConfig()).thenReturn(servletConfig);
        verifyNoInteractions(servlet);
    }

    @Test
    void verifySet() throws ParseException {
        when(servlet.getServletConfig()).thenReturn(servletConfig);
        assertNotNull(servlet);
        log.info("demo jose verify test, preparing...");
        ResourceBundle labels = ResourceBundle.getBundle("demo");
        assertNotNull(labels);
        String sKeyId = labels.getString(KEY_JWS);
        assertNotNull(sKeyId);
        String sJWS = labels.getString(JWS);
        assertNotNull(sJWS);
        JWT jwt = JWTParser.parse(sJWS);
        assert (jwt instanceof SignedJWT);
        if (jwt instanceof SignedJWT) {
            SignedJWT jwsObject = (SignedJWT) jwt;
            InputStream is = getClass().getClassLoader().getResourceAsStream("jwks.json");
            assertNotNull(is);
            boolean bResult = servlet.verifySet(is, jwsObject, sKeyId);
            assertTrue(bResult, "verify failed");
            log.info("verify ok!");
        }
    }

    @Test
    void verifySetFailure() throws ParseException {
        when(servlet.getServletConfig()).thenReturn(servletConfig);
        assertNotNull(servlet);
        ResourceBundle labels = ResourceBundle.getBundle("demo");
        assertNotNull(labels);
        String sKeyId = labels.getString(KEY_JWS);
        assertNotNull(sKeyId);
        String sJWS = labels.getString(JWS_FAIL);
        assertNotNull(sJWS);
        JWT jwt = JWTParser.parse(sJWS);
        assert (jwt instanceof SignedJWT);
        if (jwt instanceof SignedJWT) {
            SignedJWT jwsObject = (SignedJWT) jwt;
            InputStream is = getClass().getClassLoader().getResourceAsStream("jwks.json");
            assertNotNull(is);
            boolean bResult = servlet.verifySet(is, jwsObject, sKeyId);
            assertFalse(bResult, "verify succeed but should fail");
        }
    }

    @Test
    void getServletInfo() {
        when(servlet.getServletConfig()).thenReturn(servletConfig);
        String s = servlet.getServletInfo();
        assertEquals("Servlet tools for Nimbus JOSE+JWT library", s);
    }
}
