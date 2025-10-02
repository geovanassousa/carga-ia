package com.cargaia.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class MessageGenerator {
    private static final Logger logger = LoggerFactory.getLogger(MessageGenerator.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final String rabbitmqHost;
    private final String rabbitmqUser;
    private final String rabbitmqPass;
    private final String exchangeName;
    private final int msgsPerSec;

    private Connection connection;
    private Channel channel;
    private int messageCount = 0;

    public MessageGenerator() {
        this.rabbitmqHost = getEnv("RABBITMQ_HOST", "localhost");
        this.rabbitmqUser = getEnv("RABBITMQ_USER", "guest");
        this.rabbitmqPass = getEnv("RABBITMQ_PASS", "guest");
        this.exchangeName = getEnv("EXCHANGE_NAME", "img.topic");
        this.msgsPerSec = Integer.parseInt(getEnv("MSGS_PER_SEC", "6"));
    }

    public static void main(String[] args) {
        MessageGenerator generator = new MessageGenerator();
        generator.start();
    }

    public void start() {
        try {
            setupRabbitMQ();
            logger.info("Iniciando gerador de mensagens - {} msg/s", msgsPerSec);
            
            long startTime = System.currentTimeMillis();
            boolean isFace = true;

            while (true) {
                String messageType = isFace ? "face" : "team";
                String routingKey = messageType;
                
                Message message = createMessage(messageType);
                publishMessage(message, routingKey);
                
                messageCount++;
                isFace = !isFace; // Alterna entre face e team
                
                // Log a cada 10 mensagens
                if (messageCount % 10 == 0) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    double actualRate = (messageCount * 1000.0) / elapsed;
                    logger.info("Enviadas {} mensagens - Taxa atual: {:.2f} msg/s", 
                              messageCount, actualRate);
                }
                
                // Controle de taxa: ~166ms para 6 msg/s
                Thread.sleep(1000 / msgsPerSec);
            }
            
        } catch (Exception e) {
            logger.error("Erro no gerador de mensagens", e);
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

        // Declara exchange do tipo topic
        channel.exchangeDeclare(exchangeName, "topic", true);
        logger.info("Exchange '{}' declarada", exchangeName);

        // Declara filas
        channel.queueDeclare("face.q", true, false, false, null);
        channel.queueDeclare("team.q", true, false, false, null);
        logger.info("Filas 'face.q' e 'team.q' declaradas");

        // Cria bindings
        channel.queueBind("face.q", exchangeName, "face");
        channel.queueBind("team.q", exchangeName, "team");
        logger.info("Bindings criados: face -> face.q, team -> team.q");
    }

    private Message createMessage(String type) {
        return new Message(
            UUID.randomUUID().toString(),
            type,
            generateImageUrl(type),
            Instant.now().toString(),
            new MessageMeta("generator-1", "demo")
        );
    }

    private String generateImageUrl(String type) {
        String[] faceUrls = {
            "https://example.com/images/face1.jpg",
            "https://example.com/images/face2.jpg",
            "https://example.com/images/face3.jpg"
        };
        String[] teamUrls = {
            "https://example.com/images/team1.jpg",
            "https://example.com/images/team2.jpg",
            "https://example.com/images/team3.jpg"
        };
        
        String[] urls = type.equals("face") ? faceUrls : teamUrls;
        return urls[messageCount % urls.length];
    }

    private void publishMessage(Message message, String routingKey) throws IOException {
        String jsonMessage = objectMapper.writeValueAsString(message);
        
        channel.basicPublish(
            exchangeName,
            routingKey,
            null,
            jsonMessage.getBytes()
        );
        
        logger.debug("Mensagem enviada - ID: {}, Tipo: {}, Routing: {}", 
                    message.getId(), message.getType(), routingKey);
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
}
