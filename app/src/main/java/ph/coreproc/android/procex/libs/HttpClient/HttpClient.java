package ph.coreproc.android.procex.libs.HttpClient;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chrisbjr on 10/1/14.
 */
public class HttpClient {

    private static final String TAG = "HttpClient";
    private static final int TIMEOUT = 100000;

    private final Context mContext;
    private HttpClientCallback mHttpClientCallback;

    // Authorization headers
    private boolean mIsAuthorizationEnabled = false;
    private String mApiKey = "";
    private String mAuthorizationKey = "Authorization";

    public HttpClient(Context context) {
        mContext = context;
    }

    public void get(String url, HashMap<String, String> params, HttpClientCallback httpClientCallback) {

        mHttpClientCallback = httpClientCallback;

        mHttpClientCallback.onStart();

        // Build the URL
        if (params != null) {
            url += "?" + getQueryString(params);
        }

        // Let's validate
        if (!validate(url)) {
            return;
        }

        Log.i(TAG, "GET " + url);

        // Now we can get it
        if (mIsAuthorizationEnabled) {
            Ion.with(mContext, url)
                    .setHeader(mAuthorizationKey, mApiKey)
                    .setTimeout(TIMEOUT)
                    .asJsonObject()
                    .withResponse()
                    .setCallback(new GenericFutureCallback());
        } else {
            Ion.with(mContext, url)
                    .setTimeout(TIMEOUT)
                    .asJsonObject()
                    .withResponse()
                    .setCallback(new GenericFutureCallback());
        }

        return;
    }

    public void post(String url, HashMap<String, String> params, HttpClientCallback httpClientCallback) {

        mHttpClientCallback = httpClientCallback;

        mHttpClientCallback.onStart();

        // Let's validate
        if (!validate(url)) {
            return;
        }

        Log.i(TAG, "POST " + url);

        // Modify params because koush is an ass :p https://github.com/koush/ion/issues/200
        Map<String, List<String>> newParams = new HashMap<String, List<String>>();
        if (params != null) {
            for (ConcurrentHashMap.Entry<String, String> entry : params.entrySet()) {
                newParams.put(entry.getKey(), Arrays.asList(entry.getValue()));
            }
        }

        if (mIsAuthorizationEnabled) {
            Ion.with(mContext, url)
                    .setHeader(mAuthorizationKey, mApiKey)
                    .setTimeout(TIMEOUT)
                    .setBodyParameters(newParams)
                    .asJsonObject()
                    .withResponse()
                    .setCallback(new GenericFutureCallback());
        } else {
            Ion.with(mContext, url)
                    .setTimeout(TIMEOUT)
                    .setBodyParameters(newParams)
                    .asJsonObject()
                    .withResponse()
                    .setCallback(new GenericFutureCallback());
        }

    }

    public void post(String url, JsonObject params, HttpClientCallback httpClientCallback) {

        mHttpClientCallback = httpClientCallback;

        mHttpClientCallback.onStart();

        // Let's validate
        if (!validate(url)) {
            return;
        }

        Log.i(TAG, "POST " + url);

        if (mIsAuthorizationEnabled) {
            Ion.with(mContext, url)
                    .setHeader(mAuthorizationKey, mApiKey)
                    .setTimeout(TIMEOUT)
                    .setJsonObjectBody(params)
                    .asJsonObject()
                    .withResponse()
                    .setCallback(new GenericFutureCallback());
        } else {
            Ion.with(mContext, url)
                    .setTimeout(TIMEOUT)
                    .setJsonObjectBody(params)
                    .asJsonObject()
                    .withResponse()
                    .setCallback(new GenericFutureCallback());
        }
    }

    public void setAuthorization(String apiKey) {
        mApiKey = apiKey;
        mIsAuthorizationEnabled = true;
    }

    public void setAuthorization(String apiKey, String key) {
        mApiKey = apiKey;
        mAuthorizationKey = key;
        mIsAuthorizationEnabled = true;
    }

    private boolean validate(String url) {
        if (!isConnected()) {
            Log.e(TAG, "The device is not connected to the internet");
            mHttpClientCallback.onFinish();
            mHttpClientCallback.onError(HttpClientError.noInternetConnection());
            return false;
        }

        // Validate url
        if (!isUrlValid(url)) {
            Log.e(TAG, "The URL provided is invalid");
            mHttpClientCallback.onFinish();
            mHttpClientCallback.onError(HttpClientError.invalidUrl());
            return false;
        }

        return true;
    }

    private boolean isConnected() {
        try {
            ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        } catch (NullPointerException e) {
            // nothing
        }
        return true;
    }

    private String getQueryString(HashMap<String, String> params) {
        StringBuilder result = new StringBuilder();
        for (ConcurrentHashMap.Entry<String, String> entry : params.entrySet()) {
            if (result.length() > 0) result.append("&");
            result.append(entry.getKey());
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue()).replace("+", "%20"));
        }

        return result.toString();
    }

    private boolean isUrlValid(String url) {
        try {
            URL assignUrl = new URL(url);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Invalid URL: " + url);
            return false;
        }
        return true;
    }

    private class GenericFutureCallback implements FutureCallback<Response<JsonObject>> {

        @Override
        public void onCompleted(Exception e, Response<JsonObject> result) {

            if (e != null) {
                Log.e(TAG, "Error: " + e.getMessage());
                mHttpClientCallback.onFinish();
                HttpClientError httpClientError = new HttpClientError();
                httpClientError.setMessage(e.getMessage());
                if (result != null) {
                    httpClientError.setHttpCode(result.getHeaders().getResponseCode());
                } else {
                    httpClientError.setHttpCode(HttpClientError.INTERNET_UNAVAILABLE);
                }

                httpClientError.setCode("ION-EXCEPTION");
                mHttpClientCallback.onError(httpClientError);
                return;
            }

            int responseCode = result.getHeaders().getResponseCode();

            if (responseCode < 200 || responseCode > 299) {
                // response error
                Log.e(TAG, "Unsuccessful HTTP call: " + responseCode);
                //Log.d(TAG, result.getResult().toString());
                mHttpClientCallback.onFinish();
                mHttpClientCallback.onError(new HttpClientError(result));
                return;
            }

            // Now we have a success
            Log.i(TAG, "Successful HTTP call: " + responseCode);

            mHttpClientCallback.onFinish();
            mHttpClientCallback.onSuccess(result);
        }

    }
}
