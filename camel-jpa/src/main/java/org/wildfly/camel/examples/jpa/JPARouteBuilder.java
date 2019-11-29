/*
 * #%L
 * Wildfly Camel :: Example :: Camel JPA
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
package org.wildfly.camel.examples.jpa;

import javax.enterprise.context.ApplicationScoped;

import org.apache.camel.builder.RouteBuilder;
import org.wildfly.camel.examples.jpa.model.Order;

@ApplicationScoped
public class JPARouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        // Route to generate orders and persist them to the database
        from("timer:new-order?delay=0s&period=10s")
            .bean("orderService", "generateOrder")
            .toF("jpa:%s", Order.class.getName())
            .log("Inserted new order ${body.id}");

        // A second route polls the database for new orders and processes them
        fromF("jpa:%s?consumeDelete=false&transacted=true&joinTransaction=true&namedQuery=pendingOrders", Order.class.getName())
            .process(exchange -> {
                Order order = exchange.getIn().getBody(Order.class);
                order.setStatus("PROCESSED");
            })
            .toF("jpa:%s", Order.class.getName())
            .log("Processed order #id ${body.id} with ${body.amount} copies of the «${body.description}» book");
    }
}
