/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package actuatorapp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Karl
 */
public class ActuatorApp {
    //Needs to take in secret as argument
    private Socket api;
    
    private HashMap<String, Actuator> actuators = new HashMap<>();
    
    public ActuatorApp()throws IOException, JSONException{
        //Takes a sting with a relay name "RELAYLO1-10FAD.relay1"
        //Actuator a = new Actuator("RELAYLO1-12854.relay1");    
        //Starts the virtualhub that is needed to connect to the actuators
        Process process = new ProcessBuilder("src\\actuatorapp\\VirtualHub.exe").start();
        //{"yocto_addr":"10FAD","payload":{"value":true},"type":"control"}
        
        api = new Socket("10.42.72.25",8082);
        OutputStreamWriter osw = new OutputStreamWriter(api.getOutputStream(),StandardCharsets.UTF_8);
        InputStreamReader isr = new InputStreamReader(api.getInputStream(), StandardCharsets.UTF_8);
        
        //Sends JSON authentication to CommandAPI
        JSONObject secret = new JSONObject();
        secret.put("type", "authenticate");
        secret.put("secret", "testpass");
        osw.write(secret.toString()+"\r\n");
        osw.flush();
        System.out.println("sent");
        
        //Waits and recieves JSON authentication response
        BufferedReader br = new BufferedReader(isr);
        JSONObject response = new JSONObject(br.readLine());
        System.out.println(response.toString());
        if(!response.getBoolean("success"))
        {
            System.err.println("Invalid API secret");
            System.exit(1);
        }
        
        try
        {
            while(true)
            {
                //JSON object will contain message from the server
                JSONObject type = getCommand(br);
                //Forward the command to be processed (will find out which actuators to turn on/off)
                commandFromApi(type);
            }
        }
        catch(Exception e)
        {
            System.out.println("Error listening to api");
        } 
        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, JSONException{
       ActuatorApp App = new ActuatorApp();
    }
    
    public JSONObject getCommand(BufferedReader br)
    {
        JSONObject type = new JSONObject();
        try
        {
            System.out.println("Waiting for command...");
            //Waits for a message to be sent and reads that message as a string
            String message = br.readLine();
            System.out.println(message);
            //Converts the message into a JSONObject
            type = new JSONObject(message);
            return type;
        }
        catch(Exception e)
        {
            System.err.println("Error listening to api: " + e);
        }
        return type;
    }
    
    
    public void commandFromApi(JSONObject command) throws JSONException{
        //Takes the command and processes it
        try
        {
            String type = (String) command.get("type");
            if(type.equals("control") == true)
            {
                System.out.println(command);
                String yocto_addr = (String) command.get("yocto_addr");
                JSONObject payload = command.getJSONObject("payload");
                boolean value = (Boolean) payload.getBoolean("value");
                
                setActuatorState(yocto_addr, value);
            }
            else
            {
                System.out.println("Wrong command recieved");
            }
        }
        catch(Exception e)
        {
            System.out.println("Error on command SEND");
        } 
        
    }

    private void setActuatorState(String yocto_addr, boolean value) {
        Actuator a = actuators.get(yocto_addr);
        if (a == null) {
            a = new Actuator("RELAYLO1-" + yocto_addr + ".relay1");
            actuators.put(yocto_addr, a);
            System.out.println("Created actuator");
        }
        else {
            System.out.println("Retrieved actuator");
        }
        
        if (value) {
             System.out.println("Turning on");
             a.turnActuatorON();
        }
        else {
             System.out.println("Turning off");
             a.turnActuatorOFF();
        }
    }
}
