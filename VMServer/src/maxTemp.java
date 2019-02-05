import java.io.*;

import java.nio.channels.FileChannel;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class keeps track of all the maximum temperatures received.
 *
 *@author Tim Gorter
 */

public class maxTemp

{
    /**
     * This method updates the output file containing the ten highest maximum temperatures. It sorts the list with all
     * of the maximum temperatures and it gets the first 10 values.
     */
    
    public static void outputMaximums() throws IOException {
        File f = new File("/var/www/barambo.ga/html/temp/lowestmaxtemps.txt");
        if (!f.isFile()) {
            f.createNewFile();
        }
        TreeMap<Integer, Integer> map = new TreeMap();
        try (BufferedReader br = new BufferedReader(new FileReader("temp"))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains(" ")) {
                    int id = Integer.parseInt(line.substring(0, line.indexOf(" ")));
                    int temp = Integer.parseInt(line.substring(line.indexOf(" ") + 1, line.length()));
                    map.put(temp, id);
                }
            }
        }
        TreeMap<Integer, Integer> SortedMap = new TreeMap();
        SortedMap.putAll(map);

        PrintWriter writer = new PrintWriter("/var/www/barambo.ga/html/temp/lowestmaxtemps.txt", "utf-8");
        if(!SortedMap.isEmpty())
        {
            for (int i = 0; i < 10; i++) {
                String value = SortedMap.firstEntry().toString();
                String temperatuur = value.substring(0, value.indexOf("="));
                String id = value.substring(value.indexOf("=") + 1, value.length());
                String locatie = findLocation(id);
                writer.println(id +"*" + locatie + "*" + temperatuur + "*");
                SortedMap.remove(SortedMap.firstKey());
            }
        }
        writer.flush();
        writer.close();
    }

   /**
     * This method updates the output file containing the ten highest maximum temperatures. It sorts the list with all
     * of the maximum temperatures and it gets the first 10 values.
     *
     * @param id            The ID of the station which location name has to be found.
     * @return locatie      The name of the location where the station is positioned.
     */
    
    private static String findLocation(String id) throws IOException {
        String locatie = "Not found";
        try (BufferedReader br = new BufferedReader(new FileReader("/home/Project-2.2-Weerserver/VMServer/StationsData.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                if(line.contains(id))
                {
                    String[] lineInArray = line.split("\t");
                    locatie = lineInArray[1];
                }
            }
            br.close();
        }
        return locatie;
    }

   /**
     * This method updates a temporary file which contains the maximum temperature of all of the incoming station ID's and temperatures.
     *
     * @param path      The path of the newly received xml file
     */

    public static void maximumChecker(String path) throws ParserConfigurationException, SAXException, IOException {
        File f = new File("temp");
        if (!f.isFile()) {
            f.createNewFile();
        }
        Map<String, List<Integer>> map = getArrayLists.getArrayLists("temp");

        ArrayList<Integer> temperatures = (ArrayList<Integer>) map.get("temperatures");
        ArrayList<Integer> stationID = (ArrayList<Integer>) map.get("stationID");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new File(path));
        document.getDocumentElement().normalize();

        Element root = document.getDocumentElement();
        NodeList nodeList = root.getChildNodes();

        if(nodeList !=null) {
            for (int x = 0; x < nodeList.getLength(); x++) {
                Node node = nodeList.item(x);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element el = (Element) nodeList.item(x);
                    String temp = null;
                    boolean isRussia = true;
                    if (el.getNodeName().contains("MEASUREMENT")) {
                        String id = el.getElementsByTagName("STN").item(0).getTextContent();
                        try
                        {
                            temp = el.getElementsByTagName("TEMP").item(0).getTextContent();
                        }
                        catch(Exception e)
                        {
                            isRussia = false;
                        }
                        if(isRussia)
                        {
                            String finaltemp = temp.substring(0, temp.indexOf("."));
                            int stn = Integer.parseInt(id);
                            int temperatuur = Integer.parseInt(finaltemp);

                            if (stationID.contains(stn)) {
                                int index = stationID.indexOf(stn);
                                int prevmaxtemp = temperatures.get(index);

                                if (prevmaxtemp < temperatuur) {
                                    temperatures.set(index, temperatuur);
                                }
                            } else {
                                stationID.add(stn);
                                temperatures.add(temperatuur);
                            }
                        }
                    }
                }
            }

            PrintWriter writer = new PrintWriter("temp", "utf-8");
            int counter1 = 0;
            if (temperatures.size() == temperatures.size()) {
                while (counter1 < temperatures.size()) {
                    writer.println(stationID.get(counter1) + " " + temperatures.get(counter1));
                    counter1++;
                }
            } else {
                System.out.println("ERROR");
            }

            writer.flush();
            writer.close();
            System.out.println("Maximale waardes van weerservers geupdate");
        }
    }
}
