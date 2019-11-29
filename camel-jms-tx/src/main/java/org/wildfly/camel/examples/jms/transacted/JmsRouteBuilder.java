/*
 * #%L
 * Wildfly Camel :: Example :: Camel Transacted JMS
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
package org.wildfly.camel.examples.jms.transacted;

import javax.enterprise.context.ApplicationScoped;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JaxbDataFormat;
import org.wildfly.camel.examples.jms.transacted.model.Order;

@ApplicationScoped
public class JmsRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        /**
         * Configure JAXB so that it can discover model classes.
         */
        JaxbDataFormat jaxbDataFormat = new JaxbDataFormat();
        jaxbDataFormat.setContextPath(Order.class.getPackage().getName());

        /**
         * Configure a simple dead letter strategy. Whenever an IllegalStateException
         * is encountered this takes care of rolling back the JMS and JPA transactions.
         */
        onException(IllegalStateException.class)
            .maximumRedeliveries(1)
            .handled(true)
            .markRollbackOnly();

        /**
         * This route generates a random order every 15 seconds
         */
        from("timer:order?period=15s&delay=0")
            .bean("orderGenerator", "generateOrder")
            .setHeader(Exchange.FILE_NAME).method("orderGenerator", "generateFileName")
            .to("file://{{jboss.server.data.dir}}/orders");

        /**
         * This route consumes XML files from JBOSS_HOME/standalone/data/orders and sends
         * the file content to JMS destination OrdersQueue.
         */
        from("file:{{jboss.server.data.dir}}/orders")
            .transacted()
                .convertBodyTo(String.class)
                .to("jms:queue:OrdersQueue");

        /**
         * This route consumes messages from JMS destination OrdersQueue, unmarshalls the XML
         * message body using JAXB to an Order entity object. The order is then sent to the JPA
         * endpoint for persisting within an in-memory database.
         *
         * Whenever an order quantity greater than 10 is encountered, the route throws an IllegalStateException
         * which forces the JMS / JPA transaction to be rolled back and the message to be delivered to the dead letter
         * queue.
         */
        from("jms:queue:OrdersQueue")
            .unmarshal(jaxbDataFormat)
            .to("jpa:Order")
                .choice()
                .when(simple("${body.quantity} > 10"))
                    .log("Order quantity is greater than 10 - rolling back transaction!")
                    .throwException(new IllegalStateException("Invalid quantity"))
                .otherwise()
                    .log("Order processed successfully");

    }
}

