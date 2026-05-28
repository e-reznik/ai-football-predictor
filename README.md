[![Java CI with Maven](https://github.com/e-reznik/ai-football-predictor/actions/workflows/maven.yml/badge.svg)](https://github.com/e-reznik/ai-football-predictor/actions/workflows/maven.yml)
[![Deploy to vServer](https://github.com/e-reznik/ai-football-predictor/actions/workflows/deploy.yml/badge.svg)](https://github.com/e-reznik/ai-football-predictor/actions/workflows/deploy.yml)
![Last Commit](https://img.shields.io/github/last-commit/e-reznik/ai-football-predictor)

![Java](https://img.shields.io/badge/Java-25-5382A1?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.x-6DB33F?logo=springboot&logoColor=white)
![Spring AI](https://img.shields.io/badge/Spring%20AI-2.x-6DB33F?logo=spring&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-ready-2496ED?logo=docker&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-4169E1?logo=postgresql&logoColor=white)

# AI Football Predictor

A Spring Boot application that predicts football match scores using multiple AI models (Anthropic Claude, OpenAI GPT,
Mistral AI). Every night it fetches upcoming fixtures, collects recent news for each match, and sends the data to all
configured AI models. Predictions and actual results are stored in PostgreSQL and presented in a Thymeleaf dashboard
with model comparison charts.

---

## Features

- **Multi-model predictions** — queries Anthropic, OpenAI, and Mistral in parallel and stores each model's prediction
  separately
- **News-enriched prompts** — fetches recent web/news results via Brave Search API to give the AI relevant context per
  match
- **Multi-competition support** — Premier League, Bundesliga, Primera División, Serie A, Ligue 1, UEFA Champions League,
  Championship, Eredivisie, Primeira Liga, Copa Libertadores, Campeonato Brasileiro Série A (configurable)
- **Automated daily pipeline** — predictions at 01:00, result scoring at 02:00 (Europe/Berlin)
- **Scoring system** — 3 pts for exact score, 1 pt for correct tendency, 0 pts for wrong
- **Dashboard** — upcoming matches with predictions and finished matches with actual results, plus accuracy and
  performance charts per model

---

## Tech Stack

| Layer          | Technology                                                                                             |
|----------------|--------------------------------------------------------------------------------------------------------|
| Backend        | Java 25, Spring Boot 4.x                                                                               |
| AI             | Spring AI 2.x, Anthropic Claude, OpenAI GPT, Mistral AI                                                |
| External APIs  | [Brave Search API](https://brave.com/search/), [football-data.org API](https://www.football-data.org/) |
| Database       | PostgreSQL 17                                                                                          |
| Frontend       | Thymeleaf, Chart.js                                                                                    |
| Observability  | Prometheus, Grafana, Grafana Alloy, Loki                                                               |
| Infrastructure | Docker Compose, Traefik v3, TLS/HTTPS                                                                  |

---

## How It Works

```
01:00 daily
  └── Fetch upcoming fixtures (football-data.org)
        └── Fetch news per match (Brave Search)
              └── Build prompt (competition + news snippets + JSON template)
                    └── Query all AI models (Anthropic / OpenAI / Mistral)
                          └── Persist predictions (PostgreSQL)

02:00 daily
  └── Fetch finished matches (football-data.org)
        └── Update actual scores + compute prediction points
```

---

## Why a Shared News Source Instead of Per-Model Web Search?

OpenAI, Anthropic, and Mistral all offer some form of built-in web search. This project deliberately does **not** use
them, and instead fetches news once via Brave Search and passes the same snippets to every model. The reasons:

- **Fair comparison.** This is a model-vs-model accuracy benchmark. If each model fetched its own evidence, the
  measurement would mix "which model predicts better" with "which model has better search," and the comparison would
  stop being meaningful. Holding the input constant isolates the variable being tested.
- **Reproducibility & auditability.** The exact snippets that went into every prompt are logged and persisted, so a bad
  prediction can be replayed against the evidence the model actually saw. Built-in web search returns different results
  on each call and exposes them only as opaque tool-use blocks.
- **Provider parity.** Mistral has no web search on `/v1/chat/completions`, it lives on a separate `/v1/agents`
  endpoint that Spring AI's `MistralAiChatModel` doesn't call. Brave gives all three models identical treatment without
  a special path for one of them.
- **Cost & predictability.** Anthropic charges per `web_search` tool call, OpenAI's search-preview models have their
  own pricing and constraints (no `temperature`, no tools). Brave is one priced source, regardless of how many models
  the pipeline fans out to.
- **Recency control.** Results are explicitly bound to the past week and targeted at the specific matchup. Built-in
  search uses opaque recency heuristics and may pull generic team pages or unrelated previews.
- **Decoupling.** The news source is a swappable component (`NewsClient` interface, mock available). It can be replaced
  with a sports-specific feed without touching any model code.

---

## Getting Started

### Prerequisites

- Java 25
- Docker & Docker Compose
- API keys for [football-data.org](https://www.football-data.org), [Brave Search](https://brave.com/search/api/), and at
  least one AI provider

### Local setup

1. Clone the repository:
   ```bash
   git clone https://github.com/e-reznik/ai-football-predictor.git
   cd ai-football-predictor
   ```

2. Add your API keys to `src/main/resources/application.properties` (or create a local override file):
   ```properties
   football-data.token=YOUR_TOKEN
   brave.api_key=YOUR_KEY
   spring.ai.anthropic.api-key=YOUR_KEY
   spring.ai.openai.api-key=YOUR_KEY
   spring.ai.mistralai.api-key=YOUR_KEY
   ```

3. Start a local PostgreSQL instance (or use the provided Compose file):
   ```bash
   docker compose up db -d
   ```

4. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

   The dashboard is available at [http://localhost:8081](http://localhost:8081).

### Run with mock data (no API keys required)

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=mock
```

### Production deployment

Copy `.env.example` to `.env`, fill in all variables, then:

```bash
docker compose up -d
```

---

## Configuration Reference

| Property                      | Description                                             |
|-------------------------------|---------------------------------------------------------|
| `football-data.token`         | football-data.org API token                             |
| `football-data.competitions`  | Comma-separated competition codes (e.g. `BL1,PL,PD,SA`) |
| `brave.api_key`               | Brave Search API key                                    |
| `brave.suffix`                | Words appended to each news search query                |
| `spring.ai.anthropic.api-key` | Anthropic API key                                       |
| `spring.ai.openai.api-key`    | OpenAI API key                                          |
| `spring.ai.mistralai.api-key` | Mistral AI API key                                      |
| `app.api.token`               | Bearer token for `/predict` and `/result` endpoints     |
| `ai.prompt.part.1`            | First part of the AI prompt (role instruction)          |
| `ai.prompt.part.2`            | Second part (context instruction)                       |
| `ai.prompt.part.3`            | Third part (output format instruction)                  |

---

## Dashboard

**`GET /`** — Upcoming matches with AI predictions from all models. Includes bar and line charts comparing model
accuracy and total points across competitions and matchdays.

**`GET /history`** — Past matches with actual results, per-model predicted scores, and points earned.

**`GET /about`** — Project description, scoring rules, covered leagues, and AI models in use.

---

## License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.
