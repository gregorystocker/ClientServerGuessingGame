package Assignment3Starter;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.time.*;


import org.json.*;

public class Server {
    enum GameState {
        GUESSING,
        WIN,
        LOSE,
        GETNAME,
        BEGIN

    }
    static GameState gameState = GameState.BEGIN;

    //keeps track of how many correct the user has gotten
    //3 within the time frame = a win
    static int correctGuesses=0;
    //For each CharacterGroup: 1st quote = 5 pts, 2nd = 4 pts, 3rd = 3pts, 4th = 1 pt
    static int points = 0;
    static int totalPoints = 0;
    static int currentPicIdx=0;

    static String playerName = "null";





    /**
     *Creates a JSON object that represents a greeting and prompts the user for their name.
     * @return JSONObject
     */

    /**
     * Creates the basic structure of a request for my application layer protocol, and initializes all values to "null"
     * to begin with.
     * basic structure is:
     *{
     *              "header":
     *              {
     *                     "datatype":"null",
     *                     "type":"null"
     *               },
     *               "payload":
     *               {
     *                     "data":          "null",
     *                     "picture" :      "null",
     *                     "points" :       "null",
     *                     "leaderboard" :  "null"
     *
     *                }
     * }
     * @return JSONObject request template
     */


    public static JSONObject createResponseTemplate(){
        String str = "{\n" +
                "             \"header\":\n" +
                "             {\n" +
                "                    \"datatype\":\"null\",\n" +
                "                    \"type\":\"null\"\n" +
                "              },\n" +
                "              \"payload\":\n" +
                "              {\n" +
                "                    \"data\":          \"null\",\n" +
                "                    \"picture\" :      \"null\",\n" +
                "                    \"points\" :       \"null\",\n" +
                "                    \"leaderboard\" :  \"null\"\n" +
                "     \n" +
                "               }\n" +
                "}";
        return new JSONObject(str);
    }
    /**
     * Constructs the response for beginning a conversation. Presents the Client with a greeting and prompts for
     * their name.
     * @return JSONObject representing the response from Server to Client
     */
    public static JSONObject begin() {
        JSONObject response = createResponseTemplate();
        String message = "Hello! Whats your name?\n";
        response.getJSONObject("header").put("datatype","String");
        response.getJSONObject("header").put("type","begin");
        response.getJSONObject("payload").put("data",message);
        try {
            response = appendImage(response);
        }catch(Exception e){
            e.printStackTrace();
        }

        return response;
    }

    /**
     * Constructs a response to receiving the Client's name
     * @param request JSONObject request
     * @return JSONObject response
     */
    public static JSONObject getName(JSONObject request) {
        String name = null;
        JSONObject response = createResponseTemplate();
        try{
            if(request.has("payload")){
                JSONObject responsePayload = request.getJSONObject("payload");
                name = String.valueOf(responsePayload.get("data"));
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
            System.out.println("In handleGetName on server side:");
            System.out.println("\nServer says the players name is: " + name + "\n");
            playerName = name;

            response.getJSONObject("header").put("datatype", "String");
            response.getJSONObject("header").put("type", "getName");
            String message = "Hey there " + name + "!\n";
            response.getJSONObject("payload").put("data", message);
            gameState = GameState.GUESSING;

        try {
            response = appendImage(response);
        }catch(Exception e){
            e.printStackTrace();
        }
        return response;
    }//ends getName

    /**
     * This method should process an image and append it to the jsonObject.
     * @return JSONObject response with an image
     * @throws IOException
     */
    //////////////////////////////////////////////////////////USING THIS ONE FOR IMAGES//////////////////////
    public static JSONObject appendImage(JSONObject obj) throws IOException {
        File file = null;

            file = new File("img/hi.png");
            //we need to get the name next
            gameState=GameState.GETNAME;


        //read in the file, turn it into a byte array.
        if (!file.exists()) {
            System.err.println("Cannot find file: " + file.getAbsolutePath());
            System.exit(-1);
        }
        // Read in image
        BufferedImage img = ImageIO.read(file);
        byte[] bytes = null;
        //we are writing out a byte[]
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(img, "png", out);
            bytes = out.toByteArray();
        }
        //we then convert it into Base64
        if (bytes != null) {
            Base64.Encoder encoder = Base64.getEncoder();
            if(obj.has("payload")){
                obj.getJSONObject("payload").put("picture",encoder.encodeToString(bytes));
            }
            return obj;
        }
        return error("Unable to save image to byte array");
    }//ends appendImage


    public static JSONObject error(String err) {
        JSONObject json = new JSONObject();
        json.put("error", err);
        return json;
    }//ends error

    public static void main(String[] args) throws IOException {
        DatagramSocket sock = null;
        int port=-1;
        ServerSocket serv = null;
        try {
            try {
                if (args.length >= 1) {
                    port = (Integer) Integer.parseInt(args[0]);
                }else{
                    System.out.println("\nInput of port needed for Server." +
                            "try using gradle runServer\n" +
                            "which will add a default value of -Pport=8080" +
                            "or alternatively add a custom port -Pport with:" +
                            " gradle runServer -Pport=<custom_port>\n");
                    return;
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            System.out.println("Server port:" + port);
            serv = new ServerSocket(port);
            // NOTE: SINGLE-THREADED, only one connection at a time

            while (true) {//loop for accepting new clients
                //socket bundles the port and IP abstracting these 2 into one thing. And many times the IP address is implied.
                //Only the port needs to be given.

                try {

                    //this is where we wait for a new client////////////////////////
                    sock = new DatagramSocket(port); // blocking wait
                    String choice = null;

                    //defining server paramenters such as the input and ouput objects we will be communicating through

                    while (true) {
                        NetworkUtils.Tuple messageTuple = NetworkUtils.Receive(sock);
                        JSONObject message = JsonUtils.fromByteArray(messageTuple.Payload);
                        JSONObject returnMessage=null;
                        System.out.println("Message is" + message.toString());
                        //starts as Message is{"selected":-1}

                        if (message.has("header") ) {

                            JSONObject requestHeader = message.getJSONObject("header");

                            if( requestHeader.has("type") ) {

                                choice = String.valueOf(requestHeader.get("type"));
                                switch (choice) {
                                    case ("begin"):
                                        returnMessage = begin();
                                        break;
                                    case ("getName"):
                                        returnMessage = getName(message);
                                        break;
                                    default:
                                        returnMessage = error("Invalid selection: " + choice + " is not an option");
                                 }//ends switch
                                }//ends if requestType.has(type)

                            } //ends if message has header
                         else {
                            returnMessage = error("Invalid message received in Server");
                        }
                        // we are converting the JSON object we have to a byte[]
                        if(returnMessage==null){
                            System.out.println("\nresponse is null\n");
                        }
                        byte[] output = JsonUtils.toByteArray(returnMessage);
                        NetworkUtils.Send(sock, messageTuple.Address, messageTuple.Port, output);

                    }//ends while(true) loop
                } catch (Exception e) {
                    System.out.println("Client disconnect");
                } finally {
                    if (sock != null) {
                        sock.close();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serv != null) {
                serv.close();
            }
        }
    } //ends psvm
}//ends class Server