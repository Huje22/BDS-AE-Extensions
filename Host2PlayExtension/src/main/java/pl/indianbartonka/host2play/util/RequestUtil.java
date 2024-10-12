package pl.indianbartonka.host2play.util;

import com.google.gson.Gson;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import pl.indianbartonka.bds.util.GsonUtil;
import pl.indianbartonka.bds.util.HTTPUtil;
import pl.indianbartonka.host2play.Host2PlayExtension;
import pl.indianbartonka.host2play.component.KeyTest;
import pl.indianbartonka.host2play.component.payment.get.PaymentGet;
import pl.indianbartonka.host2play.component.payment.post.PaymentPost;
import pl.indianbartonka.host2play.component.payment.post.PostReceivedData;
import pl.indianbartonka.host2play.config.Config;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.Nullable;

public final class RequestUtil {

    private static final OkHttpClient HTTP_CLIENT = HTTPUtil.getOkHttpClient();
    private static final Gson GSON = GsonUtil.getGson();
    private static final String MAIN_URL = "https://host2play.pl/api/v1/";
    private static final Map<String, PostReceivedData> LAST_TRANSACTIONS = new HashMap<>();
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


        PostReceivedData receivedData = null;
        try (final Response response = sendPostRequest("payments/create", GSON.toJson(post))) {
            final String body = response.body().string();

            if (response.isSuccessful()) {
                if (response.code() != HttpURLConnection.HTTP_OK) return null;
                receivedData = GSON.fromJson(body, PostReceivedData.class);
                LAST_TRANSACTIONS.put(receivedData.data().paymentId(), receivedData);
                return receivedData;
            }
        }
        return receivedData;
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

    public static Map<String, PostReceivedData> getLastTransactions() {
        return LAST_TRANSACTIONS;
    }

    public static PostReceivedData getLastTransactionData(final String paymentID) {
        return LAST_TRANSACTIONS.get(paymentID);
    }
}
