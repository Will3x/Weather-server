import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

class ParseXML extends DocumentHandler {

    static final Object obj = new Object();

    ParseXML() {
        init();
        Thread parseThread = new Thread(() -> {
            deleteDocument();
            createNewDocument();
            insertBaseXML();
            parseBaseXMLDoc();
            while (true) {
                synchronized (obj) {
                    try {
                        obj.wait();
                        writeToFile();
                        SendingClient.sendFile(FILE);
                        deleteDocument();
                        createNewDocument();
                        insertBaseXML();
                        parseBaseXMLDoc();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                synchronized (Client.obj) {
                    Client.obj.notifyAll();
                }
            }
        });
        parseThread.start();
    }

    private void parseBaseXMLDoc(){
        try {
            document = builder.parse(FILE);
            System.out.println("Parse complete");
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inserts base XML content that all new Weatherdata XML files are based on. This consists of the root node with
     * a date and time node.
     *
     * @author Willem Pepping
     */
    private void insertBaseXML() {
        try {
            Element root = document.createElement("WEATHERDATA");
            Element date = document.createElement("DATE");
            Element time = document.createElement("TIME");

            SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd");
            date.setTextContent(sfd.format(getDate()));

            document.appendChild(root);
            root.appendChild(date);
            root.appendChild(time);

            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(FILE);

            aTransformer.transform(domSource, streamResult);
            System.out.println("Inserted base XML.");
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Appends the content of document held by thread to a single general document. This method will be accessed by
     * multiple threads and is required to be synchronized.
     *
     * @author Willem Pepping
     */
    static synchronized void appendToDoc(Document doc, StringReader reader) {
            Element root = doc.getDocumentElement();
            NodeList nodes = root.getChildNodes();

            for (int i = 0; i < nodes.getLength(); i++) {
                Node imported = document.importNode(nodes.item(i), true);
                document.getDocumentElement().appendChild(imported);
            }

        reader.close();
    }

    /**
     * Adds <TIME></TIME> value to new XML document taken from a thread's <TIME></TIME> field.
     * Adds 4 hours to the time to match with Georgia's timezone.
     *
     * @author Willem Pepping
     */
    static void addTime(String time) throws ParseException {
        Node timeTag = document.getElementsByTagName("TIME").item(0);

        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");

        Date d = df.parse(time);
        Calendar gc = new GregorianCalendar();
        gc.setTime(d);
        gc.add(Calendar.HOUR, 4);
        Date d2 = gc.getTime();

        timeTag.setTextContent(df.format(d2));
    }

    private Date getDate(){
        Calendar gc = new GregorianCalendar();
        Date d2 = gc.getTime();
        return d2;
    }
}

