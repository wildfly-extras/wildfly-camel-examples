Camel CDI Example
-----------------

This example demonstrates using the camel-cdi component with WildFly Camel to integrate CDI beans with Camel routes.

In this example, a Camel route takes a message payload from a servlet HTTP GET request and passes it on to a direct endpoint. The payload
is then passed onto a Camel CDI bean invocation to produce a message response which is displayed on the web browser page.

Prerequisites
-------------

* Maven
* An application server with WildFly Camel installed

Running the example
-------------------

To run the example.

1. Start the application server in standalone mode:

    For Linux:

        ${JBOSS_HOME}/bin/standalone.sh -c standalone-full.xml

    For Windows:

        %JBOSS_HOME%\bin\standalone.bat -c standalone-full.xml

2. Build and deploy the project `mvn install -Pdeploy`

Testing Camel CDI
-----------------

Web UI
------

Browse to http://localhost:8080/example-camel-cdi/?name=Kermit.

You should see the message "Hello Kermit" output on the web page.

The Camel route is very simple and looks like this:


    from("direct:start").beanRef("helloBean");


The `beanRef` DSL makes camel look for a bean named 'helloBean' in the bean registry. The magic that makes this bean available to Camel is found in the `SomeBean` class.

    @Named("helloBean")
    public class SomeBean {

        public String someMethod(String message) {
            return "Hello " + message;
        }
    }

By using the `@Named` annotation, camel-cdi will add this bean to the Camel bean registry.

## Undeploy

To undeploy the example run `mvn clean -Pdeploy`.
