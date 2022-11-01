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

import static org.junit.jupiter.api.Assertions.*;


@Log
class VerifyServletTest extends Mockito {

    static final String ALGORITHM = "PS512";
    static final String ALG_ERROR = "RSA-OAEP-256 not found in sEncKey";
    static final String DATA = "data.enc";
    static final String KEY = "data.key";
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
        //String s = "eyJraWQiOiJkZDA0MDllNS00MTBlLTRkOTgtODViNi1mODFhNDBiOGQ5ODAiLCJ0eXAiOiJzZWNldmVudCtqd3QiLCJhbGciOiJQUzUxMiJ9.ewogICJpc3MiOiAiaHR0cHM6Ly9hcGkuZml0a28uZGUvZml0LWNvbm5lY3QvIiwKICAiaWF0IjogMTYyMjc5NTczMiwKICAianRpIjogIjkwRDlCMjlELUMwNUYtNEYwMy04MUMwLUUyMzMxMTZDNTZEOSIsCiAgInN1YiI6ICJzdWJtaXNzaW9uOkY2NUZFQUIyLTQ4ODMtNERGRi04NUZCLTE2OTQ0ODU0NUQ5RiIsCiAgImV2ZW50cyI6IHsKICAgICJodHRwczovL3NjaGVtYS5maXRrby5kZS9maXQtY29ubmVjdC9ldmVudHMvc3VibWl0LXN1Ym1pc3Npb24iOiB7fQogIH0sCiAgInR4biI6ICJjYXNlOkY3M0QzMEM2LTg4OTQtNDQ0NC04Njg3LTAwQUU3NTZGRUE5MCIKfQ.THmHiZoYEMyyWCu2R4nEJtvgtB5PF0KAqtfu_Z-yVjfjSkXW7TtZnX96UAeCGsjpxkBJvXTXAgSB5n378KjZXebAtI7nbFE0gYgt3fwmxmpJitA-4e8v6KfvhwNcdqJHLKDzYRMq_yw7UiwLx1Cxz6nBiOKfR4piL707muKXTgD7DuP0kv-c6V9dGNQ4KzT_sJP5zDWogEzGWSVaLaJZrmDZHoUZMZ6C9kI7SvC-A7Q0ROkFznU_cpjjEAIG74_YCiICvjr91ueQWTdNyc1DBvxpEBtBWq6nWPTg0d91iQlhPUgNKbmC4QtG_tFctTYhX7stO-JbL-4VnAQjQHD5uw4SvvpPrTN4Z3Wz2IjMm8-ClI9imGKThfAqwTaWtJv7Bn_FDiN_nEuGyN2of-M2vZWa-DlZ2iPFct6ESp9PumaO_pIF5cUrX4IBoe3fcmg788-ClReytCMjD13uPVOVoIb3yimUdupOUROxb3MITowHP2-YG1gWqhQp22XSQXktugDHWezAuN0xuimwAJq_OvyoDxj4lsnn6BQkqZYdqD0hJghwqZIytg8PlIi76Cdvh8NFgVw48xZ0WUOFvBPJO2Qe8PiTSVX_P9CIIWxsKlYwg8vJ226qi0eYfD70ynjBDQIPmsOOSut6bFKgOLBFa9ZvCy6HmhyLa-EsgLhS4uc";
        String s ="eyJraWQiOiIzMjg1ODE0Ny1mMDkwLTQzYTktYjJmZC1kMjZhZTViNDFjMDMiLCJ0eXAiOiJzZWNldmVudCtqd3QiLCJhbGciOiJQUzUxMiJ9.eyJzdWIiOiJzdWJtaXNzaW9uOjVlODJjMTM1LWEzYjctNGViMC1iZDgzLTZkZjMzNzMzMTE3MCIsIiRzY2hlbWEiOiJodHRwczpcL1wvc2NoZW1hLmZpdGtvLmRlXC9maXQtY29ubmVjdFwvc2V0LXBheWxvYWRcLzEuMC4wXC9zZXQtcGF5bG9hZC5zY2hlbWEuanNvbiIsImlzcyI6Imh0dHBzOlwvXC9zdWJtaXNzaW9uLWFwaS10ZXN0aW5nLmZpdC1jb25uZWN0LmZpdGtvLmRldiIsInR4biI6ImNhc2U6YzlmMGRiOTEtOTM1NS00NTFhLWIyYTUtN2E5YmUxMGNlNzY1IiwiaWF0IjoxNjUxNjU2MTUxLCJqdGkiOiJjNmFhMzllZC00ZGRmLTRlMmEtYWY1Mi1iMWZmNzE4MDQ0NWYiLCJldmVudHMiOnsiaHR0cHM6XC9cL3NjaGVtYS5maXRrby5kZVwvZml0LWNvbm5lY3RcL2V2ZW50c1wvY3JlYXRlLXN1Ym1pc3Npb24iOnt9fX0.i_E4-ZF0MkyFMoC2Iih5lks70PMcBAWzgLgl5BDOgMVFnImt64m3MRO5nsFIihOO7LdjJQvCkgfIf80-2l6yfsE2w3XFfMYOSMDnTzqFlZt6QIVZojBQ7Q2n34PVDalgKCMSSHOPqrg7XiWtcicsR6T0zDF1Ksb0Xb3NWXNK6R1c5iMWWp1t_tRXNxPlpv29DNFCPcnJW5YMNrpaozpAA7pxTEG6Owc6Bj_tUoNY1s4HXkgQexaMAg3mfLHyssQZzTdde5JAoHzhp_Cg72x8n6zxX3FNJASy2b6_gPLJTOnj-uz3fxxoVUzOoMiEnQiC5PUzi3gWVHYwcBdbI57Tmf2jgKlSKdyYIyj3GNZCsFNxIBKmClPktO5almh_WKI17I2jb-NsqBVffJJn9-spFP4ifw2J4jGN1cm9hEnmDJ4zr9Jp6ZRwIpWfwVt7OnCaCqvrJziEizllhK0E4DIRq_iCbsTRVmDg18SvbQfV5PoEBLVC6Ly4tNpUxRFKsoeApUFVRU_i-zCrnYGxcd6JZ_YBCrF5W0PEP2kJmJ7HaYdBek7ddj2E_SzT78qvvLgRrNUIVBiZzqMLWaOBM5qDOF9vKL65FFX6I-pxM5_H2XQMzmAZnxMbvxaxqzKC4VEkr6sZWvlTQnEGHDYftpKqCp3OE6cD5x6ZvI_g1EmKuks";
        JWT jwt = JWTParser.parse(s);
        assert(jwt instanceof SignedJWT);
         if (jwt instanceof SignedJWT) {
                SignedJWT jwsObject = (SignedJWT)jwt;
                InputStream is = getClass().getClassLoader().getResourceAsStream("jwks.json");
                //SignedJWT sJWT = SignedJWT.parse("{\"sub\":\"submission:5e82c135-a3b7-4eb0-bd83-6df337331170\",\"$schema\":\"https:\\/\\/schema.fitko.de\\/fit-connect\\/set-payload\\/1.0.0\\/set-payload.schema.json\",\"iss\":\"https:\\/\\/submission-api-testing.fit-connect.fitko.dev\",\"txn\":\"case:c9f0db91-9355-451a-b2a5-7a9be10ce765\",\"iat\":1651656152,\"jti\":\"ee7e1bfd-8593-482a-a987-636665ddf438\",\"events\":{\"https:\\/\\/schema.fitko.de\\/fit-connect\\/events\\/submit-submission\":{\"authenticationTags\":{\"data\":\"LbKRwun2tAcaqAdxGJQoYg\",\"metadata\":\"rJzLKF3ApPQN-gUfK-Ahgw\",\"attachments\":{\"f50308a5-b3ed-4627-9f0b-b5a8e0095791\":\"RH7FW5Ug6qDgqKwO56g1Qg\",\"02bc84b6-7eee-473f-919c-3d4d92bd17ac\":\"Z9ih9NIkhvFDiT1NQCzCBA\"}}}}}");
                String sKeyId = "32858147-f090-43a9-b2fd-d26ae5b41c03";
                assertNotNull(is);
                boolean bResult = servlet.verifySet(is,jwsObject,sKeyId);
                assertTrue(bResult,"verify failed");
                log.info("verify ok!");
            }
    }

    @Test
    void verifySetFailure() throws ParseException {
        when(servlet.getServletConfig()).thenReturn(servletConfig);
        assertNotNull(servlet);
        String s = "eyJraWQiOiJkZDA0MDllNS00MTBlLTRkOTgtODViNi1mODFhNDBiOGQ5ODAiLCJ0eXAiOiJzZWNldmVudCtqd3QiLCJhbGciOiJQUzUxMiJ9.ewogICJpc3MiOiAiaHR0cHM6Ly9hcGkuZml0a28uZGUvZml0LWNvbm5lY3QvIiwKICAiaWF0IjogMTYyMjc5NTczMiwKICAianRpIjogIjkwRDlCMjlELUMwNUYtNEYwMy04MUMwLUUyMzMxMTZDNTZEOSIsCiAgInN1YiI6ICJzdWJtaXNzaW9uOkY2NUZFQUIyLTQ4ODMtNERGRi04NUZCLTE2OTQ0ODU0NUQ5RiIsCiAgImV2ZW50cyI6IHsKICAgICJodHRwczovL3NjaGVtYS5maXRrby5kZS9maXQtY29ubmVjdC9ldmVudHMvc3VibWl0LXN1Ym1pc3Npb24iOiB7fQogIH0sCiAgInR4biI6ICJjYXNlOkY3M0QzMEM2LTg4OTQtNDQ0NC04Njg3LTAwQUU3NTZGRUE5MCIKfQ.THmHiZoYEMyyWCu2R4nEJtvgtB5PF0KAqtfu_Z-yVjfjSkXW7TtZnX96UAeCGsjpxkBJvXTXAgSB5n378KjZXebAtI7nbFE0gYgt3fwmxmpJitA-4e8v6KfvhwNcdqJHLKDzYRMq_yw7UiwLx1Cxz6nBiOKfR4piL707muKXTgD7DuP0kv-c6V9dGNQ4KzT_sJP5zDWogEzGWSVaLaJZrmDZHoUZMZ6C9kI7SvC-A7Q0ROkFznU_cpjjEAIG74_YCiICvjr91ueQWTdNyc1DBvxpEBtBWq6nWPTg0d91iQlhPUgNKbmC4QtG_tFctTYhX7stO-JbL-4VnAQjQHD5uw4SvvpPrTN4Z3Wz2IjMm8-ClI9imGKThfAqwTaWtJv7Bn_FDiN_nEuGyN2of-M2vZWa-DlZ2iPFct6ESp9PumaO_pIF5cUrX4IBoe3fcmg788-ClReytCMjD13uPVOVoIb3yimUdupOUROxb3MITowHP2-YG1gWqhQp22XSQXktugDHWezAuN0xuimwAJq_OvyoDxj4lsnn6BQkqZYdqD0hJghwqZIytg8PlIi76Cdvh8NFgVw48xZ0WUOFvBPJO2Qe8PiTSVX_P9CIIWxsKlYwg8vJ226qi0eYfD70ynjBDQIPmsOOSut6bFKgOLBFa9ZvCy6HmhyLa-EsgLhS4uc";
        JWT jwt = JWTParser.parse(s);
        assert(jwt instanceof SignedJWT);
        if (jwt instanceof SignedJWT) {
            SignedJWT jwsObject = (SignedJWT)jwt;
            InputStream is = getClass().getClassLoader().getResourceAsStream("jwks.json");
            //SignedJWT sJWT = SignedJWT.parse("{\"sub\":\"submission:5e82c135-a3b7-4eb0-bd83-6df337331170\",\"$schema\":\"https:\\/\\/schema.fitko.de\\/fit-connect\\/set-payload\\/1.0.0\\/set-payload.schema.json\",\"iss\":\"https:\\/\\/submission-api-testing.fit-connect.fitko.dev\",\"txn\":\"case:c9f0db91-9355-451a-b2a5-7a9be10ce765\",\"iat\":1651656152,\"jti\":\"ee7e1bfd-8593-482a-a987-636665ddf438\",\"events\":{\"https:\\/\\/schema.fitko.de\\/fit-connect\\/events\\/submit-submission\":{\"authenticationTags\":{\"data\":\"LbKRwun2tAcaqAdxGJQoYg\",\"metadata\":\"rJzLKF3ApPQN-gUfK-Ahgw\",\"attachments\":{\"f50308a5-b3ed-4627-9f0b-b5a8e0095791\":\"RH7FW5Ug6qDgqKwO56g1Qg\",\"02bc84b6-7eee-473f-919c-3d4d92bd17ac\":\"Z9ih9NIkhvFDiT1NQCzCBA\"}}}}}");
            String sKeyId = "32858147-f090-43a9-b2fd-d26ae5b41c03";
            assertNotNull(is);
            boolean bResult = servlet.verifySet(is,jwsObject,sKeyId);
            assertFalse(bResult,"verify succeed but should fail");
        }
    }

    @Test
    void getServletInfo() {
        when(servlet.getServletConfig()).thenReturn(servletConfig);
        String s = servlet.getServletInfo();
        assertEquals("Demo servlet for Nimbus JOSE+JWT library", s);
    }
}
