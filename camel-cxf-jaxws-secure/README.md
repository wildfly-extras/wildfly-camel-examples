Camel Secure CXF JAX-WS
------------------------

This example demonstrates using the camel-cxf component with WildFly Camel to produce and consume JAX-WS web
services secured by an Elytron Security Domain. Elytron is a new security framework available since WildFly 10.

In this example, a Camel route takes a message payload from a direct endpoint and passes it on to a CXF producer
endpoint. The producer uses the payload to pass arguments to a CXF JAX-WS web service that is secured by BASIC HTTP
authentication.

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

2. Use the add-user script to create a new server application user and group

    For Linux:

        ${JBOSS_HOME}/bin/add-user.sh -a -u testUser -p testPassword1+ -g testRole

    For Windows:

        %JBOSS_HOME%\bin\add-user.bat -a -u testUser -p testPassword1+ -g testRole

3. Start the application server in standalone mode:

    For Linux:

        ${JBOSS_HOME}/bin/standalone.sh -c standalone-full-camel.xml

    For Windows:

        %JBOSS_HOME%\bin\standalone.bat -c standalone-full-camel.xml

4. Study `jboss-web-xml` and `web.xml` files in `webapp/WEB-INF` directory of this project. They
set the application security domain, security roles and constraints.

5. Build and deploy the project `mvn install -Pdeploy`. Note that this Maven command also invokes the CLI script
   `configure-basic-security.cli` that creates the security domain and a few other management objects.

6. Browse to http://localhost:8080/example-camel-cxf-jaxws-secure/

You should see a page titled 'Send A Greeting'. This UI enables us to interact with the test 'greeting' web service which will have also been started. The service WSDL is available at http://localhost:8080/webservices/greeting-security-basic?wsdl.

There is a single service operation named 'greet' which takes 2 String parameters named 'message' and 'name'. Invoking the web service will return a response where these values have been concatenated together.

Testing Camel Secure CXF JAX-WS
-------------------------------

Web UI
------

Browse to http://localhost:8080/example-camel-cxf-jaxws-secure/.

From the 'Send A Greeting' web form, enter a 'message' and 'name' into the text fields and press the 'send' button. You'll then see the information you entered combined to display a greeting on the UI.

`CamelCxfWsServlet` handles the POST request from the web UI. It retrieves the message and name form parameter values and constructs an object array. This object array will be the message payload that is sent to the `direct:start` endpoint. A `ProducerTemplate` sends the message payload to Camel. `The direct:start` endpoint passes the object array to a `cxf:bean` web service producer. The web service response is used by `CamelCxfWsServlet` to display the greeting on the web UI.

The full Camel route can be seen in `src/main/webapp/WEB-INF/cxfws-security-camel-context.xml`.

## Undeploy

To undeploy the example run `mvn clean -Pdeploy`.