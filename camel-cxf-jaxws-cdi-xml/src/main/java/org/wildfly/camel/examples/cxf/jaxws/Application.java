/**
 *
 */
package org.wildfly.camel.examples.cxf.jaxws;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.camel.CamelContext;
import org.apache.camel.Processor;
import org.apache.camel.cdi.ContextName;
import org.apache.camel.cdi.ImportResource;
import org.apache.camel.component.cxf.CxfComponent;
import org.apache.camel.component.cxf.CxfEndpoint;

/**
 * Based on https://github.com/apache/camel/blob/master/examples/camel-example-cdi-xml/src/main/java/org/apache/camel/example/cdi/xml/Application.java
 *
 * This example imports a Camel XML configuration file from the classpath using the {@code ImportResource} annotation.
 * <p>
 *
 * The imported Camel XML file configures a Camel context that references CDI beans declared in this class.
 *
 *
 * @author Jochen Riedlinger
 *
 */
@Named("cxf_cdi_xml_app")
@ImportResource("cxfws-camel-context.xml")
public class Application {

	@Inject
	@ContextName("cxfws-camel-context")
	CamelContext context;

	// <!-- now producedd in org.wildfly.camel.examples.cxf.jaxws.Application as CDI beans
	// <cxf:cxfEndpoint
	// address="http://localhost:8080/webservices/greeting"
	// id="cxfConsumer" serviceClass="org.wildfly.camel.examples.cxf.jaxws.GreetingService"/>
	// <cxf:cxfEndpoint
	// address="http://localhost:8080/webservices/greeting"
	// id="cxfProducer" serviceClass="org.wildfly.camel.examples.cxf.jaxws.GreetingService"/>
	// <bean
	// class="org.wildfly.camel.examples.cxf.jaxws.GreetingsProcessor" id="greetingsProcessor"/>
	//
	// -->


	@Named("greetingsProcessor")
	@Produces
	public Processor produceGreetingsProcessor() {
		return new GreetingsProcessor();
	}

	@Named("cxfConsumer")
	@Produces
	public CxfEndpoint produceCxfConsumer() {
		CxfComponent cxfComponent = new CxfComponent(this.context);
		CxfEndpoint cxfFromEndpoint = new CxfEndpoint("http://localhost:8080/webservices/greeting", cxfComponent);
		cxfFromEndpoint.setServiceClass(org.wildfly.camel.examples.cxf.jaxws.GreetingService.class);
		return cxfFromEndpoint;
	}

	@Named("cxfProducer")
	@Produces
	public CxfEndpoint produceCxfProducer() {
		CxfComponent cxfComponent = new CxfComponent(this.context);
		CxfEndpoint cxfToEndpoint = new CxfEndpoint("http://localhost:8080/webservices/greeting", cxfComponent);
		cxfToEndpoint.setServiceClass(org.wildfly.camel.examples.cxf.jaxws.GreetingService.class);
		return cxfToEndpoint;
	}

}
