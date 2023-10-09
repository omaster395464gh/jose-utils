package de.pdv.demo.jose;

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
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.*;


@Log
class HandlerServletTest extends Mockito {


    static final String MISSING_KEY = "Missing parameter jwkSet";
    @Spy
    private HandlerServlet servlet;
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
    void getServletInfo() {
        when(servlet.getServletConfig()).thenReturn(servletConfig);
        String s = servlet.getServletInfo();
        assertEquals("Servlet tools for Nimbus JOSE+JWT library", s);
    }
}
