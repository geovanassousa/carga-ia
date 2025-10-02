# Makefile para o Sistema Distribuído com IA e RabbitMQ

.PHONY: help up down logs train clean status

help: ## Mostra esta ajuda
	@echo "Comandos disponíveis:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-15s\033[0m %s\n", $$1, $$2}'

up: ## Sobe todos os containers
	docker compose up --build -d

down: ## Para e remove todos os containers
	docker compose down -v

logs: ## Mostra logs de todos os serviços
	docker compose logs -f

logs-generator: ## Mostra logs do gerador
	docker compose logs -f generator

logs-face: ## Mostra logs do consumidor de faces
	docker compose logs -f consumer-face

logs-team: ## Mostra logs do consumidor de teams
	docker compose logs -f consumer-team

logs-rabbitmq: ## Mostra logs do RabbitMQ
	docker compose logs -f rabbitmq

status: ## Mostra status dos containers
	docker compose ps

train: ## Treina os modelos Smile
	./scripts/train-models.sh

clean: ## Remove containers, volumes e imagens
	docker compose down -v --rmi all
	docker system prune -f

restart: ## Reinicia todos os serviços
	$(MAKE) down
	$(MAKE) up

ui: ## Abre interface do RabbitMQ Management
	@echo "RabbitMQ Management: http://localhost:15672"
	@echo "Usuário: user"
	@echo "Senha: pass"
	@open http://localhost:15672 || echo "Abra manualmente: http://localhost:15672"

test: ## Executa teste básico do sistema
	@echo "Testando sistema..."
	@echo "1. Verificando containers..."
	@docker compose ps
	@echo "2. Aguardando estabilização..."
	@sleep 10
	@echo "3. Verificando logs..."
	@docker compose logs --tail=5 generator
	@echo "4. Acesse http://localhost:15672 para ver as filas"
