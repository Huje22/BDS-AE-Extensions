package me.indian.host2play.util;

import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RequestUtil {

    private static OkHttpClient HTTP_CLIENT = new OkHttpClient();
    private static String API_KEY = "test.809ed3003531.JKRTTFQ-KLXEESA-TRLFHSY-3XFH7LA";


    public Response sendRequest(final String endpointURL, final RequestBody requestBody) throws IOException {
        final Request request = new Request.Builder()
                .url(endpointURL)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + API_KEY)

                .post(requestBody)
                .build();

        return HTTP_CLIENT.newCall(request).execute();
    }
}
