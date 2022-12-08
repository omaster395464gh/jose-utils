# /verify servlet 
[Home](../README.md)
## Servlet parameter
* String jwkSet: set of private keys in json-format (PS512)
* String header: header to sign 
* String payload: claimset to sign ( maximum: 10 MiB content / 15 MiB request size )

| Result | Description          |  Format |
| ----------- |----------------------|  ----------- |
| HTTP 200 | serialized jwt token | JSON |
| HTTP 422 | missing parameter    | HTML |
| HTTP 400 | sign failed          | HTML |

## Usage: Oracle PL/SQL
``` sql
set serveroutput on
declare
  p_wallet_path     varchar2(1000);  -- := 'file:?/_wallet/';
  p_wallet_password varchar2(1000);  -- := 'changeit';
  p_url             varchar2(1000)  := 'http(s)://server:port/jose-utils/sign';
  p_proxy_override  varchar2(1000);  -- := 'http://localhost:8888';
  l_result          clob;
  l_jwkSet          clob := '{"keys": [{"alg": "PS512","d": "Xm0Ua2QK...ZA"}]}';
  l_header          clob := '{"alg": "PS512","typ": "secevent+jwt","kid": "6508dbcd-ab3b-4edb-a42b-37bc69f38fed"}';
  l_payload         clob := '{"iss": "https://api.fitko.de/fit-connect/","sub": "submission:f65feab2-4883-4dff-85fb-169448545d9f","txn": "case:f73d30c6-8894-4444-8687-00ae756fea90","iat": 1670537629,"jti": "52121063-9372-4eb6-a43f-6ba3e7b8c96f","events": {"https://schema.fitko.de/fit-connect/events/accept-submission": {}}}';
  l_boundary        varchar2(100) := '572D11119A46B891';

  function get_multipart_as_clob return clob
  as
    l_crlf          varchar2(2) := CHR(13)||CHR(10);
    l_request_clob  clob;
  begin
    l_request_clob := '--'||l_boundary||l_crlf||'Content-Disposition: form-data; name="jwkSet"; filename="jwkSet.json"';
    l_request_clob := l_request_clob || l_crlf || 'Content-Type: application/octet-stream';
    l_request_clob := l_request_clob || l_crlf || l_crlf || l_jwkSet;
    l_request_clob := l_request_clob || l_crlf || '--'||l_boundary||l_crlf||'Content-Disposition: form-data; name="header"; filename="header.txt"';
    l_request_clob := l_request_clob || l_crlf || 'Content-Type: application/octet-stream';
    l_request_clob := l_request_clob || l_crlf || l_crlf ||l_header;
    l_request_clob := l_request_clob || l_crlf || '--'||l_boundary||l_crlf||'Content-Disposition: form-data; name="payload"; filename="payload.txt"';
    l_request_clob := l_request_clob || l_crlf || l_crlf || l_payload;
    l_request_clob := l_request_clob || l_crlf || '--'||l_boundary||'--' || l_crlf;
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
   dbms_output.put_line ('Length: '||dbms_lob.getlength(l_result));
   dbms_output.put_line ('Content: '||l_result);
end;
/
```