import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used to get a list with the station ID and temperatures from a temporary file. This class is used in
 * the classes maxTemp and repairTemp.
 *
 * @author Tim Gorter
 */

public class getArrayLists {

   /**
     * This method is used to get the station ID and temperature information from a temporary file.
     *
     * @param file      The path to the file that needs to be read.
     * @return map      This method returns a map containing the station ID and temperature stored in a temporary file.
     */
    
    public static Map<String,List<Integer>> getArrayLists(String file) throws IOException {
    ArrayList<Integer> temperatures = new ArrayList<>();
    ArrayList<Integer> stationID = new ArrayList<>();
    Map<String,List<Integer>> map =new HashMap();
    try (BufferedReader br = new BufferedReader(new FileReader(file)))
    {
        String line;
        while ((line = br.readLine()) != null)
        {
            if(line.contains(" "))
            {
                int id = Integer.parseInt(line.substring(0, line.indexOf(" ")));
                int temp= Integer.parseInt(line.substring(line.indexOf(" ")+1, line.length()));
                stationID.add(id);
                temperatures.add(temp);
            }
        }
    }
    map.put("temperatures", temperatures);
    map.put("stationID", stationID);

    return map;
}
}
