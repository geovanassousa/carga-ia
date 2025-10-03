# Sistema de Processamento de Imagens com IA

## Objetivo

Sistema distribuído com 4 containers (generator, rabbitmq, consumer-face, consumer-team) usando exchange topic `img.topic`, filas `face.q` e `team.q`. Objetivo: gerar ≥ 5 msg/s e processar mais lento nos consumidores (fila enche visivelmente). IA com Smile para análise de sentimentos faciais e reconhecimento de times.

## Como Rodar

```bash
# Iniciar todos os containers
docker compose up --build -d

# Acessar painel do RabbitMQ
open http://localhost:15672
# Login: user / pass

# Monitorar logs dos serviços
docker logs -f carga-ia-generator-1
docker logs -f carga-ia-consumer-face-1
docker logs -f carga-ia-consumer-team-1
```

## Arquitetura

```mermaid
flowchart LR
  G[Generator (>=5 msg/s)] -->|routing key: face| QF[queue.face]
  G -->|routing key: team| QT[queue.team]
  G --> RMQ[(RabbitMQ - topic: img.topic)]
  C1[Consumer Face (Smile, lento)] --> QF
  C2[Consumer Team (Smile, lento)] --> QT
```

## Como o Código Funciona

### 🎨 Generator - Criação de Imagens Programáticas

**O que faz:**
- Cria 4 tipos de imagens PNG de 64x64 pixels programaticamente
- Alterna entre tipos de mensagem: `face` e `team`
- Para `face`: alterna entre `HAPPY` (amarelo) e `SAD` (azul)
- Para `team`: alterna entre `COR` (vermelho) e `PAL` (verde)

**Features implementadas:**
- **Mensagens persistentes**: `deliveryMode(2)` garante que mensagens sobrevivam a reinicializações
- **Throughput logging**: Contador `AtomicLong` com relatório a cada 5 segundos
- **Taxa configurável**: Variável `MSGS_PER_SEC` (padrão 6, ≥5 conforme exigência)
- **Imagens reais**: Usa `BufferedImage` e `Graphics2D` para criar imagens PNG com cores e texto

### 🧠 FaceConsumer - Análise de Sentimentos com IA

**O que faz:**
- Recebe imagens de faces (happy/sad) via RabbitMQ
- Extrai features específicas das imagens: `[brightness, yellow_score, blue_score]`
- Usa **Smile KNN** para classificar sentimentos baseado nas cores
- Processa com delay de 900ms (consumidor lento)

**Algoritmo de análise:**
```java
// Features específicas para sentimentos:
// 1. Brightness: feliz = mais claro, triste = mais escuro
// 2. Yellow score: feliz = amarelo (R+G alto, B baixo)
// 3. Blue score: triste = azul (B alto, R+G baixo)
double yellowScore = (avgRed + avgGreen - avgBlue) / 255.0;
double blueScore = (avgBlue - (avgRed + avgGreen) / 2.0) / 255.0;
```

**Dados de treinamento:**
- `{0.8, 0.9, 0.1}` → Happy (alta luminosidade, muito amarelo, pouco azul)
- `{0.3, 0.1, 0.8}` → Sad (baixa luminosidade, pouco amarelo, muito azul)

### ⚽ TeamConsumer - Reconhecimento de Times com IA

**O que faz:**
- Recebe imagens de times (corinthians/palmeiras) via RabbitMQ
- Extrai features baseadas em cores características: `[corinthians_score, palmeiras_score]`
- Usa **Smile KNN** para classificar times baseado nas cores dominantes
- Processa com delay de 1100ms (consumidor mais lento)

**Algoritmo de análise:**
```java
// Features baseadas em cores características dos times
// Corinthians: mais vermelho/preto, Palmeiras: mais verde
double corinthiansScore = (avgRed / 255.0) + ((255 - avgGreen) / 255.0) * 0.5;
double palmeirasScore = (avgGreen / 255.0) + ((255 - avgRed) / 255.0) * 0.3;
```

### 🔧 Recursos Avançados Implementados

**1. Mensagens Persistentes:**
```java
AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
    .contentType("application/json")
    .deliveryMode(2) // persistente - sobrevive a reinicializações
    .build();
```

**2. QoS e ACK Explícito:**
```java
channel.basicQos(1); // processamento serial
channel.basicAck(msg.getEnvelope().getDeliveryTag(), false); // ACK após processamento
```

**3. Filas Duráveis:**
```java
channel.exchangeDeclare(EXCHANGE, BuiltinExchangeType.TOPIC, true); // durable
channel.queueDeclare(QUEUE, true, false, false, null); // durable queue
```

**4. Logs Visíveis do Smile:**
```java
System.out.println("[FACE] Smile KNN loaded (k=3).");
System.out.println("[FACE] Predict=" + prediction + " conf=" + confidence);
```

## Validação (Checklist Rápido)

- [ ] Exchange `img.topic` (type topic) criada e durável
- [ ] Filas `face.q` e `team.q` duráveis e ligadas a `face`/`team`
- [ ] Generator mostra ~6 msg/s, consumer-* processam mais devagar
- [ ] Em Queues (15672) o número Ready sobe nas duas filas
- [ ] Reinicie consumer-*: as mensagens persistem (por causa do deliveryMode(2))

### Screenshots do Painel RabbitMQ

**Exchanges/Bindings:**
- Exchange: `img.topic` (topic, durable)
- Bindings: `face.q` ← `face`, `team.q` ← `team`

**Queues com Ready crescendo:**
- `face.q`: Ready aumentando (consumer lento: 900ms delay)
- `team.q`: Ready aumentando (consumer lento: 1100ms delay)

## IA com Smile

### Classes/Métodos Utilizados

**FaceConsumer:**
- `smile.classification.KNN`: Classificador K-Nearest Neighbors
- `analyzeFace()`: Extrai features da imagem e faz predição
- `extractImageFeatures()`: Calcula brightness, yellow_score, blue_score
- Features: `[brightness, yellow_score, blue_score]`
- Classes: `0=feliz (amarelo), 1=triste (azul)`
- Imagens: Happy (amarelo), Sad (azul)

**TeamConsumer:**
- `smile.classification.KNN`: Classificador K-Nearest Neighbors  
- `analyzeTeam()`: Extrai features da imagem e faz predição
- `extractImageFeatures()`: Calcula scores baseados em cores características
- Features: `[corinthians_score, palmeiras_score]`
- Classes: `0=corinthians, 1=palmeiras`

### Como Reproduzir

1. Execute o sistema com `docker compose up --build -d`
2. Observe logs com Smile explicitamente visível:
   ```
   [FACE] Smile KNN loaded (k=3).
   [FACE] Predict=feliz conf=0.85
   [FACE] Predict=triste conf=0.85
   [TEAM] Smile KNN loaded (k=3).
   [TEAM] Predict=corinthians conf=0.92
   ```
3. Verifique throughput do generator a cada 5s:
   ```
   [GEN] throughput_5s=30 (~6.0 msg/s)
   ```

### Configuração de Taxa

Para alterar a taxa de mensagens, modifique `MSGS_PER_SEC` no `docker-compose.yml`:
```yaml
generator:
  environment:
    MSGS_PER_SEC: "10"   # mude para 10 se quiser encher mais rápido
```

**Taxa padrão:** 6 msg/s (≥5 conforme exigência)