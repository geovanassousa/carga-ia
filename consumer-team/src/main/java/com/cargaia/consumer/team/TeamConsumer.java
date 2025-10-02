package com.cargaia.consumer.team;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.TimeoutException;

public class TeamConsumer {
    private static final Logger logger = LoggerFactory.getLogger(TeamConsumer.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final String rabbitmqHost;
    private final String rabbitmqUser;
    private final String rabbitmqPass;
    private final String exchangeName;
    private final String queueName;
    private final long processDelayMs;

    private Connection connection;
    private Channel channel;
    private TeamAIProcessor aiProcessor;
    private int processedCount = 0;

    public TeamConsumer() {
        this.rabbitmqHost = getEnv("RABBITMQ_HOST", "localhost");
        this.rabbitmqUser = getEnv("RABBITMQ_USER", "guest");
        this.rabbitmqPass = getEnv("RABBITMQ_PASS", "guest");
        this.exchangeName = getEnv("EXCHANGE_NAME", "img.topic");
        this.queueName = getEnv("QUEUE_NAME", "team.q");
        this.processDelayMs = Long.parseLong(getEnv("PROCESS_DELAY_MS", "1100"));
    }

    public static void main(String[] args) {
        TeamConsumer consumer = new TeamConsumer();
        consumer.start();
    }

    public void start() {
        try {
            setupRabbitMQ();
            aiProcessor = new TeamAIProcessor();
            logger.info("Iniciando consumidor de teams - Delay: {}ms", processDelayMs);
            
            // Configura o consumidor
            channel.basicQos(1); // Processa uma mensagem por vez
            channel.basicConsume(queueName, false, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope,
                                         AMQP.BasicProperties properties, byte[] body) {
                    try {
                        processMessage(body);
                        channel.basicAck(envelope.getDeliveryTag(), false);
                    } catch (Exception e) {
                        logger.error("Erro ao processar mensagem", e);
                        try {
                            channel.basicNack(envelope.getDeliveryTag(), false, false);
                        } catch (IOException ioException) {
                            logger.error("Erro ao fazer NACK", ioException);
                        }
                    }
                }
            });

            logger.info("Aguardando mensagens na fila '{}'...", queueName);
            
            // Mantém o consumidor rodando
            while (true) {
                Thread.sleep(1000);
            }
            
        } catch (Exception e) {
            logger.error("Erro no consumidor de teams", e);
            System.exit(1);
        }
    }

    private void setupRabbitMQ() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitmqHost);
        factory.setUsername(rabbitmqUser);
        factory.setPassword(rabbitmqPass);

        connection = factory.newConnection();
        channel = connection.createChannel();

        // Declara exchange
        channel.exchangeDeclare(exchangeName, "topic", true);
        
        // Declara fila
        channel.queueDeclare(queueName, true, false, false, null);
        
        // Cria binding
        channel.queueBind(queueName, exchangeName, "team");
        
        logger.info("RabbitMQ configurado - Exchange: {}, Queue: {}", exchangeName, queueName);
    }

    private void processMessage(byte[] body) throws Exception {
        String messageJson = new String(body);
        Message message = objectMapper.readValue(messageJson, Message.class);
        
        long startTime = System.currentTimeMillis();
        
        // Simula processamento com IA
        String team = aiProcessor.processTeam(message.getImageUrl());
        
        // Delay intencional para demonstrar backpressure
        Thread.sleep(processDelayMs);
        
        long processingTime = System.currentTimeMillis() - startTime;
        processedCount++;
        
        logger.info("Team processado - ID: {}, Time: {}, Tempo: {}ms, Total: {}", 
                   message.getId(), team, processingTime, processedCount);
    }

    private String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }

    public void shutdown() {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
            if (connection != null && connection.isOpen()) {
                connection.close();
            }
            logger.info("Conexão RabbitMQ fechada");
        } catch (Exception e) {
            logger.error("Erro ao fechar conexão", e);
        }
    }

    // Classe interna para representar a mensagem
    public static class Message {
        private String id;
        private String type;
        private String imageUrl;
        private String timestamp;
        private MessageMeta meta;

        // Getters e setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        public MessageMeta getMeta() { return meta; }
        public void setMeta(MessageMeta meta) { this.meta = meta; }
    }

    public static class MessageMeta {
        private String source;
        private String notes;

        // Getters e setters
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
}
