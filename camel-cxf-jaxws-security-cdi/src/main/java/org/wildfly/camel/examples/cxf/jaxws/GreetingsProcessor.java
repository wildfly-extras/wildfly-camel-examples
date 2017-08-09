/*
 * #%L
 * Wildfly Camel :: Example :: Camel CXF JAX-WS
 * %%
 * Copyright (C) 2013 - 2016 RedHat
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

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class GreetingsProcessor implements Processor {
    /**
     * Simple processor to return a response from the web service client invocation.
     *
     * The original @WebParam values are contained within the exchange body, and these are used to build
     * a 'greetings' response.
     */
    @Override
    public void process(Exchange exchange) throws Exception {
        Object[] args = exchange.getIn().getBody(Object[].class);
        exchange.getOut().setBody(args[0] + " " + args[1]);
    }
}
