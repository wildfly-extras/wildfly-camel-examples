package org.wildfly.camel.examples.cxf.jaxws;

import java.util.List;

import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.ContextName;
import org.apache.camel.component.cxf.CxfComponent;
import org.apache.camel.component.cxf.CxfEndpoint;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.security.JAASLoginInterceptor;
import org.apache.cxf.message.Message;
import org.wildfly.extension.camel.CamelAware;

@Startup
@CamelAware
@ApplicationScoped
@ContextName("java-dsl-context")
public class JavaDSLRouteBuilder extends RouteBuilder {


//	@Inject
//	private CamelContext camelContext;


    @Override
    public void configure() throws Exception {


    	CxfComponent cxfComponent = new CxfComponent(getContext());
    	CxfEndpoint cxfConsumerEndpoint = new CxfEndpoint("http://localhost:8080/webservices/greeting2", cxfComponent);
//    	cxfFromEndpoint.setDataFormat(DataFormat.PAYLOAD);
    	cxfConsumerEndpoint.setServiceClass(org.wildfly.camel.examples.cxf.jaxws.GreetingService.class);
    	List<Interceptor<? extends Message>> inInterceptors = cxfConsumerEndpoint.getInInterceptors();
    	JAASLoginInterceptor jaasLoginInterceptor =  new JAASLoginInterceptor();
    	jaasLoginInterceptor.setContextName("other");
    	inInterceptors.add(jaasLoginInterceptor);



    	CxfEndpoint cxfProducerEndpoint = new CxfEndpoint("http://localhost:8080/webservices/greeting2", cxfComponent);
    	cxfProducerEndpoint.setServiceClass(org.wildfly.camel.examples.cxf.jaxws.GreetingService.class);
    	cxfProducerEndpoint.setUsername("testUser");
    	cxfProducerEndpoint.setPassword("testPassword1+");


    	from("direct:start2").to(cxfProducerEndpoint);

    	from(cxfConsumerEndpoint).process(new GreetingsProcessor());

    }
}
