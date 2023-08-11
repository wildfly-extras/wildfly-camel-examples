/*
 * #%L
 * Wildfly Camel :: Example :: Camel Auth Policy Legacy
 * %%
 * Copyright (C) 2013 - 2014 RedHat
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.wildfly.camel.examples.restricted;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.cdi.ContextName;
import org.apache.camel.impl.DefaultCamelContext;
import org.wildfly.extension.camel.security.DomainPrincipal;
import org.wildfly.extension.camel.security.UsernamePasswordPrincipal;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = "/cleanup", loadOnStartup = 1)
public class CleanupServlet extends HttpServlet {

    @Inject
    private CamelContext camelContext;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ProducerTemplate producer = camelContext.createProducerTemplate();

        // use credentials for EAP user
        String username = "testUser";
        String password = "testPassword1+";
        Subject subject = new Subject();
        subject.getPrincipals().add(new UsernamePasswordPrincipal(username, password.toCharArray()));

        producer.requestBodyAndHeader("direct:start", null, Exchange.AUTHENTICATION, subject, String.class);
        response.sendRedirect("index.jsp");
    }

}
