package de.pdv.demo.jose;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.java.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;


@Log
class VerifyServletTest extends Mockito {

    static final String KEY_JWS = "key.jws";
    static final String JWS = "data.jws";
    static final String JWS_FAIL = "data.jws_fail";
    static final String MISSING_KEY = "Missing parameter jwkSet";
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
        SignedJWT jwsObject = (SignedJWT) jwt;
        InputStream is = getClass().getClassLoader().getResourceAsStream("jwks.json");
        assertNotNull(is);
        boolean bResult = servlet.verifySet(is, jwsObject, sKeyId);
        assertTrue(bResult, "verify failed");
        log.info("verify ok!");
    }

    @Test
    void verifySetFailure() throws ParseException, IOException {
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
        SignedJWT jwsObject = (SignedJWT) jwt;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("jwks.json")) {
            assertNotNull(is);
            assertFalse(servlet.verifySet(is, jwsObject, "wrongKey"), "verify succeed but should fail because of wrong key");
        }
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("jwks.json")) {
            assertNotNull(is);
            assertFalse(servlet.verifySet(is, jwsObject, sKeyId), "verify succeed but should fail because if wrong JWS");
        }
        try (InputStream is = new ByteArrayInputStream(labels.getString("jwks.pub_fail").getBytes())) {
            assertNotNull(is);
            assertFalse(servlet.verifySet(is, jwsObject, sKeyId), "verify succeed but should fail because of wrong algorithm");
        }
        try (InputStream is = new ByteArrayInputStream(("not json! wrong public key").getBytes())) {
            assertNotNull(is);
            assertFalse(servlet.verifySet(is, jwsObject, sKeyId), "verify succeed but should fail because of wrong public key format");
        }
    }

    @Test
    void testHandleWarning() throws IOException {
        when(servlet.getServletConfig()).thenReturn(servletConfig);
        when(response.getWriter()).thenReturn(printWriter);
        assertNotNull(servlet);
        assertNotNull(response);
        servlet.handleWarning(response, 422, "Warning");
        verify(response, atLeastOnce()).sendError(422, "Warning");
        when(response.getWriter()).thenThrow(IOException.class);
        servlet.handleWarning(response, 422, "Warning");
    }
    @Test
    void testHandleError() throws IOException {
        when(servlet.getServletConfig()).thenReturn(servletConfig);
        when(response.getWriter()).thenReturn(printWriter);
        assertNotNull(servlet);
        assertNotNull(response);
        servlet.handleError(response, 422, "Error");
        verify(response, atLeastOnce()).sendError(422, "Error");
        when(response.getWriter()).thenThrow(IOException.class);
        servlet.handleError(response, 422, "Error");
    }

    @Test
    void testProcessPostForSignedJWT() throws IOException {
        when(servlet.getServletConfig()).thenReturn(servletConfig);
        when(response.getWriter()).thenReturn(printWriter);
        assertNotNull(servlet);
        assertNotNull(response);
        servlet.processPostForSignedJWT(response, false);
        servlet.processPostForSignedJWT(response, true);
        when(response.getWriter()).thenThrow(IOException.class);
        servlet.processPostForSignedJWT(response, true);
    }

    @Test
    void doGet() throws IOException {
        when(servlet.getServletConfig()).thenReturn(servletConfig);
        when(response.getWriter()).thenReturn(printWriter);
        log.info("demo jose doGet test, preparing...");
        servlet.doGet(request, response);
        verify(printWriter, atLeastOnce()).println("<pre>");
        when(response.getWriter()).thenThrow(IOException.class);
        servlet.doGet(request, response);
    }

    @Test
    void doPost() throws IOException, ServletException {
        when(servlet.getServletConfig()).thenReturn(servletConfig);
        when(response.getWriter()).thenReturn(printWriter);
        assertNotNull(servlet);
        log.info("demo jose doPost test, preparing...");
        ResourceBundle labels = ResourceBundle.getBundle("demo");
        assertNotNull(labels);
        servlet.doPost(request, response);
        verify(response, atLeastOnce()).sendError(422, MISSING_KEY);
        doThrow(new IOException()).when(response).sendError(422, MISSING_KEY);
        servlet.doPost(request, response);
        when(request.getParts()).thenReturn(new ArrayList<>());
        servlet.doPost(request, response);
    }

    @Test
    void getServletInfo() {
        when(servlet.getServletConfig()).thenReturn(servletConfig);
        String s = servlet.getServletInfo();
        assertEquals("Servlet tools for Nimbus JOSE+JWT library", s);
    }
}
