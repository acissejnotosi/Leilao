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
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import static leilao.Inicial.assinatura;
import static leilao.Inicial.listaProdutos;
import static leilao.Inicial.myChavePrivada;
import static leilao.Inicial.procesosInteresados;
import static leilao.Inicial.processList;

/**
 *
 * @author a1562711
 */
public class ServidorMultiCast extends Thread {

    String leitura1 = " ";
    Scanner scan = new Scanner(System.in);
    MulticastSocket s;
    InetAddress group;
    Processo process = null;
    DatagramSocket socket = null;
    String ipMulticast = null;
    int portMulticast = 0;
    List<String> vivos = new ArrayList<>();
    static boolean vivo;

    public ServidorMultiCast(Processo process, String ipMulticast, int portMulticast) throws SocketException, UnknownHostException, IOException {
        this.socket = new DatagramSocket();
        this.process = process;
        this.ipMulticast = ipMulticast;
        this.portMulticast = portMulticast;

        // ********************************************
        // Inserts this thread into the Multicast Group.
        group = InetAddress.getByName(ipMulticast);
        s = new MulticastSocket(portMulticast);
        s.joinGroup(group);
       

    }

    @Override
    public void run() {

        byte[] buffer;
        char type;
        int k =0;
        DatagramPacket messageIn;
        byte[] msg;
        ByteArrayInputStream bis;
        ObjectInputStream ois;
        Chaves gera_chave = null;
        
     
        
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

                switch (type) {

                    case 'N':
                        String pid = ois.readUTF();

                        if (pid.equals(process.getId())) {
                            break;
                        } else {

                            // *********************************************
                            // Desempacotando o resto da mensagem
                            String port = ois.readUTF();
                            PublicKey chavePublica = (PublicKey) ois.readObject();
                            List<Produto> listaProduto = (ArrayList<Produto>) ois.readObject();

                            // *********************************************
                            // Adicionado lista de Produtos a minha lista de Produtos local
                            adicionaListaDeProdutos(pid, listaProduto);

                            // *********************************************
                            // Criando um novo processo e adicionando na lista de processos
                            Processo novoProcesso = new Processo(pid, port, chavePublica);
                            Inicial.processList.add(novoProcesso);
                            List<Produto> produtos = retornaListadeProdutosdeProcesso(process.getId());
                            // *********************************************
                            // Enviando para o novo processo essas informações
                            // Packing the message.

                            // *********************************************
                            // Guardando Hash Map chavePublica
                            Autenticacao auto = new Autenticacao();
                            auto.setPublic_chave(chavePublica);
                            gera_chave = new Chaves();
                            assinatura.put(pid, auto);

                            ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
                            ObjectOutputStream oos = new ObjectOutputStream(bos);
                            oos.writeChar('N');
                            oos.writeUTF(process.getId());
                            oos.writeUTF(process.getPort());
                            oos.writeObject(process.getChavePublica());
                            oos.writeObject(produtos);
//                            oos.writeInt(tmp.length);
//                            oos.write(tmp);
                            oos.flush();

                            byte[] output = bos.toByteArray();
                            DatagramPacket messageOut = new DatagramPacket(output, output.length, messageIn.getAddress(), Integer.parseInt(port));
                            System.out.println("");
                            System.out.print("[MULTICAST - recebe]");
                            System.out.print(" ID do participante: " + pid);
                            System.out.print(", Porta: " + port);
                            System.out.print(", Chave publica: - ");
                            for (Produto p : listaProduto) {
                                System.out.println("Informações sobre o " + p.getName() + " produto da lista do processo " + pid);
                                System.out.println(", ID Produto " + p.getName() + ": " + p.getId());
                                System.out.println(", Descrição Produto " + p.getName() + ": " + p.getDescricao());
                                System.out.println(", Preço Produto " + p.getName() + ": " + p.getPrecoInicial());
                            }

                            System.out.println("");
                            System.out.print("[UNICAST - envia]");
                            System.out.print(" ID do participante: " + process.getId());
                            System.out.print(", Porta: " + process.getPort());
                            System.out.print(", Chave publica: - ");
                            for (Produto p : retornaListadeProdutosdeProcesso(process.getId())) {
                                System.out.println("Informações sobre o " + p.getName() + " produto da lista do processo " + pid);
                                System.out.println(", ID Produto " + p.getName() + ": " + p.getId());
                                System.out.println(", Descrição Produto " + p.getName() + ": " + p.getDescricao());
                                System.out.println(", Preço Produto " + p.getName() + ": " + p.getPrecoInicial());
                            }
                            socket.send(messageOut);

                            break;
                        }

                    case 'A':
                        // *********************************************

                        String id = ois.readUTF();
                        if (!process.getId().equals(id)) {
                            // Debug recebi  atualizaço de prooduto
                            System.out.print("[MULTICAST - Recebe]");
                            System.out.print("Atualizacao de produto leiloeiro: " + id);

                        }
                        String idProduto = ois.readUTF();
                        String novoValor = ois.readUTF();
                        atualizaValorProduto(idProduto, novoValor);
                        break;

                    case 'R':
                        // *********************************************

                        String vencedorID = ois.readUTF();
                        String produtoID = ois.readUTF();

                        // *********************************************
                        // Atualizando Novo Proprietario                        
                        System.out.print("[MULTICAST - Envia]");
                        System.out.print(" ID do participante: " + vencedorID);
                        atualizaProprientario(vencedorID, produtoID);

                        break;
        
                }

            } catch (IOException ex) {
                Logger.getLogger(ServidorMultiCast.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ServidorMultiCast.class.getName()).log(Level.SEVERE, null, ex);

            }

        }
    }

    public Processo ProcuraProcesso(String id) {
        Processo processo = null;
        for (Processo p : processList) {
            if (p.getId().equals(id)) {
                processo = p;
                break;
            }
        }
        return processo;

    }

    public void processosVivos() {
        boolean tmp = true;

        for (Produto p : listaProdutos) {
            for (String v : vivos) {
                if (p.getId().equals(v)) {
                    tmp = false;
                }
            }
            if (tmp) {
              listaProdutos.remove(p);
            }
            tmp =true;
        }

    }

    public void atualizaValorProduto(String idProduto, String novoValor) {
        for (Produto p : listaProdutos) {
            if (p.getId().equals(idProduto)) {
                p.setPrecoInicial(novoValor);
            }

        }

    }

    public static void atualizaProprientario(String idProcesso, String idProduto) {

        for (Produto p : listaProdutos) {
            if (p.getId().equals(idProduto)) {

                p.setIdProcesso(idProcesso);

            }
        }

    }

    public void adicionaListaDeProdutos(String id, List<Produto> listaProduto) {

        for (Produto p : listaProduto) {
            listaProdutos.add(p);
            Controle controle = new Controle(p.getId(), p.getPrecoInicial());
            procesosInteresados.add(controle);
        }

    }

    public List<Produto> retornaListadeProdutosdeProcesso(String idProcesso) {

        List<Produto> produtos = new ArrayList<>();
        for (Produto p : listaProdutos) {
            if (p.getIdProcesso().equals(idProcesso)) {
                produtos.add(p);
            }

        }
        return produtos;
    }

    public static void retornaListadeProdutosdeProcesso() {

        for (Produto p : listaProdutos) {
            System.out.println("Processo" + p.getIdProcesso() + "produto" + p.getId());

        }
    }

    public static boolean isVivo() {
        return vivo;
    }

    public static void setVivo(boolean vivo) {
        ServidorMultiCast.vivo = vivo;
    }
    

}