<div align="center">

## Instalacja integracji Discord


Aby aplikacja miała dostęp do informacji o graczu musisz
posiadać [BDS-Auto-Enable-Management-Pack](https://github.com/Huje22/BDS-Auto-Enable-Management-Pack)

</div>

### WEBHOOK

<u>_**AKTUALNIE NIE UŻYWANE W PROJEKCIE**_</u>

#### Jak zacząć

* Pierw musisz utworzyć webhook na kanale , tu
  znajdziesz [poradnik](https://support.discord.com/hc/en-us/articles/228383668-Intro-to-Webhooks)
* Gdy masz już link ustaw `chatUrl` (w przyszłości pojawi się ich więcej) na link twojego webhooku kanału z
  wiadomościami
* Możesz także ustawić `avatarUrl` i `name` na jakie tylko chcesz
    * Jeśli link do avataru bezie niepoprawny prawdopodobnie webhook nie będzie działać

### JDA

Czat `Discord <-> Minecraft` i nie tylko

#### Jak zacząć

* Musisz utworzyć bota, tu
  znajdziesz [poradnik](https://appmaster.io/pl/blog/discord-bot-jak-go-stworzyc-i-dodac-na-serwer) <br>
* I dalej wypełniasz Config danymi informacjami, czym jest co znajdziesz opis w komentarzach `#`
* Jak nie wiesz jak skopiować id kanału to tu
  znajdziesz [poradniki](https://www.google.com/amp/s/pl.jugomobile.com/jak-znalezc-identyfikator-serwera-w-discord-na-komputerze-pc-lub-smartfonie/%3famp)<br>
* Aby bot działał poprawnie będziesz musiał włączyć wszystkie <br>
  `Privileged Gateway Intents`: <br>
    * MESSAGE CONTENT INTENT
    * PRESENCE INTENT
    * SERVER MEMBERS INTENT

![Discord](https://github.com/Huje22/.github/blob/main/assets/Discord-Privileged-Gateway-Intents.jpg)
