Camel CXF JAX-WS Example changed to using CDI and with Authentication and Authorization added
------------------------

Starting from the "Camel CXF JAX-WS Example" I replaced the spring configuration "cxfws-camel-context.xml" with "org.wildfly.camel.examples.cxf.jaxws.JavaDSLRouteBuilder.java". 

This example demonstrates using the JAAS authentication in the camel-cxf webservice component that is configured via Java-DSL

In this example, a Camel route takes a message payload from a direct endpoint and passes it on to a CXF producer endpoint. The producer uses the payload
to pass arguments to a CXF JAX-WS web service that is secured by TLS mutual authentication (two-way ssl / client certificate autehtication)

Prerequisites
-------------

* Maven
* An application server with the wildfly-camel subsystem installed

Running the example
-------------------

To run the example.

1. Start the application server in standalone mode `${JBOSS_HOME}/bin/standalone.sh -c standalone-full-camel.xml`
2. Edit ${jboss.home.dir}\modules\system\layers\fuse\org\wildfly\extension\camel\cxf\undertow\main\module.xml
	and add this line:
		<module name="org.picketbox" />
	to the <dependencies> section. otherwies you'll see this Exception later in org.apache.cxf.interceptor.security.JAASLoginInterceptor Line:163 ....but not logged;-(
	
	javax.security.auth.login.LoginException: LoginModule-Klasse kann nicht gefunden werden: org.jboss.security.auth.spi.BaseCertLoginModule from [Module "org.wildfly.extension.camel.cxf.undertow:main" from local module loader @4b53f538 (finder: local module finder @134593bf (roots: C:\daten\wildfly-camel-10.1.0.Final\modules,C:\daten\wildfly-camel-10.1.0.Final\modules\system\layers\fuse,C:\daten\wildfly-camel-10.1.0.Final\modules\system\layers\base))]
	
	
	Also be sure that "jboss-deployment-structure.xml" is within the war file.
	If the dependency to the CXF modules are missing you'll get a
		java.lang.NoClassDefFoundError: org/apache/cxf/security/transport/TLSSessionInfo
	
	
3. Add system properties (via CLI)

	/system-property=javax.net.ssl.trustStore/:add(value=${jboss.home.dir}/standalone/configuration/application.keystore)  
	
	/system-property=javax.net.ssl.trustStorePassword/:add(value=password)  
	
4. Add security domain(s) for client-cert authentication (via CLI)

	/subsystem=security/security-domain=certificate-trust-domain:add()
	/subsystem=security/security-domain=certificate-trust-domain/jsse=classic:add(truststore={"password"=>"password","url"=>"${jboss.home.dir}/standalone/configuration/application.keystore"})
	
	/subsystem=security/security-domain=client-cert:add()
	/subsystem=security/security-domain=client-cert/authentication=classic:add(login-modules=[{"code"="Certificate","flag"="required","module-options"=>["securityDomain"=>"certificate-trust-domain"]}])

5. Add verify-client Attribute to the Undertow https listener
	
	/subsystem=undertow/server=default-server/https-listener=https/:write-attribute(name=verify-client,value=REQUESTED)
	
6. Re-Start the application server 
7. Build and deploy the project `mvn install -Pdeploy`
8. Browse to http://localhost:8080/example-camel-cxfws-security-cdi/

You should see a page titled 'Send A Greeting'. This UI enables us to interact with the test 'greeting' web service which will have also been
started. The service WSDL is available at http://localhost:8080/example-camel-cxfws-security-cdi/greeting?wsdl.

There is a single service operation named 'greet' which takes 2 String parameters named 'message' and 'name'. Invoking the web service will return
a response where these values have been concatenated together.

Testing Camel CXF JAX-WS
------------------------

Web UI
------

Browse to http://localhost:8080/example-camel-cxfws-security-cdi/.

From the 'Send A Greeting' web form, enter a 'message' and 'name' into the text fields and press the 'send' button. You'll then
see the information you entered combined to display a greeting on the UI.

So what just happened there?

`CamelCxfWsServlet` handles the POST request from the web UI. It retrieves the message and name form parameter values and constructs an
object array. This object array will be the message payload that is sent to the `direct:start` endpoint. A `ProducerTemplate`
sends the message payload to Camel. `The direct:start` endpoint passes the object array to a `cxf:bean` web service producer. 
The web service response is used by `CamelCxfWsServlet` to display the greeting on the web UI.

The full Camel route can be seen in `src/main/webapp/WEB-INF/cxfws-camel-context.xml`.

## Undeploy

To undeploy the example run `mvn clean -Pdeploy`.

## Learn more

Additional camel-cxf documentation can be found at the [WildFly Camel GitBook](http://wildflyext.gitbooks.io/wildfly-camel/content/javaee/jaxws.html
) site.
