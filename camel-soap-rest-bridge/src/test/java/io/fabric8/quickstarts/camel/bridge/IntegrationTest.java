/*
 *  Copyright 2005-2018 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package io.fabric8.quickstarts.camel.bridge;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.ibm.wdata.WeatherPortImpl;
import com.ibm.wdata.WeatherPortType;
import com.ibm.wdata.WeatherRequest;
import com.ibm.wdata.WeatherResponse;

import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.jaxrs.client.spec.ClientImpl.WebTargetImpl;
import org.apache.cxf.jaxrs.impl.ResponseImpl;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.keycloak.OAuth2Constants;
import org.keycloak.util.BasicAuthHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class IntegrationTest {

    public static Logger LOG = LoggerFactory.getLogger(IntegrationTest.class);
    static String WEATHER_HOST = System.getProperty("weather.service.host", "localhost");
    static String JAXWS_URI_STS = "http://" + WEATHER_HOST + ":8283/WeatherService";
   
    static QName SERVICE_QNAME = new QName("http://ibm.com/wdata", "weatherService");
    static String CAMEL_ROUTE_HOST = System
        .getProperty("camel.route.host", "http://localhost:8080");
    static String JAXRS_URL = CAMEL_ROUTE_HOST + "/bridge/camelcxf/jaxrs";
    static String SSO_URL = System.getProperty("sso.server", "http://localhost:8180");
    
    CloseableHttpClient httpClient;
    SSLContext sslContext;

    @BeforeClass
    public static void beforeClass() {
        Object implementor = new WeatherPortImpl();

      

        EndpointImpl impl = (EndpointImpl)Endpoint.publish(JAXWS_URI_STS, implementor);

        Map<String, Object> inProps = new HashMap<>();
        inProps.put("action", "Timestamp SAMLTokenSigned");
        inProps.put("signatureVerificationPropFile", "/ws-security/bob.properties");
        impl.getProperties().put("ws-security.saml2.validator", "io.fabric8.quickstarts.camel.bridge.security.Saml2Validator");

        impl.getInInterceptors().add(new WSS4JInInterceptor(inProps));
        impl.getInInterceptors().add(new LoggingInInterceptor());
        impl.getOutInterceptors().add(new LoggingOutInterceptor());
    }

    @BeforeClass
    public static void initLogging() {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    @AfterClass
    public static void cleanupLogging() {
        SLF4JBridgeHandler.uninstall();
    }

    

    
    

    @Test

    public void testRestClientWithSTS() throws Exception {

        String accessToken = fetchAccessToken();

        Client client = ClientBuilder.newClient().register(JacksonJsonProvider.class)
            .register(LoggingFeature.class);

        WeatherRequest request = new WeatherRequest();
        request.setZipcode("M3H 2H8");
       
        
        String payload = new ObjectMapper().writeValueAsString(request);
        WebTargetImpl target = (WebTargetImpl)client.target(JAXRS_URL + "/request");
        WeatherResponse response = target.request().header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .post(Entity.entity(payload, MediaType.APPLICATION_JSON), WeatherResponse.class);
        Assert.assertEquals("M3H 2H8", response.getZip());
        Assert.assertEquals("LA", response.getCity());
        Assert.assertEquals("CA", response.getState());
        Assert.assertEquals("95%", response.getHumidity());
        Assert.assertEquals("28", response.getTemperature());
    }
    
    @Test

    public void testRestClientWithIncorrectToken() throws Exception {

        String accessToken = fetchAccessToken();
        Client client = ClientBuilder.newClient().register(JacksonJsonProvider.class)
            .register(LoggingFeature.class);

        WeatherRequest request = new WeatherRequest();
        request.setZipcode("M3H 2H8");

        // POST @WeatherPortType#weatherRequest(WeatherRequest)
        String payload = new ObjectMapper().writeValueAsString(request);
        WebTargetImpl target = (WebTargetImpl)client.target(JAXRS_URL + "/request");
        try {
            target.request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken + 123)
                .post(Entity.entity(payload, MediaType.APPLICATION_JSON), WeatherResponse.class);
            fail("Should receive HTTP 401 Unauthorized with incorrect access token so can't pass RH SSO authentication");
        } catch (javax.ws.rs.NotAuthorizedException ex) {
            assertTrue(ex.getMessage().contains("HTTP 401 Unauthorized"));
        }
        
    }

    @Test

    public void testRestClientWithSTSInvalidZipCode() throws Exception {

        String accessToken = fetchAccessToken();

        Client client = ClientBuilder.newClient().register(JacksonJsonProvider.class)
            .register(LoggingFeature.class);

        WeatherRequest request = new WeatherRequest();
        request.setZipcode("M3H 278");

        // POST @WeatherPortType#weatherRequest(WeatherRequest)
        String payload = new ObjectMapper().writeValueAsString(request);

        try {
            WebTargetImpl target = (WebTargetImpl)client.target(JAXRS_URL + "/request");
                       
            target.request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .post(Entity.entity(payload, MediaType.APPLICATION_JSON), WeatherResponse.class);
            fail("should throw schema validation exception since \"M3H 278\" isn't a valid zip code");
        } catch (javax.ws.rs.WebApplicationException ex) {
            org.apache.cxf.jaxrs.impl.ResponseImpl resp = (ResponseImpl)ex.getResponse();

            InputStream is = (InputStream)resp.getEntity();
            if (is != null) {
                CachedOutputStream bos = new CachedOutputStream();
                try {
                    IOUtils.copy(is, bos);

                    bos.flush();
                    is.close();
                    bos.close();
                    String faultMessage = new String(bos.getBytes());
                    assertTrue(faultMessage
                        .contains("org.apache.cxf.interceptor.Fault: Marshalling Error: cvc-pattern-valid: Value 'M3H 278' is not facet-valid with respect to pattern '[A-Z][0-9][A-Z] [0-9][A-Z][0-9]' for type 'zipType'."));
                } catch (IOException e) {
                    throw new Fault(e);
                }
            }

        }

    }
    
   
    
    @Test
    public void testJavaClient() throws Exception {

        Service service = Service.create(new URL(JAXWS_URI_STS + "?wsdl"), SERVICE_QNAME);
        WeatherPortType port = service.getPort(WeatherPortType.class);
        Assert.assertNotNull("Address not null", port);
        try {

            WeatherRequest request = new WeatherRequest();
            request.setZipcode("M3H 2J8");
            WeatherResponse response = port.weatherRequest(request);
            fail("should fail caz no security");
            Assert.assertEquals("M3H 2J8", response.getZip());
            Assert.assertEquals("LA", response.getCity());
            Assert.assertEquals("CA", response.getState());
            Assert.assertEquals("95%", response.getHumidity());
            Assert.assertEquals("28", response.getTemperature());
        } catch (Exception ex) {
            Assert.assertEquals("A security error was encountered when verifying the message",
                                ex.getMessage());
        }
    }

    

    private String fetchAccessToken()
        throws UnsupportedEncodingException, IOException, ClientProtocolException {
        String accessToken = null;

        try (CloseableHttpClient client = HttpClients.custom().build()) {
            // "4.3. Resource Owner Password Credentials Grant"
            // from https://tools.ietf.org/html/rfc6749#section-4.3
            // we use "resource owner" credentials directly to obtain the token
            HttpPost post = new HttpPost(SSO_URL
                                         + "/auth/realms/camel-soap-rest-bridge/protocol/openid-connect/token");
            
            LinkedList<NameValuePair> params = new LinkedList<>();
            params.add(new BasicNameValuePair(OAuth2Constants.GRANT_TYPE, OAuth2Constants.PASSWORD));
            params.add(new BasicNameValuePair("username", "admin"));
            params.add(new BasicNameValuePair("password", "passw0rd"));
            UrlEncodedFormEntity postData = new UrlEncodedFormEntity(params);
            post.setEntity(postData);

            String basicAuth = BasicAuthHelper.createHeader("camel-bridge",
                                                            "f1ec716d-2262-434d-8e98-bf31b6b858d6");
            post.setHeader("Authorization", basicAuth);
            CloseableHttpResponse response = client.execute(post);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response.getEntity().getContent());
            if (json.get("error") == null) {
                accessToken = json.get("access_token").asText();
                LOG.info("token: {}", accessToken);
            } else {
                LOG.warn("error: {}, description: {}", json.get("error"), json.get("error_description"));
                fail();
            }
            response.close();
        }
        return accessToken;
    }    
}
