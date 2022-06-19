<!doctype html>
<html lang="en" data-theme="dark">
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
        <h1>Decrypt json withNimbus JOSE+JWT library</h1>
        <h2>Links</h2>
    </hgroup>
    <ul>
        <li>Demo: <a href="decrypt" data-tooltip="Run decrypt servlet with demo data">decrypt servlet</a></li>
        <li>Monitoring: <a href="monitoring" data-tooltip="Go to JavaMelody Monitoring page">JavaMelody</a></li>
    </ul>

    <hgroup>
        <h1>Sample Call for RSA-OAEP-256 decryption</h1>
        <h2>Oracle PL/SQL <a href="https://oracle-base.com/articles/misc/utl_http-and-ssl">Source: oracle-base</a> )</h2>
    </hgroup>
        <pre>
set serveroutput on size unlimited
declare
  p_wallet_path varchar2(1000) := 'file:?/_wallet/';
  p_wallet_password varchar2(1000) := 'changeit';
  p_url := varchar2(1000) := 'https://servername:port/demo-jose-servlet/decrypt'
  l_http_request   UTL_HTTP.req;
  l_http_response  UTL_HTTP.resp;
  l_text           varchar2(32767);
begin
   IF p_wallet_path IS NOT NULL AND p_wallet_password IS NOT NULL THEN
     UTL_HTTP.set_wallet('file:' || p_wallet_path, p_wallet_password);
   end if;

   -- Make a HTTP request and get the response.
   utl_http.get_request(p_url => p_url, p_method => 'POST');
   l_http_response := UTL_HTTP.get_response(l_http_request);
   begin
     loop
       UTL_HTTP.read_text(l_http_response, l_text, 32766);
       DBMS_OUTPUT.put_line (l_text);
     end loop;
   exception
     WHEN UTL_HTTP.end_of_body THEN
       UTL_HTTP.end_response(l_http_response);
   end;
end;</pre>

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
    <p>page built with <a href="https://picocss.com/">Pico.css</a>, decryption with <a href="https://bitbucket.org/connect2id/nimbus-jose-jwt">Nimbus JOSW+JWT (connect2id)</a> </p>
</main>
</body>
</html>