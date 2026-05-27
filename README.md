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
