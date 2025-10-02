package de.pdv.demo.jose;

import lombok.NonNull;
import lombok.extern.java.Log;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Generic warning / error handler class
 */
@Log
public class HandlerServlet extends HttpServlet {
    /**
     * handle warning
     *
     * @param response       response object
     * @param iHttpErrorCode HTTP Status Code
     * @param sWarning       Warning Message
     */
    public void handleWarning(@NonNull HttpServletResponse response, int iHttpErrorCode, String sWarning) {
        try {
            log.warning(sWarning);
            response.sendError(iHttpErrorCode, sWarning);
        } catch (IOException e) {
            log.severe(String.format("IOException while sendError - %s", e.getMessage()));
        }
    }

    /**
     * handle error
     *
     * @param response       response object
     * @param iHttpErrorCode HTTP Status Code
     * @param sError         Error message
     */
    public void handleError(@NonNull HttpServletResponse response, int iHttpErrorCode, String sError) {
        try {
            log.severe(sError);
            response.sendError(iHttpErrorCode, sError);
        } catch (IOException e) {
            log.severe(String.format("IOException while sendError - %s", e.getMessage()));
        }
    }

    /**
     * Servlet for Nimbus JOSE+JWT library
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Servlet tools for Nimbus JOSE+JWT library";
    }// </editor-fold>

}
