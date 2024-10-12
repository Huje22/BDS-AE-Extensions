package pl.indianbartonka.rest.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import eu.okaeri.configs.annotation.Header;
import java.util.List;

@Header("################################################################")
@Header("#           Ustawienia Strony z RestAPI                        #")
@Header("################################################################")

public class RestApiConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"Apy zobaczyć dostępne endpointy zobacz:"})
    @Comment({"https://github.com/Huje22/Bds-Auto-Enable/blob/master/RestAPI.MD"})
    @Comment({"Czy włączyć strone z Rest API?"})
    @CustomKey("Enable")
    private boolean enabled = false;

    @Comment({""})
    @Comment({"Port strony "})
    @CustomKey("Port")
    private int port = 8080;

    @Comment({""})
    @Comment({"Rate limit na dany endpoint"})
    @CustomKey("RateLimit")
    private int rateLimit = 20;

    @Comment({""})
    @Comment({"Adresy IP które omijają RateLimit"})
    @CustomKey("WhiteListedIP")
    private List<String> whitelistedIP = List.of("127.0.0.1");

    @Comment({""})
    @Comment({"Klucze api które możesz rozdać użytkownikom rest api aby mogli: pobierać backupy , więcej wkrótce "})
    @CustomKey("ApiKeys")
    private APIKeyConfig apiKeyConfig = new APIKeyConfig();

    public boolean isEnabled() {
        return this.enabled;
    }

    public int getPort() {
        return this.port;
    }

    public int getRateLimit() {
        return this.rateLimit;
    }

    public List<String> getWhitelistedIP() {
        return this.whitelistedIP;
    }

    public APIKeyConfig getAPIKeys() {
        return this.apiKeyConfig;
    }

    public void setApiKeyConfig(final APIKeyConfig apiKeyConfig) {
        this.apiKeyConfig = apiKeyConfig;
    }
}