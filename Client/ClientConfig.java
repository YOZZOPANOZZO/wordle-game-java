public class ClientConfig {
    public int port;
    public String multicastAddress;
    public int multicastPort;
    public ClientConfig(int port, String multicastAddress, int multicastPort) {
        this.port = port;
        this.multicastAddress = multicastAddress;
        this.multicastPort = multicastPort;
    }


}
