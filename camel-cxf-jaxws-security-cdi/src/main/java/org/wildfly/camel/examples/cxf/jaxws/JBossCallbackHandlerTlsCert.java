/**
 *
 */
package org.wildfly.camel.examples.cxf.jaxws;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.security.auth.callback.CallbackHandler;

import org.apache.cxf.interceptor.security.callback.CallbackHandlerProvider;
import org.apache.cxf.interceptor.security.callback.CertificateToNameMapper;
import org.apache.cxf.interceptor.security.callback.NameToPasswordMapper;
import org.apache.cxf.message.Message;
import org.apache.cxf.security.transport.TLSSessionInfo;
import org.jboss.security.auth.callback.UsernamePasswordHandler;

/**
 *
 * Copy of <code>org.apache.cxf.interceptor.security.callback.CallbackHandlerTlsCert</code> that returns a
 * Callback Handler that fits to the <code>org.jboss.security.auth.spi.BaseCertLoginModule</code>
 * Due to some private methods/fields I made a copy instead of a subclass that just overrides create(...)
 *
 * @author Jochen Riedlinger
 *
 */
public class JBossCallbackHandlerTlsCert /*extends CallbackHandlerTlsCert*/ implements CallbackHandlerProvider {

	private CertificateToNameMapper certMapper;
    private NameToPasswordMapper nameToPasswordMapper;
    private String fixedPassword;

    public JBossCallbackHandlerTlsCert(String alias) {
        // By default use subjectDN as userName
        this.certMapper = new CertificateToNameMapper() {
            public String getUserName(Certificate cert) {
//                return ((X509Certificate)cert).getSubjectDN().getName();
            	return alias;
            }
        };
        // By default use fixed password
        this.nameToPasswordMapper = new NameToPasswordMapper() {
            public String getPassword(String userName) {
                return fixedPassword;
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
        String password = nameToPasswordMapper.getPassword(name);
        return new UsernamePasswordHandler(name, cert);
    }

    /**
     * Extracts certificate from message, expecting to find TLSSessionInfo inside.
     *
     * @param message
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

    public void setFixedPassword(String fixedPassword) {
        this.fixedPassword = fixedPassword;
    }

    public void setNameToPasswordMapper(NameToPasswordMapper nameToPasswordMapper) {
        this.nameToPasswordMapper = nameToPasswordMapper;
    }

}
