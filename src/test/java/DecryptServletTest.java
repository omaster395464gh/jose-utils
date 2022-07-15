import com.nimbusds.jose.JOSEException;
import de.pdv.demo.jose.DecryptServlet;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.ResourceBundle;

class DecryptServletTest {

    @Test
    void decrypt() {
        System.out.println("demo jose decrypt test");
        System.out.println("Preparing...");
        ResourceBundle labels = ResourceBundle.getBundle("demo");
        String sEncMetaData = labels.getString("data.enc");
        String sEncKey = labels.getString("data.key");
        assert(sEncKey).contains("RSA-OAEP-256");
        DecryptServlet s = new DecryptServlet();
        try {
            String sResult = s.decryptPayload(sEncKey,sEncMetaData).toString();
            assert(sResult).contains("Thüringer Antragssystem für Verwaltungsleistungen TEST");
            System.out.println("ok: " + sResult);
        } catch (JOSEException | ParseException e) {
            e.printStackTrace();
        }

    }
}
