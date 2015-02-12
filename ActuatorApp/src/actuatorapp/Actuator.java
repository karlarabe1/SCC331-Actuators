/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package actuatorapp;

/**
 *
 * @author Karl
 */
import com.yoctopuce.YoctoAPI.*;
import com.yoctopuce.YoctoAPI.YRelay;

public class Actuator
{
    //declares a relay variable
    YRelay relay1;
    
    //creates Actuator class and calls method to setup virtual hub on local computer
    //Takes a sting with a relay name "RELAYLO1-10FAD.relay1"
    public Actuator(String actuatorName)
    {
        setRelays(actuatorName);
    }
    
    //sets up hub API on local computer and returns relay connected to USB port
    public void setRelays(String name)
    {
        try
        {
            YAPI.RegisterHub("127.0.0.1");
            //finds & returns relay with specified address if connected to USB port
            relay1 = YRelay.FindRelay(name);
        }
        catch(YAPI_Exception e)
        {
            System.out.println("cannot contact virtual hub");
        }
    }
    
    public void turnActuatorON()
    {
        //turns actuators connected to relay1 ON by setting its state to 1
        try 
        {
            relay1.setState(1);
            int state = relay1.getState();
            if (state != 1)
            {
                System.out.println("Failed to turn actuator on");
                turnActuatorON();
            }
        }
        catch(YAPI_Exception e) 
        {
            System.out.println("Can't turn actuator ON");
        }
    }
    
    public void turnActuatorOFF()
    {
        //turns actuator connected to relay1 OFF by setting its state to 0
        try 
        {
            relay1.setState(0);
            int state = relay1.getState();
            if (state != 0)
            {
                System.out.println("Failed to turn actuator off");
                turnActuatorON();
            }
        }
        catch(YAPI_Exception e) 
        {
            System.out.println("Can't turn actuator OFF");
        }
    }
    
    public boolean isOn()
    {
        //gets the current status of the actuator either on or off
        try 
        {
            if(relay1.getState() == 1)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        catch(YAPI_Exception e) 
        {
            System.out.println("Cant get State");
        }
        return false;
    }
}
