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
import java.net.UnknownHostException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import static leilao.InitSystem.processList;

/**
 *
 * @author a1562711
 */
public class ReadingThread extends Thread {

    String leitura1 = " ";
    Scanner scan = new Scanner(System.in);
    MulticastSocket s;
    InetAddress group;
    Process process = null;
    DatagramSocket socket = null;
    String ipMulticast = null;
    int portMulticast = 0;

    public ReadingThread(Process process, String ipMulticast, int portMulticast) throws SocketException, UnknownHostException, IOException {
        this.socket = new DatagramSocket();
        this.process = process;
        this.ipMulticast = ipMulticast;
        this.portMulticast = portMulticast;

        // ********************************************
        // Isere essa thread na transmissão multicast
        group = InetAddress.getByName(ipMulticast);
        s = new MulticastSocket(portMulticast);
        s.joinGroup(group);

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
                buffer = new byte[1024];
                messageIn = new DatagramPacket(buffer, buffer.length);

                s.receive(messageIn);

                msg = messageIn.getData();
                bis = new ByteArrayInputStream(msg);

                ois = new ObjectInputStream(bis);
                type = ois.readChar();
                // ********************************************
                // types supported:
                // N --> Novo participante(pega a chave public dele e distribui)
                // M --> For mining and key validation
                // V --> Transaction confirmation

                switch (type) {

                    case 'N':
                        String pid = ois.readUTF();

                        //**********************************************
                        // Se a mensagem capturada é do próprio ID desse processo,então ela será ignorada.
                        if (pid.equals(process.getId())) {
                            break;
                        } else {
                            // *********************************************
                            // Desempacotando o resto da mensagem
                            String port = ois.readUTF();
                            PublicKey chavePublica = (PublicKey) ois.readObject();
                            List<Product> listaProduto = (ArrayList<Product>) ois.readObject();

                            // *********************************************
                            // Criando um novo processo e adicionando na lista de processos
                            Process novoProcesso = new Process(pid, port, chavePublica, (ArrayList<Product>) listaProduto);
                            InitSystem.processList.add(novoProcesso);

                            // *********************************************
                            // Enviando para o novo processo essas informações
                            // Packing the message.
                            ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
                            ObjectOutputStream oos = new ObjectOutputStream(bos);
                            oos.writeChar('N');
                            oos.writeUTF(process.getId());
                            oos.writeUTF(process.getPort());
                            oos.writeObject(process.getChavePublica());
                            oos.writeObject(process.getListaProdutos());

                            oos.flush();

                            byte[] output = bos.toByteArray();
                            DatagramPacket messageOut = new DatagramPacket(output, output.length, messageIn.getAddress(), Integer.parseInt(port));
                            System.out.println("");
                            System.out.print("[MULTICAST - RECEIVE]");
                            System.out.print(" ID do participante: " + pid);
                            System.out.print(", Porta: " + port);
                            System.out.print(", Chave publica: - ");
                            for (Product p : listaProduto) {
                                System.out.println("Informações sobre o " + p.getName()+ " produto da lista do processo " + pid);
                                System.out.println(", ID Produto " + p.getName()+ ": " + p.getId());
                                System.out.println(", Descrição Produto " + p.getName()+ ": " + p.getDescricao());
                                System.out.println(", Preço Produto " + p.getName() + ": " + p.getPrecoInicial());
                            }

                            System.out.println("");
                            System.out.print("[UNICAST - SEND]");
                            System.out.print(" ID do participante: " + pid);
                            System.out.print(", Porta: " + port);
                            System.out.print(", Chave publica: - ");
                            for (Product p : listaProduto) {
                                System.out.println("Informações sobre o " + p.getName()+ " produto da lista do processo " + pid);
                                System.out.println(", ID Produto " + p.getName()+ ": " + p.getId());
                                System.out.println(", Descrição Produto " + p.getName()+ ": " + p.getDescricao());
                                System.out.println(", Preço Produto " + p.getName() + ": " + p.getPrecoInicial());
                            }
                            socket.send(messageOut);
                            break;
                        }

                    case 'A':
                        // *********************************************
                        // Desempacotando o resto da mensagem e atulizando o resto dos produtos.
                        String id = ois.readUTF(); //id processo
                        String idProduto = ois.readUTF();
                        String novoValor = ois.readUTF();
                        atualizaValorProduto(idProduto, novoValor);

                }

            } catch (IOException ex) {
                Logger.getLogger(ReadingThread.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ReadingThread.class.getName()).log(Level.SEVERE, null, ex);

            }

        }
    }

    public Process ProcuraProcesso(String id) {
        Process processo = null;
        for (Process p : processList) {
            if (p.getId().equals(id)) {
                processo = p;
                break;
            }
        }
        return processo;

    }

    public void atualizaValorProduto(String idProduto, String novoValor) {
        
        for (Product p : process.getListaProdutos()) {
            if (p.getId().equals(idProduto)) {
                p.setPrecoInicial(novoValor);
              
                break;
            }
        }

    }

}
