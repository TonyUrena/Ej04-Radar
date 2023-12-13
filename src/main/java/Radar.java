// Radar.java
import org.eclipse.paho.client.mqttv3.*;
import redis.clients.jedis.Jedis;

public class Radar {

    public static void main(String[] args) {
        String broker = "tcp://localhost:1883";
        String clientId = "Radar";
        String topic = "car/speed";

        try {
            MqttClient mqttClient = new MqttClient(broker, clientId);
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) {
                    System.out.println("Connection to broker lost!");
                }

                @Override
                public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                    String[] data = mqttMessage.toString().split(":");
                    String licensePlate = data[0];
                    int speed = Integer.parseInt(data[1]);

                    if (speed > 80) {
                        Jedis jedis = new Jedis("localhost");
                        jedis.set("EXCESO:80:" + licensePlate, String.valueOf(speed));
                        jedis.sadd("VEHICULOS", licensePlate);
                        jedis.close();
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                }
            });

            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("Connecting to broker: " + broker);
            mqttClient.connect(connOpts);
            System.out.println("Connected");

            mqttClient.subscribe(topic);

        } catch (MqttException me) {
            me.printStackTrace();
        }
    }
}
