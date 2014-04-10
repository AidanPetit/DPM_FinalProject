
import lejos.nxt.*;
import lejos.nxt.comm.*;
import lejos.util.TextMenu;

/**
 * Initialize an LCP responder to handle LCP requests. Allow the
 * User to choose between Bluetooth, USB and RS485 protocols.
 * 
 * This is the code that is run on our SLAVE brick
 * 
 * @param connector NXTCommConnector standard connector interface connect/to wait for connection
 * 
 *  @author Wei-Di Chang, Aidan Petit
 *  @version 3.0
 *  @since 1.0
 */
public class NXTLCPRespond
{
    /*
     * Our local Responder class so that we can override the standard
     * behaviour. We modify the disconnect action so that the thread will
     * exit.
     */
    static class Responder extends LCPResponder
    {
    	//Super constructor
        Responder(NXTCommConnector con)
        {
            super(con);
        }

        protected void disconnect()
        {
            super.disconnect();
            super.shutdown();
        }
    }

    /*
     * This 'main' is what is running on our SLAVE brick
     * It opens and maintains a connection with the MASTER brick
     * to relay sensor data
     */
    public static void main(String[] args) throws Exception
    {
        String[] connectionStrings = new String[]{"Bluetooth", "USB", "RS485"};
        TextMenu connectionMenu = new TextMenu(connectionStrings, 1, "Connection");
        NXTCommConnector[] connectors = {Bluetooth.getConnector(), USB.getConnector(), RS485.getConnector()};

        int connectionType = connectionMenu.select();
        LCD.clear();
        LCD.clear();
        LCD.drawString("Type: " + connectionStrings[connectionType], 0, 0);
        LCD.drawString("Running...", 0, 1);
        Responder resp = new Responder(connectors[connectionType]);
        resp.start();
        resp.join();
        LCD.drawString("Closing...  ", 0, 1);
    }
}
