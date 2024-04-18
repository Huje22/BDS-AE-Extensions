### <u>Wymaga rozszerzenia [RestWebsite](..%2FRestWebsite)</u>

### Wyślij wiadomość do kanału czatu jako gracz

```
POST /discord/message
```

#### Przykładowy JSON

```json
{
  "name": "REST API",
  "message": "Wysyłam wiadomość na discord!",
  "messageType": "WEBHOOK"
}
```

**<sup>Możesz użyć też <code>JDA</code></sup>**

#### Przykład wysłania JSON z poziomu konsoli

```
curl -X POST -H "Content-Type: application/json" -H "Authorization:KLUCZ API" -d "{ \"name\": \"REST API\",\"message\": \"Wysyłam wiadomość na discord!\",\"messageType\":\"WEBHOOK\"}" http://localhost:PORT/discord/message
```
