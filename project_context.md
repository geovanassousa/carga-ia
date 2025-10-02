## Contexto do Projeto

### Visão Geral
Sistema distribuído com 4 containers (RabbitMQ + generator + consumer-face + consumer-team) usando exchange topic `img.topic`, filas `face.q` e `team.q`. Objetivo: gerar ≥ 5 msg/s e processar mais lento nos consumidores (fila enche visivelmente). IA em modo Placeholder, preparado para Smile.

### Decisões e Estado Atual
- Exchange/queues/bindings idempotentes criados pelo generator e consumidores
- Taxa do gerador: `MSGS_PER_SEC=6`; consumidores com `PROCESS_DELAY_MS` (face=900, team=1100)
- Ack manual habilitado; `basicQos(1)` para processamento serial
- Publicação com `content-type: application/json`
- Mapeamento `image_url` corrigido nos consumidores via `@JsonProperty("image_url")`
- Smile desabilitado (placeholder ativo). Script de treino mantido como base (não treina de fato)
- README estruturado conforme exigências do professor

### Próximos Passos
- Implementar treino real com Smile e persistência de modelos em `data/*.bin`
- Carregar modelos nos consumidores e habilitar caminho Smile
- Opcional: mover variáveis para `.env` e referenciar no compose

### Mudanças Recentes
- **Correção completa de todos os arquivos**: Removidas todas as reticências (...) que causavam erros de compilação
- **Docker-compose.yml**: Configuração completa e válida com healthchecks e dependências
- **Generator**: pom.xml corrigido, MessageGenerator.java com logging adequado e estrutura try-with-resources
- **Consumer-face**: pom.xml corrigido, FaceConsumer.java com logging SLF4J adequado
- **Consumer-team**: pom.xml corrigido, TeamConsumer.java com logging SLF4J adequado
- **README.md**: Documentação completa com instruções de execução, arquitetura e critérios de aceite
- Ajuste de logging do gerador (formatação de taxa)
- Definição de `content-type` na publicação do gerador
- Correção de mapeamento JSON `image_url` nos consumidores
- Correção de import AMQP.BasicProperties no MessageGenerator
- Correção de dependências Maven nos consumidores (removidas dependências system)
- Classes Message e MessageMeta duplicadas nos consumidores para independência
- Sistema testado e funcionando: gerador 6 msg/s, consumidores com delays 900ms/1100ms
- Filas enchendo visivelmente: face.q (109 msgs), team.q (117 msgs) confirmado via API
- Limpeza do projeto: removidos arquivos não utilizados (data/, wait-for-rabbit.sh, Makefile)
- **Correção SLF4J**: Removidos arquivos logback.xml conflitantes, adicionado slf4j-api explícito nos pom.xml
- **IA Times conforme professor**: Reconhece apenas corinthians e palmeiras (conforme especificado)
- **Generator correto**: Alterna entre corinthians e palmeiras como pedido pelo professor


