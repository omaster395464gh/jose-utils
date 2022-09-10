package de.pdv.demo.jose;

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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;


@Log
class DecryptServletTest extends Mockito {

    static final String ALGORITHM = "RSA-OAEP-256";
    static final String ALG_ERROR = "RSA-OAEP-256 not found in sEncKey";
    static final String DATA = "data.enc";
    static final String KEY = "data.key";
    static final String MISSING_KEY = "Missing parameter privateKey";
    @Spy
    private DecryptServlet servlet;
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
    void decryptPayload() {
        when(servlet.getServletConfig()).thenReturn(servletConfig);
        assertNotNull(servlet);
        log.info("demo jose decrypt test, preparing...");
        ResourceBundle labels = ResourceBundle.getBundle("demo");
        assertNotNull(labels);
        String sEncMetaData = labels.getString(DATA);
        assertNotNull(sEncMetaData);
        String sEncKey = labels.getString(KEY);
        assertNotNull(sEncKey);
        assertTrue(sEncKey.contains(ALGORITHM), ALG_ERROR);
        String sResult = servlet.decryptPayload(sEncKey, sEncMetaData).toString();
        assertTrue(sResult.contains("Thüringer Antragssystem für Verwaltungsleistungen TEST"), "decrypt result incorrect");
    }

    @Test
    void decryptPayloadFailure() {
        when(servlet.getServletConfig()).thenReturn(servletConfig);
        assertNotNull(servlet);
        log.info("demo jose decrypt FAILURE TESTS, preparing...");
        ResourceBundle labels = ResourceBundle.getBundle("demo");
        assertNotNull(labels);
        String sEncMetaData = labels.getString(DATA);
        assertNotNull(sEncMetaData);
        String sEncKey = labels.getString(KEY);
        assertNotNull(sEncKey);
        assertTrue(sEncKey.contains(ALGORITHM), ALG_ERROR);

        assertNull(servlet.decryptPayload("a", ",b"));
        assertNull(servlet.decryptPayload(sEncKey, ",b"));
        assertNull(servlet.decryptPayload(sEncKey, sEncMetaData.replace('a', 'b')));
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
        assertNotNull(servlet);
        log.info("demo jose doPost test, preparing...");
        ResourceBundle labels = ResourceBundle.getBundle("demo");
        assertNotNull(labels);
        String sEncMetaData = labels.getString(DATA);
        assertNotNull(sEncMetaData);
        String sEncKey = labels.getString(KEY);
        assertNotNull(sEncKey);
        assertTrue(sEncKey.contains(ALGORITHM), ALG_ERROR);
        servlet.doPost(request, response);
        verify(response, atLeastOnce()).sendError(422, MISSING_KEY);

        doThrow(new IOException()).when(response).sendError(422, MISSING_KEY);
        servlet.doPost(request, response);

        //doThrow(new IOException()).when(request).getParts();
        //servlet.doPost(request, response);

        when(request.getParts()).thenReturn(new ArrayList<>());
        servlet.doPost(request, response);

    }

    @Test
    void getServletInfo() {
        when(servlet.getServletConfig()).thenReturn(servletConfig);
        String s = servlet.getServletInfo();
        assertEquals("Demo servlet for Nimbus JOSE+JWT library", s);
    }
}
