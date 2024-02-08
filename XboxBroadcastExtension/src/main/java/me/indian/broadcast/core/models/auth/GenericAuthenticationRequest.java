package me.indian.broadcast.core.models.auth;

import java.util.List;

public class GenericAuthenticationRequest {
    public String RelyingParty;
    public String TokenType;
    public GenericAuthenticationRequestProperties Properties;

    public GenericAuthenticationRequest(final String RelyingParty, final String TokenType, final GenericAuthenticationRequestProperties Properties) {
        this.RelyingParty = RelyingParty;
        this.TokenType = TokenType;
        this.Properties = Properties;
    }

    public static class UserProperties implements GenericAuthenticationRequestProperties {
        public String AuthMethod;
        public String SiteName;
        public String RpsTicket;
        public JsonJWK ProofKey;

        public UserProperties(final String AuthMethod, final String SiteName, final String RpsTicket, final JsonJWK ProofKey) {
            this.AuthMethod = AuthMethod;
            this.SiteName = SiteName;
            this.RpsTicket = RpsTicket;
            this.ProofKey = ProofKey;
        }
    }

    public static class DeviceProperties implements GenericAuthenticationRequestProperties {
        public String AuthMethod;
        public String Id;
        public String DeviceType;
        public String Version;
        public JsonJWK ProofKey;

        public DeviceProperties(final String AuthMethod, final String Id, final String DeviceType, final String Version, final JsonJWK ProofKey) {
            this.AuthMethod = AuthMethod;
            this.Id = Id;
            this.DeviceType = DeviceType;
            this.Version = Version;
            this.ProofKey = ProofKey;
        }
    }

    public static class TitleProperties implements GenericAuthenticationRequestProperties {
        public String AuthMethod;
        public String DeviceToken;
        public String RpsTicket;
        public String SiteName;
        public JsonJWK ProofKey;

        public TitleProperties(final String AuthMethod, final String DeviceToken, final String RpsTicket, final String SiteName, final JsonJWK ProofKey) {
            this.AuthMethod = AuthMethod;
            this.DeviceToken = DeviceToken;
            this.RpsTicket = RpsTicket;
            this.SiteName = SiteName;
            this.ProofKey = ProofKey;
        }
    }

    public static class XSTSProperties implements GenericAuthenticationRequestProperties {
        public List<String> UserTokens;
        public String DeviceToken;
        public String TitleToken;
        public String SandboxId;
        public JsonJWK ProofKey;

        public XSTSProperties(final List<String> UserTokens, final String DeviceToken, final String TitleToken, final String SandboxId, final JsonJWK ProofKey) {
            this.UserTokens = UserTokens;
            this.DeviceToken = DeviceToken;
            this.TitleToken = TitleToken;
            this.SandboxId = SandboxId;
            this.ProofKey = ProofKey;
        }
    }
}
