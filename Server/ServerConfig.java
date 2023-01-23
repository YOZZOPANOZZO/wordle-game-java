public class ServerConfig {
    public int timeoutMinutes;
    public int port;
    public  String  multicastAddress;
    public int multicastPort;
    public ServerConfig(int timeoutMinutes, int port, String multicastAddress, int multicastPort) {
        this.timeoutMinutes = timeoutMinutes;
        this.port = port;
        this.multicastAddress = multicastAddress;
        this.multicastPort = multicastPort;
    }

}
