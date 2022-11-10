# /verify servlet
[Home](../README.md)

## Servlet parameter
* String privateKey: Private Key in json-format (RSA-OAEP-256)
* String encodedString: Base64 encoded data ( maximum: 50 MiB / 30 MiB content / 60 MiB request size )
* String resultAsBase64: Off / On (Default: Off)

| Result | Description |  Format |
| ----------- | ----------- |  ----------- |
| HTTP 200 |decrypted string, optionally encoded as base64 | binary or text with charset UTF-8 |
| HTTP 422 | missing parameter | HTML |
| HTTP 400 | verification failed | HTML |

## Usage: Oracle PL/SQL
``` sql
declare
  p_wallet_path     varchar2(1000);  -- := 'file:?/_wallet/';
  p_wallet_password varchar2(1000);  -- := 'changeit';
  p_url             varchar2(1000)  := 'http(s)://server:port/jose-utils/verify';
  p_proxy_override  varchar2(1000);  -- := 'http://localhost:8888';
  l_result          clob;
  l_jwkSet          clob := '{"keys": [{"alg": "PS512","e": "AQAB","key_ops": ["verify"],"kid": "32858147...",...}]}';
  l_securityEventToken clob := 'eyJra...Kuks';
  l_keyId           varchar2(200) := '32858147...';
  l_boundary        varchar2(100) := '572D11119A46B891'; -- random value

  function get_multipart_as_clob(resultAsBase64 varchar2 := 'off' ) return clob
  as
    l_crlf          varchar2(2) := CHR(13)||CHR(10);
    l_request_clob  clob;
  begin
    l_request_clob := '--'||l_boundary||l_crlf||'Content-Disposition: form-data; name="jwkSet"; filename="jwkSet.json"';
    l_request_clob := l_request_clob || l_crlf || 'Content-Type: application/octet-stream';
    l_request_clob := l_request_clob || l_crlf || l_crlf || l_jwkSet;
    l_request_clob := l_request_clob || l_crlf || '--'||l_boundary||l_crlf||'Content-Disposition: form-data; name="securityEventToken"; filename="securityEventToken.bin"';
    l_request_clob := l_request_clob || l_crlf || 'Content-Type: application/octet-stream';
    l_request_clob := l_request_clob || l_crlf || l_crlf ||l_securityEventToken;
    l_request_clob := l_request_clob || l_crlf || '--'||l_boundary||l_crlf||'Content-Disposition: form-data; name="keyId"; filename="keyId.txt"';
    l_request_clob := l_request_clob || l_crlf || l_crlf || l_keyId;
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