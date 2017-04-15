package bitcoins;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

/**
 *
 * @author Samuel Pelegrinello Caipers
 * Sistemas Distribuidos - Tarefa 01
 * 
 */
public class BitCoins {
    static ArrayList<Process> processList = new ArrayList<>();
    static PrivateKey privKey             = null;
    static int MINING_REWARD              = 1;
    
    public static void main(String[] args) throws UnknownHostException, IOException {
        int MULT_PORT           = 6789;
        String MULT_IP          = "228.5.6.7";
        MulticastSocket s       = null;
        DatagramSocket socket   = null;
        Process process         = null;
        Keys keys               = null;
        PublicKey pubKey        = null;
        
        try {
            // ********************************************
            // Initialization of the Multicast's group
            InetAddress group = InetAddress.getByName(MULT_IP);
            s = new MulticastSocket(MULT_PORT);
            s.joinGroup(group);
            socket = new DatagramSocket();

            // ********************************************
            // Receiving data from user
            int id;
            int port;
            int coinPrice;
            int coinAmount = 100;
            
            Scanner in = new Scanner(System.in);
            System.out.println("Inform the ID of this process:");
            id = Integer.parseInt(in.nextLine().trim());
            
            System.out.println("Inform the port of communication:");
            port = Integer.parseInt(in.nextLine().trim());
           
            System.out.println("Inform the price of coins:");
            coinPrice = Integer.parseInt(in.nextLine().trim());
            
            // ********************************************
            // Generating keys for this process
            keys = new Keys();
            privKey = keys.getPriv();
            pubKey = keys.getPub();
             
            // ********************************************
            // Packeting all information in a process' object
            // Serializing the process' object
            // N --> New process
            process = new Process(id, port, pubKey, coinAmount, coinPrice);
            BitCoins.processList.add(process);
            
            ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            
            oos.writeChar('N');
            oos.writeInt(id);
            oos.writeInt(port);
            oos.writeObject(pubKey);
            oos.writeInt(coinAmount);
            oos.writeInt(coinPrice);
            oos.flush();
            
            // *********************************************
            // Initializing multicast and unicast communication
            MultiCastServer multCastComm = new MultiCastServer(process, MULT_IP, MULT_PORT);
            multCastComm.start();
            UniCastServer uniCastComm = new UniCastServer(process, MULT_IP, MULT_PORT);
            uniCastComm.start();
            
            // *********************************************
            // Sending multicast notification of its presence.
            byte[] m = bos.toByteArray();
            DatagramPacket messageOut = new DatagramPacket(m, m.length, group, MULT_PORT);
 
            System.out.println("\n[MULTICAST SEND] Sending information about this new process:");
            System.out.print("[MULTICAST SEND]");
            System.out.print(" ID: " + id);
            System.out.print(", Port: " + port);
            System.out.print(", Public Key: Intern");
            System.out.print(", Coin Amount: " + coinAmount);
            System.out.println(", Coin Price: " + coinPrice);
            s.send(messageOut);
            
            // *********************************************
            // Interaction phase.
            while (true) {
                String cmd;
                
                System.out.println("");
                System.out.println("Press: ");
                System.out.println("[B] to buy coins ");
                System.out.println("[V] to List Of Processes ");
                System.out.println("[T] to List Of Transactions ");
                System.out.println("[E] to Exit");
                
                cmd = in.nextLine().trim().toUpperCase();
                System.out.println("");
                
                Iterator it;
                switch (cmd) {
                    
                    case "B":
                        // check if there are more than 2 active processes
                        if (BitCoins.processList.size() <= 2) {
                            System.out.println("Must have at least three ative processes to buy coins");
                            break;
                        }
                        
                        int cmdBId;
                        System.out.println("Of what process do you wish to buy?");
                        cmdBId = Integer.parseInt(in.nextLine().trim());
                        System.out.println("");
                        
                        // *********************************************
                        // check if this process ID exists
                        Process paux = null;
                        it = BitCoins.processList.iterator();
                        while (it.hasNext()) {
                            Process p = (Process) it.next();
                            if (p.getId() == cmdBId && cmdBId != process.getId()) {
                                paux = p;
                            }
                        }
                        
                        if (paux == null) {
                            System.out.println("Process has not been found or self-buying, try again");
                            break;
                        }
                        
                        System.out.println("Process has been found");
                        
                        // *********************************************
                        // getting seller information
                        int sid             = paux.getId();
                        int sport           = paux.getPort();
                        PublicKey sPubKey   = paux.getPubKey();
                        int sCoinAmount     = paux.getCoinAmount();
                        int sCoinPrice      = paux.getCoinPrice();
                        
                        System.out.println("How much coins do you want to buy?");
                        int bAmount = Integer.parseInt(in.nextLine().trim());
                        
                        // checking if seller has enough coins to sell + mining reward
                        if (bAmount + MINING_REWARD > sCoinAmount) {
                            System.out.println("Seller has not enough coins to sell!");
                            break;
                        }
                        
                        // *********************************************
                        // packing unicast message to seller
                        ByteArrayOutputStream bos1 = new ByteArrayOutputStream(10);
                        ObjectOutputStream oos1 = new ObjectOutputStream(bos1);
                        oos1.writeChar('B');
                        oos1.writeInt(process.getId());
                        oos1.writeInt(process.getPort());
                        oos1.writeInt(bAmount);
                        oos1.flush();
                        
                        // sending unicast message to seller
                        byte[] output = bos1.toByteArray();
                        DatagramPacket messageOut1 = new DatagramPacket(output, output.length, InetAddress.getLocalHost(), sport);
                        System.out.println("");    
                        System.out.print("[UNICAST - SEND]");
                        System.out.print(" Sending buy request to seller process " + paux.getId());
                        System.out.print(", Buyer process " + process.getId());
                        System.out.println(", Coin Amount " + bAmount);
                        
                        socket.send(messageOut1);
                        break;
                        
                    case "V":
                        System.out.println("List of Process:");
                        it = BitCoins.processList.iterator();
                        while (it.hasNext()) {
                            Process p = (Process) it.next();
                            System.out.println(p.printProcess());
                        }
                        break;
                    
                    case "T":
                        System.out.println("Print Transactions:");
                        Transaction t = null;
                        Iterator iter = Process.transactionList.iterator();
                        while (iter.hasNext()) {
                            t = (Transaction) iter.next();
                            t.printTransaction();
                        }
                        break;
                        
                    case "S":
                        System.out.println("Bye!");
                        s.leaveGroup(group);
                        s.close();
                        System.exit(0);
                }
            }
        } catch (SocketException e) {
            System.out.println("Socket error: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO error: " + e.getMessage());
        } finally {
            if (s != null) {
                s.close();
            }
        }                    
    }
}

class Process implements Serializable {
    private final int id;
    private final int port;
    private int coinAmount;
    private final int coinPrice;
    private final PublicKey pubKey;
    static ArrayList<Transaction> transactionList = new ArrayList<>();
    
    public Process(int id, int port, PublicKey pubKey, int coinAmount, int coinPrice) {
        this.id             = id;
        this.port           = port;
        this.pubKey         = pubKey;
        this.coinAmount     = coinAmount;
        this.coinPrice      = coinPrice;
    }
    
    public void setCoinAmount(int coinAmount) {
        this.coinAmount = coinAmount;
    }
    
    public String printProcess() {
        return "ID: " + id + ", Port: " + port + ", Coin Amount: " + coinAmount + ", Coin Price: " + coinPrice;
    }
    
    public int getId() {
        return id;
    }
    
    public int getPort() {
        return port;
    }
    
    public int getCoinAmount() {
        return coinAmount;
    }
    
    public int getCoinPrice() {
        return coinPrice;
    }
    
    public PublicKey getPubKey() {
        return pubKey;
    }
}