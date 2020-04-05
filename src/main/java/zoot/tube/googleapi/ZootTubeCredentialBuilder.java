package zoot.tube.googleapi;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;

public class ZootTubeCredentialBuilder extends Credential.Builder {

    public ZootTubeCredentialBuilder() {
        super(BearerToken.authorizationHeaderAccessMethod());
        setTokenServerEncodedUrl(GoogleOAuthConstants.TOKEN_SERVER_URL);
    }

    public ZootTubeCredentialBuilder setClientSecrets(String clientId, String clientSecret) {
        setClientAuthentication(new ClientParametersAuthentication(clientId, clientSecret));
        return this;
    }


    @Override
    public ZootTubeCredentialBuilder setTransport(HttpTransport transport) {
        return (ZootTubeCredentialBuilder) super.setTransport(transport);
    }


    @Override
    public ZootTubeCredentialBuilder setJsonFactory(JsonFactory jsonFactory) {
        return (ZootTubeCredentialBuilder) super.setJsonFactory(jsonFactory);
    }
}
