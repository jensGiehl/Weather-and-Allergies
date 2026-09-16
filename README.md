# Allergy Daily Report

A Spring Boot application that sends a daily weather and pollen report to a Telegram chat every morning and provides a mobile-friendly web interface to track allergy symptoms per family member.

## What it does

Every day at **07:00 (Europe/Berlin)** the scheduler:

1. Creates a unique daily report entry in the database (UUID-based, not guessable)
2. Fetches today's **weather data** from [Open-Meteo](https://open-meteo.com/) (temperature, precipitation, wind, UV index, sunrise/sunset)
3. Fetches today's **pollen levels** from the Open-Meteo Air Quality API (alder, birch, grass, mugwort, olive, ragweed)
4. Sends a formatted **Telegram message** with all data and a personal link to the symptom tracker

The Telegram message contains a link like `https://your-domain.com/report/3f8a2b1c-…`.  
Opening it shows a mobile-first page where each family member can tap their symptoms — saved automatically in the background.

Every **Sunday at 20:00 (Europe/Berlin)** an additional **weekly summary** is sent to Telegram:

- A compact weather strip (one emoji per day Mon–Sun) plus the week's average max / min temperature
- For each family member with at least one recorded symptom: how many days they reported, and how often each symptom occurred (most frequent first)
- Persons without any entries that week are omitted to keep the message short
- A link back to the overview page

Die tägliche Telegram-Nachricht enthält die Uhrzeit der Höchsttemperatur aus der Stundenprognose sowie die Temperaturen um **8, 12 und 14 Uhr**, jeweils mit einem Wetter-Emoji für diese Stunde. Dazu kommt eine Kleidungsempfehlung für die Schule von **8 bis 14 Uhr**. Sie berücksichtigt die verfügbaren stündlichen Temperaturen in diesem Zeitraum, Temperaturunterschiede sowie Regen und Schnee. Fehlen Temperaturwerte für diesen Zeitraum, weist die Nachricht darauf hin. Alle Uhrzeiten beziehen sich auf `weather.timezone`. Bei gleicher Höchsttemperatur wird die erste Stunde angezeigt. Fehlende Stundenwerte erscheinen als `–`, fehlende Wettercodes als `❓`.

### Web UI

| Page | URL | Description |
|---|---|---|
| Overview | `/` | DataTable with all days, persons, and recorded symptoms |
| Daily report | `/report/{uuid}` | Mobile symptom tracker for a specific day |
| JSON export | `/api/export` | All overview data as JSON (see below) |
| H2 Console | `/h2-console` | Database browser (development) |

### JSON export

`GET /api/export` returns the same dataset as the overview as a JSON array, sorted by date descending. Each element contains the daily weather, all pollen levels, and every allergy entry recorded for that day.

```bash
curl http://localhost:8080/api/export
```

Example response:

```json
[
  {
    "date": "2026-05-27",
    "weather": {
      "code": 2,
      "label": "Teilweise bewölkt",
      "temperatureMax": 22.5,
      "temperatureMin": 12.0,
      "precipitationSum": 0.0,
      "precipitationProbability": 10,
      "windspeedMax": 15.2,
      "uvIndexMax": 5.5,
      "uvLevel": "MODERATE",
      "sunrise": "05:30",
      "sunset": "21:15"
    },
    "pollen": [
      {"name": "Erle", "value": 0.0, "level": "NONE", "label": "keine"},
      {"name": "Birke", "value": 12.3, "level": "MEDIUM", "label": "mäßig"}
    ],
    "entries": [
      {
        "personName": "Papa",
        "personType": "MANN",
        "symptoms": [
          {"code": "NIESEN", "label": "Niesen", "icon": "bi-wind"}
        ],
        "updatedAt": "2026-05-27T08:00:00"
      }
    ]
  }
]
```

---

## Prerequisites

- Java 25+
- Maven (or use the included `./mvnw` wrapper)
- A Telegram Bot token and chat ID (see below)

---

## Creating a Telegram Bot

### 1 — Create the bot

1. Open Telegram and search for **@BotFather**
2. Send `/newbot`
3. Choose a display name (e.g. `Family Allergy Report`)
4. Choose a username ending in `bot` (e.g. `familyallergyreport_bot`)
5. BotFather replies with your **Bot Token** — save it:
   ```
   1234567890:ABCDefGhIJKlmNoPQRstuVWXyz
   ```

### 2 — Find your Chat ID

Send any message to your new bot, then open this URL in a browser (replace `<TOKEN>`):

```
https://api.telegram.org/bot<TOKEN>/getUpdates
```

Look for `"chat"` → `"id"` in the JSON response. That number is your **Chat ID**.

> **Group chats:** Add the bot to the group, send a message mentioning it, then call `getUpdates` — the group's chat ID starts with `-`.

---

## Configuration

All settings live in `src/main/resources/application.properties`.

### Minimum required changes

```properties
# Your Telegram credentials
telegram.bot-token=1234567890:ABCDefGhIJKlmNoPQRstuVWXyz
telegram.chat-id=987654321

# Public URL where this app is reachable (used for the link in the Telegram message)
app.base-url=https://your-domain.com
```

### Full reference

```properties
# ── Location ────────────────────────────────────────
weather.latitude=52.52
weather.longitude=13.41
weather.location-name=Berlin          # displayed in the Telegram message
weather.timezone=Europe/Berlin

# ── Open-Meteo Weather API ───────────────────────────
weather.api-url=https://api.open-meteo.com/v1/forecast
weather.daily-variables=temperature_2m_max,temperature_2m_min,precipitation_sum,\
  precipitation_probability_max,windspeed_10m_max,weathercode,uv_index_max,sunrise,sunset
weather.hourly-variables=temperature_2m,weather_code

# ── Open-Meteo Air Quality API (pollen) ─────────────
pollen.api-url=https://air-quality-api.open-meteo.com/v1/air-quality
pollen.daily-variables=alder_pollen,birch_pollen,grass_pollen,mugwort_pollen,olive_pollen,ragweed_pollen

# ── Telegram ─────────────────────────────────────────
telegram.bot-token=YOUR_BOT_TOKEN
telegram.chat-id=YOUR_CHAT_ID
telegram.api-url=https://api.telegram.org

# ── Application ──────────────────────────────────────
app.base-url=http://localhost:8080

# ── Scheduler ────────────────────────────────────────
scheduler.cron=0 0 7 * * *            # daily report — every day at 07:00
scheduler.weekly-cron=0 0 20 * * SUN  # weekly summary — every Sunday at 20:00
scheduler.timezone=Europe/Berlin

# ── Database (H2 file-based) ─────────────────────────
spring.datasource.url=jdbc:h2:file:./data/allergydb;DB_CLOSE_ON_EXIT=FALSE;DB_CLOSE_DELAY=-1
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

### Customising persons

Edit `src/main/resources/persons.json`. Supported types: `MANN`, `FRAU`, `JUNGE`, `MAEDCHEN`.

```json
[
  {"name": "Papa",   "type": "MANN"},
  {"name": "Mama",   "type": "FRAU"},
  {"name": "Kind 1", "type": "JUNGE"},
  {"name": "Kind 2", "type": "MAEDCHEN"}
]
```

### Customising symptoms

Edit `src/main/resources/symptoms.json`. Icons are [Bootstrap Icons](https://icons.getbootstrap.com/) class names.

```json
[
  {"code": "NIESEN", "label": "Niesen", "icon": "bi-wind"},
  ...
]
```

---

## Running the application

```bash
# Start
./mvnw spring-boot:run
```

The app starts on **port 8080**. The scheduler fires automatically at 07:00 Berlin time.

---

## Docker und GitHub Actions

Das [Dockerfile](Dockerfile) baut mit Maven und Java 25 ein Spring-Boot-JAR und übernimmt dessen Schichten in ein Java-25-JRE-Image. Die Anwendung läuft als Benutzer `spring` (UID/GID `10001`) auf Port `8080`. Der Healthcheck prüft alle 30 Sekunden, ob der HTTP-Port Verbindungen annimmt.

Im Footer der Weboberfläche steht die kurze Git-Commit-ID des Builds. Maven schreibt sie beim Paketieren in das JAR; ohne Git-Metadaten erscheint `lokal`.

Die [GitHub Action](.github/workflows/ci.yml) läuft bei jedem Push auf **`main` oder `master`**. Nach erfolgreichem Maven-Build inklusive Tests baut sie das Docker-Image und veröffentlicht es in der **GitHub Container Registry (GHCR)**:

```text
ghcr.io/jensgiehl/weather-and-allergies:latest
ghcr.io/jensgiehl/weather-and-allergies:main
ghcr.io/jensgiehl/weather-and-allergies:master
ghcr.io/jensgiehl/weather-and-allergies:sha-<vollständiger-commit-sha>
```

Pro Lauf werden `latest`, der jeweilige Branch-Tag und der Commit-Tag veröffentlicht. `latest` zeigt auf den zuletzt erfolgreich veröffentlichten Build aus einem der beiden Branches. Die Anmeldung erfolgt mit dem automatisch bereitgestellten `GITHUB_TOKEN`; der Publish-Job erhält dafür `packages: write`. Ein zusätzliches Registry-Secret ist nicht erforderlich. Das bestehende SSH-Deployment läuft weiterhin ausschließlich bei Pushes auf `master`.

### Container starten oder aktualisieren

Ersetze im folgenden Bash-Beispiel die Zugangsdaten und die Adresse. `APP_BASE_URL` ist die vom Nutzer erreichbare Adresse für die Links in Telegram.

```bash
docker rm -f weather-and-allergies 2>/dev/null

docker run -d \
  --name weather-and-allergies \
  --pull=always \
  -p 8089:8080 \
  --restart unless-stopped \
  -e TELEGRAM_BOT_TOKEN=YOUR_BOT_TOKEN \
  -e TELEGRAM_CHAT_ID=YOUR_CHAT_ID \
  -e APP_BASE_URL=https://your-domain.com \
  -e WEATHER_LOCATION_NAME=Frankenthal \
  -e WEATHER_LATITUDE=49.5366 \
  -e WEATHER_LONGITUDE=8.3483 \
  -e WEATHER_TIMEZONE=Europe/Berlin \
  -v weather-and-allergies-data:/app/data \
  ghcr.io/jensgiehl/weather-and-allergies:latest
```

**8089 ist der Port am Host**, `8080` der Port im Container. Die Weboberfläche ist lokal unter `http://localhost:8089` erreichbar. Das benannte Volume `weather-and-allergies-data` speichert die H2-Datenbank dauerhaft unter `/app/data/allergydb.mv.db`; beim Ersetzen des Containers bleibt es erhalten.

Alternativ lässt sich ein Host-Ordner einbinden. Bereite ihn unter Linux mit passenden Schreibrechten vor:

```bash
sudo mkdir -p /srv/weather-and-allergies/data
sudo chown 10001:10001 /srv/weather-and-allergies/data
```

Ersetze dann die Volume-Zeile im Startbefehl durch `-v /srv/weather-and-allergies/data:/app/data`. Eine vorhandene H2-Datenbank kann bei gestoppter Anwendung in diesen Ordner übernommen werden.

Ist das GHCR-Paket privat, melde dich auf dem Host vor dem Start mit `docker login ghcr.io -u DEIN_GITHUB_BENUTZERNAME` an und verwende einen Token mit `read:packages`. Für einen Start ohne Anmeldung stelle die Sichtbarkeit des Pakets in GitHub auf öffentlich.

### Image lokal bauen

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
docker build -t weather-and-allergies:local .
```

Zum Starten des lokalen Images verwende im obigen Startbefehl `weather-and-allergies:local` und `--pull=never`. Im Docker-Build selbst werden Tests übersprungen; die GitHub Action führt sie vorher aus.

Die Umsetzung orientiert sich an den offiziellen Anleitungen für [Spring-Boot-Image-Schichten](https://docs.spring.io/spring-boot/reference/packaging/container-images/dockerfiles.html) und [Docker-Publishing mit GitHub Actions](https://docs.docker.com/build/ci/github-actions/push-multi-registries/).

---

## Local development profile

The `local` Spring profile provides a self-contained development environment without any external dependencies.

### What it changes

| Setting | Default | `local` |
|---|---|---|
| Database | H2 file (`./data/allergydb`) | H2 in-memory (wiped on restart) |
| Daily cron scheduler | Enabled (07:00 daily) | Disabled |
| Weekly summary scheduler | Enabled (Sun 20:00) | Disabled |
| Telegram | Real bot token required | Dummy values (send intentionally fails) |

On startup the application seeds the in-memory database with **6 days** of example weather/pollen data and **8 allergy entries** across all four persons.

### Dev trigger page

The profile activates a dedicated page at **`/dev`** with a button that runs the same logic as the cron job on demand — it fetches live weather and pollen data, updates the database, and attempts to send a Telegram message (which will fail and be logged as an error, as expected).

| Page | URL |
|---|---|
| Dev Tools | `/dev` |

### Starting with the local profile

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Or with the JAR:

```bash
java -jar target/daily-report-weather-and-quality-report-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

### Build a runnable JAR

```bash
./mvnw package -DskipTests
java -jar target/daily-report-weather-and-quality-report-0.0.1-SNAPSHOT.jar
```

### Override configuration at runtime

```bash
java -jar app.jar \
  --telegram.bot-token=YOUR_TOKEN \
  --telegram.chat-id=YOUR_CHAT_ID \
  --app.base-url=https://your-domain.com \
  --weather.location-name=München \
  --weather.latitude=48.14 \
  --weather.longitude=11.58
```

---

## Data storage

The H2 database is stored in `./data/allergydb.mv.db` relative to the working directory.  
The file persists across restarts. Browse it at `http://localhost:8080/h2-console` with:

| Field | Value |
|---|---|
| JDBC URL | `jdbc:h2:file:./data/allergydb` |
| Username | `sa` |
| Password | *(empty)* |

### Database tables

| Table | Description |
|---|---|
| `daily_report` | One row per day — UUID, weather data, pollen levels |
| `allergy_entry` | One row per person per day — recorded symptoms (comma-separated codes) |

---

## Tech stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 4 / Spring Framework 7 |
| Language | Java 25 |
| Database | H2 (file-based) via Spring Data JDBC |
| Templating | Thymeleaf |
| Frontend | Bootstrap 5.3 + Bootstrap Icons 1.11 (WebJars) |
| Tables | DataTables 2.x |
| Weather API | [Open-Meteo](https://open-meteo.com/) (free, no API key required) |
| Messaging | Telegram Bot API |
