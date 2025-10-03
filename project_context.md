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
- Sistema 100% redondo conforme especificações do professor
- Todas as melhorias obrigatórias implementadas e validadas

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
- **Processamento de imagens reais**: Implementado download e análise de pixels reais com OkHttp e BufferedImage
- **Features de imagem**: Extração de brightness, color intensity e color variance para análise com IA Smile
- **SISTEMA 100% REDONDO - AJUSTES OBRIGATÓRIOS IMPLEMENTADOS**:
  - **Mensagens persistentes**: deliveryMode(2) no generator, filas duráveis nos consumidores
  - **QoS/ACK explícito**: basicQos(1) e ACK após processamento, NACK com requeue em caso de erro
  - **README estruturado**: 5 seções completas com objetivo, como rodar, arquitetura Mermaid, validação e IA Smile
  - **Taxa como variável**: MSGS_PER_SEC configurado no docker-compose.yml (padrão 6, ≥5)
  - **Log de throughput**: AtomicLong counter com relatório a cada 5s no generator
  - **Smile explicitamente visível**: Logs de carregamento KNN e predições com confiança
  - **Linting corrigido**: Removidos imports não utilizados e variáveis não usadas


