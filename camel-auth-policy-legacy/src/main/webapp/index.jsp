<%--
  #%L
  Wildfly Camel :: Example :: Camel Camel Restricted Elytron
  %%
  Copyright (C) 2013 - 2016 RedHat
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
<html>
<head>
  <title>WildFly Camel Subsystem Restricted Example (Elytron)</title>
  <link href="bootstrap.min.css" rel="stylesheet"/>
</head>
<body>
  <div class="container">
    <div class="page-header">
      <h1>Welcome administrator</h1>
        <p>
          Initiate server cleanup
        </p>
        <form action="cleanup" method="post">
          <div class="form-group">
            <input type="submit" class="btn btn-primary" value="Start"/>
          </div>
        </form>
    </div>
  </div>
</body>
</html>
