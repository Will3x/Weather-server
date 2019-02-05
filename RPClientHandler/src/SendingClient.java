import java.io.*;
import java.net.Socket;
import java.net.SocketException;

class SendingClient {

    private static DataOutputStream dOut;

    static boolean makeConnection() {
        try {
            Socket s = new Socket("51.136.52.8", 3332);

            dOut = new DataOutputStream(s.getOutputStream());
            System.out.println("Connected to " + s.getInetAddress().toString());

        } catch (IOException e) {
            System.err.println("Could not connect to server.");
            return false;
        }
        return true;
    }

    static void sendFile(File file) {
        if (dOut != null && file.exists() && file.isFile()) {
            try {
                System.out.println("Sending data to server...");

                FileInputStream input = new FileInputStream(file);
                int read;

                try {
                    dOut.writeLong(file.length());
                    while ((read = input.read()) != -1) {
                        dOut.writeByte(read);
                    }

                    dOut.flush();

                } catch (SocketException e) {
                    System.err.println("Error: connection with server lost.");
                    dOut.close();
                    input.close();
                    if (SendingClient.reconnect()) {
                        sendFile(file);
                    }
                }
                input.close();
                System.out.println("File successfully sent!\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static boolean reconnect() {
        int maxAttempts = 5;
        System.out.println("Attempting to reconnect...");
        for (int i = 1; i <= maxAttempts; i++) {
            System.out.println("Attempt: " + i);
            if (makeConnection()) {
                return true;
            }
            if (i < maxAttempts) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
        System.err.println("Could not connect to server after " + maxAttempts + " attempts.\nExiting program.");
        System.exit(-1);
        return false;
    }
}
