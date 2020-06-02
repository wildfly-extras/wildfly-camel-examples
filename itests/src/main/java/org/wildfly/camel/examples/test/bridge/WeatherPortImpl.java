/*
 * #%L
 * Wildfly Camel :: Testsuite
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
package org.wildfly.camel.examples.test.bridge;

import java.util.logging.Logger;

import com.ibm.wdata.WeatherPortType;
import com.ibm.wdata.WeatherRequest;

@javax.jws.WebService(
                      serviceName = "weatherService",
                      portName = "WeatherPort",
                      targetNamespace = "http://ibm.com/wdata",
                      wsdlLocation = "/wsdl/weatherprovider.wsdl",
                      endpointInterface = "com.ibm.wdata.WeatherPortType")

public class WeatherPortImpl implements WeatherPortType {

    private static final Logger LOG = Logger.getLogger(WeatherPortImpl.class.getName());

    /* (non-Javadoc)
     * @see com.ibm.wdata.WeatherPortType#weatherRequest(com.ibm.wdata.WeatherRequest weatherRequest)*
     */
    public com.ibm.wdata.WeatherResponse weatherRequest(WeatherRequest weatherRequest) {
        LOG.info("Executing operation weatherRequest");
        
        try {
            com.ibm.wdata.WeatherResponse _return = new com.ibm.wdata.WeatherResponse();
            _return.setZip(weatherRequest.getZipcode());
            _return.setCity("LA");
            _return.setState("CA");
            _return.setHumidity("95%");
            _return.setTemperature("28");
            return _return;
        } catch (java.lang.Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
