import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import org.json.*;
import java.util.List;



public class MultiCastReceiver implements  Runnable{
    private MulticastSocket socket;
    List<JSONObject> msgs;

    public MultiCastReceiver(int port, String ip, List<JSONObject> msgs) throws IOException {

        try {
            this.socket = new MulticastSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        InetAddress address = InetAddress.getByName(ip);
        this.socket.joinGroup(address);
        this.msgs = msgs;
    }

    public void run() {

        while (true) {
            byte[] buff = new byte[1024];
            DatagramPacket notification = new DatagramPacket(buff, buff.length);

            try {
                socket.receive(notification);
                String json = new String(notification.getData(), StandardCharsets.UTF_8);
                JSONObject user = new JSONObject(json);

                synchronized (this.msgs) {
                    this.msgs.add(user);
                }
            } catch (IOException e) {
                System.out.println("There was an error getting other users info, feature not available");
                throw new RuntimeException(e);
            }
        }


    }
}
