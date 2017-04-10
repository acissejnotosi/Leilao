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
import java.net.SocketException;
import java.security.PublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;
import static leilao.InitSystem.procesosInteresados;

/**
 *
 * @author allan
 */
public class Cronometro extends Thread {
    
    DatagramSocket socket = null;
    Process processo =null;
    String idProduto;

    Cronometro(DatagramSocket socket, String idProduto ) {
       this.socket = socket;
       this.idProduto = idProduto;
            
    }
    @Override
    public void run() {
        
        byte[] buffer;
        char type; // type of message
        DatagramPacket messageIn;
        ByteArrayInputStream bis;
        ObjectInputStream ois;
           System.out.println("passei");
            try { 
                int i=0;
                while (i<3) {          
                    i++;
                    Thread.sleep(20000); 
                }
                
                for(Controle c:  procesosInteresados){
                    if (c.getProdutoId().equals(idProduto)) {
                            for(Process p: InitSystem.processList){
                                if(p.getId().equals(c.getLancadorId())){ 
                                    processo =p;
                                    break;
                                }   
                            }
                        }   
                    }
                 System.out.println("saii");
                 ByteArrayOutputStream bos1 = new ByteArrayOutputStream(10);
                 ObjectOutputStream oos1 = new ObjectOutputStream(bos1);
                 oos1.writeChar('F');
                 oos1.writeUTF(processo.getId());
                 oos1.writeUTF(processo.getPort());
                 oos1.writeUTF(processo.getListaProdutos().get(Integer.parseInt(idProduto)).getName());
                 oos1.flush();
                
                byte[] output = bos1.toByteArray();
                DatagramPacket request = new DatagramPacket(output, output.length,InetAddress.getLocalHost(), Integer.parseInt(processo.getPort()));
                System.out.println("");    
                System.out.print("[UNICAST - SEND]");
                System.out.print("Vencedor do leilo: " + processo.getId());
                System.out.print("Produto arrematado:" + processo.getListaProdutos().get(Integer.parseInt(idProduto)).getName());     
                socket.send(request);
                System.out.println("");
            }
              
            catch (InterruptedException e) {  
            } catch (IOException ex) {  
            Logger.getLogger(Cronometro.class.getName()).log(Level.SEVERE, null, ex);
        }  
        }  
  
    
}
