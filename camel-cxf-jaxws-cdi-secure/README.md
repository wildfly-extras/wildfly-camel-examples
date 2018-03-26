Camel Secure CDI CXF JAX-WS
---------------------------

This example demonstrates using JAAS authentication with camel-cxf and JAX-WS.

In this example, a Camel route takes a message payload from a direct endpoint and passes it on to a CXF producer endpoint. The producer uses the payload to pass arguments to a CXF JAX-WS web service that is secured by TLS mutual authentication (two-way SSL / client certificate authentication).

Prerequisites
-------------

* Maven
* An application server with Red Hat Fuse installed

Running the example
-------------------

To run the example

1. Add a new application user using the add-user utility:

    For Linux:

    ${JBOSS_HOME}/bin/add-user.sh -a -u CN=localhost -p testPassword1+ -g testRole

    For Windows:

    %JBOSS_HOME%\bin\add-user.bat -a -u CN=localhost -p testPassword1+ -g testRole

2. Start the application server in standalone mode:

    For Linux:

    ${JBOSS_HOME}/bin/standalone.sh -c standalone-full.xml

    For Windows:

    %JBOSS_HOME%\bin\standalone.bat -c standalone-full.xml

3. Generate a self signed certificate (Note: you only need to do this once):

    curl http://localhost:8443

    > If cURL is not available on your platform you can use a web browser instead

4. Configure TLS security:

    For Linux:

    ${JBOSS_HOME}/bin/jboss-cli.sh --connect --file=configure-tls-security.cli

    For Windows:

    %JBOSS_HOME%\bin\jboss-cli.bat --connect --file=configure-tls-security.cli

5. Build and deploy the project `mvn install -Pdeploy`

6. Browse to https://localhost:8443/example-camel-cxf-jaxws-cdi-secure/. Since we are using a self signed SSL certificate, your browser may complain that the connection is insecure.

You should see a page titled 'Send A Greeting'. This UI enables us to interact with the test 'greeting' web service which will have also been started. The service WSDL is available at https://localhost:8443/webservices/greeting-secure-cdi?wsdl.

There is a single service operation named 'greet' which takes 2 String parameters named 'message' and 'name'. Invoking the web service will return a response where these values have been concatenated together.

Testing Camel Secure CDI CXF JAX-WS
-----------------------------------

Web UI
------

Browse to https://localhost:8443/example-camel-cxf-jaxws-cdi-secure/.

From the 'Send A Greeting' web form, enter a 'message' and 'name' into the text fields and press the 'send' button. You'll then see the information you entered combined to display a greeting on the UI.

`CamelCxfWsServlet` handles the POST request from the web UI. It retrieves the message and name form parameter values and constructs an object array. This object array will be the message payload that is sent to the `direct:start` endpoint. A `ProducerTemplate` sends the message payload to Camel. `The direct:start` endpoint passes the object array to a `cxf:bean` web service producer. The web service response is used by `CamelCxfWsServlet` to display the greeting on the web UI.

## Undeploy

1. To undeploy the example run `mvn clean -Pdeploy`.

2. Unconfigure the example tls security configuration :

    For Linux:

    ${JBOSS_HOME}/bin/jboss-cli.sh --connect --file=remove-tls-security.cli

    For Windows:

    %JBOSS_HOME%\bin\jboss-cli.bat --connect --file=remove-tls-security.cli
