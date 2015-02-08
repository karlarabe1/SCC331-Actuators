/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package actuatorapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Karl
 */
public class ActuatorApp {
    //Needs to take in secret as argument
    public ActuatorApp()throws IOException, JSONException{
        //Takes a sting with a relay name "RELAYLO1-10FAD.relay1"
        //Actuator a = new Actuator("RELAYLO1-12854.relay1");    
        //Starts the virtualhub that is needed to connect to the actuators
        Process process = new ProcessBuilder("src\\actuatorapp\\VirtualHub.exe").start();
        //Port 8082 is for actuator commands while 8081 is for base station
        Socket api = new Socket("10.42.72.25",8081);
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
        
        new Thread(new listenToApi()).start();    
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, JSONException{
       ActuatorApp App = new ActuatorApp();
    }
    
    private class listenToApi implements Runnable
    {
        public void run()
        {
            try
            {
                Socket api = new Socket("10.42.72.25",8081);
                BufferedReader br = new BufferedReader(new InputStreamReader(api.getInputStream()));
                while(true)
                {    
                    String message = br.readLine();
                    JSONObject type = new JSONObject(message);
                    commandFromApi(type);
                }
            }
            catch(Exception e)
            {
                System.out.println("Error listening to api");
            } 
        }    
    }
    
    public void commandFromApi(JSONObject command) throws JSONException{
        //Takes the command and processes it
        try
        {
            String type = (String) command.get("type");
            if(type.equals("control") == true)
            {
                String yoct_addr = (String) command.get("yoct_addr");
                String state = (String) command.get("value");
                //state is a command for on or off
                if(state.equals("on"))
                {
                    ActuatorOn(yoct_addr);
                }
                else
                {
                    ActuatorOff(yoct_addr);
                }
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
    
    private void ActuatorOn(String yoct_addr)
    {
        //Takes a sting with a relay name "RELAYLO1-10FAD.relay1"
        Actuator a = new Actuator("RELAYLO1-" + yoct_addr + ".relay1");
        if (a.isOn() == true)
        {
            System.out.println("Actuator is already on");
        }
        else
        {
            a.turnActuatorON();
        }
    }
    
    private void ActuatorOff(String yoct_addr)
    {
        //Takes a sting with a relay name "RELAYLO1-10FAD.relay1"
        Actuator a = new Actuator("RELAYLO1-" + yoct_addr + ".relay1");
        if (a.isOn() == false)
        {
            System.out.println("Actuator is already off");
        }
        else
        {
            a.turnActuatorOFF();
        }
    }
}
