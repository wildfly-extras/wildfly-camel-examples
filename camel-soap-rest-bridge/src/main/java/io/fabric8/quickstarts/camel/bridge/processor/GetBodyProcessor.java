package io.fabric8.quickstarts.camel.bridge.processor;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import com.ibm.wdata.WeatherRequest;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.http.common.HttpMessage;
import org.keycloak.KeycloakPrincipal;

public class GetBodyProcessor implements Processor  {
    
    public void process(Exchange exchange) throws Exception {
        
        HttpServletRequest servletRequest = ((HttpMessage)exchange.getIn()).getRequest();
        if (servletRequest != null) {
            KeycloakPrincipal userPrincipal = (KeycloakPrincipal)servletRequest.getUserPrincipal();
            if (userPrincipal != null) {
                               exchange.getIn().setHeader("UserName", userPrincipal.getName());
                exchange.setProperty("cxf.UserName", userPrincipal.getName());
                String token = userPrincipal.getKeycloakSecurityContext().getTokenString();
                exchange.setProperty("bridge.Token", token);
                
            }
        }
        WeatherRequest request = exchange.getIn().getBody(WeatherRequest.class);
        exchange.getIn().setBody(request);  
    }
}

