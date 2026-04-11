[![Java CI with Maven](https://github.com/e-reznik/ai-football-predictor/actions/workflows/maven.yml/badge.svg)](https://github.com/e-reznik/ai-football-predictor/actions/workflows/maven.yml)
[![Deploy to vServer](https://github.com/e-reznik/ai-football-predictor/actions/workflows/deploy.yml/badge.svg)](https://github.com/e-reznik/ai-football-predictor/actions/workflows/deploy.yml)
![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.x-brightgreen?logo=springboot)

# AI Football Predictor

A Spring Boot application that predicts football match scores using AI models (Anthropic, OpenAI). It fetches upcoming
fixtures from the [football-data.org](https://www.football-data.org) API for supported competitions (Bundesliga, Premier
League), sends them to one or more AI models for score predictions, and persists both the predictions and the actual
results for later comparison.
