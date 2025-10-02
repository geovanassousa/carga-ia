package com.cargaia.consumer.face;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FaceConsumer {
    private static final Logger log = LoggerFactory.getLogger(FaceConsumer.class);
    private static final ObjectMapper om = new ObjectMapper();

    private final String host = env("RABBITMQ_HOST","localhost");
    private final String user = env("RABBITMQ_USER","guest");
    private final String pass = env("RABBITMQ_PASS","guest");
    private final String exchange = env("EXCHANGE_NAME","img.topic");
    private final String queue = env("QUEUE_NAME","face.q");
    private final long delay = Long.parseLong(env("PROCESS_DELAY_MS","900"));

    public static void main(String[] args) throws Exception { new FaceConsumer().start(); }

    private void start() throws Exception {
        ConnectionFactory f = new ConnectionFactory();
        f.setHost(host); f.setUsername(user); f.setPassword(pass);
        try (Connection c = f.newConnection(); Channel ch = c.createChannel()) {
            ch.exchangeDeclare(exchange, BuiltinExchangeType.TOPIC, true);
            ch.queueDeclare(queue, true, false, false, null);
            ch.queueBind(queue, exchange, "face");
            ch.basicQos(1);

            boolean autoAck = false;
            ch.basicConsume(queue, autoAck, (tag, delivery) -> {
                long dtag = delivery.getEnvelope().getDeliveryTag();
                try {
                    JsonNode n = om.readTree(delivery.getBody());
                    String id = n.get("id").asText();
                    Thread.sleep(delay);

                    // Placeholder de IA (trocar por Smile quando quiser)
                    String result = "feliz";
                    log.info("[FACE] id={} result={}", id, result);

                    ch.basicAck(dtag, false);
                } catch (Exception e) {
                    log.error("Erro no FACE", e);
                    ch.basicNack(dtag, false, false);
                }
            }, tag -> {});
            log.info("FaceConsumer pronto em {}. Aguardando...", queue);
            Thread.currentThread().join();
        }
    }

    private static String env(String k, String d){ String v=System.getenv(k); return v!=null?v:d; }
}