package bitcoins;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.security.PublicKey;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Samuel Pelegrinello Caipers
 * Sistemas Distribuidos - Tarefa 01
 * 
 */
public class UniCastServer extends Thread {
    DatagramSocket socket = null;
    MulticastSocket s     = null;
    InetAddress group     = null;
    Process process       = null;
    String MULT_IP        = null;
    int MULT_PORT         = 0;
    
    /*
    * @param p Process
    * @param MULT_IP Multicast IP
    * @param MULT_PORT Multicast Port
    */
    public UniCastServer(Process p, String MULT_IP, int MULT_PORT) {
        this.process    = p;
        this.MULT_IP    = MULT_IP;
        this.MULT_PORT  = MULT_PORT;
        
        // ********************************************
        // Creates the UDP Socket in the port of the process.
        try {
            socket = new DatagramSocket(process.getPort());
        } catch (IOException ex) {
            System.out.println("Creation of socket: " + ex);
        }
        
        // ********************************************
        // Joining the multicast group.
        try {
            group = InetAddress.getByName(MULT_IP);
            s = new MulticastSocket(MULT_PORT);
            s.joinGroup(group);
        } catch (IOException e) {
            System.out.println("Joining Multicast:" + e.getMessage());
        }
    }
    
    @Override
    public void run() {
        byte[] buffer;
        char type; // type of message
        DatagramPacket messageIn;
        ByteArrayInputStream bis;
        ObjectInputStream ois;
        
        while (true) {
            try {
                int id;
                int port;
                PublicKey pubKey;
                int coinAmount;
                int coinPrice;
                
                // ********************************************
                // Receiving an UDP message
                buffer = new byte[1024];
                messageIn = new DatagramPacket(buffer, buffer.length);
                socket.receive(messageIn);
                
                bis = new ByteArrayInputStream(buffer);
                ois = new ObjectInputStream(bis);
                type = ois.readChar();
                
                switch (type) {
                    // ********************************************
                    // types supported:
                    // N --> Information of others process.
                    // B --> Buying request
                    
                    case ('N'): 
                        // *********************************************
                        // Unpacking rest of the message
                        id = ois.readInt();
                        port = ois.readInt();
                        pubKey = (PublicKey) ois.readObject();
                        coinAmount = ois.readInt();
                        coinPrice = ois.readInt();
                        
                        // *********************************************
                        // Creating new process and add in the list of process
                        Process newProcess = new Process(id, port, pubKey, coinAmount, coinPrice);
                        BitCoins.processList.add(newProcess);
                        
                        System.out.println("");
                        System.out.print("[UNICAST - RECEIVE]");
                        System.out.print(", ID: " + id);
                        System.out.print(", Port: " + port);
                        System.out.print(", Public Key: Intern");
                        System.out.print(", Coin Amount: " + coinAmount);
                        System.out.println(", Coin Price: " + coinPrice);
                        break;
                        
                    case ('B'):
                        // *********************************************
                        // Unpacking rest of the message
                        id = ois.readInt();
                        port = ois.readInt();
                        int bAmount = ois.readInt();
                        
                        System.out.println("");    
                        System.out.print("[UNICAST - RECEIVE]");
                        System.out.println(" Buying Request from process: " + id);
                        
                        // *********************************************
                        // Encrypting buyer's ID with Seller's Private Key
                        String text = "" + id;
                        byte[] encryptedText = Keys.encrypt(text, BitCoins.privKey);
                        
                        // *********************************************
                        // Packing mining and validation message.
                        Timestamp date = new Timestamp(System.currentTimeMillis());
                        long ts = date.getTime();
                        
                        ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
                        ObjectOutputStream oos = new ObjectOutputStream(bos);
                        oos.writeChar('M');
                        oos.writeInt(id);
                        oos.writeInt(port);
                        oos.writeInt(process.getId());
                        oos.writeInt(process.getPort());
                        oos.writeInt(bAmount);
                        oos.writeInt(encryptedText.length);
                        oos.write(encryptedText);
                        oos.writeLong(ts);
                        oos.flush();
                        
                        // Converte o objeto para uma array de bytes e envia por datagrama
                        byte[] msg = bos.toByteArray();
                        DatagramPacket messageOut = new DatagramPacket(msg, msg.length, group, MULT_PORT);
                        
                        System.out.print("[MULTICAST SEND]");
                        System.out.print(" Sending mining and validation request");
                        System.out.print(" Buyer ID: " + id);
                        System.out.print(", Seller ID: " + process.getId());
                        System.out.print(", Coin Amount: " + bAmount);
                        System.out.println(", Transaction ID: "+ ts);
                        System.out.println("");
                        s.send(messageOut);
                        break;
                }
            } catch (IOException ex) {
                System.out.println("Unicast Exception");
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(UniCastServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
