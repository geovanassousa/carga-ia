# Atividade 6 – Sistema de Carga com IA nos Consumidores (RabbitMQ Topic)

## Como executar
```bash
docker compose down -v
docker compose up --build
Painel RabbitMQ: http://localhost:15672 (user: user, pass: pass)
```

## Arquitetura
4 containers: rabbitmq, generator, consumer-face, consumer-team
Exchange topic `img.topic` com bindings:
- face → face.q
- team → team.q

## Critérios de aceite
- Gerador publica ~6 msg/s alternando face/team.
- Consumidores processam mais devagar (delays 900ms e 1100ms) ⇒ Ready sobe nas filas.
- Logs:
  - `[FACE] id=... result=feliz|triste` (Smile/KNN de demonstração)
  - `[TEAM] id=... team=corinthians|palmeiras|santos|flamengo` (Smile/KNN)

---

# Como validar (rapidinho)
1. Na raiz:
   ```bash
   docker compose down -v
   docker compose up --build
   ```
2. Abra http://localhost:15672 → Exchanges: `img.topic` (topic) com bindings face e team.
3. Queues: `face.q` e `team.q` com Consumers = 1 e Ready subindo.
4. Logs:
   ```bash
   docker compose logs -f generator
   docker compose logs -f consumer-face
   docker compose logs -f consumer-team
   ```
   Veja o gerador publicando e os consumidores classificando (Smile/KNN), com consumo mais lento.