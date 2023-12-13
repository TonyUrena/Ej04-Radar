// CarSimulator.java
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Random;

public class CarSimulator {

    public static void main(String[] args) {
        String broker = "tcp://localhost:1883";
        String clientId = "CarSimulator";
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttClient mqttClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("Connecting to broker: " + broker);
            mqttClient.connect(connOpts);
            System.out.println("Connected");

            Random rand = new Random();

            while (true) {
                int speed = rand.nextInt(81) + 60;
                String licensePlate = String.format("%04d%s", rand.nextInt(10000), randomAlphaNumeric(3));

                String topic = "car/speed";
                String content = licensePlate + ":" + speed;
                MqttMessage message = new MqttMessage(content.getBytes());
                mqttClient.publish(topic, message);
                System.out.println("Sent message: " + content);

                Thread.sleep(1000);
            }

        } catch (MqttException | InterruptedException me) {
            me.printStackTrace();
        }
    }

    private static String randomAlphaNumeric(int count) {
        String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }
}
