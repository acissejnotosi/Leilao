/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leilao;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.logging.Level;
import java.util.logging.Logger;
import static leilao.Inicial.listaProdutos;
import static leilao.Inicial.processList;
import static leilao.ServidorMultiCast.isVivo;

/**
 *
 * @author allan
 */
public class WatchDog extends Thread {

    DatagramSocket socket = null;
    InetAddress localHost = null;

    WatchDog(DatagramSocket socket, InetAddress localHost) {
     this.socket= socket;
     this.localHost= localHost;
    }

    public void run() {

        byte[] buffer;
        char type; // type of message
        DatagramPacket messageIn;
        ByteArrayInputStream bis;
        ObjectInputStream ois;

        int i = 0;
        while (true) {
            i++;
            try {
                Thread.sleep(1000);

  
                for (Processo p : processList) { 
                    // *********************************************
                    // Empacado mensagem vivo
                    ByteArrayOutputStream bos1 = new ByteArrayOutputStream(10);
                    ObjectOutputStream oos1 = new ObjectOutputStream(bos1);
                    oos1.writeChar('W');
                    oos1.writeUTF(p.getId());
                    oos1.flush();
                    byte[] output = bos1.toByteArray();
                    DatagramPacket request = new DatagramPacket(output, output.length, localHost, Integer.parseInt(p.getPort()));
                    socket.send(request);
                    System.out.println("Envien watch");
                    Thread.sleep(100);
                    if(isVivo()){
                        System.out.println("Estavivo");
                    }else{
                        System.out.println("deletaar da lista");
                    }
                    
                }
               


            } catch (IOException ex) {
                Logger.getLogger(WatchDog.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(WatchDog.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }

}
