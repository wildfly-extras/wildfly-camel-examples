Camel JMS example
-----------------

This example demonstrates using the camel-jms component with WildFly Camel to produce and consume JMS messages.

In this example, a Camel route consumes files from ${JBOSS_HOME}/standalone/data/orders and places their contents onto an in-memory ActiveMQ Artemis queue named 'OrdersQueue'. A second route consumes any messages from 'OrdersQueue' and through a simple [content based router (http://camel.apache.org/content-based-router.html) sorts the orders into individual country directories within ${JBOSS_HOME}/standalone/data/orders/processed.

Prerequisites
-------------

* Maven
* An application server with WildFly Camel installed

Connecting to an external broker
--------------------------------

For example to connect to an external Artemis broker follow the instructions given in the WildFly documentation for the [Artemis Resource Adapter](http://docs.wildfly.org/15/Admin_Guide.html#Messaging) and then inject the connection factory as you would with the default connection factory.

    @Resource(mappedName = "java:jboss/RemoteJmsXA")
    ConnectionFactory connectionFactory;

Running the example
-------------------

To run the example.

1. Set the `JBOSS_HOME` environment variable to point at the root directory of your application server installation:

    For Linux:

        export JBOSS_HOME=...

    For Windows:

        set JBOSS_HOME=...

2. Start the application server in standalone mode:

    For Linux:

        ${JBOSS_HOME}/bin/standalone.sh -c standalone-full.xml

    For Windows:

        %JBOSS_HOME%\bin\standalone.bat -c standalone-full.xml

3. Build and deploy the project `mvn install -Pdeploy`. Note that this Maven command also invokes the CLI script
   `configure-jms-queues.cli` that creates the JMS queue.

4. Browse to http://localhost:8080/example-camel-jms/orders

You should see a page titled 'Orders Received'. As we send orders to the example application, a list of orders per country will be listed on this page.

Testing Camel JMS
-----------------

There are some example order XML files within the `src/main/resources` directory. Camel will choose a file at random every 5 seconds and will copy it into ${JBOSS_HOME}/standalone/data/orders for processing.

The console will output messages detailing what happened to each of the orders. The output will look something like this.

    JmsConsumer[OrdersQueue]) Sending order to the UK
    JmsConsumer[OrdersQueue]) Sending order to another country
    JmsConsumer[OrdersQueue]) Sending order to the US

Once the files have been consumed, you can return to http://localhost:8080/example-camel-jms/orders. The count of received orders for each country should have been increased by 1.

All processed orders will have been output to:

    ${JBOSS_HOME}/standalone/data/orders/processed/uk
    ${JBOSS_HOME}/standalone/data/orders/processed/us
    ${JBOSS_HOME}/standalone/data/orders/processed/other

Undeploy
--------

1. To undeploy the example run `mvn clean -Pdeploy`.

2. Remove JMS queues:

    For Linux:

        ${JBOSS_HOME}/bin/jboss-cli.sh --connect --file=remove-jms-queues.cli

    For Windows:

        %JBOSS_HOME%\bin\jboss-cli.bat --connect --file=remove-jms-queues.cli
