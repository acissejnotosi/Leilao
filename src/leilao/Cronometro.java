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
import static leilao.InitSystem.listaProdutos;
import static leilao.InitSystem.procesosInteresados;

/**
 *
 * @author allan
 */
public class Cronometro extends Thread {

    DatagramSocket socket = null;
    Process processo = null;
    String idProduto;
    Process leiloero = null;
    MulticastSocket s= null;
    int MULT_PORT = 0;
   
   InetAddress group = null;
    Cronometro(DatagramSocket socket, String idProduto, Process leiloero, MulticastSocket s, InetAddress group, int MULT_PORT) {
        this.socket = socket;
        this.idProduto = idProduto;
        this.leiloero = leiloero;
        this.s=s;
        this.group= group;
        this.MULT_PORT = MULT_PORT;
    }

    @Override
    public void run() {

        byte[] buffer;
        char type; // type of message
        DatagramPacket messageIn;
        ByteArrayInputStream bis;
        ObjectInputStream ois;

        try {
            int i = 0;
            while (i < 1) {
                i++;
                Thread.sleep(10000);
            }
//              Procurando produtos
            Product product = null;
            for (Product pro : listaProdutos) {
                if (pro.getId().equals(idProduto)) {
                    product = pro;
                    break;
                }

            }
             //remove produto do leiloeiro
            ///Adiciona Produto no processo local
//          leiloero.getListaProdutos().remove(product);
            System.out.println("Tempo de leilao Finalizado!");
            for (Controle c : procesosInteresados) {
                if (c.getProdutoId().equals(idProduto)) {
                    for (Process p : InitSystem.processList) {
                        if (p.getId().equals(c.getUltimo())) {
                            processo = p;
                            break;
                        }
                    }
                }
            }
                // *********************************************
            // 
            ByteArrayOutputStream bos1 = new ByteArrayOutputStream(10);
            ObjectOutputStream oos1 = new ObjectOutputStream(bos1);
            oos1.writeChar('F');
            oos1.writeUTF(leiloero.getId());
            oos1.writeUTF(processo.getId());
            oos1.writeUTF(processo.getPort());
            oos1.writeObject(product);
            oos1.flush();

            byte[] output = bos1.toByteArray();
            DatagramPacket request = new DatagramPacket(output, output.length, InetAddress.getLocalHost(), Integer.parseInt(processo.getPort()));
            System.out.println("");
            System.out.print("[UNICAST - SEND]");
            System.out.print("Vencedor do leilo: " + processo.getId());
            System.out.print("Produto arrematado:" + idProduto);
            socket.send(request);

            System.out.println("");

            System.out.println("");
            System.out.print("[MULTICAST - SEND]");
            System.out.print("Atualiza valores de produto");

             // *********************************************
            // Empacotando mensagem
            ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeChar('R');
            oos.writeUTF(processo.getId());
            oos.writeUTF(product.getId());
            oos.flush();

            byte[] m1 = bos.toByteArray();
            DatagramPacket messageOut = new DatagramPacket(m1, m1.length, group, MULT_PORT);
            s.send(messageOut);
        } catch (InterruptedException e) {
        } catch (IOException ex) {
            Logger.getLogger(Cronometro.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
