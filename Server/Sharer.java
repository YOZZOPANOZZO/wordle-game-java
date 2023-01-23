import com.google.gson.Gson;

import java.io.IOException;
import java.net.*;


public class Sharer {
    public static String ip ;
    private static DatagramSocket socket;
    private final int  ClientPort;
    private final MulticastSocket multicast; // multicast socket per inviare le condivisioni delle partite
    private final InetAddress multicastGroup; // gruppo multicast su cui inviare
    public Sharer(String multicastAddress, int port) throws IOException {

        this.ClientPort = port;
        this.ip = multicastAddress;
        multicast = new MulticastSocket(ClientPort);
        multicast.setReuseAddress(true);
        multicastGroup = InetAddress.getByName(ip);
        multicast.joinGroup(multicastGroup);
        this.socket = multicast;


    }

    public void notifyUser(User user){
        Gson gson = new Gson();
        try{
            user.passwd = String.valueOf(user.getAverage());
            String json = gson.toJson(user);
            DatagramPacket packet =  new DatagramPacket(json.getBytes(),json.length());
            packet.setAddress(multicastGroup);
            packet.setPort(ClientPort);
            socket.send(packet);

        }
        catch (Exception e){
            e.printStackTrace();
    }}
}
