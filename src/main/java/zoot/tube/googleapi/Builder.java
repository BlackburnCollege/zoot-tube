package zoot.tube.googleapi;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;

/**
 * Source: com.google.api.client.googleapis.auth.oauth2.Credential.Builder
 * The above mentioned Credential class has been deprecated. But the functionality
 * is still correct for the Builder.
 * <p>
 * Primary use is to be able to create a blank {@link Credential}.
 */
public class Builder extends Credential.Builder {

    public Builder() {
        super(BearerToken.authorizationHeaderAccessMethod());
        setTokenServerEncodedUrl(GoogleOAuthConstants.TOKEN_SERVER_URL);
    }

    public Builder setClientSecrets(String clientId, String clientSecret) {
        setClientAuthentication(new ClientParametersAuthentication(clientId, clientSecret));
        return this;
    }

    @Override
    public Builder setTransport(HttpTransport transport) {
        return (Builder) super.setTransport(transport);
    }

    @Override
    public Builder setJsonFactory(JsonFactory jsonFactory) {
        return (Builder) super.setJsonFactory(jsonFactory);
    }
}
