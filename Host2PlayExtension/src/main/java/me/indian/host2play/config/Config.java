package me.indian.host2play.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import eu.okaeri.configs.annotation.Header;
import java.util.UUID;
import me.indian.util.MessageUtil;

@Header("################################################################")
@Header("#           Ustawienia Host2PlayExtension                      #")
@Header("################################################################")

public class Config extends OkaeriConfig {

    @Comment({""})
    @Comment({"Klucz API brany z:"})
    @Comment({"https://host2play.pl/api/settings"})
    @CustomKey("ApiKey")
    private String apiKey = MessageUtil.generateCode(50);

    @Comment({""})
    @Comment({"Czy włączyć !donate ?"})
    @Comment({"Zezwala ona na tworzenie płatności przez użytkowników, aby mogli dokonać darowizny"})
    @CustomKey("DonateCommand")
    private boolean donateCommand = true;

    @Comment({""})
    @Comment({"Twój aktualny adres IP , potrzebny dla 'notificationUrl'"})
    @CustomKey("IP")
    private String ip;

    @Comment({""})
    @Comment({"Jeśli masz zmienny adres ip zostaw to na true"})
    @CustomKey("DynamicIP")
    private boolean dynamicIP = true;

    @Comment({""})
    @Comment({"Url strony gdy użytkownik dokona płatności"})
    @CustomKey("SuccessRedirectUrl")
    private String successRedirectUrl = "https://example.com/payment/success";

    @Comment({""})
    @Comment({"Url strony gdy użytkownik anuluje płatność"})
    @CustomKey("CancelRedirectUrl")
    private String cancelRedirectUrl = "https://example.com/payment/cancel";

    @Comment({""})
    @Comment({"ID endpointu 'notification' , tak aby nie było trzeba używać żadnych api kluczy,"})
    @Comment({"jest on ci nie potrzebny ale jak ktoś go dostanie najlepiej go usuń a wygeneruje sie nowy , nic nie będziesz musiał zmieniać"})
    @CustomKey("EndpointUUID")
    private String endpointUUID = UUID.randomUUID().toString();

    public String getApiKey() {
        return this.apiKey;
    }

    public String getIp() {
        return this.ip;
    }

    public void setIp(final String ip) {
        this.ip = ip;
    }

    public boolean isDonateCommand() {
        return this.donateCommand;
    }

    public boolean isDynamicIP() {
        return this.dynamicIP;
    }

    public String getSuccessRedirectUrl() {
        return this.successRedirectUrl;
    }

    public String getCancelRedirectUrl() {
        return this.cancelRedirectUrl;
    }

    public String getEndpointUUID() {
        return this.endpointUUID;
    }
}
