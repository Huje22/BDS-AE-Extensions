package me.indian.broadcast.core.models.auth;

public class LiveDeviceCodeResponse {
    public String user_code;
    public String device_code;
    public String verification_uri;
    public int interval;
    public int expires_in;
}
