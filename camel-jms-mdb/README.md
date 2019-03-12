Camel JMS MDB example
---------------------

This example demonstrates using the camel-jms component in conjunction with Message Driven Beans (MDB).

In this example, a Camel route sends a JMS message to an in-memory ActiveMQ Artemis queue named 'OrdersQueue'. An MDB consumes any messages from 'OrdersQueue' and uses a CDI injected `ProducerTemplate` to send the JMS message payload to a direct route named `direct:jmsIn`.

The direct consumer on `direct:jmsIn` outputs the exchange message body to the console.

Prerequisites
-------------

* Maven
* An application server with WildFly Camel installed

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

Testing Camel JMS
-----------------

Keep watching the server console output. Every 5 seconds a new JMS message will be produced by Camel and consumed by the MDB.

You should see log entries like the following:

    09:48:08,218 INFO  [route6] (Thread-33 (ActiveMQ-client-global-threads-1610269076)) Received message: Message 1 created at Thu May 04 09:48:08 BST 2017
    09:48:13,208 INFO  [route6] (Thread-34 (ActiveMQ-client-global-threads-1610269076)) Received message: Message 2 created at Thu May 04 09:48:13 BST 2017
    09:48:18,203 INFO  [route6] (Thread-35 (ActiveMQ-client-global-threads-1610269076)) Received message: Message 3 created at Thu May 04 09:48:18 BST 2017

Undeploy
--------

1. To undeploy the example run `mvn clean -Pdeploy`.

2. Remove JMS queues:

    For Linux:

        ${JBOSS_HOME}/bin/jboss-cli.sh --connect --file=remove-jms-queues.cli

    For Windows:

        %JBOSS_HOME%\bin\jboss-cli.bat --connect --file=remove-jms-queues.cli
