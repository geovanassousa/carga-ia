#!/bin/bash

# Script para treinar os modelos Smile
# Executa o treinamento dos modelos de IA para faces e teams

echo "Iniciando treinamento dos modelos Smile..."

# Cria diretório de dados se não existir
mkdir -p data

# Treina modelo para faces
echo "Treinando modelo para faces..."
cd consumer-face
mvn compile exec:java -Dexec.mainClass="com.cargaia.consumer.face.FaceAIProcessor" -Dexec.args="train"
cd ..

# Treina modelo para teams
echo "Treinando modelo para teams..."
cd consumer-team
mvn compile exec:java -Dexec.mainClass="com.cargaia.consumer.team.TeamAIProcessor" -Dexec.args="train"
cd ..

echo "Treinamento concluído!"
echo "Modelos salvos em:"
echo "  - data/face-model.bin"
echo "  - data/team-model.bin"
