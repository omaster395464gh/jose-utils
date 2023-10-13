# jose-utils [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=omaster395464gh_demo-jose-servlet&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=omaster395464gh_demo-jose-servlet) [![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=omaster395464gh_demo-jose-servlet&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=omaster395464gh_demo-jose-servlet)
Servlets for decrypt json file and verify sets with Nimbus JOSE+JWT library

| Servlet                     | Description                                |
|-----------------------------|--------------------------------------------|
| [/decrypt](docs/decrypt.md) | Decrypt provided data up to 30 MiB content |
| [/verify](docs/verify.md)   | Verify a set (up to 10 MiB)                              |
| [/sign](docs/sign.md)       | Sign a set (up to 10 MiB)                  |

* lots of samples for post form, curl and oracle pl/sql 
  optional output as base64 (UTF-8)
* Nimbus JOSE+JWT (connect2id) for decryption and set validation
* Uses JavaMelody for monitoring
* Uses Pico.css webjar for elegant styles with a minimal css framework
* Use Java 11 LTS (also tested with Java 17 LTS and Java 21 LTS)

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
* either build or download release war from https://github.com/omaster395464gh/jose-utils/packages/1703886
* rename target/jose-utils*.war to jose-utils.war
* copy jose-utils.war to tomcat webapps folder
* open server url http(s)://servername:port/jose-utils/ and check the servlet samples

### Debugging
Add to Tomcat logging.properties:
```
de.pdv.demo.level = ALL
```

### Deploy to tomcat 9.x (IntelliJ / Netbeans)
Run http://localhost:port/

Examples:
* http://localhost:8080/jose_utils_war_exploded/
* http://localhost:8080/jose_utils_war_exploded/monitoring

### Useful links
* https://bitbucket.org/connect2id/nimbus-jose-jwt
* https://docs.fitko.de/fit-connect/docs/receiving/decrypt/
* https://docs.fitko.de/fit-connect/docs/getting-started/event-log/set-validation/
* https://git.fitko.de/fit-connect/examples/-/blob/main/java/crypto/src/main/java/GenerateSignedToken.java
* https://github.com/javamelody/javamelody/wiki
* https://picocss.com/
* Demo data for curl / jsp: <br/>
  [src/main/resources/demo.properties](src/main/resources/demo.properties)<br/>
  [src/main/resources/jwks.json](src/main/resources/jwks.json)<br/>
  [src/main/webapp/demo/privateKey.txt](src/main/webapp/demo/privateKey.txt)<br/>
  [src/main/webapp/demo/encodedString.txt](src/main/webapp/demo/encodedString.txt)<br/>

