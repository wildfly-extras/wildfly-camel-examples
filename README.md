## WildFly Camel Examples

[![Jenkins](https://img.shields.io/jenkins/s/https/ci.fabric8.io/wildfly-camel.svg?maxAge=600)](https://fabric8-ci.fusesource.com/view/wildfly-camel/job/wildfly-camel-examples)
[![License](https://img.shields.io/:license-Apache2-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Join the chat at freenode:wildfly-camel](https://img.shields.io/badge/irc-freenode%3A%20%23wildfly--camel-blue.svg)](http://webchat.freenode.net/?channels=%23wildfly-camel)

[![Open workspace in Eclipse Che](http://beta.codenvy.com/factory/resources/codenvy-contribute.svg)](https://beta.codenvy.com/f?id=chknwakr0ykhqr1q)

This directory contains a suite of useful modules to demonstrate various features of the WildFly Camel Subsystem.
Their aim is to provide small, specific and working examples that can be used for reference in your own projects.

### Prerequisites

Please refer to the project [README documentation](https://github.com/wildfly-extras/wildfly-camel/blob/master/README.md) for information on how to build and test the project.
Please take into consideration the minimum Java and Maven requirements. The examples also require a running application server
with the wildfly-camel subsystem deployed.

### Running Examples

Each example aims to be interactive to help you learn how to get started with the WildFly Camel Subsystem. Each example
can be accessed by changing into the example source directory, building the project `mvn clean install` and then deploying
to a running application server `mvn install -Pdeploy`.

Examples can be undeployed from a running application server by running `mvn clean -Pdeploy`.
