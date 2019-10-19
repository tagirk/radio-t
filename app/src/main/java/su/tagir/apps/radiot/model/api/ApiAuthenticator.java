package su.tagir.apps.radiot.model.api;

import androidx.annotation.Nullable;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class ApiAuthenticator  implements Authenticator {


    private AuthHolder authHolder;

    public ApiAuthenticator(AuthHolder authHolder) {
        this.authHolder = authHolder;
    }

    @Nullable
    @Override
    public synchronized Request authenticate(Route route, Response response) {
        String storedToken = authHolder.getAccessToken();
        String authHeader = authHolder.getAuthHeader();
        String requestToken = response.request().header(authHeader);



        if (storedToken == null || storedToken.equals(requestToken)) {
            authHolder.refresh();
            return null;
        }
        Request.Builder requestBuilder = response.request().newBuilder();
        return buildRequest(requestBuilder);
    }

    private Request buildRequest(Request.Builder requestBuilder) {
        String authHeader = authHolder.getAuthHeader();
        return requestBuilder.header(authHeader, authHolder.getAccessToken()).build();
    }

}
