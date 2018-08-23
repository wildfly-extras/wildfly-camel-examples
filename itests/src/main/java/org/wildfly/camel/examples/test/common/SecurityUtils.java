package org.wildfly.camel.examples.test.common;

import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;

/**
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public class SecurityUtils {

    public static SSLConnectionSocketFactory createSocketFactory(Path truststoreFile, Path keystoreFile, String password)
            throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException,
            IOException, UnrecoverableKeyException {
        final char[] pwd = password.toCharArray();
        SSLContextBuilder sslcontextBuilder = SSLContexts.custom()
                .loadTrustMaterial(truststoreFile.toFile(), pwd, TrustSelfSignedStrategy.INSTANCE)
        ;
        if (keystoreFile != null) {
            sslcontextBuilder.loadKeyMaterial(keystoreFile.toFile(), pwd, pwd);
        }

        sslcontextBuilder.setProtocol("TLSv1.2");

        return new SSLConnectionSocketFactory(sslcontextBuilder.build(), new HostnameVerifier() {
            @Override
            public boolean verify(final String s, final SSLSession sslSession) {
                return true;
            }
        });
    }
}
