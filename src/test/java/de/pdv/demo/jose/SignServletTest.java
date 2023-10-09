package de.pdv.demo.jose;

import com.nimbusds.jwt.SignedJWT;
import lombok.extern.java.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;


@Log
class SignServletTest extends Mockito {

    static final String MISSING_KEY = "Missing parameter jwkSet";
    private static final ResourceBundle labels = ResourceBundle.getBundle("demo");
    private static final String SIGN_HEADER = labels.getString("sign.header");
    private static final String SIGN_PAYLOAD = labels.getString("sign.payload");
    @Spy
    private SignServlet servlet;
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
    void verifySignSet() {
        when(servlet.getServletConfig()).thenReturn(servletConfig);
        assertNotNull(servlet);
        log.info("demo jose sign test, preparing...");
        assertNotNull(SIGN_HEADER);
        assertNotNull(SIGN_PAYLOAD);
        InputStream is = getClass().getClassLoader().getResourceAsStream("jwks2.json");
        assertNotNull(is);
        SignedJWT mySignedJWT = servlet.signSet(is, SIGN_HEADER, SIGN_PAYLOAD);
        assertNotNull(mySignedJWT);
        log.info("sign ok!");
    }

    @Test
    void verifySetFailure() {
        when(servlet.getServletConfig()).thenReturn(servletConfig);
        assertNotNull(servlet);
        log.info("demo jose sign test, preparing...");
        assertNotNull(SIGN_HEADER);
        assertNotNull(SIGN_PAYLOAD);
        InputStream is = getClass().getClassLoader().getResourceAsStream("jwks.json");
        assertNotNull(is);
        SignedJWT mySignedJWT = servlet.signSet(is, SIGN_HEADER, SIGN_PAYLOAD);
        assertNull(mySignedJWT, "sign succeed but should fail because of wrong jwks");

        InputStream is2 = getClass().getClassLoader().getResourceAsStream("jwks.json");
        mySignedJWT = servlet.signSet(is2, "a", "b");
        assertNull(mySignedJWT, "sign succeed but should fail because of wrong jwks");

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

        when(response.getWriter()).thenThrow(IOException.class);
        doThrow(new IOException()).when(response).sendError(422, MISSING_KEY);
        servlet.doPost(request, response);
        when(request.getParts()).thenReturn(new ArrayList<>());
        servlet.doPost(request, response);
    }
}
