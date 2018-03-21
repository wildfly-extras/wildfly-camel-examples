Camel CXF JAX-RS Example
------------------------

This example demonstrates using the camel-cxf component with Red Hat Fuse on EAP to produce and consume JAX-RS REST services.

In this example, a Camel route takes a message payload from a direct endpoint and passes it on to a CXF producer endpoint. The producer uses the payload
to pass arguments to a CXF JAX-RS REST service.

Prerequisites
-------------

* Maven
* An application server with Red Hat Fuse installed

Running the example
-------------------

To run the example.

1. Start the application server in standalone mode `${JBOSS_HOME}/bin/standalone.sh -c standalone-full.xml`
2. Build and deploy the project `mvn install -Pdeploy`
3. Browse to http://localhost:8080/example-camel-cxf-jaxrs/

You should see a page titled 'Send A Greeting'. This UI enables us to interact with the test 'greeting' REST service which will have also been
started.

There is a single service operation named 'greet' which takes a String parameter called 'name'. Invoking the service will return
a response as a simple String greeting message.

Testing Camel CXF JAX-RS
------------------------

Web UI
------

Browse to http://localhost:8080/example-camel-cxf-jaxrs/.

From the 'Send A Greeting' web form, enter a 'name' into the text field and press the 'send' button. You'll then
see a simple greeting message displayed on the UI.

So what just happened there?

`CamelCxfRsServlet` handles the POST request from the web UI. It retrieves the name form parameter value and constructs an
object array. This object array will be the message payload that is sent to the `direct:start` endpoint. A `ProducerTemplate`
sends the message payload to Camel. `The direct:start` endpoint passes the object array to a `cxfrs:bean` REST service producer.
The REST service response is used by `CamelCxfRsServlet` to display the greeting on the web UI.

The full Camel route can be seen in `src/main/webapp/WEB-INF/cxfrs-camel-context.xml`.

## Undeploy

To undeploy the example run `mvn clean -Pdeploy`.

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

Find the s2i-fuse70-eap-camel-cxf-jaxrs template and click the Select button. You can accept the default values and click 'Create'. The Application created screen now opens. Click Continue to overview
to go to the Overview tab of the OpenShift console. In the 'Builds' section you can monitor progress of the s2i-fuse70-eap-camel-cxf-jaxrs S2I build.

When the build has completed successfully, click Overview in the left-hand navigation pane to view the running pod for this application. You can test
the application by clicking on application URL link displayed at the top right of the pod overview. For example:

    http://s2i-fuse70-eap-camel-cxf-jaxrs-redhat-fuse.192.168.42.51.nip.io

Note: You can find the correct host name with 'oc get route s2i-fuse70-eap-camel-cxf-jaxrs'

The application URL exposes a web UI which enables you to trigger a camel route which invokes a JAX-RS greetings service via a CXF consumer & producer.

Deploying from the command line
-------------------------------

You can deploy this quickstart example to OpenShift by triggering an S2I build by running the following:

    oc new-app s2i-fuse70-eap-camel-cxf-jaxrs

You can follow progress of the S2I build by running:

    oc logs -f bc/s2i-fuse70-eap-camel-cxf-jaxrs

When the S2I build is complete and the application is running you can test by navigating to route endpoint. You can find the application route
hostname via 'oc get route s2i-fuse70-eap-camel-cxf-jaxrs'. For example:

    http://s2i-fuse70-eap-camel-cxf-jaxrs-redhat-fuse.192.168.42.51.nip.io

The application URL exposes a web UI which enables you to trigger a camel route which invokes a JAX-RS greetings service via a CXF consumer & producer.

Cleaning up
-------------------------------

You can delete all resources created by the quickstart application by running:

    oc delete all -l 's2i-fuse70-eap-camel-cxf-jaxrs'
