## JBoss Fuse on EAP Examples

This directory contains a suite of useful modules to demonstrate various features of JBoss Fuse on EAP.
Their aim is to provide small, specific and working examples that can be used for reference in your own projects.

### Prerequisites

The examples require a running EAP 7.1 application server with JBoss Fuse on EAP deployed.

### Running Examples

Each example aims to be interactive to help you learn how to get started with the JBoss Fuse on EAP. Each example
can be accessed by changing into the example source directory, building the project `mvn clean install` and then deploying
to a running application server `mvn install -Pdeploy`.

Examples can be undeployed from a running application server by running `mvn install -Pdeploy`.