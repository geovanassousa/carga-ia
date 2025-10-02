# Sistema de Processamento de Imagens com IA

Sistema distribuído para processamento de imagens usando RabbitMQ, com gerador de mensagens e consumidores especializados para análise de faces e identificação de times.

## Como Executar

```bash
docker compose up --build
```

Após a inicialização, acesse o RabbitMQ Management em:
- URL: http://localhost:15672
- Usuário: `user`
- Senha: `pass`

## Arquitetura

O sistema é composto por **4 serviços**:

### 1. RabbitMQ
- Exchange: `img.topic` (tipo topic)
- Filas: `face.q` e `team.q`
- Routing keys: `face` e `team`

### 2. Generator
- Gera mensagens alternando entre tipos `face` e `team`
- Taxa: **6 mensagens/segundo** (≥5 msg/s conforme critério)
- Publica no exchange `img.topic` com routing keys correspondentes

### 3. Consumer Face
- Consome mensagens da fila `face.q`
- Delay de processamento: **900ms** (mais lento que o gerador)
- Placeholder de IA: retorna "feliz" como resultado

### 4. Consumer Team
- Consome mensagens da fila `team.q`
- Delay de processamento: **1100ms** (mais lento que o gerador)
- Placeholder de IA: retorna "time-demo" como resultado

## Critério de Aceite

✅ **Gerador ≥5 msg/s**: Configurado para 6 msg/s
✅ **Consumidores com delay**: Face (900ms) e Team (1100ms)
✅ **Fila enche visivelmente**: As filas acumulam mensagens porque os consumidores processam mais devagar que o gerador

### Validação
1. Execute `docker compose up --build`
2. Acesse http://localhost:15672 → Queues
3. Observe as filas `face.q` e `team.q` com Ready crescendo
4. Monitore os logs:
   ```bash
   docker compose logs -f generator
   docker compose logs -f consumer-face
   docker compose logs -f consumer-team
   ```

## IA (Smile)

Atualmente os consumidores estão em **modo placeholder**:
- **Face**: retorna "feliz" fixo
- **Team**: retorna "time-demo" fixo

### Como Ativar Smile Posteriormente

1. **Treinar modelos**: Execute o script `scripts/train-models.sh`
2. **Carregar modelos**: Modifique os consumidores para carregar os modelos treinados
3. **Processar imagens**: Substitua os placeholders pela lógica real do Smile
4. **Persistir resultados**: Implemente armazenamento dos resultados da IA

### Estrutura de Dados

```json
{
  "id": "uuid",
  "type": "face|team",
  "image_url": "https://example.com/image.jpg",
  "timestamp": "2024-01-01T00:00:00Z",
  "meta": {
    "source": "generator-1",
    "notes": "demo"
  }
}
```

## Tecnologias

- **Java 17**: Linguagem principal
- **RabbitMQ**: Message broker
- **Maven**: Build tool
- **Docker**: Containerização
- **Jackson**: Serialização JSON
- **Logback**: Logging
- **Smile**: Biblioteca de Machine Learning (preparada para uso futuro)