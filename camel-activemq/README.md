Camel ActiveMQ Example
----------------------

This example demonstrates using the camel-activemq component with WildFly Camel to produce and consume JMS messages.

In this example, a Camel route consumes files from ${JBOSS_HOME}/standalone/data/orders and places their contents onto an ActiveMQ queue named 'OrdersQueue'. A second route consumes any messages from 'OrdersQueue' and through a simple [content based router](http://camel.apache.org/content-based-router.html) sorts the orders into individual country directories within JBOSS_HOME/standalone/data/orders/processed.

Prerequisites
-------------

* Maven
* An application server with WildFly Camel installed
* An ActiveMQ broker

Running the example
-------------------

To run the example.

1. Ensure your ActiveMQ broker instance is running. By default, this example expects the broker to be accessible on localhost. This can be changed by editing `configure-resource-adapter.cli` and modifying the `ServerUrl` attribute from `tcp://127.0.0.1:61616` to your desired host name or IP address

2. Set the `JBOSS_HOME` environment variable to point at the root directory of your application server installation:

    For Linux:

        export JBOSS_HOME=...

    For Windows:

        set JBOSS_HOME=...

3. Start the application server in standalone mode:

    For Linux:

        ${JBOSS_HOME}/bin/standalone.sh -c standalone-full-camel.xml

    For Windows:

        %JBOSS_HOME%\bin\standalone.bat -c standalone-full-camel.xml

4. Deploy the ActiveMQ resource adapter `mvn install -Pdeploy-rar`.

5. Build and deploy the project `mvn install -Pdeploy`. Note that the resource adapter needs to get configured
   which is done via CLI script `configure-resource-adapter.cli` invoked by Maven in the next step

6. Browse to http://localhost:8080/example-camel-activemq/orders

You should see a page titled 'Orders Received'. As we send orders to the example application, a list of orders per country will be listed on this page.

Testing Camel ActiveMQ
----------------------

There are some example order XML files within the `src/main/resources` directory. Camel will choose a file at random every 5 seconds and will copy it into ${JBOSS_HOME}/standalone/data/orders for processing.

The console will output messages detailing what happened to each of the orders. The output will look something like this.

    JmsConsumer[OrdersQueue]) Sending order to the UK
    JmsConsumer[OrdersQueue]) Sending order to another country
    JmsConsumer[OrdersQueue]) Sending order to the US

Once the files have been consumed, you can return to http://localhost:8080/example-camel-activemq/orders. The count of
received orders for each country should have been increased by 1.

All processed orders will have been output to:

    ${JBOSS_HOME}/standalone/data/orders/processed/uk
    ${JBOSS_HOME}/standalone/data/orders/processed/us
    ${JBOSS_HOME}/standalone/data/orders/processed/other

Undeploy
--------

To undeploy the example run `mvn clean -Pdeploy`.

