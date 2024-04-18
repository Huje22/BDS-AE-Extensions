# Dokumentacja API

---

### Autoryzacja
Niektóre endpointy wymagają autoryzacji przy użyciu klucza API. Aby uzyskać dostęp, wystarczy dodać nagłówek `Authorization` zawierający klucz API.

---

## Statystyki

### Pobierz informacje o śmierciach graczy

```
GET /api/stats/deaths
```

Zwraca statystyki dotyczące liczby śmierci graczy.

### Pobierz informacje o czasie gry graczy

```
GET /api/stats/playtime
```

Zwraca statystyki dotyczące czasu gry graczy w milisekundach.

### Pobierz informacje o postawionych blokach graczy

```
GET /api/stats/block/placed
```

Zwraca statystyki dotyczące liczby postawionych bloków przez graczy.

### Pobierz informacje o zniszczonych blokach graczy

```
GET /api/stats/block/broken
```

Zwraca statystyki dotyczące liczby zniszczonych bloków przez graczy.

### Pobierz informacje o graczu na podstawie nazwy gracza

```
GET /api/stats/player/name/{playerName}
```

Endpoint ten zwraca statystyki dotyczące konkretnego gracza na podstawie jego nazwy.

### Pobierz informacje o graczu na podstawie XUID

```
GET /api/stats/player/xuid/{xuid}
```

Endpoint ten zwraca statystyki dotyczące konkretnego gracza na podstawie jego XUID.

### Pobierz informacje o graczach online i offline

```
GET /api/stats/players
```

Zwraca informacje o graczach, którzy są obecnie online oraz offline.

---

## Backup

### Pobierz backup za pomocą klucza autoryzacji


### Pobierz listę nazw plików backup za pomocą klucza autoryzacji

```
GET /api/backup/
```

Pobierz aktualną listę nazw plików backup

### Pobierz konkretny backup.

```
GET /api/backup/{filename}
```

Pobiera dostępny backup o określonej nazwie przy użyciu klucza autoryzacji.

---
## Logi

### Pobiera listę nazw plików logów servera

```
GET /api/log/
```
Pobierz aktualną listę nazw plików logów servera

### Pobierz konkretny log servera.

```
GET /api/log/{filename}
```

Zwraca `TEXT_PLAIN` z aktualnego pliku log servera.

---

## Skin

### Pobiera skórkę głowy gracza na podstawie jego XUID
```
GET /api/skin/head/xuid/{xuid}
```
Zwraca link do głowy gracza po jego XUID

### Pobiera skórkę głowy gracza na podstawie nicku
```
GET /api/skin/head/name/{name}
```

Zwraca link do głowy gracza po jego nicku

### Pobiera skórkę ciała gracza na podstawie jego XUID
```
GET /api/skin/body/xuid/{xuid}
```
Zwraca link do ciała gracza gracza po jego XUID

### Pobiera skórkę ciała gracza na podstawie nicku
```
GET /api/skin/body/name/{name}
```
Zwraca link do ciała gracza gracza po jego nicku

---

## Konsola serwera

### Wykonaj polecenie w konsoli serwera

```
POST /command/
```

Wykonuje polecenie w konsoli serwera i zwraca ostatnią linię z konsoli.

#### Przykładowy JSON

```json
{
  "command": "say To wiadomość oddana za pomocą REST API"
}
```

#### Przykład wysłania JSON z poziomu konsoli

```
curl -X POST -H "Content-Type: application/json" -H "Authorization:KLUCZ API" -d "{ \"command\": \"say To wiadomość oddana za pomocą REST API\"}" http://localhost:PORT/command/KLUCZ_API
```

---

# Dodatkowe

```
POST /playerInfo/
```

#### Przykładowy JSON

WORK IN PROGRESS

```json
{}
```

---

### Ostrzeżenie

Nie udostępniaj kluczy API
