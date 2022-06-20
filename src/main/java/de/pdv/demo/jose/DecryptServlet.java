package de.pdv.demo.jose;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.jwk.RSAKey;
import org.json.JSONObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "decrypt", value = "/decrypt")
public class DecryptServlet extends HttpServlet {
    private String message;

    private static final String  sEncMetaData = "eyJ6aXAiOiJERUYiLCJraWQiOiJDNkdHTk1CZklqSENRT0Y0SUNlWDZEUmNXQm1udEVwdkNUZWFfSDNQZGk0IiwiY3R5IjoiYXBwbGljYXRpb25cL2pzb24iLCJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiUlNBLU9BRVAtMjU2In0.ArCOZB0NGUaYft-0zv9OQaEwTJU_M0dTNayzcJi2lGRBHixsWJ_fkR6LrW9v0KBCtbGW9bCKBJQFGZncvmS2PFcQFhAZrZgoffnqxD53G-yYtrJwT6FKSoEcpK1yOrn2QkbVYCt1PjdFBHdzMKU9qzZu7jYH2WqhL4hVXEMv_LKXBtn77akb-HT4WQk2k8HZqW2LiIajiaQwkFO5zAcxkv7ergB3MJRZiGTG1RG9nucTtGTsdKhigTVJsIQ3DlThiQV54mGm54_tlPczmjNPjntg9FIxA6VN5d2Kariy86EgqPWVrpJjUOsadiJuz_nHEghMZulqKusrDL_k5aFqEkbqgGpYlIp44x_Un6DO5adnGfmvoWzAxDn2ibI29GlXkaGiTV3v9NkvBxNRefcXnrrG6X6Nto5M0x0Se7k9CI4alMJwJMElRC6nXU5POK8haVRDx-2ezROOcKxXfvwyjBJ2rkQ1_IkBctyF51sgk8hqEDxY5pKbrqbvj0XAcX2ckbskzRp99LhHwAqQWf83otX741Oj4PMVVAiPyIPA8n0NjD6ecm-1sDIki5fe3hifi8nb3HLZGZi-wl8AHhjvBX9gE55PPKSlpEgKAxnO_euuShAzJsQ1e805REOUEWm76ZXE8NFMj8kGeC4vzpukoxmQGS6k66MHOdnYoW6D6cg.Qpp8Z_A0DohQ7iDP.zKGbIsfMhKH18uNU2TSoOeM19arGT1saTMYZ9bLpOhzACFJULa05-m8S_HSezFxxYg0rxAefr9W3lLKoVQWGzXyVG19QuJowAhzKuc_Yr7L413l6WEG1PTrdiRpUgF8GLgaBG6EolyxwXm_H-lblyvfDNK5mfiyu-6pqVlilS-ZWmMNxVqMHHcUFeYYyqU5OShYGRV7rc3OlUDJFL9tLMcV65c_P8V6r22B62A9ZSKLNB9Ikhmhy-gGy5zx35hrItIloAP2H4TH2wj9TKGxgfmTEGUSwKMoowzb3zif_RdWP29MjG-1m0ga2s1We6MdBORLF9SGzMPdPFOxV8fQNgiM-aFWPt4zNDK24TbIRHXgsS0FEpugwst_vv8CaK-1H_fhDKJQypMlqubIePJysZt__43yYGlOL4u3kPplbQ-0vL813M51tUXmvCFiY5FoVaHnbOgJjDZpE-RNIApOFiBOFyQUL6id8zZtOUrie0guTWmAeSxy3V-BDQcOa4tGhKmBD0fR3P2yqk-cTwi5YMPPjkJaUK06uwnmE8Wv-xKwowJFRjy3tD6jmbjGFt7U3I6HXtrGif8sXVbjBleZ2oer3RZoKLn5e2mPsAjuDvkeA_3-xNcC0DMZJVWDQ6XfJvPGvOct1I6h0ICMzjBATIgVMRyichxB_8SI6qyYuU5YxFI2_zzddSFEA4kiDUd5htz5vOZQcFf7NZE_v2dA45H8Yl7vBz3rO2JFsPnwmdA9D_7bybJsXQhdMDGVMpBCV_QWjs9NcoiLi2Er-rNuhOHYlrykjpPJV9agXBJJCtjkCbFCWCtYhShjjFZwNfk7T4XwrdaumtT8VmL93ZqAYqd_aJVF8d0gGYeKAhXIxO1VKcnxuq_Xo8NJrQtDnaZ1Qx7IUC_l0sJ3FBdNRWg.gZGx1fiD_gmTXJ2f_Esh_A";
    private static final Logger logger = Logger.getLogger(DecryptServlet.class.getName());


    public void init() {
        message = "Decrypt demo!";
    }

    /*
    public String encrypt() throws JOSEException {
        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.HS256),
        new Payload(message));
        byte[] sharedKey = new byte[32];
        new SecureRandom().nextBytes(sharedKey);
       // Apply the HMAC to the JWS object
        jwsObject.sign(new MACSigner(sharedKey));
        return jwsObject.serialize();
    }
     */

    public String decrypt(String keyStr, String encryptedStr) throws JOSEException, ParseException {
        RSAKey jwk = RSAKey.parse(keyStr);
        JWEObject jweObject = JWEObject.parse(encryptedStr);
        jweObject.decrypt(new RSADecrypter(jwk));
        return jweObject.getPayload().toString();
    }

    public String outCrKey (String id)
    {
        String CrKey;

        if (Objects.equals(id, "PUBLIC_KEY"))
        {
            CrKey ="{\"alg\":\"RSA-OAEP-256\",\"e\":\"AQAB\",\"key_ops\":[\"wrapKey\"],\"kid\":\"C6GGNMBfIjHCQOF4ICeX6DRcWBmntEpvCTea_H3Pdi4\",\"kty\":\"RSA\",\"n\":\"5EwPY4lKA4yv3GZKFB71RVCBiokUQCGb0x3XiaS201yYkZzfEINDiqpzNtzZqs_hvykUwLFogb0uZxB59R7B-0IQ50noO-MI0P1NVNUl3HX76BUhhavMIWh2T5mAxNlyauqea1mICTzmwDy0xjo3cDGmaFoJM_sy208J6r6Baxo9RiLmju5mHUGcrBuFovz1m2dpP-NyLPmQqYunwc0zjOF1Yw4ISCtwqnf15n1AmeB9bbhk1rr2VYL9kMCRU2RZOv-9HI8ABbJKkTdkXeSETRiJtAtR431kbE_yHiinWPLjD52-i2NNxedxwVE2De0d9FvM--l4zRaDbm6DMiA3OiDiJCZuWUxJCUbbTW47NY-bupiWMPlNROboVLnrF73w0IJhOXEjh9a0WOuYfUAOJ36tB06UarGStTLfLGUPFvQyLX_Nz_qUBxpyjpPxsZ2DxMJ88UMEgtzvCLf9v0VN_ve5gPmFu818rAfr_FDjD7DT0AaDg3e_2nygGj1swsvSdZfmJdGXKylQ4VhujXWbXCSWhq7xobC483UbNaWVyJP5iJG9BOQ0JmPhKNoex1YDkO8hjUHrOy5Fv07OfhSI3SwIP_hgc0G4q4b0dbb68xhF4g5AlBsWRbhLSfLRjw4vHklTakq3wJ_D3OGwSIdUy66tRBrl59MLna6S4vZ4fT0\",\"x5c\":[\"MIIFCTCCAvECBAVSuEowDQYJKoZIhvcNAQENBQAwSTELMAkGA1UEBhMCREUxFTATBgNVBAoMDFRlc3RiZWhvZXJkZTEjMCEGA1UEAwwaRklUIENvbm5lY3QgVGVzdHplcnRpZmlrYXQwHhcNMjIwNDI4MTAxNzU2WhcNMzIwNDI1MTAxNzU2WjBJMQswCQYDVQQGEwJERTEVMBMGA1UECgwMVGVzdGJlaG9lcmRlMSMwIQYDVQQDDBpGSVQgQ29ubmVjdCBUZXN0emVydGlmaWthdDCCAiIwDQYJKoZIhvcNAQEBBQADggIPADCCAgoCggIBAORMD2OJSgOMr9xmShQe9UVQgYqJFEAhm9Md14mkttNcmJGc3xCDQ4qqczbc2arP4b8pFMCxaIG9LmcQefUewftCEOdJ6DvjCND9TVTVJdx1++gVIYWrzCFodk+ZgMTZcmrqnmtZiAk85sA8tMY6N3AxpmhaCTP7MttPCeq+gWsaPUYi5o7uZh1BnKwbhaL89ZtnaT/jciz5kKmLp8HNM4zhdWMOCEgrcKp39eZ9QJngfW24ZNa69lWC/ZDAkVNkWTr/vRyPAAWySpE3ZF3khE0YibQLUeN9ZGxP8h4op1jy4w+dvotjTcXnccFRNg3tHfRbzPvpeM0Wg25ugzIgNzog4iQmbllMSQlG201uOzWPm7qYljD5TUTm6FS56xe98NCCYTlxI4fWtFjrmH1ADid+rQdOlGqxkrUy3yxlDxb0Mi1/zc/6lAcaco6T8bGdg8TCfPFDBILc7wi3/b9FTf73uYD5hbvNfKwH6/xQ4w+w09AGg4N3v9p8oBo9bMLL0nWX5iXRlyspUOFYbo11m1wkloau8aGwuPN1GzWllciT+YiRvQTkNCZj4SjaHsdWA5DvIY1B6zsuRb9Ozn4UiN0sCD/4YHNBuKuG9HW2+vMYReIOQJQbFkW4S0ny0Y8OLx5JU2pKt8Cfw9zhsEiHVMuurUQa5efTC52ukuL2eH09AgMBAAEwDQYJKoZIhvcNAQENBQADggIBAK6jqjaC/XUOi4d4YWhQ9RgPQUN6W/4mrihpnPci+O4qVuo5Dh00KP/VQWzHUr+t0qR/N3dsBtda4aKAHQcCd11ONWMC6kmFBIxTGFqj9l+lRW20uqFIHMCGvzymNM+hQSo2+bVlKEzwMjz1jtLYYxkeqGbnwRKcmB22VODMBZLVC7skAUUhNWW9hkWz40hshG0kZ4V0YcDHMkcelo1BvakY12IBShLAKstPtnHHvegukKdzz0ofrWz1rZmE45VxLAjH98zZbTrUC5m7LDPrD2jU9aO+tK9E6ZqdXhfMEQoR2kXURxoI4Mk6lwm+2ik1oxHSJ7C0USJvgyWlnKS43TJ3IhmXuP+1oAeWRHO6mT8B34B2or5LNkI9u/a6LbzoKKJLHfBiQnwJTkOFsG6KfTtJBpqAnmpRxG43WADJyPqZP+i1Y8hFJMcnaOibLOva9mlCjs+/Zk73hD3J5R8OY0XKlZ4QJAl+u8MsNwhuIrUgnCHYY1ONnvCoisfGxltuLdFcvpB5U4Lw/NH/XyTYQwx0sTK1gusTTPML7qrmuOsiTOp9emflr0b0+L3GSnxVwOpFQMdZ3HKhONaeYiiOQaKgSgRFWLz9dLiV8cRJf+rLPhvDpAGWUB9+Te2nwSlADR5AJdrCSO7MK/4oaS9ZaEX7FuW8X6qku31i6PCa+O35\"]}";

        }
        else if (Objects.equals(id, "PRIVATE_KEY"))
        {
            CrKey ="{\"alg\":\"RSA-OAEP-256\",\"d\":\"pVxqcSCKWdPXtf7pfXWHFvPfQOdVobY2Vk7xRiPmwWX6pM5JzH4nuBMKbRy_FTiKPZqWC5T1K7RKHiETFXd37nTGJaGk82vZgPdYwi-5wtw64Lodk-MCcthlifaYDP2sIMFciklJfdd9FJVkI4kbisqLzF4a-GnlRnETr9LZjyhsZZb2BWWs1VgnEYrF2276v9vBoqMummESZZxgoMHvqwXLrzoEybGdY_yMQq1K06qFb6n2cIrvzrSY0faJzSuMbm27Hyp9S6L-2_yRWGkjY2x4oc7pO6whx0qJB0tPG-CikzchQ3lkZgrknLAkRSYRYmP0MCvGclbyUFT_9pjbJoNGyFOrLCQ5EBaA7-HxU90nGCu28vDTiQnLaQk_EPm6qtGz4rVsvVk1en5LmCvrH3iZ1meuu2uUb0nFDEbhadzkDD-Db51oQ3teCV0liPdL5Z0FWz77e9RwQsFvy1Z22WtoeUjiRqWsqcrtg4jjgS9yaDbsT_zgFFAUN7KOwsrhYMkRKusXgp2m5CeGU8UoNETc__rYHgfQtrW261U9HWW2lVbYFr9ghLVXzzabFSM1Zdh2BfCMI79NaJq4qs9AtcyMxDjsRSqNVo3dnqyjC7Wwa_L7-Xcgli5UaFXdm1FV09msAwRMjQaAhlV8jSTfaZYlDTKtAk3LPTkCpEJP-ME\",\"dp\":\"b458e6bOBDb75e37EVda5inF4bLyFX-GIuSWGqnPY5Sir-TdQ5v2iaeywyZzka8son5ZsE7jXanBfuLiRwIrc7h3F3mKu08zcPX2QklezK4w7xI1g0fUY_lDQaCdOQlIXF-fOq_nbnevHPl1SYEQcz2gWrFp-8UjyytAmo_V35SI_SepF23b53DvWzbX7JcGUZhvXRpjKM09198BJZsQlZEkYqri8AI2JuUq--qfJ8JmEkjI3kZy1B3AXeXvVdihv6Gz5UoPZuFJkyLSDZOb5cbP1XDpxsVXMN42cErqwWTjhXmLAqhGvXpKQ8Q9Li0ObfZJd9oZPu9P98BK2PELXQ\",\"dq\":\"2fSxdY3yN4LBkAW0S3rDBGEzCw1-ELdnGwasGLdGOEq4tFJgJ4vPdPRh-a82NqmmBE1oeq2sDdB9Z0GxEesmXsG2Xa6L4sHHp7zkeq7R44J_mfyeGBBYxP8QpChL5Ye4601VymGIaiXbFLicOWzE-lygFPpOjzcTuZfkjjB-rAjUnpOUXSDdLjnJFF03PvUlB_ZUfNzHv9mujP4gmBmQuVKeq0KxyPJnEWoJE1j-5yY3L1ukeNUvhw15WvlPPeiK5s9by4krjz1ExIBeiX_tFLJm_RXXbuvF2PazyyPvSJBG9eyplU9SoyLbSQbpld3ySoFMOKQXpWDASd-Bcv074Q\",\"e\":\"AQAB\",\"key_ops\":[\"unwrapKey\"],\"kid\":\"C6GGNMBfIjHCQOF4ICeX6DRcWBmntEpvCTea_H3Pdi4\",\"kty\":\"RSA\",\"n\":\"5EwPY4lKA4yv3GZKFB71RVCBiokUQCGb0x3XiaS201yYkZzfEINDiqpzNtzZqs_hvykUwLFogb0uZxB59R7B-0IQ50noO-MI0P1NVNUl3HX76BUhhavMIWh2T5mAxNlyauqea1mICTzmwDy0xjo3cDGmaFoJM_sy208J6r6Baxo9RiLmju5mHUGcrBuFovz1m2dpP-NyLPmQqYunwc0zjOF1Yw4ISCtwqnf15n1AmeB9bbhk1rr2VYL9kMCRU2RZOv-9HI8ABbJKkTdkXeSETRiJtAtR431kbE_yHiinWPLjD52-i2NNxedxwVE2De0d9FvM--l4zRaDbm6DMiA3OiDiJCZuWUxJCUbbTW47NY-bupiWMPlNROboVLnrF73w0IJhOXEjh9a0WOuYfUAOJ36tB06UarGStTLfLGUPFvQyLX_Nz_qUBxpyjpPxsZ2DxMJ88UMEgtzvCLf9v0VN_ve5gPmFu818rAfr_FDjD7DT0AaDg3e_2nygGj1swsvSdZfmJdGXKylQ4VhujXWbXCSWhq7xobC483UbNaWVyJP5iJG9BOQ0JmPhKNoex1YDkO8hjUHrOy5Fv07OfhSI3SwIP_hgc0G4q4b0dbb68xhF4g5AlBsWRbhLSfLRjw4vHklTakq3wJ_D3OGwSIdUy66tRBrl59MLna6S4vZ4fT0\",\"p\":\"93Bq-Ax9qBFCjq4s9gxtLn0ykXGcfL5DB5HwalNrRAXFzHqgENqQPNd_qA1MC-k3TOuY9EPa2lRLhbwQ0yq1q1DVwv4R1pWwRhaAsxozgA5lgMAtGein6OX5j8NpA-lzXD9Hk8tf-eSMXXlTl202HD7u1szGUMtchvq4t-RZByJXTCM7CdMhlbuPXf_NStughrv3-RV2oZ_v9q9I6gnGWqjP51FyyCtfyroSDFWYCIn75iu_JjYx-tLoP51pmwcOysEEH8Mu_nAQ82WmS3HRCPBbmYbZ1OWVJ8Wh156SaOKgtL5fiGTLLCcgCCp_TCbAWmznUSdIvp3MrsaWePfVFQ\",\"q\":\"7DIZrc7T2LzvgLMYPDUX6_hPbj-rmgXS77f3U_tSeb9bRzao235lSt_F8qDkAHk4sp3luKxU-zGKa7g9_NWnSForYMoCaO_OU7LocgChAKUjJ1FcN2VgnyEgZ7tBQ9RO2YoN0MovFmTnrdwkLRP9hjIP9QEMP58o06K6lhNkI9xPNPtsfQzt3JK7kY9hofFJJwJoAw2dvouvXNNw2LWNUAT6Yuyq7jAfK_Gvj3FNxVWNUu37t9dl1QNWX63R330vJ2CTfi8RbNPf1geQjyxfp0W53TUiyEFhBoEcvfIgMMNlq8s8m_NeRdF_Q1C6PfhqPEKslSQvhLNNtq6ezVDhiQ\",\"qi\":\"EwGTmcgSsjsy4SK0Nk1AitlXBe4aZojgeI5oxSocY0juTdzzRarKMWNaYbvkIbhy16h6Y0NsO83yv7eNe2KHJkDtML2gOgOPMo0k0ybJYOmdijiZzoo4F8lkPm80fBzsS218JoMrMAuP0B_2H0q0hg63u5-EZx3XOlh15w09POoxohzNB2FORviNaLCQVx95_j6-c_k6kX7KNLcDUr0woAoLt7t7w_3PMfWEpwEHhAli5MIP3vCRLj-yh9fEC2pdM5a9gnv_06f-d4EqypDzKW3ag7337iueFnSv5lnw31_BPYt3GrJIsw_k-jXJQu5_DPlmLIDAq4-4nKohs-lSvA\"}";
        }
        else
        {
            CrKey ="xx";
        } // end of if-else
        return CrKey;
    }


    public void processRequest (PrintWriter out)
    {
        out.println("<html lang=\"en\" data-theme=\"dark\">\n" +
                "<head>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                "    <meta name=\"description\" content=\"Servlet demo for decrypt json file with Nimbus JOSE+JWT library\">\n" +
                "    <link rel=\"stylesheet\" href=\"webjars/pico/css/pico.min.css\">\n" +
                "    <title>Decrypt json with Nimbus JOSE+JWT library</title>\n" +
                "</head>\n<body>\n" +
                "<main class=\"container\">");
        out.println("<h1>" + message + "</h1>");
        try {
            out.println("<pre>");
            // out.println("Data: " + message);
            // out.println("Data2: " + sEncData);
            // String sEncrypted = encrypt();
            // out.println("Encrypted:" + sEncrypted);
            String sDecrypted = decrypt(outCrKey("PRIVATE_KEY"), sEncMetaData);
            out.println("Decrypted:" + sDecrypted);
            out.println("</pre>");
            out.println("<pre>");
            out.println("Decrypted pretty:" + new JSONObject(sDecrypted).toString(4));
            out.println("</pre>");
        } catch (JOSEException | ParseException e) {
            throw new RuntimeException(e);
        }
        out.println("</main></body></html>");
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws IOException if a servlet-specific error occurs
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.log(Level.INFO,"doGet");
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        processRequest(response.getWriter());
        logger.log(Level.INFO,"Process complete");
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws IOException if a servlet-specific error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        //ServletContext context = getServletContext();
        logger.log(Level.INFO,"doPost");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // String contentType = request.getContentType();
        if ( (request.getParameter("privKey") != null) && (request.getParameter("encodedString") != null) )
        {
            String privKey = request.getParameter("privKey");
            String encodedString = request.getParameter("encodedString");
            String sDecrypted;
            try {
                sDecrypted = decrypt(privKey, encodedString);
                response.getWriter().write(sDecrypted);
            } catch (JOSEException e) {
                logger.log(Level.SEVERE,"Decryption failed, JOSEException %s ", Arrays.toString(e.getStackTrace()));
                response.sendError(500 ,"Decryption failed, JOSEException - see logs for details");
            } catch (ParseException e) {
                logger.log(Level.SEVERE,"Decryption failed, ParseException %s", Arrays.toString(e.getStackTrace()));
                e.printStackTrace();
                response.sendError(500 ,"Decryption failed, ParseException - see logs for details");
            }
            // processRequest(response.getWriter());
        } else {
            response.sendError(422 ,"Missing parameter");
        }
        logger.log(Level.INFO,"Process complete");
    }

    /**
     * Demo servlet for Nimbus JOSE+JWT library
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Demo servlet for Nimbus JOSE+JWT library";
    }// </editor-fold>

    @Override
    public void destroy() {
    }
}