package ph.coreproc.android.procex.libs.HttpClient;

import com.google.gson.JsonObject;
import com.koushikdutta.ion.Response;

/**
 * Interface for HTTP requests
 *
 * @author chrisbjr
 */
public interface HttpClientCallback {
    public void onStart();

    public void onFinish();

    public void onError(HttpClientError httpClientError);

    public void onSuccess(Response<JsonObject> response);
}
