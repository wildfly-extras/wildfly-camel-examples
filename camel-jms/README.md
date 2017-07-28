Camel JMS example
-----------------

This example demonstrates using the camel-jms component with WildFly Camel susbsystem to produce and consume JMS messages.

In this example, a Camel route consumes files from ${JBOSS_HOME}/standalone/data/orders and places their contents onto an in-memory ActiveMQ Artemis queue
named 'OrdersQueue'. A second route consumes any messages from 'OrdersQueue' and through a simple [content based router](http://camel.apache.org/content-based-router.html)
sorts the orders into individual country directories within ${JBOSS_HOME}/standalone/data/orders/processed.

CLI scripts take care of creating and removing the JMS 'OrdersQueue' for you when the
application is deployed and undeployed. These scripts are located within the `src/main/resources/cli` directory.

Prerequisites
-------------

* Maven
* An application server with the wildfly-camel subsystem installed

Running the example
-------------------

To run the example.

1. Start the application server in standalone mode `${JBOSS_HOME}/bin/standalone.sh -c standalone-full-camel.xml`
2. Build and deploy the project `mvn install -Pdeploy`
3. Browse to http://localhost:8080/example-camel-jms/orders

You should see a page titled 'Orders Received'. As we send orders to the example application, a list
of orders per country will be listed on this page.

Testing Camel JMS
-----------------

There are some example order XML files within the `src/main/resources` directory. Camel will choose a file at random every 5 seconds and
will copy it into ${JBOSS_HOME}/standalone/data/orders for processing.

The console will output messages detailing what happened to each of the orders. The output
will look something like this.

```
JmsConsumer[OrdersQueue]) Sending order to the UK
JmsConsumer[OrdersQueue]) Sending order to another country
JmsConsumer[OrdersQueue]) Sending order to the US
```

Once the files have been consumed, you can return to http://localhost:8080/example-camel-jms/orders. The count of
received orders for each country should have been increased by 1.

All processed orders will have been output to:

    ${JBOSS_HOME}/standalone/data/orders/processed/uk
    ${JBOSS_HOME}/standalone/data/orders/processed/us
    ${JBOSS_HOME}/standalone/data/orders/processed/other

Undeploy
--------

To undeploy the example run `mvn clean -Pdeploy`.

Learn more
----------

Additional camel-jms documentation can be found at the [WildFly Camel User Guide](http://wildfly-extras.github.io/wildfly-camel/#_jms
) site.
