#!/bin/bash

# Script para aguardar o RabbitMQ estar disponível
# Uso: ./wait-for-rabbit.sh [host] [port] [timeout]

RABBITMQ_HOST=${1:-rabbitmq}
RABBITMQ_PORT=${2:-5672}
TIMEOUT=${3:-60}

echo "Aguardando RabbitMQ em $RABBITMQ_HOST:$RABBITMQ_PORT..."

for i in $(seq 1 $TIMEOUT); do
    if nc -z $RABBITMQ_HOST $RABBITMQ_PORT; then
        echo "RabbitMQ está disponível!"
        exit 0
    fi
    echo "Tentativa $i/$TIMEOUT - Aguardando..."
    sleep 1
done

echo "Timeout: RabbitMQ não ficou disponível em $TIMEOUT segundos"
exit 1
