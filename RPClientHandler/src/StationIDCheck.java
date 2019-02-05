import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;

class StationIDCheck {

    private static final File TARGETSTATIONS = new File("Target stations.txt");
    private static final File STDATA = new File("StationsData.txt");

    private static ArrayList<String> target;
    private static ArrayList<String> data;

    private static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    private DocumentBuilder builder;
    private Document newDoc;
    private String time;
    private StringReader reader;

    private boolean match = false;
    private boolean keepConnection = false;

    StationIDCheck() {
        try {
            this.builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Takes the xmlString content of a thread and parses to XML to make sense of the information inside. Afterwards,
     * it will look for every station ID and does a check with {target} to see if it matches with the station ID's that
     * we're looking for.
     *
     * @return True if it contains data we need. False if all data is not needed for our application.
     * @author Willem Pepping
     */
    boolean Filter(String xmlString) throws IOException, SAXException {
            reader = new StringReader(xmlString);

            newDoc = builder.parse(new InputSource(reader));
            Element root = newDoc.getDocumentElement();
            time = newDoc.getElementsByTagName("TIME").item(0).getTextContent();
            NodeList measurements = root.getChildNodes();


            for (int i = 0; i < measurements.getLength(); i++) {
                if (measurements.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    NodeList children = measurements.item(i).getChildNodes();

                    String station = children.item(1).getTextContent();

                    for (String result : target) {
                        if (result.equals(station)) {
                            String[] location = checkCountryCity(station);
                            if (location != null) {
                                String city = location[1];
                                String country = location[2];

                                filterNodes(measurements.item(i), country);

                                ((Element) measurements.item(i)).setAttribute("city", city); // add attribute to node with country name.
                                ((Element) measurements.item(i)).setAttribute("country", country); // add attribute to node with country name.
                            }

                            match = true;
                            keepConnection = true; // keep this cluster connection alive since it contains data we need.
                            break;
                        }
                    }

                    if (!match) {
                        deleteNode(measurements.item(i));
                    }
                    match = false;
                }
            }
        builder.reset();
        return keepConnection;
    }

    /**
     * Reads data from external .txt files and saves this information inside a static data structure.
     * This ensures that only two I/O actions have to be made. Otherwise every thread will have to access these .txt
     * files for every iteration, over and over again.
     *
     * @author Willem Pepping
     */
    static void readOnce(){
        target = new ArrayList<>();
        data = new ArrayList<>();

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(TARGETSTATIONS));
            BufferedReader stReader = new BufferedReader(new FileReader(STDATA));

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                target.add(line);
            }

            while ((line = stReader.readLine()) != null) {
                data.add(line);
            }

            bufferedReader.close();
            stReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Filters the XML contents based on country of parent node. If parent node is Russia, all nodes can be deleted
     * except for <TEMP></TEMP> and <STN></STN>. In all other cases all nodes will be deleted except for <CLC></CLC>
     * and <STN></STN>.
     *
     * @author Willem Pepping
     */
    private void filterNodes(Node node, String country) {
        if(country.equals("RUSSIA")) {
            for (int y = 0; y < 21; y++) {
                node.removeChild(node.getLastChild());
            }
        } else{
            for (int y = 0; y < 2; y++) {
                node.removeChild(node.getLastChild());
            }
            for (int y = 0; y < 19; y++){
                node.removeChild(node.getChildNodes().item(6));
            }
        }
        for (int y = 0; y < 4; y++) {
            node.removeChild(node.getChildNodes().item(2));
        }
    }

    /**
     * Deletes an entire <MEASUREMENT></MEASUREMENT> node including it's children.
     *
     * @author Willem Pepping
     */
    private void deleteNode(Node node) {
        while (node.hasChildNodes()) {
            node.removeChild(node.getFirstChild());
        }
        node.getParentNode().removeChild(node);
    }

    /**
     * Loops through data structure which consists of station ID and associated country name and location and looks for
     * the line which consists of given station ID. If a match was found, returns list of associated information.
     *
     * @return List of Strings, split on tab ("\t") which consists of station ID, country name and location.
     * @author Willem Pepping
     */
    private String[] checkCountryCity(String station) {
        for (String result : data) {
            if (result.contains(station)) {
                return result.split("\t");
            }
        }
        return null;
    }

    Document getNewDoc() {
        return newDoc;
    }

    StringReader getReader() {
        return reader;
    }

    String getTime() {
        return time;
    }
}
