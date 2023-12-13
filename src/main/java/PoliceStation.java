// PoliceStation.java
import org.eclipse.paho.client.mqttv3.*;
import redis.clients.jedis.Jedis;

import java.util.Set;

public class PoliceStation {

    public static void main(String[] args) {
        String broker = "tcp://localhost:1883";
        String clientId = "PoliceStation";
        String excessTopic = "excess/speed";

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
                    int speed = Integer.parseInt(data[1]);

                    int fine = calculateFine(speed);

                    String licensePlate = data[2];
                    String fineTopic = "fine/" + licensePlate;
                    MqttMessage fineMessage = new MqttMessage(String.valueOf(fine).getBytes());
                    mqttClient.publish(fineTopic, fineMessage);

                    Jedis jedis = new Jedis("localhost");
                    jedis.del("EXCESO:80:" + licensePlate);
                    jedis.sadd("VEHICULOSDENUNCIADOS", licensePlate);
                    jedis.close();
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

            mqttClient.subscribe(excessTopic);

            while (true) {
                Jedis jedis = new Jedis("localhost");
                Set<String> vehicles = jedis.smembers("VEHICULOS");
                Set<String> finedVehicles = jedis.smembers("VEHICULOSDENUNCIADOS");
                System.out.println("Total vehicles: " + vehicles.size());
                System.out.println("Percentage of fined vehicles: " + ((double) finedVehicles.size() / vehicles.size()) * 100 + "%");

                Thread.sleep(1000);
            }

        } catch (MqttException | InterruptedException me) {
            me.printStackTrace();
        }
    }

    private static int calculateFine(int speed) {
        int limit = 80;
        if (speed > limit && speed <= limit + 10) {
            return 100;
        } else if (speed > limit + 10 && speed <= limit + 20) {
            return 200;
        } else if (speed > limit + 20) {
            return 500;
        } else {
            return 0;
        }
    }
}
