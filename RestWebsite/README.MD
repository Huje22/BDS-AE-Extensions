<div align="center">

# RestWebsite

Strona [REST](https://pl.wikipedia.org/wiki/Representational_state_transfer) [API](https://pl.wikipedia.org/wiki/API) ,
z statystykami graczy<br>
Wszystkie domyślnie dostępne endpointy [DOCUMENTATION.MD](DOCUMENTATION.MD)

</div>

## API

Te rozserzenie zawiera także API pod robienie własnych endpointów<br>

### Klasa główna

```java
public class MyExtension extends Extension {

    @Override
    public void onEnable() {
        final RestWebsite restWebsite = (RestWebsite) this.getBdsAutoEnable().getExtensionManager().getExtension("RestWebsite");

        if (restWebsite != null) {
            if (restWebsite.isEnabled()) {
                restWebsite.register(new ExampleHandler());
            }
        }
    }
}
```

### ExampleHandler.java

```java
public class ExampleHandler extends HttpHandler {

    @Override
    public void handle(final Javalin app) {
    }
}
```