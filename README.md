# jose-utils [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=omaster395464gh_demo-jose-servlet&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=omaster395464gh_demo-jose-servlet) [![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=omaster395464gh_demo-jose-servlet&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=omaster395464gh_demo-jose-servlet)
Servlet for decrypt json file and verify sets with Nimbus JOSE+JWT library

* lots of samples for post form, curl and oracle pl/sql 
  optional output as base64 (UTF-8)
* Nimbus JOSE+JWT (connect2id) for decryption and set validation
  https://bitbucket.org/connect2id/nimbus-jose-jwt
  https://docs.fitko.de/fit-connect/docs/receiving/decrypt/
  https://docs.fitko.de/fit-connect/docs/getting-started/event-log/set-validation/
* Uses JavaMelody for monitoring
  https://github.com/javamelody/javamelody/wiki
* Uses Pico.css webjar for elegant styles with a minimal css framework
  https://picocss.com/
* Use Java 8 LTS (also tested with Java 17 LTS and Java 11 LTS)
* Demo data for curl / jsp: <br/>
  [src/main/resources/demo.properties](src/main/resources/demo.properties)<br/>
  [src/main/resources/jwks.json](src/main/resources/jwks.json)<br/>
  [src/main/webapp/demo/privateKey.txt](src/main/webapp/demo/privateKey.txt)<br/>
  [src/main/webapp/demo/encodedString.txt](src/main/webapp/demo/encodedString.txt)<br/>

## /decrypt servlet parameter
* String privateKey: Private Key in json-format (RSA-OAEP-256)
* String encodedString: Base64 encoded data ( maximum: 50 MiB / 30 MiB content / 60 MiB request size )
* String resultAsBase64: Off / On (Default: Off)

| Result | Description |  Format |
| ----------- | ----------- |  ----------- |
| HTTP 200 |decrypted string, optionally encoded as base64 | binary or text with charset UTF-8 |
| HTTP 422 | missing parameter | HTML |
| HTTP 400 | verification failed | HTML |

## /verify servlet parameter
* String jwkSet: public keys in json-format (PS512)
* String securityEventToken: content to verify ( maximum: 10 MiB content / 15 MiB request size )
* String keyId: key to choose from public key list (p.e. 32858147-f090-43a9-b2fd-d26ae5b41c03)

| Result | Description |  Format |
| ----------- | ----------- |  ----------- |
| HTTP 200 | verification succeed | JSON |
| HTTP 422 | missing parameter | HTML |
| HTTP 400 | verification failed | HTML |

## Run tests
`mvn test`

## Build
`mvn package verify`

## Upgrade and release
* set new snapshot version in pom.xml
* check dependent libraries for updates
* run tests and build
* tag the new release
* create github release

## Installation
* either build or download release war from https://github.com/omaster395464gh/jose-utils/packages/1491033
* rename target/jose-utils*.war to jose-utils.war
* copy jose-utils.war to tomcat webapps folder
* open server url http(s)://servername:port/jose-utils/ and check the servlet samples

### Debugging
Add to Tomcat logging.properties:
```
de.pdv.demo.level = ALL
```
### Usage Oracle PL/SQL
``` sql
set serveroutput on size unlimited define off
declare
  p_wallet_path     varchar2(1000);  -- := 'file:?/_wallet/';
  p_wallet_password varchar2(1000);  -- := 'changeit';
  p_url             varchar2(1000)  := 'http(s)://server:port/jose-utils/decrypt';  
  p_proxy_override  varchar2(1000);  -- := 'http://localhost:8888';
  l_result_clob     clob;
  l_result_blob     blob;
  l_privateKey      clob := '{"alg":"RSA-OAEP-256","d":"pVx...di4","kty":"RSA","n":"5Ew...SvA"}';
  l_encodedString   clob := 'eyJ...h_A';
  l_boundary        varchar2(100) := '472D11119A46B891';
  
  function get_multipart_as_clob(resultAsBase64 varchar2 := 'off' ) return clob 
  as
    l_crlf          varchar2(2) := CHR(13)||CHR(10);
    l_request_clob  clob;
  begin
    l_request_clob := '--'||l_boundary||l_crlf||'Content-Disposition: form-data; name="privateKey"; filename="privateKey.txt"';
    l_request_clob := l_request_clob || l_crlf || 'Content-Type: text/plain';
    l_request_clob := l_request_clob || l_crlf ;
    l_request_clob := l_request_clob || l_crlf || l_privateKey;
    l_request_clob := l_request_clob || l_crlf || '--'||l_boundary||l_crlf||'Content-Disposition: form-data; name="encodedString"; filename="huge.base64"';
    l_request_clob := l_request_clob || l_crlf || 'Content-Type: application/octet-stream';
    l_request_clob := l_request_clob || l_crlf || l_crlf ||l_encodedString;
    l_request_clob := l_request_clob || l_crlf || '--'||l_boundary||l_crlf||'Content-Disposition: form-data; name="resultAsBase64"';
    l_request_clob := l_request_clob || l_crlf || l_crlf || resultAsBase64;    -- on / off (default: off)
    l_request_clob := l_request_clob || l_crlf || '--'||l_boundary||'--' || l_crlf;
    return l_request_clob;
  end;
 
begin
    -- select your blob or clob data here
    -- select blob_data into l_encodedString from my_table;

    dbms_output.put_line ('BLOB Sample');
    apex_web_service.g_request_headers(1).name := 'content-type';
    apex_web_service.g_request_headers(1).value := 'multipart/form-data; boundary='||l_boundary;
    l_result_blob := apex_web_service.make_rest_request_b(
        p_url => p_url
        , p_http_method => 'POST'
        , p_proxy_override => p_proxy_override
        , p_body => get_multipart_as_clob()
        , p_wallet_path => p_wallet_path
        , p_wallet_pwd => p_wallet_password
    );
    dbms_output.put_line ('Code : '||apex_web_service.g_status_code);
    dbms_output.put_line (utl_http.get_detailed_sqlerrm);
    dbms_output.put_line ('Length blob: '||dbms_lob.getlength(l_result_blob));
    -------------
    dbms_output.put_line ('CLOB Sample');
    apex_web_service.g_request_headers(1).name := 'content-type';
    apex_web_service.g_request_headers(1).value := 'multipart/form-data; boundary='||l_boundary;
    l_result_clob := apex_web_service.make_rest_request(
        p_url => p_url
        , p_http_method => 'POST'
        , p_proxy_override => p_proxy_override
        , p_body => get_multipart_as_clob( resultAsBase64 => 'on' )
        , p_wallet_path => p_wallet_path
        , p_wallet_pwd => p_wallet_password
    );
    dbms_output.put_line ('Code : '||apex_web_service.g_status_code);
    dbms_output.put_line (utl_http.get_detailed_sqlerrm);
    dbms_output.put_line ('Length clob: '||length(l_result_clob));
    dbms_output.put_line ('Length blob: '||length(apex_web_service.clobbase642blob(l_result_clob)));
end;
/
```

### Deploy to tomcat 9.x (IntelliJ / Netbeans)
Run http://localhost:port/

Example:
* http://localhost:8080/jose_utils_war_exploded/
* http://localhost:8080/jose_utils_war_exploded/monitoring

