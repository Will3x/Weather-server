import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**This class finds the values of missing data/wrong data and corrects it.
 *
 * @author Tim Gorter
 */

public class peakTemp {
    
       /**
         * This is the main method of the class repairTemp, it executes all of the required methods
         *
         * @param path      The path of the newly received xml file
         */

    public static void peakTemp(String path) throws IOException, ParserConfigurationException, SAXException {
        File file = new File("prevtemp");

        // If the receiver hasn't received data yet (At the first time running this code this will execute.
        // This code looks if prevtemp is created yet)
        if (!file.isFile())
        {
            file.createNewFile();
            System.out.println("prevtemp aangemaakt");
        }

        //If there is previous data
        else {
            ArrayList<Integer> toProcess;
            toProcess = findPeakTemps(path);                                         // Find data bigger or smaller than 20% compared to the previous file
            Map<Integer, ArrayList> gemiddeldelijst = getLastTemps(path, toProcess); // Get the last average temperature of the previous 30 XML files
            if(gemiddeldelijst != null){repairPeakTemp(gemiddeldelijst, path);}     // If there are incorrect values, repair the incorrect data
            System.out.println("Incorrecte gegevens bewerkt");
        }
        writePrevFile(path);
    }

     /**
     * This method compares the received XML file with the previously received XML file.
     * When a temperature is higher or lower then 20% compared to the previous temperature it adds this value to a ArrayList.
     *
     * @param path      The path of the newly received xml file
     * @return          This ArrayList contains the station ID's that need to be updated
     */
    
    private static ArrayList<Integer> findPeakTemps(String path) throws IOException, ParserConfigurationException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new File(path));
        document.getDocumentElement().normalize();

        Element root = document.getDocumentElement();
        NodeList nodeList = root.getChildNodes();
        ArrayList<Integer> toProcess = new ArrayList<>();

        Map<String, List<Integer>> map = getArrayLists.getArrayLists("prevtemp");
        ArrayList<Integer> temperatures = (ArrayList<Integer>) map.get("temperatures");
        ArrayList<Integer> stationID = (ArrayList<Integer>) map.get("stationID");
        if(nodeList !=null) {
            for (int x = 0; x < nodeList.getLength(); x++) {
                Node node = nodeList.item(x);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element el = (Element) nodeList.item(x);
                    String temp = null;
                    boolean isRussia = true;
                    if (el.getNodeName().contains("MEASUREMENT")) {
                        String id = el.getElementsByTagName("STN").item(0).getTextContent();
                        try {
                            temp = el.getElementsByTagName("TEMP").item(0).getTextContent();
                        } catch (Exception e) {
                            isRussia = false;
                        }
                        if (isRussia) {
                            String finaltemp = temp.substring(0, temp.indexOf("."));
                            int stn = Integer.parseInt(id);
                            float temperatuur = Float.parseFloat(finaltemp);
                            if (stationID.contains(stn)) {
                                int index = stationID.indexOf(stn);
                                int prevtemp = temperatures.get(index);
                                float maxtemp;
                                float mintemp;
                                if (prevtemp < 3 && prevtemp > -3) {
                                    mintemp = -5;
                                    maxtemp = 5;
                                } else {
                                    if (prevtemp < 0) {
                                        maxtemp = (float) (prevtemp * 0.80);
                                        mintemp = (float) (prevtemp * 1.20);
                                    } else {
                                        mintemp = (float) (prevtemp * 0.80);
                                        maxtemp = (float) (prevtemp * 1.20);
                                    }
                                }
                                if (temperatuur < mintemp || temperatuur > maxtemp) {
                                    toProcess.add(stn);
                                }
                            }
                        }
                    }
                }
            }
        }
        return(toProcess);
    }

   /**
     * This method returns the 30 previously measured data. (When there are stations with incorrect data)
     *
     * @param path                  The path of the newly received xml file
     * @param toProcess             The ArrayList of station's that need to be corrected
     * @return                      This method returns a Map with a key value of the station ID. Every key value has a
     *                              ArrayList with the previous 30 temperatures
     */
    
    private static Map<Integer, ArrayList> getLastTemps(String path, ArrayList<Integer> toProcess) throws ParserConfigurationException, SAXException, IOException

    {
        //als er geen foute gegevens zijn
        if (toProcess.size() == 0) {
            System.out.println("Geen foute gegevens gevonden");
            return null;
            //als er wel foute gegevens zijn: lees de vorige 30 bestanden en bereken hiervan het gemiddelde
        } else {
            Map<Integer, ArrayList> gemiddeldelijst = new HashMap<>();
            for (Integer toProces : toProcess) {
                ArrayList<Float> waardes = new ArrayList<>();
                gemiddeldelijst.put(toProces, waardes);
            }
            System.out.println(path);
            int filecounter = Integer.parseInt(path.substring(36, path.length() - 4));
            int aantalBestanden;
            if (filecounter < 30) {
                aantalBestanden = filecounter;
            } else {
                aantalBestanden = 30;
            }
            for (int i = 0; i < aantalBestanden; i++) {
                filecounter--;
                String fileToRead = "/var/www/barambo.ga/html/weer/output" + (filecounter) + ".xml";

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(new File(fileToRead));
                document.getDocumentElement().normalize();

                Element root = document.getDocumentElement();
                NodeList nodeList = root.getChildNodes();

                if(nodeList != null) {
                    for (int x = 0; x < nodeList.getLength(); x++) {
                        Node node = nodeList.item(x);
                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                            Element el = (Element) nodeList.item(x);
                            if (el.getNodeName().contains("MEASUREMENT")) {
                                String id = el.getElementsByTagName("STN").item(0).getTextContent();
                                int stn = Integer.parseInt(id);
                                ArrayList<Float> waarde = (ArrayList<Float>) gemiddeldelijst.get(stn);
                                if (waarde != null) {
                                    try {
                                        String temp = el.getElementsByTagName("TEMP").item(0).getTextContent();
                                        String finaltemp = temp;
                                        float temperatuur = Float.parseFloat(finaltemp);
                                        waarde.add(temperatuur);
                                        gemiddeldelijst.put(stn, waarde);
                                    } catch (Exception e) {
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return(gemiddeldelijst);
        }
    }


    /**
     * This method calculates the average temperature of the 30 previously measured temperatures and writes the
     * corrected data to the newly received XML file.
     *
     * @param path                  The path of the newly received xml file
     * @param gemiddeldelijst       Map with station ID as key and as valua a ArrayList with the 30 previously measured values
     */
    
    private static void repairPeakTemp(Map<Integer, ArrayList> gemiddeldelijst, String path) throws ParserConfigurationException, IOException, SAXException {
        for (Map.Entry<Integer, ArrayList> entry : gemiddeldelijst.entrySet()) {
            float totaal = 0;
            int idKey = entry.getKey();
            ArrayList waardes = entry.getValue();
            for (int i = 0; i < waardes.size(); i++) {
                totaal = totaal + (float) waardes.get(i);
                System.out.println(waardes.get(i));
            }
            float gemiddelde = totaal / waardes.size();
            String afgerond = String.format("%.01f", gemiddelde);
            afgerond = afgerond.replace(",", ".");
            System.out.println("----------------- ");
            System.out.println(afgerond + "             (gemiddelde)");
            System.out.println("-----------------");

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(path));
            document.getDocumentElement().normalize();

            Element root = document.getDocumentElement();
            NodeList nodeList = root.getElementsByTagName("STN");

            for (int x = 0; x < nodeList.getLength(); x++) {
                Node node = nodeList.item(x);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element el = (Element) nodeList.item(x);
                    if (el.getNodeName().contains("MEASUREMENT")) {
                        String id = el.getElementsByTagName("STN").item(0).getTextContent();
                        int stn = Integer.parseInt(id);
                        if (stn == idKey) {
                            try {
                                el.getElementsByTagName("TEMP").item(0).setTextContent(afgerond);
                            }
                            catch(Exception e) {e.printStackTrace();}
                        }
                    }
                }
            }
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = null;
            try {
                transformer = transformerFactory.newTransformer();
            } catch (TransformerConfigurationException e) {
                e.printStackTrace();
            }
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(path));
            try {
                transformer.transform(source, result);
            } catch (TransformerException e) {}
        }
    }

    private static void writePrevFile(String path) throws IOException, ParserConfigurationException, SAXException {
        PrintWriter writer = new PrintWriter("prevtemp", "utf-8");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new File(path));
        document.getDocumentElement().normalize();

        Element root = document.getDocumentElement();
        NodeList nodeList = root.getChildNodes();
        if(nodeList != null) {
            for (int x = 0; x < nodeList.getLength(); x++) {
                Node node = nodeList.item(x);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element el = (Element) nodeList.item(x);
                    if (el.getNodeName().contains("MEASUREMENT"))
                    {
                        boolean isRussia = true;
                        String id = el.getElementsByTagName("STN").item(0).getTextContent();
                        String temp = null;
                        try {
                            temp = el.getElementsByTagName("TEMP").item(0).getTextContent();
                        } catch (Exception e) {
                            isRussia = false;
                        }
                        if(isRussia)
                        {
                            temp = temp.substring(0, temp.indexOf("."));
                            int stn = Integer.parseInt(id);
                            int temperatuur = Integer.parseInt(temp);
                            writer.println(stn + " " + temperatuur);
                        }
                    }
                }
            }
        }
        writer.flush();
        writer.close();
    }
}
