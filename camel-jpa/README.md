Camel JPA example
-----------------

This example demonstrates using the camel-jpa component with Red Hat Fuse on EAP to persist entities to a database.

In this example, a Camel route creates order entities and persists them to a database. The Camel REST DSL is used to expose endpoints for
retrieving records from the database.

Prerequisites
-------------

* Maven
* An application server with Red Hat Fuse installed

Running the example
-------------------

To run the example.

1. Start the application server in standalone mode `${JBOSS_HOME}/bin/standalone.sh -c standalone-full.xml`
2. Build and deploy the project `mvn install -Pdeploy`

Testing Camel JPA
-----------------

The console will output messages every 10 seconds as new orders are generated and processed. The output
will look something like this.

```
(Camel (camel-jpa-context) thread #10 - timer://new-order) Inserted new order 1
(Camel (camel-jpa-context) thread #9 - jpa://org.wildfly.camel.examples.jpa.model.Order) Processed order #id 1 with 6 copies of the «ActiveMQ in Action» book
```

Browse the following REST endpoint to view a list of available books:

    http://localhost:8080/rest/api/books

Browse the following REST endpoint to view details about a specific processed order:

    http://localhost:8080/rest/api/books/order/1

Undeploy
--------

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

This example uses the mysql-ephemeral ImageStream. If you do not have this installed on your cluster, you can run the following command to add it:

    oc create -f https://raw.githubusercontent.com/openshift/origin/master/examples/db-templates/mysql-ephemeral-template.json

Find the s2i-fuse70-eap-camel-jpa template and click the Select button. You can accept the default values and click 'Create'. The Application
created screen now opens. Click Continue to overview to go to the Overview tab of the OpenShift console. In the 'Builds' section
you can monitor progress of the s2i-fuse70-eap-camel-jpa S2I build.

When the build has completed successfully, click Overview in the left-hand navigation pane to view the running pod for this application. You can test
the application by clicking on application URL link displayed at the top right of the pod overview. For example:

    http://s2i-fuse70-eap-camel-jpa-redhat-fuse.192.168.42.51.nip.io

Note: You can find the correct host name with 'oc get route s2i-fuse70-eap-camel-jpa'

Browse the following REST endpoint to view a list of available books:

    http://s2i-fuse70-eap-camel-jpa-redhat-fuse.192.168.42.51.nip.io/rest/api/books

Browse the following REST endpoint to view details about a specific processed order:

    http://s2i-fuse70-eap-camel-jpa-redhat-fuse.192.168.42.51.nip.io/rest/api/books/order/1

Deploying from the command line
-------------------------------

You can deploy this quickstart example to OpenShift by triggering an S2I build:

    oc new-app s2i-fuse70-eap-camel-jpa

You can follow progress of the S2I build by running:

    oc logs -f bc/s2i-fuse70-eap-camel-jpa

When the S2I build is complete and the application is running you can test by navigating to route endpoint. You can find the application route
hostname via 'oc get route s2i-fuse70-eap-camel-jpa'. For example:

    http://s2i-fuse70-eap-camel-jpa-redhat-fuse.192.168.42.51.nip.io

Browse the following REST endpoint to view a list of available books:

    http://s2i-fuse70-eap-camel-jpa-redhat-fuse.192.168.42.51.nip.io/rest/api/books

Browse the following REST endpoint to view details about a specific processed order:

    http://s2i-fuse70-eap-camel-jpa-redhat-fuse.192.168.42.51.nip.io/rest/api/books/order/1

Cleaning up
-------------------------------

You can delete all resources created by the quickstart application by running:

    oc delete all -l 'app=s2i-fuse70-eap-camel-jpa'
