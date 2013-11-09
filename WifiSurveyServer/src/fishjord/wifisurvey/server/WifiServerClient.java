/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fishjord.wifisurvey.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author fishjord
 */
public abstract class WifiServerClient {
    protected void setupConnection(DataInputStream in, DataOutputStream out, byte type) throws IOException {
        out.writeInt(ClientConnection.MAGIC_NUMBER);
        out.writeByte(ClientConnection.VERSION);

        out.writeByte(type);
    }
}
