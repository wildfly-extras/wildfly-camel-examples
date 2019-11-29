/*
 * #%L
 * Wildfly Camel :: Example :: Camel CXF JAX-WS CDI XML
 * %%
 * Copyright (C) 2013 - 2014 RedHat
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.wildfly.camel.examples.cxf.jaxws;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.camel.CamelContext;
import org.apache.camel.Processor;
import org.apache.camel.cdi.ImportResource;
import org.apache.camel.component.cxf.CxfEndpoint;

/**
 *
 * This example imports a Camel XML configuration file from the classpath using the {@code ImportResource} annotation.
 *
 * The imported Camel XML file configures a Camel context that references CDI beans declared in this class.
 *
 */
@Named("cxf_cdi_xml_app")
@ImportResource("cxfws-cdi-xml-camel-context.xml")
public class Application {

    private static final String CXF_ENDPOINT_URI = "cxf:http://localhost:8080/webservices/greeting-cdi-xml";

    @Inject
    CamelContext context;

    @Named("greetingsProcessor")
    @Produces
    public Processor createGreetingsProcessor() {
        return new GreetingsProcessor();
    }

    @Named("cxfConsumer")
    @Produces
    public CxfEndpoint createCxfConsumer() {
        CxfEndpoint cxfFromEndpoint = context.getEndpoint(CXF_ENDPOINT_URI, CxfEndpoint.class);
        cxfFromEndpoint.setServiceClass(GreetingService.class);
        return cxfFromEndpoint;
    }

    @Named("cxfProducer")
    @Produces
    public CxfEndpoint createCxfProducer() {
        CxfEndpoint cxfToEndpoint = context.getEndpoint(CXF_ENDPOINT_URI, CxfEndpoint.class);
        cxfToEndpoint.setServiceClass(GreetingService.class);
        return cxfToEndpoint;
    }
}
