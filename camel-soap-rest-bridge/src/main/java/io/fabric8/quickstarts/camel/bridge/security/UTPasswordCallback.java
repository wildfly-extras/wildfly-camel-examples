package io.fabric8.quickstarts.camel.bridge.security;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.wss4j.common.ext.WSPasswordCallback;

public class UTPasswordCallback implements CallbackHandler {

    private Map<String, String> passwords = new HashMap<>();

    public UTPasswordCallback() {
        passwords.put("karafadmin", "karafadmin");
        passwords.put("admin", "passw0rd");
    }

    /**
     * Here, we attempt to get the password from the private
     * alias/passwords map.
     */
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            WSPasswordCallback pc = (WSPasswordCallback)callbacks[i];

            String pass = passwords.get(pc.getIdentifier());
            if (pass != null) {
                pc.setPassword(pass);
                return;
            } else {
                pc.setPassword("passw0rd");
                return;
            }
        }
    }
}