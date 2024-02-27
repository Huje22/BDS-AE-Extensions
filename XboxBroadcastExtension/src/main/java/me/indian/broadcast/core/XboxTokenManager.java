package me.indian.broadcast.core;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.indian.bds.logger.Logger;
import me.indian.broadcast.core.models.auth.GenericAuthenticationRequest;
import me.indian.broadcast.core.models.auth.GenericAuthenticationResponse;
import me.indian.broadcast.core.models.auth.JsonJWK;
import me.indian.broadcast.core.models.auth.SISUAuthenticationResponse;
import me.indian.broadcast.core.models.auth.XboxTokenCache;
import me.indian.broadcast.core.models.auth.XboxTokenInfo;

/**
 * Handle authentication against Xbox servers and caching of the received tokens
 */
public class XboxTokenManager {
    private final Path cache;
    private final HttpClient httpClient;
    private final ECKey jwk;
    private final Logger logger;

    /**
     * Create an instance of XboxTokenManager
     *
     * @param cache      The directory to store the cached tokens in
     * @param httpClient The HTTP client to use for all requests
     * @param logger     The logger to use for outputting messages
     */
    public XboxTokenManager(final String cache, final HttpClient httpClient, final Logger logger) {
        this.cache = Paths.get(cache, "xbox_token.json");
        this.httpClient = httpClient;
        this.logger = logger;

        ECKey jwk = null;
        try {
            jwk = new ECKeyGenerator(Curve.P_256)
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(JWSAlgorithm.ES256)
                    .generate();
        } catch (final JOSEException exception) {
            logger.error("Failed to setup xbox jwk", exception);
        }
        this.jwk = jwk;
    }

    /**
     * Check if the cached token is valid
     *
     * @return true if the token in the cache is valid
     */
    public boolean verifyTokens() {
        final XboxTokenCache tokenCache = this.getCache();

        // Check if the token is null
        if (tokenCache.xstsToken() == null ||
                tokenCache.xstsToken().expiresOn() == null) {
            return false;
        }

        // Check if the XSTS token is valid
        final long xstsExpiry = Instant.parse(tokenCache.xstsToken().expiresOn()).toEpochMilli() - Instant.now().toEpochMilli();
        final boolean xstsValid = xstsExpiry > 1000;

        return xstsValid;
    }

    /**
     * Fetch the XSTS token from cache
     *
     * @return The stored XSTS token and relevant information
     */
    public XboxTokenInfo getCachedXstsToken() {
        return this.getCache().xstsToken();
    }

    /**
     * Get a device token from the xbox authentication servers
     *
     * @return The fetched JWT device token
     */
    public String getDeviceToken() {
        try {
            // Create the JWT device request payload
            final GenericAuthenticationRequest requestContent = new GenericAuthenticationRequest(
                    "http://auth.xboxlive.com",
                    "JWT",
                    new GenericAuthenticationRequest.DeviceProperties(
                            "ProofOfPossession",
                            "{" + UUID.randomUUID() + "}",
                            "Android",
                            "10",
                            new JsonJWK(this.jwk.toPublicJWK())
                    )
            );

            // Convert the request to a string, so it can be used to sign the request
            final String requestContentString = Constants.OBJECT_MAPPER.writeValueAsString(requestContent);

            // Send the payload off to the authentication service
            final HttpRequest authRequest = HttpRequest.newBuilder()
                    .uri(Constants.DEVICE_AUTHENTICATE_REQUEST)
                    .header("Content-Type", "application/json")
                    .header("Signature", this.sign(Constants.DEVICE_AUTHENTICATE_REQUEST, requestContentString))
                    .POST(HttpRequest.BodyPublishers.ofString(requestContentString))
                    .build();

            // Get and parse the response
            final GenericAuthenticationResponse tokenResponse = Constants.OBJECT_MAPPER.readValue(this.httpClient.send(authRequest, HttpResponse.BodyHandlers.ofString()).body(), GenericAuthenticationResponse.class);

            return tokenResponse.Token;
        } catch (final IOException | InterruptedException exception) {
            this.logger.error("Failed to get device authentication token", exception);
            return null;
        }
    }

    /**
     * Get a XSTS token from the xbox authentication servers
     * using a full set of user, device and title tokens
     *
     * @param sisuAuthenticationResponse The response from SISU containing the user, device and title tokens
     * @return The XSTS token received
     */
    public XboxTokenInfo getXSTSToken(final SISUAuthenticationResponse sisuAuthenticationResponse) {
        try {
            // Create the XSTS request payload
            final GenericAuthenticationRequest requestContent = new GenericAuthenticationRequest(
                    Constants.RELAYING_PARTY,
                    "JWT",
                    new GenericAuthenticationRequest.XSTSProperties(
                            Collections.singletonList(sisuAuthenticationResponse.UserToken().Token),
                            sisuAuthenticationResponse.DeviceToken(),
                            sisuAuthenticationResponse.TitleToken().Token,
                            "RETAIL",
                            new JsonJWK(this.jwk.toPublicJWK())
                    )
            );

            // Convert the request to a string, so it can be used to sign the request
            final String requestContentString = Constants.OBJECT_MAPPER.writeValueAsString(requestContent);

            // Send the payload off to the authentication service
            final HttpRequest authRequest = HttpRequest.newBuilder()
                    .uri(Constants.XSTS_AUTHENTICATE_REQUEST)
                    .header("Content-Type", "application/json")
                    .header("Signature", this.sign(Constants.XSTS_AUTHENTICATE_REQUEST, requestContentString))
                    .POST(HttpRequest.BodyPublishers.ofString(requestContentString))
                    .build();

            // Get and parse the response
            final GenericAuthenticationResponse tokenResponse = Constants.OBJECT_MAPPER.readValue(this.httpClient.send(authRequest, HttpResponse.BodyHandlers.ofString()).body(), GenericAuthenticationResponse.class);

            // Create the cut down information for caching
            final XboxTokenInfo xboxTokenInfo = new XboxTokenInfo(
                    tokenResponse.DisplayClaims.xui.get(0).xid,
                    tokenResponse.DisplayClaims.xui.get(0).uhs,
                    tokenResponse.DisplayClaims.xui.get(0).gtg,
                    tokenResponse.Token,
                    tokenResponse.NotAfter);

            // Update the cache with the new information
            this.updateCache(new XboxTokenCache(xboxTokenInfo));

            return xboxTokenInfo;
        } catch (final IOException | InterruptedException exception) {
            this.logger.error("Failed to get XSTS authentication token", exception);
            return null;
        }
    }

    /**
     * Use SISU to get a user and title token from the xbox authentication servers
     *
     * @param msa         The MSA token to use
     * @param deviceToken The device token to use
     * @return The SISU response containing the user title and device tokens
     */
    public SISUAuthenticationResponse getSISUToken(final String msa, final String deviceToken) {
        try {
            final Map<String, Object> requestContent = new HashMap<>() {{
                this.put("AccessToken", "t=" + msa);
                this.put("AppId", Constants.AUTH_TITLE);
                this.put("deviceToken", deviceToken);
                this.put("Sandbox", "RETAIL");
                this.put("UseModernGamertag", true);
                this.put("SiteName", "user.auth.xboxlive.com");
                this.put("RelyingParty", "https://multiplayer.minecraft.net/");
                this.put("ProofKey", new JsonJWK(XboxTokenManager.this.jwk.toPublicJWK()));
            }};

            // Convert the request to a string, so it can be used to sign the request
            final String requestContentString = Constants.OBJECT_MAPPER.writeValueAsString(requestContent);

            // Send the payload off to the authentication service
            final URI uri = URI.create("https://sisu.xboxlive.com/authorize");
            final HttpRequest authRequest = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/json")
                    .header("Signature", this.sign(uri, requestContentString))
                    .POST(HttpRequest.BodyPublishers.ofString(requestContentString))
                    .build();

            // Get and parse the response
            final SISUAuthenticationResponse tokenResponse = Constants.OBJECT_MAPPER.readValue(this.httpClient.send(authRequest, HttpResponse.BodyHandlers.ofString()).body(), SISUAuthenticationResponse.class);

            return tokenResponse;
        } catch (final IOException | InterruptedException exception) {
            this.logger.error("Failed to get SISU authentication token", exception);
            return null;
        }
    }

    /**
     * Sign the given text string into the format the api uses
     * Based on https://github.com/PrismarineJS/prismarine-auth/blob/b2890e09d1767a3ac501c62ea7f094a7f5d4231b/src/TokenManagers/XboxTokenManager.js#L100
     *
     * @param uri     The URI to generate the signature for
     * @param payload The request payload to sign
     * @return The base64 string for the hash
     */
    private String sign(final URI uri, final String payload) {
        try {
            // Their backend servers use Windows epoch timestamps, account for that. The server is very picky,
            // bad percision or wrong epoch may fail the request.
            final long windowsTimestamp = (((System.currentTimeMillis() / 1000)) + 11644473600L) * 10000000L;
            // Only the /uri?and-query-string
            final String pathAndQuery = uri.getPath();

            // Allocate the buffer for signature, TS, path, tokens and payload and NUL termination
            final int allocSize = /* sig */ 5 +  /* ts */ 9 +  /* POST */ 5 + pathAndQuery.length() + 1 +  /* empty field */ 1 + payload.length() + 1;
            ByteBuffer buf = ByteBuffer.allocate(allocSize);
            buf.putInt(1); // Policy Version
            buf.put((byte) 0x0);
            buf.putLong(windowsTimestamp);
            buf.put((byte) 0x0);
            buf.put("POST".getBytes(StandardCharsets.UTF_8));
            buf.put((byte) 0x0);
            buf.put(pathAndQuery.getBytes(StandardCharsets.UTF_8));
            buf.put((byte) 0x0);
            // Empty field would be here
            buf.put((byte) 0x0);
            buf.put(payload.getBytes(StandardCharsets.UTF_8));
            buf.put((byte) 0x0);

            buf.rewind();

            // Sign the buffer with our key
            final Signature signature = Signature.getInstance("SHA256withECDSAinP1363Format");
            signature.initSign(this.jwk.toECPrivateKey());
            signature.update(buf);

            final byte[] arrSignature = signature.sign();

            // Construct the final buffer data
            buf = ByteBuffer.allocate(arrSignature.length + 12);
            buf.putInt(1); // Policy Version
            buf.putLong(windowsTimestamp);
            buf.put(arrSignature);

            buf.rewind();
            final byte[] arrFinal = new byte[buf.remaining()];
            buf.get(arrFinal);

            // Encode the final data as base64 and return
            return Base64.getEncoder().encodeToString(arrFinal);
        } catch (final NoSuchAlgorithmException | JOSEException | InvalidKeyException | SignatureException exception) {
            this.logger.error("Failed to get create signature for message", exception);
        }

        return null;
    }

    /**
     * Read and parse the current cache file
     *
     * @return The parsed cache
     */
    private XboxTokenCache getCache() {
        try {
            return Constants.OBJECT_MAPPER.readValue(Files.readString(this.cache), XboxTokenCache.class);
        } catch (final IOException exception) {
            return new XboxTokenCache();
        }
    }

    /**
     * Store updated values into the cache file
     *
     * @param updatedCache The updated values to store
     */
    private void updateCache(final XboxTokenCache updatedCache) {
        try (final FileWriter writer = new FileWriter(this.cache.toString(), StandardCharsets.UTF_8)) {
            Constants.OBJECT_MAPPER.writeValue(writer, updatedCache);
        } catch (final IOException exception) {
            this.logger.error("Failed to update xbox token cache", exception);
        }
    }
}
