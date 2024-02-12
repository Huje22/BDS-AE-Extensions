package me.indian.rest;

public interface Request {

    //TODO: Zmień nazwę na "HttpHandler" i zrób z tego klasabstrakcjna
    void init();

    void handle();
    void handle(final Javalin app);

}
