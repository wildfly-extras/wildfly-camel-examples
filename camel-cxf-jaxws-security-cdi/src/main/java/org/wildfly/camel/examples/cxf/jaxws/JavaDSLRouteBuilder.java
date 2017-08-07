package org.wildfly.camel.examples.cxf.jaxws;

import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.ContextName;
import org.apache.camel.component.cxf.CxfEndpoint;

@Startup
@ApplicationScoped
@ContextName("java-dsl-context")
public class JavaDSLRouteBuilder extends RouteBuilder {

	@Inject
	@Named("greetingsProcessor")
	Processor greetingsProcessor;

	@Inject
	@Named("cxfConsumerEndpoint")
	CxfEndpoint cxfConsumerEndpoint;

	@Inject
	@Named("cxfProducerEndpoint")
	CxfEndpoint cxfProducerEndpoint;


	@Override
	public void configure() throws Exception {

		from("direct:start").to(this.cxfProducerEndpoint);

		from(this.cxfConsumerEndpoint).process(this.greetingsProcessor);

	}


}
