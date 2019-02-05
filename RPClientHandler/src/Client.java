import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.ParseException;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicLong;

public class Client implements Runnable {

    private Socket connection;
    private BufferedReader bin;
    private StationIDCheck check;

    private static final int INTERVAL = 30;
    private static final AtomicLong counter = new AtomicLong();
    private static Phaser barrier = new Phaser();

    static final Object obj = new Object();

    private String result = "";
    private String message = "";

    Client(Socket connection) {
        barrier.register();
        check = new StationIDCheck();
        this.connection = connection;
    }


    /**
     * Main loop for every new thread spawned.
     *
     * @author Willem Pepping
     */
    @Override
    public void run() {
        try {
            Server.semaphore.acquire();
            bin = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            while ((message = bin.readLine()) != null) {
                result += message;
                if (message.equals("</WEATHERDATA>")) {
                    counter.addAndGet(1); // Increment for every thread entering from this point.

                    if (!check.Filter(result)) {
                        break;
                    }
                    ParseXML.appendToDoc(check.getNewDoc(), check.getReader());

                    barrier.arriveAndAwaitAdvance(); // Wait for all other threads, then continue.

                    synchronized (obj) {
                        if (counter.decrementAndGet() == 0) { // Decrement and check if it's the last thread entering.
                            synchronized (ParseXML.obj) {
                                ParseXML.addTime(check.getTime());
                                ParseXML.obj.notify(); // Wake up thread in ParseXML so it can write to file.
                            }
                        }
                        obj.wait(); // Wait for thread in ParseXML to finish doing it's task.
                    }

                    clearBuffer(bin, INTERVAL);
                    result = "";
                }
            }
        } catch (InterruptedException | IOException | ParseException | SAXException e) {
            e.printStackTrace();
        }
        closeConnection();
    }

    /**
     * Clears buffer in BufferedReader to get data as real-time as possible.
     * Without this method, old data would be read from buffer which holds no value in this applications purpose.
     *
     * @param interval Reads buffer for x times. A cluster is sent every 1 second so reading a buffer for 5 minus 1
     *                 (time to process data) times means there's also an interval of 5 seconds.
     *
     * @author Willem Pepping
     */
    private void clearBuffer(BufferedReader bin, int interval) throws IOException {
        interval -= 3;
        for (int x = 0; x < interval; x++) {
            while ((message = bin.readLine()) != null) {
                if (message.equals("</WEATHERDATA>")) {
                    break;
                }
            }
        }
    }

    private void closeConnection() {
        try {
            barrier.arriveAndDeregister();
            bin.close();
            counter.decrementAndGet();
            connection.close();
            Server.semaphore.release();

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}
