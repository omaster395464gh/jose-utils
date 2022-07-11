# demo-jose-servlet
Servlet demo for decrypt json file with Nimbus JOSE+JWT library

* 6 samples for post form, curl and oracle pl/sql 
* Nimbus JOSE+JWT (connect2id) for decryption
  https://bitbucket.org/connect2id/nimbus-jose-jwt
* Uses JavaMelody for monitoring
  https://github.com/javamelody/javamelody/wiki
* Uses Pico.css webjar for elegant styles with a minimal css framework
  https://picocss.com/
* Use Java 8 LTS (also tested with Java 17 LTS and Java 11 LTS)
* Demo data for curl / jsp: <br/>
  [src/main/resources/demo.properties](src/main/resources/demo.properties)<br/>
  [src/main/webapp/demo/privateKey.txt](src/main/webapp/demo/privateKey.txt)<br/>
  [src/main/webapp/demo/encodedString.txt](src/main/webapp/demo/encodedString.txt)

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
* either build or download release war from https://github.com/omaster395464gh/demo-jose-servlet/packages/1491033
* rename target/demo-jose-servlet*.war to demo-jose-servlet.war
* copy demo-jose-servlet.war to tomcat webapps folder
* open server url http(s)://servername:port/demo-jose-servlet/ and check the servlet samples

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
  p_url             varchar2(1000)  := 'http(s)://server:port/demo-jose-servlet/decrypt';  
  p_proxy_override  varchar2(1000);  -- := 'http://localhost:8888';
  l_result          clob;
  l_privateKey      clob := '{"alg":"RSA-OAEP-256","d":"pVx...di4","kty":"RSA","n":"5Ew...SvA"}';
  l_encodedString   clob := 'eyJ...h_A';
  l_boundary        varchar2(100) := '472D11119A46B891';
  
  function get_multipart_as_clob return clob 
  as
    l_crlf          varchar2(2) := CHR(13)||CHR(10);
    l_request_clob  clob;
  begin
    l_request_clob := '--'||l_boundary||l_crlf||'Content-Disposition: form-data; name="privateKey"';
    l_request_clob := l_request_clob || l_crlf ;
    l_request_clob := l_request_clob || l_crlf || l_privateKey;
    l_request_clob := l_request_clob || l_crlf || '--'||l_boundary||l_crlf||'Content-Disposition: form-data; name="encodedString"';
    l_request_clob := l_request_clob || l_crlf || l_crlf ||l_encodedString;
    l_request_clob := l_request_clob || l_crlf || '--'||l_boundary||'--';
    return l_request_clob;
  end;
 
begin
    apex_web_service.g_request_headers(1).name := 'content-type';
    apex_web_service.g_request_headers(1).value := 'multipart/form-data; boundary='||l_boundary;
    l_result := apex_web_service.make_rest_request(
        p_url => p_url
        , p_http_method => 'POST'
        , p_proxy_override => p_proxy_override
        , p_body => get_multipart_as_clob()
        , p_wallet_path => p_wallet_path
        , p_wallet_pwd => p_wallet_password
    );
   dbms_output.put_line ('Code : '||apex_web_service.g_status_code);
   dbms_output.put_line (utl_http.get_detailed_sqlerrm);
   dbms_output.put_line ('Body : '||l_result);   
end;
/
```

### Deploy to tomcat 9.x (IntelliJ / Netbeans)
Run http://localhost:port/

Example:
* http://localhost:8080/demo_jose_servlet_war_exploded/
* http://localhost:8080/demo_jose_servlet_war_exploded/monitoring

