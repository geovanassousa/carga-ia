package com.cargaia.consumer.face;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smile.classification.KNN;

public class FaceConsumer {
    private static final Logger log = LoggerFactory.getLogger(FaceConsumer.class);
    private static final ObjectMapper om = new ObjectMapper();

    private final String host = env("RABBITMQ_HOST","localhost");
    private final String user = env("RABBITMQ_USER","guest");
    private final String pass = env("RABBITMQ_PASS","guest");
    private final String exchange = env("EXCHANGE_NAME","img.topic");
    private final String queue = env("QUEUE_NAME","face.q");
    private final long delay = Long.parseLong(env("PROCESS_DELAY_MS","900"));

    private final KNN<double[]> knn;

    public FaceConsumer() {
        double[][] X = {{1,1,0},{1,0,0},{0,1,0},{0,0,1},{0,0,0}};
        int[] y = {1,1,1,0,0}; // 1=feliz, 0=triste
        knn = KNN.fit(X, y, 1);
    }

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
                    String id  = n.get("id").asText();
                    String url = n.has("image_url") ? n.get("image_url").asText().toLowerCase() : "";

                    double[] feat = {
                        url.contains("smile") ? 1 : 0,
                        url.contains("happy") ? 1 : 0,
                        url.contains("sad")   ? 1 : 0
                    };

                    Thread.sleep(delay);
                    int pred = knn.predict(feat);
                    String result = (pred == 1) ? "feliz" : "triste";
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