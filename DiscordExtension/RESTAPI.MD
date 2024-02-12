### <u>Wymaga rozszerzenia [RestWebsite](..%2FRestWeb)</u>

### Wyślij wiadomość do kanału czatu jako gracz

```
POST /discord/message/{api-key}
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
curl -X POST -H "Content-Type: application/json" -d "{ \"name\": \"REST API\",\"message\": \"Wysyłam wiadomość na discord!\",\"messageType\":\"WEBHOOK\"}" http://localhost:PORT/discord/message/KLUCZ_API
```
