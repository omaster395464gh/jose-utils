<%@ page import="java.util.ResourceBundle" %>

<%
    ResourceBundle labels = ResourceBundle.getBundle("demo");
    String sEncMetaData = labels.getString("data.enc");
    String sEncKey = labels.getString("data.key");
%>

<!doctype html>
<html lang="en">
<!-- <html lang="en" data-theme="dark"> -->
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="Servlet demo for decrypt json file with Nimbus JOSE+JWT library">
    <link rel="stylesheet" href="webjars/pico/css/pico.min.css">
    <title>Decrypt json with Nimbus JOSE+JWT library</title>
</head>
<body>
<main class="container">
    <hgroup>
        <h1>Decrypt json with Nimbus JOSE+JWT library</h1>
        <h2>Links</h2>
    </hgroup>
    <ul>
        <li>Demo: <a href="decrypt" data-tooltip="Run decrypt servlet with demo data">decrypt servlet</a></li>
        <li>Monitoring: <a href="monitoring" data-tooltip="Go to JavaMelody Monitoring page">JavaMelody</a></li>
    </ul>
    <hgroup>
        <h3>Sample calls for RSA-OAEP-256 decryption</h3>
    </hgroup>
    <!-- Secondary -->
    <details open>
        <summary role="button" class="secondary">Sample 1: Post form (application/x-www-form-urlencoded)</summary>
        <form method="post" action="decrypt" >
            <div class="grid">
                <label for="privateKey">
                    privateKey
                    <textarea type="text" id="privateKey" name="privateKey" placeholder="enter private key (format: json)" required><%= sEncKey %></textarea>
                </label>
                <label for="encodedString">
                    encodedString
                    <textarea type="text" id="encodedString" name="encodedString" placeholder="enter base64 encoded string (charset: utf-8, format: json)" required><%= sEncMetaData %></textarea>
                </label>
            </div>
            <button type="submit">Submit</button>
        </form>
    </details>
    <details>
        <summary role="button" class="secondary">Sample 2: curl</summary>
        <pre>data.txt
-------
privateKey=<%= sEncKey %>&encodedString=<%= sEncMetaData %>
-------
        curl -vvv  http://localhost:8080/demo_jose_servlet_war_exploded/decrypt -d "@data.txt"
-------
> POST ${pageContext.request.contextPath}/decrypt HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.79.1
> Accept: */*
> Content-Length: 5082
> Content-Type: application/x-www-form-urlencoded
>
* Mark bundle as not supporting multiuse
< HTTP/1.1 200
< Content-Type: application/json;charset=UTF-8
< Transfer-Encoding: chunked
< Date: Sun, 26 Jun 2022 17:54:12 GMT
<
{"additionalReferenceInfo":{"x-applicant":{"login":{"identifier":"156","authorizer":"https://govos.de"}},"senderReferenc
e":"ThAVEL-Test-9879","applicationDate":"2022-05-04","x-sender":{"product":{"versiom":"14.5.0","name":"GovOS","descripti
on":"Governmental Operation System (GovOS)","manufacturer":{"name":"FJD Information Technologies AG","description":""}},
"address":"https://thavelt.thueringen.de/thavelt","name":"ThAVEL-Test","description":"Thüringer Antragssystem für Verwal
tungsleistungen TEST"}},"publicServiceType":{"identifier":"urn:de:govos:parent:a3builder-7676-qs","name":"QS test"},"con
tentStructure":{"attachments":[{"purpose":"form","description":"Antragsformular","mimeType":"application/pdf","attachmen
tId":"654bf3b3-787d-4606-8913-efd769f69db7","hash":{"type":"sha512","content":"868cccc243ca7736a2c065ad6cf57c50f5d71145c
d0e9bf9817965166312983fb4569c623e53a7c162c8de0a5b7b80aeee6cb47824a9606e2c64031d62e96f66"}}],"data":{"submissionSchema":{
"schemaUri":"urn:de:govos:data","mimeType":"application/json"},"hash":{"type":"sha512","content":"dbd374a0af15caa979b326
dc113fc56420c0a564bd630a42a03fe9e05a5c26a93531fcd9fb93c46f1daddb43dd8b52ae45584a925bec077676d02c02715b9356"}}}}* Connect
ion #0 to host localhost left intact
    </pre>
    </details>
    <details open>
        <summary role="button" class="secondary">Sample 3: Oracle PL/SQL <a href="https://oracle-base.com/articles/misc/utl_http-and-ssl">Source: oracle-base</a></summary>
        <pre>
set serveroutput on size unlimited define off
declare
  p_wallet_path varchar2(1000); -- := 'file:?/_wallet/';
  p_wallet_password varchar2(1000); -- := 'changeit';
  p_url varchar2(1000) := 'http://server:port${pageContext.request.contextPath}/decrypt';
  l_http_request   UTL_HTTP.req;
  l_http_response  UTL_HTTP.resp;
  l_text           varchar2(32767);
  l_result         clob;
  l_params         varchar2(32767) := 'privateKey=<%= sEncKey %>&encodedString=<%= sEncMetaData %>';
begin
  -- Make a HTTP request and get the response.
  l_http_request  := UTL_HTTP.begin_request( url => p_url, method => 'POST');

   IF p_wallet_path IS NOT NULL AND p_wallet_password IS NOT NULL THEN
     UTL_HTTP.set_wallet('file:' || p_wallet_path, p_wallet_password);
   end if;
   UTL_HTTP.SET_HEADER (r      =>  l_http_request, name   =>  'Content-Type',   value  =>  'application/x-www-form-urlencoded');
   UTL_HTTP.SET_HEADER (r      =>  l_http_request, name   =>   'Content-Length', value  => length(l_params) );
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
        </pre>
    </details>

    <hgroup>
        <h2>Debugging</h2>
        <h3>Add to tomcat conf/logging.properties</h3>
    </hgroup>
    <pre>org.apache.tomcat.util.http.Parameters.level = ALL
de.pdv.demo.level = ALL</pre>

    <h2>Server Info / Environment</h2>
    <pre>
Server Version:   <%= application.getServerInfo() %>
Servlet Version:  <%= application.getMajorVersion() %>.<%= application.getMinorVersion() %>
JSP Version:      <%= JspFactory.getDefaultFactory().getEngineInfo().getSpecificationVersion() %>
Context Path:     ${pageContext.request.contextPath}

java.version:     ${System.getProperty("java.version")}
java.vm.vendor:   ${System.getProperty("java.vm.vendor")}
user.country:     ${System.getProperty("user.country")}
user.language:    ${System.getProperty("user.language")}
user.name:        ${System.getProperty("user.name")}
user.timezone:    ${System.getProperty("user.timezone")}
os.name:          ${System.getProperty("os.name")}
os.version:       ${System.getProperty("os.version")}
file.encoding:    ${System.getProperty("file.encoding")}
sun.jnu.encoding: ${System.getProperty("sun.jnu.encoding")}</pre>
    <p>page built with <a href="https://picocss.com/">Pico.css</a>, decryption with <a href="https://bitbucket.org/connect2id/nimbus-jose-jwt">Nimbus JOSE+JWT (connect2id)</a> </p>
</main>
</body>
</html>