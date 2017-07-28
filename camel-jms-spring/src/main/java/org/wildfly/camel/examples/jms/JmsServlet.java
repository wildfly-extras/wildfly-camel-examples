/*
 * #%L
 * Wildfly Camel :: Example :: Camel JMS Spring
 * %%
 * Copyright (C) 2013 - 2017 RedHat
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
package org.wildfly.camel.examples.jms;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.wildfly.camel.examples.jms.OrderGenerator.COUNTRIES;

@SuppressWarnings("serial")
@WebServlet(name = "HttpServiceServlet", urlPatterns = {"/orders/*"}, loadOnStartup = 1)
public class JmsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //Work out a count of order files processed for each country
        Map<String, Integer> orderCounts = new HashMap<>();

        for (String country : COUNTRIES) {
            int orderCount = countOrdersForCountry(country);

            if (orderCount > 0) {
                orderCounts.put(country, orderCount);
            }
        }

        request.setAttribute("orders", orderCounts);
        request.getRequestDispatcher("orders.jsp").forward(request, response);
    }

    private int countOrdersForCountry(String country) throws IOException {
        Path countryPath = new File(System.getProperty("jboss.server.data.dir")).toPath().resolve("orders/processed/" + country);
        File file = countryPath.toFile();

        if (file.isDirectory()) {
            return file.list().length;
        }

        return 0;
    }
}
