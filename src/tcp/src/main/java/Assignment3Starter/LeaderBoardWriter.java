package Assignment3Starter;
import org.json.JSONObject;

import org.json.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This is a class focused on managing the leaderboard and converting it to different formats.
 */
public class LeaderBoardWriter {
    /**
     * Create a FileWriter and just pass the filename string in the constructor.
     * use file.write(JSONObject.toString()) to write the JSONObject to a file
     * file.flush()
     * @param data
     * @param filename
     * @return
     */
    public static boolean writeJSONFile(JSONObject data, String filename){
        try{
            FileWriter file = new FileWriter(filename);
            file.write(data.toString());
            file.flush(); //flushes out the fileWriter so that it can be used with a clean slate next time
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Reads the data in file given into a JSONObject or null if there was a problem.
     * @param filepath location/name of the file we want to read.
     * @return JSON object representation of the file given by filepath
     */
    public static JSONObject readJSONFile(String filepath){
        /**
         * Using a bufferedReader in order to reach the end of each line in the JSON file,
         * we need to append each line to a string builder object.
         * We can use the StringBuilder object to then create the JSONObject.
         */
        try{
            BufferedReader reader = new BufferedReader(new FileReader(filepath));
            StringBuilder sb = new StringBuilder();
            String line = reader.readLine();
            while(line != null){
                sb.append(line);
                line = reader.readLine();
            }
            JSONObject jsonData = new JSONObject(sb.toString());
            return jsonData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Reads the data in file given into a JSONObject or null if there was a problem.
     * @param filepath location/name of the file we want to read.
     * @return JSON object representation of the file given by filepath
     */
    public static JSONArray readJSONArrayFromFile(String filepath){
        /**
         * Using a bufferedReader in order to reach the end of each line in the JSON file,
         * we need to append each line to a string builder object.
         * We can use the StringBuilder object to then create the JSONObject.
         */
        try{
            BufferedReader reader = new BufferedReader(new FileReader(filepath));
            StringBuilder sb = new StringBuilder();
            String line = reader.readLine();
            while(line != null){
                sb.append(line);
                line = reader.readLine();
            }
            JSONArray jsonData = new JSONArray(sb.toString());
            return jsonData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }//end readJSONFileToArray
    /**
     * Create a FileWriter and just pass the filename string in the constructor.
     * use file.write(JSONArray.toString()) to write the JSON array to a file
     * file.flush() clears out the writer for next usage.
     * @param data
     * @param filename
     * @return
     */
    public static boolean writeJSONArrayToFile(JSONArray data, String filename){
        try{
            FileWriter file = new FileWriter(filename);
            file.write(data.toString());
            file.flush(); //flushes out the fileWriter so that it can be used with a clean slate next time
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }//end writeJSONFileToArray

    JSONObject createDefaultBoard() {
        JSONObject board = new JSONObject();
        board.append("Alan Touring", "1");
        board.append("Arnold Schwarzenegger", "0");
        board.append("Wolfgang Amadeus Mozart", "-1");
        board.append("Samuel L Jackson", "-2");
        board.append("Theo Von", "-3");
        return board;
    }




}//ends class





