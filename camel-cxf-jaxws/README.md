Camel CXF JAX-WS Example
------------------------

This example demonstrates using the camel-cxf component with Red Hat Fuse on EAP to produce and consume JAX-WS web services.

In this example, a Camel route takes a message payload from a direct endpoint and passes it on to a CXF producer endpoint. The producer uses the payload to pass arguments to a CXF JAX-WS web service.

Prerequisites
-------------

* Maven
* An application server with Red Hat Fuse installed

Running the example
-------------------

To run the example.

1. Start the application server in standalone mode:

    For Linux:

    ${JBOSS_HOME}/bin/standalone.sh -c standalone-full.xml

    For Windows:

    %JBOSS_HOME%\bin\standalone.bat -c standalone-full.xml

2. Build and deploy the project `mvn install -Pdeploy`

3. Browse to http://localhost:8080/example-camel-cxf-jaxws/

You should see a page titled 'Send A Greeting'. This UI enables us to interact with the test 'greeting' web service which will have also been started. The service WSDL is available at http://localhost:8080/webservices/greeting?wsdl.

There is a single service operation named 'greet' which takes 2 String parameters named 'message' and 'name'. Invoking the web service will return a response where these values have been concatenated together.

Testing Camel CXF JAX-WS
------------------------

Web UI
------

Browse to http://localhost:8080/example-camel-cxf-jaxws/.

From the 'Send A Greeting' web form, enter a 'message' and 'name' into the text fields and press the 'send' button. You'll then see the information you entered combined to display a greeting on the UI.

`CamelCxfWsServlet` handles the POST request from the web UI. It retrieves the message and name form parameter values and constructs an object array. This object array will be the message payload that is sent to the `direct:start` endpoint. A `ProducerTemplate` sends the message payload to Camel. `The direct:start` endpoint passes the object array to a `cxf:bean` web service producer. The web service response is used by `CamelCxfWsServlet` to display the greeting on the web UI.

The full Camel route can be seen in `src/main/webapp/WEB-INF/cxfws-camel-context.xml`.

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
choose the Red Hat Fuse category.

Find the s2i-fuse70-eap-camel-cxf-jaxws template and click the Select button. You can accept the default values and click 'Create'. The Application created screen now opens. Click Continue to overview
to go to the Overview tab of the OpenShift console. In the 'Builds' section you can monitor progress of the s2i-fuse70-eap-camel-cxf-jaxws S2I build.

When the build has completed successfully, click Overview in the left-hand navigation pane to view the running pod for this application. You can test
the application by clicking on application URL link displayed at the top right of the pod overview. For example:

    http://s2i-fuse70-eap-camel-cxf-jaxws-redhat-fuse.192.168.42.51.nip.io

Note: You can find the correct host name with 'oc get route s2i-fuse70-eap-camel-cxf-jaxws'

The application URL exposes a web UI which enables you to trigger a camel route which invokes a JAX-WS greetings service via a CXF consumer & producer. The
WSDL is available at:

    http://s2i-fuse70-eap-camel-cxf-jaxws-redhat-fuse.192.168.42.51.nip.io/webservices/greeting?wsdl

Deploying from the command line
-------------------------------

You can deploy this quickstart example to OpenShift by triggering an S2I build by running the following:

    oc new-app s2i-fuse70-eap-camel-cxf-jaxws

You can follow progress of the S2I build by running:

    oc logs -f bc/s2i-fuse70-eap-camel-cxf-jaxws

When the S2I build is complete and the application is running you can test by navigating to route endpoint. You can find the application route
hostname via 'oc get route s2i-fuse70-eap-camel-cxf-jaxws'. For example:

    http://s2i-fuse70-eap-camel-cxf-jaxws-redhat-fuse.192.168.42.51.nip.io

The application URL exposes a web UI which enables you to trigger a camel route which invokes a JAX-RS greetings service via a CXF consumer & producer. The
WSDL is available at:

    http://s2i-fuse70-eap-camel-cxf-jaxws-redhat-fuse.192.168.42.51.nip.io/webservices/greeting?wsdl

Cleaning up
-------------------------------

You can delete all resources created by the quickstart application by running:

    oc delete all -l 's2i-fuse70-eap-camel-cxf-jaxws'
