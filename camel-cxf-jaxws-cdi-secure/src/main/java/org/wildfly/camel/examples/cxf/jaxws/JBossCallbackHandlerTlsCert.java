/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wildfly.camel.examples.cxf.jaxws;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.security.auth.callback.CallbackHandler;

import org.apache.cxf.interceptor.security.callback.CallbackHandlerProvider;
import org.apache.cxf.interceptor.security.callback.CertificateToNameMapper;
import org.apache.cxf.message.Message;
import org.apache.cxf.security.transport.TLSSessionInfo;
import org.jboss.security.auth.callback.UsernamePasswordHandler;

/**
 * Copy of <code>org.apache.cxf.interceptor.security.callback.CallbackHandlerTlsCert</code> that returns a
 * Callback Handler that fits to the <code>org.jboss.security.auth.spi.BaseCertLoginModule</code>.
 */
public class JBossCallbackHandlerTlsCert implements CallbackHandlerProvider {

    private CertificateToNameMapper certMapper;

    public JBossCallbackHandlerTlsCert() {
        // By default use subjectDN as userName
        this.certMapper = new CertificateToNameMapper() {
            @Override
            public String getUserName(Certificate cert) {
                String name = ((X509Certificate) cert).getSubjectDN().getName();
                return name;
            }
        };
    }

    @Override
    public CallbackHandler create(Message message) {
        TLSSessionInfo tlsSession = message.get(TLSSessionInfo.class);
        if (tlsSession == null) {
            return null;
        }
        Certificate cert = getCertificate(message);
        String name = certMapper.getUserName(cert);
        return new UsernamePasswordHandler(name, cert);
    }

    /**
     * Extracts a certificate from a message, expecting to find TLSSessionInfo inside.
     */
    private Certificate getCertificate(Message message) {
        TLSSessionInfo tlsSessionInfo = message.get(TLSSessionInfo.class);
        if (tlsSessionInfo == null) {
            throw new SecurityException("Not TLS connection");
        }

        Certificate[] certificates = tlsSessionInfo.getPeerCertificates();
        if (certificates == null || certificates.length == 0) {
            throw new SecurityException("No certificate found");
        }

        // Due to RFC5246, senders certificates always comes 1st
        return certificates[0];
    }

    public void setCertMapper(CertificateToNameMapper certMapper) {
        this.certMapper = certMapper;
    }
}
