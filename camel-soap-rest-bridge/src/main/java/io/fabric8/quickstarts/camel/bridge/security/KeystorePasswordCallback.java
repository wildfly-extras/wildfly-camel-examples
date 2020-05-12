package io.fabric8.quickstarts.camel.bridge.security;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.wss4j.common.ext.WSPasswordCallback;

public class KeystorePasswordCallback implements CallbackHandler {

    private Map<String, String> passwords =
        new HashMap<>();

    public KeystorePasswordCallback() {
        passwords.put("Alice", "abcd!1234");
        passwords.put("alice", "password");
        passwords.put("Bob", "abcd!1234");
        passwords.put("bob", "password");
        passwords.put("abcd", "dcba");
        passwords.put("6e0e88f36ebb8744d470f62f604d03ea4ebe5094", "password");
        passwords.put("wss40rev", "security");
        passwords.put("morpit", "password");
        passwords.put("myclientkey", "ckpass");
        passwords.put("myservicekey", "skpass");
    }

    /**
     * It attempts to get the password from the private
     * alias/passwords map.
     */
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            WSPasswordCallback pc = (WSPasswordCallback)callbacks[i];
            if (pc.getUsage() == WSPasswordCallback.PASSWORD_ENCRYPTOR_PASSWORD) {
                pc.setPassword("this-is-a-secret");
            } else {
                String pass = passwords.get(pc.getIdentifier());
                if (pass != null) {
                    pc.setPassword(pass);
                    return;
                }
                pc.setPassword("password");
            }
        }
    }


}
