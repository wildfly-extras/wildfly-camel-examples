Camel JPA example
-----------------

This example demonstrates using the camel-jpa component with WildFly Camel susbsystem to persist entities to a database.

In this example, a Camel route creates order entities and persists them to a database. The Camel REST DSL is used to expose endpoints for
retrieving records from the database.

Prerequisites
-------------

* Maven
* An application server with the wildfly-camel subsystem installed

Running the example
-------------------

To run the example.

1. Start the application server in standalone mode `${JBOSS_HOME}/bin/standalone.sh -c standalone-full-camel.xml`
2. Build and deploy the project `mvn install -Pdeploy`

Testing Camel JPA
-----------------

The console will output messages every 10 seconds as new orders are generated and processed. The output
will look something like this.

```
(Camel (camel-jpa-context) thread #10 - timer://new-order) Inserted new order 1
(Camel (camel-jpa-context) thread #9 - jpa://org.wildfly.camel.examples.jpa.model.Order) Processed order #id 1 with 6 copies of the «ActiveMQ in Action» book
```

Browse the following REST endpoint to view a list of available books:

http http://localhost:8080/rest/api/books

Browse the following REST endpoint to view details about a specific processed order:

http://localhost:8080/rest/api/books/order/1

Undeploy
--------

To undeploy the example run `mvn clean -Pdeploy`.

Learn more
----------

Additional camel-jpa documentation can be found at the [WildFly Camel User Guide](http://wildfly-extras.github.io/wildfly-camel/#_camel_jpa) site.
