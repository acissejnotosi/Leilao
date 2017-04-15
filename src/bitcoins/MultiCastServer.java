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
import java.net.SocketException;
import java.security.PublicKey;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Samuel Pelegrinello Caipers
 * Sistemas Distribuidos - Tarefa 01
 * 
 */
public class MultiCastServer extends Thread {
    MulticastSocket s       = null;
    InetAddress group       = null;
    Process process         = null;
    DatagramSocket socket   = null;
    String MULT_IP          = null;
    int MULT_PORT           = 0;
    
    /**
     *
     * @param p Process
     * @param MULT_IP Multicast IP
     * @param MULT_PORT Multicast Port
     */
    public MultiCastServer (Process p, String MULT_IP, int MULT_PORT) throws SocketException {
        this.socket     = new DatagramSocket();
        this.process    = p;
        this.MULT_IP    = MULT_IP;
        this.MULT_PORT  = MULT_PORT;
        
        // ********************************************
        // Inserts this thread into the Multicast Group.
        try {
            group = InetAddress.getByName(MULT_IP);
            s = new MulticastSocket(MULT_PORT);
            s.joinGroup(group);
        } catch (IOException e) {
            System.out.println("Connection:" + e.getMessage());
        }
    }
    
    @Override
    public void run() {
        byte[] buffer;
        char type; // type of message
        DatagramPacket messageIn;
        byte[] msg;
        ByteArrayInputStream bis;
        ObjectInputStream ois;
        
        while (true) {
            try {
                int bid;
                int sid;
                int mid;
                int bport;
                int sport;
                int bAmount;
                long transactionID;
                char status;
                Process paux;

                // ********************************************
                // Receives the multicast message and deserializing it
                buffer = new byte[1024];
                messageIn = new DatagramPacket(buffer, buffer.length);
                s.receive(messageIn);
                
                msg = messageIn.getData();
                bis = new ByteArrayInputStream(msg);
                ois = new ObjectInputStream(bis);
                type = ois.readChar();
                
                switch (type) {
                    // ********************************************
                    // types supported:
                    // N --> New processes (get its Public Key)
                    // M --> For mining and key validation
                    // V --> Transaction confirmation
                    
                    case 'N':
                        int pid = ois.readInt();
                        
                        // if msg id is of the this process, ignores it.
                        if (pid == process.getId()) {
                            break;
                        } else {
                            // *********************************************
                            // Unpacking rest of the message
                            int port = ois.readInt();
                            PublicKey pubKey = (PublicKey) ois.readObject();
                            int coinAmount = ois.readInt();
                            int coinPrice = ois.readInt();
                            
                            // *********************************************
                            // Creating new process and add in the list of process
                            Process newProcess = new Process(pid, port, pubKey, coinAmount, coinPrice);
                            BitCoins.processList.add(newProcess);
                            
                            // *********************************************
                            // Sending to new process its infos.
                            // Packing the message.
                            ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
                            ObjectOutputStream oos = new ObjectOutputStream(bos);
                            oos.writeChar('N');
                            oos.writeInt(process.getId());
                            oos.writeInt(process.getPort());
                            oos.writeObject(process.getPubKey());
                            oos.writeInt(process.getCoinAmount());
                            oos.writeInt(process.getCoinPrice());
                            oos.flush();
                            
                            byte[] output = bos.toByteArray();
                            DatagramPacket messageOut = new DatagramPacket(output, output.length, messageIn.getAddress(), port);
                            System.out.println("");    
                            System.out.print("[MULTICAST - RECEIVE]");
                            System.out.print(" ID: " + pid);
                            System.out.print(", Port: " + port);
                            System.out.print(", Public Key: -Intern-");
                            System.out.print(", Coin Amount: " + coinAmount);
                            System.out.println(", Coin Price: " + coinPrice);
                            
                            System.out.println("");    
                            System.out.print("[UNICAST - SEND]");
                            System.out.print(" Sending to ID " + pid + " these infos:");
                            System.out.print(" ID: " + process.getId());
                            System.out.print(", Port: " + process.getId());
                            System.out.print(", Public Key: -Intern-");
                            System.out.print(", Coin Amount: " + process.getCoinAmount());
                            System.out.println(", Coin Price: " + process.getCoinPrice());
                            
                            socket.send(messageOut);
                            break;
                        }

                    case ('M'):
                        Transaction transaction;
                        // *********************************************
                        // Unpacking rest of the message
                        bid     = ois.readInt();
                        bport   = ois.readInt();
                        sid     = ois.readInt();
                        sport   = ois.readInt();
                        bAmount = ois.readInt();
                        int encryptedLen = ois.readInt();
                        byte[] encryptedText = new byte[encryptedLen];
                        for (int i = 0; i < encryptedLen; i++) {
                            encryptedText[i] = ois.readByte();
                        }
                        transactionID = ois.readLong();
                        
                        // *********************************************
                        // Check this process is seller or buyer
                        // If yes, break; ow, go on.
                        if (process.getId() == bid || process.getId() == sid) {
                            // transaction log for buyers or sellers.
                            transaction = new Transaction(transactionID, bid, sid, bAmount);
                            transaction.setStatus("NC");
                            Process.transactionList.add(transaction);
                            break;
                        }
                        
                        System.out.println("This process is mining now\n");
                        // *********************************************
                        // Those miners who decided to mining will save a
                        // transaction log.
                        transaction = new Transaction(transactionID, bid, sid, bAmount);
                        transaction.setStatus("NC");
                        Process.transactionList.add(transaction);
                        
//                      // *********************************************
//                      // Creating a delay simulating mineration
                        Random random = new Random();
                        long fraction = (long)(4000 * random.nextDouble());
                        int randomnumbr = (int)(fraction + 1000);
                        try{
                            Thread.sleep(randomnumbr);
                        } catch(InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                        
                        // *********************************************
                        // Searching for the seller's process and try to decrypt.
                        paux = null;
                        Iterator it = BitCoins.processList.iterator();
                        while (it.hasNext()) {
                            Process p = (Process) it.next();
                            if (p.getId() == sid) {
                                paux = p;
                            }
                        }
                        
                        if (paux == null) {
                            System.out.println("Seller not found. Seller ID = " + sid);
                            System.exit(1);
                        }
                        
                        // *********************************************
                        // Validation of the transaction.
                        String decrypedText = Keys.decrypt(encryptedText, paux.getPubKey());
                        
                        if (Integer.parseInt(decrypedText) == sid) {
                            System.out.println("Key of Seller not confirmed. ID = " + sid);
                            status = 'I';
                        } else {
                            status = 'C';
                        }
                        
                        System.out.println("");    
                        System.out.print("[MULTICAST - SEND]");
                        System.out.print(" Mining of transaction " + transactionID);
                        System.out.print(" has been completed\n");
                        
                        // *********************************************
                        // Packing transaction validation.
                        ByteArrayOutputStream bos1 = new ByteArrayOutputStream(10);
                        ObjectOutputStream oos1 = new ObjectOutputStream(bos1);
                        oos1.writeChar('V');
                        oos1.writeInt(bid);                 //buyer ID
                        oos1.writeInt(sid);                 //Seller ID
                        oos1.writeInt(bAmount);             //Selling Amount
                        oos1.writeInt(process.getId());     //Miner ID
                        oos1.writeLong(transactionID);      //Transaction ID
                        oos1.writeChar(status);             //Transaction Status
                        oos1.flush();
                        
                        byte[] m1 = bos1.toByteArray();
                        DatagramPacket messageOut = new DatagramPacket(m1, m1.length, group, MULT_PORT);
                        s.send(messageOut);
                            
                        break;
                    case ('V'):
                        Iterator iter;
                        
                        // *********************************************
                        // Unpacking rest of the message
                        bid             = ois.readInt();
                        sid             = ois.readInt();
                        bAmount         = ois.readInt();
                        mid             = ois.readInt();
                        transactionID   = ois.readLong();
                        status          = ois.readChar();
                        
                        // *********************************************
                        // Finding transaction                        
                        Transaction t = null;
                        iter = Process.transactionList.iterator();
                        while (iter.hasNext()) {
                            t = (Transaction) iter.next();
                            if (t.getId() == transactionID) {
                                break;
                            }
                        }
                        
                        if (t.getStatus().equals("C") || t.getStatus().equals("I")) {
                            break;
                        } else {
                            t.setStatus("C");
                            // *********************************************
                            // Printing validation of the process
                            String out;
                            out = "Transaction " + transactionID;
                            out += " has been firstly mined by process " + mid;
                            System.out.println(out);
                        }
                        
                        // *********************************************
                        // Updating DB
                        paux = null;
                        iter = BitCoins.processList.iterator();
                        while (iter.hasNext()) {
                            Process proc = (Process) iter.next();
                            if (proc.getId() == bid) {
                                proc.setCoinAmount(proc.getCoinAmount() + bAmount);
                            }
                            if (proc.getId() == sid) {
                                proc.setCoinAmount(proc.getCoinAmount() - bAmount - BitCoins.MINING_REWARD);
                            }
                            if (proc.getId() == mid) {
                                proc.setCoinAmount(proc.getCoinAmount() + BitCoins.MINING_REWARD);
                            }
                        }
                    break;
                }
            } catch (IOException e) {
                System.out.println("run" + e.getMessage());
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(MultiCastServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
