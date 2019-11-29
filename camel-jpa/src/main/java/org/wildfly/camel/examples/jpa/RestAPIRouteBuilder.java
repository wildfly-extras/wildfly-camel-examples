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
import org.apache.camel.model.rest.RestBindingMode;
import org.wildfly.camel.examples.jpa.model.Order;

@ApplicationScoped
public class RestAPIRouteBuilder extends RouteBuilder {

    public void configure() throws Exception {
        restConfiguration()
            .contextPath("/rest/api")
            .apiContextPath("/api-doc")
            .apiProperty("api.title", "Camel REST API")
            .apiProperty("api.version", "1.0")
            .apiProperty("cors", "true")
            .apiContextRouteId("doc-api")
            .component("undertow")
            .bindingMode(RestBindingMode.json);

        rest("/books").description("Books REST service")
            .get("/").description("The list of all the books")
                .route()
                    .toF("jpa:%s?nativeQuery=select distinct description from orders", Order.class.getName())
                .endRest()
            .get("order/{id}").description("Details of an order by id")
                .route()
                    .toF("jpa:%s?consumeDelete=false&parameters=#queryParameters&nativeQuery=select * from orders where id = :id", Order.class.getName())
                    .process("jpaResultProcessor")
                .endRest();
    }
}
