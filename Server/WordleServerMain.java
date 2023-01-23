import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WordleServerMain {
    public static void main(String[] args) throws IOException {
        Database DB = new Database();
        ServerConfig params = DB.loadConfig();
        System.out.println("Config loaded");


        try (ServerSocket listener = new ServerSocket(params.port)) {
            System.out.println("WORDLE server started on port " + params.port);
            try {
                ExecutorService pool = Executors.newCachedThreadPool();
                ScheduledExecutorService updateWordAsync = Executors.newSingleThreadScheduledExecutor();
                System.out.println("SW timeout set to " + params.timeoutMinutes + " minutes");
                updateWordAsync.scheduleAtFixedRate(new WordUpdater(DB), 0, params.timeoutMinutes, TimeUnit.MINUTES);
                Sharer MultiCastSharer = new Sharer(params.multicastAddress,params.multicastPort);
                System.out.println("Multicast server started on "  + params.multicastAddress+ ":" + params.multicastPort);
                System.out.println("Waiting for clients to connect...");




                while (true) {
                    pool.execute(new WordleServerInstance(listener.accept(), DB,MultiCastSharer));
                }
            }

            catch (Exception e) {
                System.out.println(e);
            }
        }
        catch (BindException e) {
            System.out.println("Error: Port " + params.port + " is already being used by another process");
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }
}



