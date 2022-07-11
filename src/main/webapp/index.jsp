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
        <li>Demo 2: <a href="decrypt" data-tooltip="Run decrypt servlet with demo data">decrypt servlet</a></li>
        <li>Monitoring: <a href="monitoring" data-tooltip="Go to JavaMelody Monitoring page">JavaMelody</a></li>
    </ul>
    <hgroup>
        <h3>Sample calls for RSA-OAEP-256 decryption</h3>
    </hgroup>
    <!-- Secondary -->
    <details open>
        <summary role="button" class="secondary">Sample 1: Post form to decrypt (multipart/form-data)</summary>
        <form method="post" action="decrypt" enctype="multipart/form-data">
            <div class="grid">
                <label for="privateKey2">
                    privateKey
                    <textarea type="text" id="privateKey2" name="privateKey" placeholder="enter private key (format: json)" required><%= sEncKey %></textarea>
                </label>
                <label for="encodedString2">
                    encodedString
                    <textarea type="text" id="encodedString2" name="encodedString" placeholder="enter base64 encoded string (charset: utf-8, format: json)" required><%= sEncMetaData %></textarea>
                </label>
            </div>
            <button type="submit">Submit</button>
        </form>
    </details>
    <details>
        <summary role="button" class="secondary">Sample 2: Post file to decrypt (multipart/form-data) - max 20 MiB </summary>
        <p>Download <a href="demo/privateKey.txt">privateKey.txt</a> and <a href="demo/encodedString.txt">encodedString.txt</a> </p>
        <form method="post" action="decrypt" enctype="multipart/form-data">
            <div class="grid">
                <label for="privateKey1">
                    privateKey (select file with json content)
                    <input type="file" id="privateKey1" name="privateKey"/>
                </label>
                <label for="encodedString1">
                    encodedString (select file with base64 content)
                    <input type="file" id="encodedString1" name="encodedString"/>
                </label>
            </div>
            <button type="submit">Submit</button>
        </form>
    </details>

    <details>
        <summary role="button" class="secondary">Sample 3: curl</summary>
        <p>Download <a href="demo/privateKey.txt">privateKey.txt</a> and <a href="demo/encodedString.txt">encodedString.txt</a> </p>
        <pre>
-------
curl -vvv http://localhost.fiddler:8080${pageContext.request.contextPath}/decrypt -F "privateKey=@privateKey.txt" -F "encodedString=@encodedString.txt"
-------
> POST http://localhost.fiddler:8080/demo_jose_servlet_war_exploded/decrypt HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.79.1
> Accept: */*
> Proxy-Connection: Keep-Alive
> Content-Length: 5412
> Content-Type: multipart/form-data; boundary=------------------------41621a951b5d72c5
>
* We are completely uploaded and fine
* Mark bundle as not supporting multiuse
< HTTP/1.1 200
< Content-Type: application/json;charset=UTF-8
< Transfer-Encoding: chunked
< Date: Sun, 10 Jul 2022 17:41:30 GMT
< Keep-Alive: timeout=20
< Connection: keep-alive
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
        <summary role="button" class="secondary">Sample 4: Oracle APEX (any version) decrypt with apex_web_service.make_rest_request</summary>
        <pre>

set serveroutput on size unlimited define off
declare
  p_wallet_path     varchar2(1000);  -- := 'file:?/_wallet/';
  p_wallet_password varchar2(1000);  -- := 'changeit';
  p_url             varchar2(1000)  := 'http(s)://server:port${pageContext.request.contextPath}/decrypt';
  p_proxy_override  varchar2(1000);  -- := 'http://localhost:8888';
  l_result          clob;
  l_privateKey      clob := '<%= sEncKey %>';
  l_encodedString   clob := '<%= sEncMetaData %>';
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
        </pre>
    </details>

    <details>
        <summary role="button" class="secondary">Sample 5: Oracle APEX (20.x+) decrypt with apex_web_service.make_rest_request</summary>
        <pre>
set serveroutput on size unlimited define off
declare
  p_wallet_path     varchar2(1000);  -- := 'file:?/_wallet/';
  p_wallet_password varchar2(1000);  -- := 'changeit';
  p_url             varchar2(1000)  := 'http(s)://server:port${pageContext.request.contextPath}/decrypt';
  p_proxy_override  varchar2(1000);  -- := 'http://localhost:8888';
  l_request_blob    blob;
  l_result          clob;
  l_privateKey      clob := '<%= sEncKey %>';
  l_encodedString   clob := '<%= sEncMetaData %>';
  l_boundary        varchar2(100) := '472D11119A46B891';
  l_multipart apex_web_service.t_multipart_parts;
begin
    apex_web_service.APPEND_TO_MULTIPART (
        p_multipart    => l_multipart,
        p_name         => 'privateKey',
        p_body         => l_privateKey );
    apex_web_service.APPEND_TO_MULTIPART (
        p_multipart    => l_multipart,
        p_name         => 'encodedString',
        p_body         => l_encodedString );
    l_request_blob := apex_web_service.generate_request_body ( p_multipart => l_multipart );
    
    l_result := apex_web_service.make_rest_request(
        p_url => p_url
        , p_http_method => 'POST'
        , p_proxy_override => p_proxy_override
        , p_body_blob => l_request_blob
        , p_wallet_path => p_wallet_path
        , p_wallet_pwd => p_wallet_password
    );
   dbms_output.put_line ('Code : '||apex_web_service.g_status_code);
   dbms_output.put_line (utl_http.get_detailed_sqlerrm);
   dbms_output.put_line ('Body : '||l_result);   
end;
/
        </pre>
    </details>

    <details>
        <summary role="button" class="secondary">Sample 6: Oracle PL/SQL</summary>
        <pre>
set serveroutput on size unlimited define off
declare
  p_wallet_path     varchar2(1000);     -- := 'file:?/_wallet/';
  p_wallet_password varchar2(1000); -- := 'changeit';
  p_url             varchar2(1000)  := 'http(s)://server:port${pageContext.request.contextPath}/decrypt';
  p_proxy_override  varchar2(1000); --  := 'http://localhost:8888';
  l_result          clob;
  l_privateKey      clob := '<%= sEncKey %>';
  l_encodedString   clob := '<%= sEncMetaData %>';
  l_boundary        varchar2(100) := '472D11119A46B891';

  function PostRecClob(url varchar2, request clob) return clob as
      req utl_http.req;
      resp utl_http.resp;
      length binary_integer;
      response clob;
      buffer varchar2(32767);
      amount pls_integer := 32767;
      offset pls_integer := 1;
  begin
      req := utl_http.begin_request(url, 'POST', 'HTTP/1.1');
      utl_http.set_header(req, 'Content-Type', 'multipart/form-data; boundary='||l_boundary);
      utl_http.set_header (r => req, name => 'Content-Length', value => dbms_lob.getlength(request) );
      -- utl_http.set_header(req, 'Transfer-Encoding', 'chunked');
      length := dbms_lob.getlength(request);
      UTL_HTTP.SET_BODY_CHARSET ('AL32UTF8');

      while(offset < length) loop
        dbms_lob.read(request, amount, offset, buffer);
        utl_http.write_text(req, buffer);
        offset := offset + amount;
      end loop;
      resp := utl_http.get_response(req);
    
    -- Code to read the response in 32k chunks
      dbms_lob.createtemporary(response, false);
      begin
       loop
        utl_http.read_text(resp, buffer);
        dbms_lob.writeappend(response, dbms_lob.getlength(buffer), buffer);
       end loop;
      utl_http.end_response(resp);
      exception
       when utl_http.end_of_body then
       utl_http.end_response(resp);
      end;
    return response;
end;

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
   IF p_wallet_path IS NOT NULL AND p_wallet_password IS NOT NULL THEN
     UTL_HTTP.set_wallet(p_wallet_path, p_wallet_password);
   end if;
   IF p_proxy_override IS NOT NULL THEN
     UTL_HTTP.set_proxy(p_proxy_override);   
   END IF;
   l_result := PostRecClob(p_url, get_multipart_as_clob());
   dbms_output.put_line (utl_http.get_detailed_sqlerrm);
   dbms_output.put_line ('Body : '||substr(l_result,1,10000));   
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