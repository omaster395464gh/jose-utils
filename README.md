# demo-jose-servlet
Servlet demo for decrypt json file with Nimbus JOSE+JWT library

* 3 samples for post form, curl and oracle pl/sql 
* Nimbus JOSE+JWT (connect2id) for decryption
  https://bitbucket.org/connect2id/nimbus-jose-jwt
* Uses JavaMelody for monitoring
  https://github.com/javamelody/javamelody/wiki
* Uses Pico.css webjar for elegant styles with a minimal css framework
  https://picocss.com/
* Use Java 8 LTS (also tested with Java 17 LTS and Java 11 LTS)
* Demo data for curl / jsp: [src/main/resources/demo.properties](src/main/resources/demo.properties)

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
  p_wallet_path varchar2(1000); -- := 'file:?/_wallet/';
  p_wallet_password varchar2(1000); -- := 'changeit';
  p_url varchar2(1000) := 'http://server:port/demo-jose-servlet/decrypt';
  l_http_request   UTL_HTTP.req;
  l_http_response  UTL_HTTP.resp;
  l_text           varchar2(32767);
  l_result         clob;
  l_params         varchar2(32767) := 'privateKey={"alg":"RSA-OAEP-256","d":"pVx...di4","kty":"RSA","n":"5Ew...SvA"}&encodedString=eyJ...h_A';
begin
  -- Make a HTTP request and get the response.
  l_http_request  := UTL_HTTP.begin_request( url => p_url, method => 'POST');

   IF p_wallet_path IS NOT NULL AND p_wallet_password IS NOT NULL THEN
     UTL_HTTP.set_wallet('file:' || p_wallet_path, p_wallet_password);
   end if;
   UTL_HTTP.SET_HEADER (r      =>  l_http_request, name   =>  'Content-Type',   value  =>  'application/x-www-form-urlencoded');
   UTL_HTTP.SET_HEADER (r      =>  l_http_request, name   =>  'Content-Length', value  => length(l_params) );
   UTL_HTTP.SET_BODY_CHARSET ('AL32UTF8');
   UTL_HTTP.WRITE_TEXT (r      =>  l_http_request, data   =>   l_params);
   l_http_response := UTL_HTTP.get_response(l_http_request );
   l_result := '';
   begin
     loop
       UTL_HTTP.read_text(l_http_response, l_text, 32766);
       DBMS_OUTPUT.put_line (l_text);
       l_result := l_result || l_text;
     end loop;
   exception
     WHEN UTL_HTTP.end_of_body THEN
       UTL_HTTP.end_response(l_http_response);
   end;
end;
/
```

### Deploy to tomcat 9.x (IntelliJ / Netbeans)
Run http://localhost:port/

Example:
* http://localhost:8080/demo_jose_servlet_war_exploded/
* http://localhost:8080/demo_jose_servlet_war_exploded/monitoring

