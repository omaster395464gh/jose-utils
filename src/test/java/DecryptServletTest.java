import de.pdv.demo.jose.DecryptServlet;
import lombok.extern.java.Log;
import org.junit.jupiter.api.Test;

import java.util.ResourceBundle;

@Log
class DecryptServletTest {

    @Test
    void decrypt() {
        log.info("demo jose decrypt test, preparing...");
        ResourceBundle labels = ResourceBundle.getBundle("demo");
        String sEncMetaData = labels.getString("data.enc");
        String sEncKey = labels.getString("data.key");
        assert(sEncKey).contains("RSA-OAEP-256");
        DecryptServlet s = new DecryptServlet();
        String sResult = s.decryptPayload(sEncKey,sEncMetaData).toString();
        assert(sResult).contains("Thüringer Antragssystem für Verwaltungsleistungen TEST");
        log.info(String.format("ok: %s",sResult));
    }
}
