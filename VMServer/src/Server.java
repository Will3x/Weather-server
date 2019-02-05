import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Server extends Thread {
    private static final int PORT = 3332;
    private InputStream in;
    private DataInputStream clientData;
    private int count = 0;
    private Socket s;

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);

            while (true) {
                System.out.println("Waiting for connections...");
                s = serverSocket.accept();
                in = s.getInputStream();
                clientData = new DataInputStream(in);
                System.out.println("Connected to " + s.getInetAddress().toString());
                while (!s.isClosed()) {
                    saveFile();

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveFile() {
        try {
            int bytesRead;
//            String outputFile = "C:\\xampp\\htdocs\\personal\\barambo.ga\\weer\\output" + count + ".xml";
            String outputFile = "/var/www/barambo.ga/html/weer/output" + count + ".xml";

            long len = clientData.readLong();
            byte[] buffer = new byte[8192];

            OutputStream output = new FileOutputStream(new File(outputFile));

            while (len > 0 && (bytesRead = clientData.read(buffer, 0, (int) Math.min(buffer.length, len))) != -1) {
                output.write(buffer, 0, bytesRead);
                len -= bytesRead;
            }

            output.close();
            Decrypter.main(outputFile);

            System.out.println("File " + count + " received!");

            if(count > 30) {
                peakTemp.peakTemp(outputFile);
                maxTemp.maximumChecker(outputFile);
                maxTemp.outputMaximums();
            }
            count++;
        } catch (SocketException | EOFException e) {
            System.err.println("Connection with " + s.getInetAddress() + " lost.");
            try {
                in.close();
                clientData.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Server().start();
    }
}  