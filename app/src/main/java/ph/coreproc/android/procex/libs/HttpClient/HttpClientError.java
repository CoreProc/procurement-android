package ph.coreproc.android.procex.libs.HttpClient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.ion.Response;

import java.util.Map;
import java.util.Set;

/**
 * @author chrisbjr
 */
public class HttpClientError {

    private String code;
    private int httpCode;
    private String message;

    private String TAG = "HttpClient";

    public static final int INTERNET_UNAVAILABLE = 0;
    public static final int URL_INVALID = 1;

    public HttpClientError() {
        // we can initialize this with an empty constructor
    }

    public HttpClientError(Response<JsonObject> response) {

        if (response.getHeaders().get("Content-type").equals("application/json") == false) {
            // we are expecting JSON all the time
            httpCode = response.getHeaders().getResponseCode();
            message = HttpResponse.getHttpResponseMessage(httpCode);
            return;
        }

        JsonObject result = response.getResult();

        if (!result.has("error")) {
            // This is an error we cannot read
            httpCode = response.getHeaders().getResponseCode();
            message = HttpResponse.getHttpResponseMessage(httpCode);
            return;
        }

        JsonObject error = result.get("error").getAsJsonObject();

        // we set the values
        code = error.get("code").getAsString();
        httpCode = error.get("http_code").getAsInt();

        // parse message
        if (error.has("message")) {
            if (error.get("message").isJsonObject()) {
                message = "";
                JsonObject messageObject = error.get("message").getAsJsonObject();
                Set<Map.Entry<String, JsonElement>> entrySet = messageObject.entrySet();
                for (Map.Entry<String, JsonElement> entry : entrySet) {
                    message += messageObject.get(entry.getKey()).getAsString() + "\n";
                }
            } else {
                // we assume that message is a string?
                message = error.get("message").getAsString();
            }
        } else {
            message = HttpResponse.getHttpResponseMessage(httpCode);
        }
    }

    public static HttpClientError noInternetConnection() {
        HttpClientError httpClientError = new HttpClientError();
        httpClientError.httpCode = HttpClientError.INTERNET_UNAVAILABLE;
        httpClientError.message = "No Internet Connection";
        httpClientError.code = "NO-INTERNET-CONNECTION";
        return httpClientError;
    }

    public static HttpClientError invalidUrl() {
        HttpClientError httpClientError = new HttpClientError();
        httpClientError.httpCode = HttpClientError.URL_INVALID;
        httpClientError.message = "Invalid URL";
        httpClientError.code = "INVALID-URL";
        return httpClientError;
    }

    public String getCode() {
        return code;
    }

    public int getHttpCode() {
        return httpCode;
    }

    public String getMessage() {
        return message;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setHttpCode(int httpCode) {
        this.httpCode = httpCode;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
