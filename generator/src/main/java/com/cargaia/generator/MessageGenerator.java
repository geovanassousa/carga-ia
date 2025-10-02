package com.cargaia.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.UUID;

public class MessageGenerator {
    private static final Logger log = LoggerFactory.getLogger(MessageGenerator.class);
    private static final ObjectMapper om = new ObjectMapper();

    private final String host = env("RABBITMQ_HOST","localhost");
    private final String user = env("RABBITMQ_USER","guest");
    private final String pass = env("RABBITMQ_PASS","guest");
    private final String exchange = env("EXCHANGE_NAME","img.topic");
    private final int msgsPerSec = Math.max(1, Integer.parseInt(env("MSGS_PER_SEC","6")));

    public static void main(String[] args) throws Exception { new MessageGenerator().start(); }

    private void start() throws Exception {
        ConnectionFactory f = new ConnectionFactory();
        f.setHost(host); f.setUsername(user); f.setPassword(pass);
        try (Connection conn = f.newConnection(); Channel ch = conn.createChannel()) {
            ch.exchangeDeclare(exchange, BuiltinExchangeType.TOPIC, true);
            ch.queueDeclare("face.q", true, false, false, null);
            ch.queueDeclare("team.q", true, false, false, null);
            ch.queueBind("face.q", exchange, "face");
            ch.queueBind("team.q", exchange, "team");

            long interval = Math.max(1, 1000L / msgsPerSec);
            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                    .contentType("application/json").build();
            boolean toggle = false;

            log.info("Gerador ON | {} msg/s | exchange={}", msgsPerSec, exchange);
            while (true) {
                String type = (toggle = !toggle) ? "face" : "team";
                Message m = new Message(
                        UUID.randomUUID().toString(),
                        type,
                        type.equals("face") ? "https://example.com/face.jpg" : "https://example.com/team.png",
                        Instant.now().toString(),
                        new MessageMeta("generator-1","demo")
                );
                ch.basicPublish(exchange, type, props, om.writeValueAsBytes(m));
                log.info("Publicado {} -> {}", m.getId(), type);
                Thread.sleep(interval);
            }
        }
    }

    private static String env(String k, String d){ String v=System.getenv(k); return v!=null?v:d; }
}