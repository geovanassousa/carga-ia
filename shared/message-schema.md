# Especificação do Schema de Mensagens

## Formato JSON

As mensagens trocadas no sistema seguem o seguinte formato JSON:

```json
{
  "id": "uuid-v4",
  "type": "face" | "team",
  "image_url": "https://example.com/images/face1.jpg",
  "timestamp": "2025-01-27T21:00:00Z",
  "meta": {
    "source": "generator-1",
    "notes": "demo"
  }
}
```

## Campos

### `id` (string, obrigatório)
- Identificador único da mensagem
- Formato UUID v4
- Exemplo: `"550e8400-e29b-41d4-a716-446655440000"`

### `type` (string, obrigatório)
- Tipo da mensagem/imagem
- Valores possíveis: `"face"` ou `"team"`
- Determina o routing key no RabbitMQ

### `image_url` (string, obrigatório)
- URL da imagem a ser processada
- Pode ser URL externa ou base64 (para casos offline)
- Exemplo: `"https://example.com/images/face1.jpg"`

### `timestamp` (string, obrigatório)
- Timestamp ISO 8601 da criação da mensagem
- Formato: `"YYYY-MM-DDTHH:mm:ssZ"`
- Exemplo: `"2025-01-27T21:00:00Z"`

### `meta` (object, obrigatório)
- Metadados adicionais da mensagem

#### `meta.source` (string, obrigatório)
- Identificador da origem da mensagem
- Exemplo: `"generator-1"`

#### `meta.notes` (string, opcional)
- Notas adicionais sobre a mensagem
- Exemplo: `"demo"`, `"test"`, `"production"`

## Routing Keys

- **face**: Mensagens com `type: "face"` são roteadas para `face.q`
- **team**: Mensagens com `type: "team"` são roteadas para `team.q`

## Exemplos

### Mensagem Face
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "type": "face",
  "image_url": "https://example.com/images/person1.jpg",
  "timestamp": "2025-01-27T21:00:00Z",
  "meta": {
    "source": "generator-1",
    "notes": "demo"
  }
}
```

### Mensagem Team
```json
{
  "id": "987fcdeb-51a2-43d7-8f9e-123456789abc",
  "type": "team",
  "image_url": "https://example.com/images/flamengo-logo.jpg",
  "timestamp": "2025-01-27T21:00:00Z",
  "meta": {
    "source": "generator-1",
    "notes": "demo"
  }
}
```
