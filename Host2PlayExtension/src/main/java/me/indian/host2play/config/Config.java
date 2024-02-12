package me.indian.host2play.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import java.util.UUID;

public class Config extends OkaeriConfig {


    @Comment({""})
    @Comment({"Klucz API brany z:"})
    @Comment({"https://host2play.pl/api/settings"})
    private String apiKey = "ssadajJ";

    @Comment({""})
    @Comment({"Url strony gdy użytkownik dokona płatności"})
    private String successRedirectUrl = "https://example.com/payment/success";

    @Comment({""})
    @Comment({"Url strony gdy użytkownik anuluje płatność"})
    private String cancelRedirectUrl = "https://example.com/payment/cancel";

    @Comment({""})
    @Comment({"ID endpointu , tak aby nie było trzeba uzywać zadnych api kluczy,"})
    @Comment({"jest on ci nie potrzebny ale jak ktoś go dostanie najlepiej go usuń a wygeneruje sie nowy , nic nie będziesz musiał zmieniać"})
    private String endpointUUID = UUID.randomUUID().toString();


    public String getApiKey() {
        return this.apiKey;
    }

    public String getEndpointUUID() {
        return this.endpointUUID;
    }

    public String getSuccessRedirectUrl() {
        return this.successRedirectUrl;
    }

    public String getCancelRedirectUrl() {
        return this.cancelRedirectUrl;
    }
}
