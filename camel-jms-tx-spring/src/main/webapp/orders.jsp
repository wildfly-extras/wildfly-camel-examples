<%--
  #%L
  Wildfly Camel :: Example :: Camel Transacted JMS Spring
  %%
  Copyright (C) 2013 - 2017 RedHat
  %%
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  #L%
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <title>WildFly Camel Subsystem JMS Example</title>
    <link href="bootstrap.min.css" rel="stylesheet"/>
    <meta http-equiv="refresh" content="10" />
</head>
<body>
<div class="container">
    <div class="page-header">
        <h1>Orders Received</h1>
        <c:forEach var="order" items="${orders}">
        <div class="row">
            <div class="col-md-4">Order ID: <strong>${order.id}</strong></div>
            <div class="col-md-4">Product Name: <strong>${order.productName}</strong></div>
            <div class="col-md-4">Quantity Ordered: ${order.quantity}</div>
        </div>
        </c:forEach>
    </div>
</div>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
</body>
</html>
