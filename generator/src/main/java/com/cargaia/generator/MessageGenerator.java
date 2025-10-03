package com.cargaia.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class MessageGenerator {
    private static final Logger log = LoggerFactory.getLogger(MessageGenerator.class);
    private static final ObjectMapper om = new ObjectMapper();
    private final AtomicLong messageCounter = new AtomicLong(0);

    private final String host = env("RABBITMQ_HOST","localhost");
    private final String user = env("RABBITMQ_USER","guest");
    private final String pass = env("RABBITMQ_PASS","guest");
    private final String exchange = env("EXCHANGE_NAME","img.topic");
    private final int msgsPerSec = Integer.parseInt(env("MSGS_PER_SEC","6"));

    public static void main(String[] args) throws Exception {
        new MessageGenerator().start();
    }

    private void start() throws Exception {
        ConnectionFactory f = new ConnectionFactory();
        f.setHost(host);
        f.setUsername(user);
        f.setPassword(pass);

        // Configura throughput logging a cada 5 segundos
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            long count = messageCounter.getAndSet(0);
            System.out.println("[GEN] throughput_5s=" + count + " (~" + (count/5.0) + " msg/s)");
        }, 5, 5, TimeUnit.SECONDS);

        try (Connection c = f.newConnection(); Channel ch = c.createChannel()) {
            // Propriedades com mensagens persistentes
            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                .contentType("application/json")
                .deliveryMode(2) // persistente
                .build();

            boolean toggle = false;
            boolean teamToggle = false;
            boolean faceToggle = false;
            
            // Cria imagens reais programaticamente
            byte[] happyImage = createImage(64, 64, Color.YELLOW, "HAPPY");
            byte[] sadImage = createImage(64, 64, Color.BLUE, "SAD");
            byte[] corinthiansImage = createImage(64, 64, Color.RED, "COR");
            byte[] palmeirasImage = createImage(64, 64, Color.GREEN, "PAL");
            
            log.info("Gerador ON | {} msg/s | exchange={}", msgsPerSec, exchange);
            while (true) {
                String type = (toggle = !toggle) ? "face" : "team";
                byte[] imageBytes;
                String imageColor;
                int imageNumber;
                if (type.equals("face")) {
                    // Face: alterna entre happy e sad
                    faceToggle = !faceToggle;
                    imageBytes = faceToggle ? happyImage : sadImage;
                    imageColor = faceToggle ? "amarela" : "azul";
                    imageNumber = faceToggle ? 1 : 2;
                } else {
                    // Team: alterna entre corinthians e palmeiras (como pedido pelo professor)
                    teamToggle = !teamToggle;
                    imageBytes = teamToggle ? corinthiansImage : palmeirasImage;
                    imageColor = teamToggle ? "vermelha" : "verde";
                    imageNumber = teamToggle ? 3 : 4;
                }
                
                Message m = new Message(
                        UUID.randomUUID().toString(),
                        type,
                        imageBytes,
                        Instant.now().toString(),
                        new MessageMeta("generator-1","demo")
                );
                
                ch.basicPublish(exchange, type, props, om.writeValueAsBytes(m));
                System.out.println("[GEN] Enviando " + type + ": Imagem " + imageNumber + " - " + imageColor);
                messageCounter.incrementAndGet();
                Thread.sleep(1000 / msgsPerSec);
            }
        }
    }
    
    private byte[] createImage(int width, int height, Color color, String text) {
        try {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            
            // Fundo colorido
            g2d.setColor(color);
            g2d.fillRect(0, 0, width, height);
            
            // Texto branco
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            FontMetrics fm = g2d.getFontMetrics();
            int x = (width - fm.stringWidth(text)) / 2;
            int y = (height - fm.getHeight()) / 2 + fm.getAscent();
            g2d.drawString(text, x, y);
            
            g2d.dispose();
            
            // Converte para bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            byte[] imageBytes = baos.toByteArray();
            
            log.info("Imagem criada: {}x{} {} ({} bytes)", width, height, text, imageBytes.length);
            return imageBytes;
        } catch (Exception e) {
            log.error("Erro ao criar imagem: {}", e.getMessage());
            return new byte[0];
        }
    }
    
    private static String env(String k, String d){ String v=System.getenv(k); return v!=null?v:d; }
}