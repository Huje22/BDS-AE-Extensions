package me.indian.host2play.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.HttpURLConnection;
import me.indian.bds.util.GsonUtil;
import me.indian.host2play.Host2PlayExtension;
import me.indian.host2play.component.KeyTest;
import me.indian.host2play.component.payment.get.PaymentGet;
import me.indian.host2play.component.payment.post.PaymentPost;
import me.indian.host2play.component.payment.post.PostReceivedData;
import me.indian.host2play.config.Config;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.Nullable;

public final class RequestUtil {


    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient();
    private static final Gson GSON = GsonUtil.getGson();
    private static final String MAIN_URL = "https://host2play.pl/api/v1/";
    private static Host2PlayExtension EXTENSION;
    private static Config CONFIG;
    private static String API_KEY;

    public static void init(final Host2PlayExtension extension) {
        EXTENSION = extension;
        CONFIG = extension.getConfig();
        API_KEY = CONFIG.getApiKey();
    }

    public static boolean testKey() {
        try (final Response response = sendGetRequest("ok")) {
            if (response.isSuccessful()) {
                final String json = response.body().string();
                return GSON.fromJson(json, KeyTest.class).success() == 1;
            }
        } catch (final IOException exception) {
            exception.printStackTrace();
        }
        return false;
    }

    @Nullable
    public static PaymentGet getPayment(final String id) throws IOException {
        try (final Response response = sendGetRequest("payments/" + id)) {
            if (response.isSuccessful()) {
                if (response.code() != HttpURLConnection.HTTP_OK) return null;
                return GSON.fromJson(response.body().string(), PaymentGet.class);
            }
        }
        return null;
    }

    @Nullable
    public static PostReceivedData createPaymentPost(final String emil, final int money) throws IOException {
        final PaymentPost post = new PaymentPost(emil,
                money,
                "PLN",
                EXTENSION.getFullNotificationEndpoint(),
                CONFIG.getSuccessRedirectUrl(),
                CONFIG.getCancelRedirectUrl(),
                "Płatność za pomocą rozszerzenia",
                true);

        try (final Response response = sendPostRequest("payments/create", GSON.toJson(post))) {
            if (response.isSuccessful()) {
                if (response.code() != HttpURLConnection.HTTP_OK) return null;
                return GSON.fromJson(response.body().string(), PostReceivedData.class);
            }
        }
        return null;
    }

    public static Response sendGetRequest(final String endpointName) throws IOException {
        final Request request = new Request.Builder()
                .url(MAIN_URL + endpointName)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + API_KEY)
                .get()
                .build();

        return HTTP_CLIENT.newCall(request).execute();
    }

    public static Response sendPostRequest(final String endpointName, final String jsonBody) throws IOException {
        final Request request = new Request.Builder()
                .url(MAIN_URL + endpointName)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + API_KEY)
                .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                .build();

        return HTTP_CLIENT.newCall(request).execute();
    }

    @Nullable
    public static String getOwnIP() throws IOException {
        final Request request = new Request.Builder()
                .url("https://api64.ipify.org?format=json")
                .build();


        try (final Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (response.isSuccessful()) {

                final JsonObject jsonObject = JsonParser.parseString(response.body().string())
                        .getAsJsonObject();

                if (jsonObject.has("ip")) {
                    return jsonObject.get("ip").getAsString();
                }
            }
        }
        return null;
    }
}