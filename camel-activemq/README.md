Camel ActiveMQ Example
----------------------

This example demonstrates using the camel-activemq component with Red Hat Fuse on EAP to produce and consume JMS messages.

In this example, a Camel route consumes files from ${JBOSS_HOME}/standalone/data/orders and places their contents onto an ActiveMQ queue
named 'OrdersQueue'. A second route consumes any messages from 'OrdersQueue' and through a simple [content based router](http://camel.apache.org/content-based-router.html)
sorts the orders into individual country directories within JBOSS_HOME/standalone/data/orders/processed.

CLI scripts automatically configure the ActiveMQ resource adapter. These scripts are located within the `src/main/resources/cli` directory.

Prerequisites
-------------

* Maven
* An application server with Red Hat Fuse installed
* An ActiveMQ broker

Running the example
-------------------

To run the example.

1. Ensure your ActiveMQ broker instance is running. By default, this example expects the broker to be accessible on localhost. This can be changed by editing `src/main/resources/cli/configure-resource-adapter.cli` and modifying the `ServerUrl` attribute from `tcp://127.0.0.1:61616` to your desired host name or IP address
2. Start the application server in standalone mode `${JBOSS_HOME}/bin/standalone.sh -c standalone-full.xml`
3. Deploy the ActiveMQ resource adapter `mvn install -Pdeploy-rar`
4. Restart the application server for the resource adapter configuration to take effect
5. Build and deploy the project `mvn install -Pdeploy`
6. Browse to http://localhost:8080/example-camel-activemq/orders

You should see a page titled 'Orders Received'. As we send orders to the example application, a list
of orders per country will be listed on this page.

Testing Camel ActiveMQ
----------------------

There are some example order XML files within the `src/main/resources` directory. Camel will choose a file at random every 5 seconds and
will copy it into ${JBOSS_HOME}/standalone/data/orders for processing.

The console will output messages detailing what happened to each of the orders. The output
will look something like this.

```
JmsConsumer[OrdersQueue]) Sending order to the UK
JmsConsumer[OrdersQueue]) Sending order to another country
JmsConsumer[OrdersQueue]) Sending order to the US
```

Once the files have been consumed, you can return to `http://localhost:8080/example-camel-activemq/orders`. The count of
received orders for each country should have been increased by 1.

All processed orders will have been output to:

    ${JBOSS_HOME}/standalone/data/orders/processed/uk
    ${JBOSS_HOME}/standalone/data/orders/processed/us
    ${JBOSS_HOME}/standalone/data/orders/processed/other

Undeploy
--------

To undeploy the example run `mvn clean -Pdeploy`.

This step removes the ActiveMQ resource adapter configuration but this will not take effect until the application server has been restarted.

Deploying to OpenShift
----------------------

Prerequisites
-------------

* Fuse Integration Services (FIS) image streams have been installed
* Fuse Integration Services application templates have been installed

Deploying from the OpenShift console
------------------------------------

When logged into the OpenShift console, browse to the 'Add to Project' screen, from the Browse Catalog tab, click Java to open the list of Java templates and then
choose the Red Hat Red Hat Fuse category.

This project assumes that you have already deployed an A-MQ broker somewhere within your OpenShift cluster. See the documentation for the A-MQ xPaaS middleware image
to see how to do this.

Find the s2i-fuse70-eap-camel-amq template and click the Select button. You must provide the correct value for the 'A-MQ Service Prefix' parameter. If the broker
requires authentication then you should supply the login credentials in fields 'A-MQ Username' and 'A-MQ Password'.

When you have provided all of the required template parameters, click the 'Create' button.

The Application created screen now opens. Click Continue to overview
to go to the Overview tab of the OpenShift console. In the 'Builds' section you can monitor progress of the s2i-fuse70-eap-camel-amq S2I build.

When the build has completed successfully, click Overview in the left-hand navigation pane to view the running pod for this application. You can test
the application by clicking on application URL link displayed at the top right of the pod overview. For example:

    http://s2i-fuse70-eap-camel-amq-redhat-fuse.192.168.42.51.nip.io/orders

Note: You can find the correct host name with 'oc get route s2i-fuse70-eap-camel-amq'

You can observe Camel routes generating and processing messages by viewing the pod logs.

Deploying from the command line
-------------------------------

This project assumes that you have already deployed an A-MQ broker somewhere within your cluster. See the documentation for the A-MQ xPaaS middleware image
to see how to do this.

You can deploy this quickstart example to OpenShift by triggering an S2I build. If your broker requires authentication, you'll need
to provide parameters -p MQ_USERNAME=myuser -p MQ_PASSWORD=mysecret. Also, you may need to provide MQ_SERVICE_PREFIX if the default 'broker-amq' does
not match with your broker service name.

    oc new-app s2i-fuse70-eap-camel-amq

You can follow progress of the S2I build by running:

    oc logs -f bc/s2i-fuse70-eap-camel-amq

When the S2I build is complete and the application is running you can test by navigating to route endpoint. You can find the application route
hostname via 'oc get route s2i-fuse70-eap-camel-amq'. For example:

    http://s2i-fuse70-eap-camel-amq-redhat-fuse.192.168.42.51.nip.io/orders

You can observe Camel routes generating and processing messages by viewing the pod logs with (Note: you pod name may be different):

    oc logs -f s2i-fuse70-eap-camel-amq-1-ds8mg

Cleaning up
-------------------------------

You can delete all resources created by the quickstart application by running:

    oc delete all -l 'app=s2i-fuse70-eap-camel-amq'
