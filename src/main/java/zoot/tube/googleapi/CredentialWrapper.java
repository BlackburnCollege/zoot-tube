package zoot.tube.googleapi;

import com.google.api.client.auth.oauth2.Credential;
import java.io.Serializable;

public class CredentialWrapper implements Serializable {

    private static final long serialVersionUID = 1L;

    private Credential credential;

    public CredentialWrapper(Credential credential) {
        this.credential = credential;
    }

    public Credential getCredential() {
        return credential;
    }
}
