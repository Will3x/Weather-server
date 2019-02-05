import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static final int PORT = 2550;
    private static final int MAXCONNECTIONS = 800;
    static CountSemaphore semaphore = new CountSemaphore(MAXCONNECTIONS);

    private void start() {
        Socket connection;
        try {
            ServerSocket server = new ServerSocket(PORT);
            System.err.println("Server started. Max connections: " + MAXCONNECTIONS);

            while (true) {
                connection = server.accept();
                Thread client = new Thread(new Client(connection));
                client.start();
            }
        } catch (java.io.IOException ignored) { }
    }

    public static void main(String[] args) throws InterruptedException {
        if (!SendingClient.makeConnection()){
            Thread.sleep(1);
            if(SendingClient.reconnect()){
                StationIDCheck.readOnce();
                new ParseXML();
                new Server().start();
            }
        } else {
            StationIDCheck.readOnce();
            new ParseXML();
            new Server().start();
        }
    }
}
