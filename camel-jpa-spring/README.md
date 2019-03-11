Camel JPA Spring example
------------------------

This example demonstrates using the camel-jpa component with Spring and WildFly Camel to persist entities to an in-memory database.

In this example, a Camel route consumes XML files from ${JBOSS_HOME}/standalone/data/customers. Camel then uses JAXB to unmarshal the data to a Customer entity. This entity is then passed to a jpa endpoint and is persisted to a 'customer' database table.

Prerequisites
-------------

* Maven
* An application server with WildFly Camel installed

Running the example
-------------------

To run the example.

1. Start the application server in standalone mode:

    For Linux:

        ${JBOSS_HOME}/bin/standalone.sh -c standalone-full.xml

    For Windows:

        %JBOSS_HOME%\bin\standalone.bat -c standalone-full.xml

2. Build and deploy the project `mvn install -Pdeploy`

Testing Camel JPA Spring
------------------------

The console will output messages every 10 seconds as new orders are generated and processed. The output will look something like this.

    (Camel (camel-jpa-context) thread #10 - timer://new-order) Inserted new order 1
    (Camel (camel-jpa-context) thread #9 - jpa://org.wildfly.camel.examples.jpa.model.Order) Processed order #id 1 with 6 copies of the «ActiveMQ in Action» book

Browse the following REST endpoint to view a list of available books:

http http://localhost:8080/rest/api/books

Browse the following REST endpoint to view details about a specific processed order:

http://localhost:8080/rest/api/books/order/1

Undeploy
--------

To undeploy the example run `mvn clean -Pdeploy`.
