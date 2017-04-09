/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leilao;

import java.awt.AWTException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.management.ManagementFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

/**
 *
 * @author allan
 */
public class InitSystem {

    static ArrayList<Process> processList = new ArrayList<>();
    static List<Controle> procesosInteresados = new ArrayList<>();
    static List<String> produtosLancados = new ArrayList<>();
    static PublicKey chave_publica = null;
    static boolean lance = false;
    static char tipo;

    public static void main(String[] args) throws InterruptedException, AWTException, NoSuchAlgorithmException, InvalidKeySpecException, UnknownHostException, IOException {

        int PORT_MULTICAST = 6789;
        String IP_MULTICAST = "228.5.6.7";
        MulticastSocket s = null;
        DatagramSocket socket = null;
        Process process = null;
        GeraChave gera_chave = null;
        PrivateKey chave_privada = null;

        InetAddress group = InetAddress.getByName(IP_MULTICAST);
        s = new MulticastSocket(PORT_MULTICAST);
        s.joinGroup(group);
        socket = new DatagramSocket();

        // ********************************************
        // Receiving data from user
        String id;
        String port;
        String nomeProduto;
        String idProduto;
        String descProduto;
        String precoProduto;
        String tempoFinal = "120000"; //para que cada produto tenha um tempo de leilão de 2 min

        String name = ManagementFactory.getRuntimeMXBean().getName();

        //instância um objeto da classe Random usando o construtor padrão
        Random gerador = new Random();

        //imprime sequência de 10 números inteiros aleatórios
        int k = gerador.nextInt(8) + 1;
        String com = String.valueOf(k);
        StringBuilder strBuilder = new StringBuilder(com);
        for (int i = 0; i < 3; i++) {
            k = gerador.nextInt(8) + 1;
            strBuilder.append(String.valueOf(k));

        }
       
        Scanner in = new Scanner(System.in);
        System.out.println("Informe o número do participante:");
        id = name;

        System.out.println("Informe a porta para comunicação UNICAST:");
//      port = in.nextLine();
        port = strBuilder.toString();

        System.out.println("Informe o nome do produto:");
//      nomeProduto = in.nextLine();
        nomeProduto = strBuilder.toString();
        System.out.println("Informe o id do produto:");
//       idProduto = in.nextLine();
        idProduto = strBuilder.toString();
        System.out.println("Informe descricao do produto:");
//      descProduto = in.nextLine();
        descProduto = strBuilder.toString();
        System.out.println("Informe o preço do produto:");
//      precoProduto = in.nextLine();
        precoProduto = strBuilder.toString();
        // ********************************************
        // Generating keys for this process
        gera_chave = new GeraChave();
        gera_chave.geraChave();
        chave_privada = gera_chave.getChavePrivada();
        chave_publica = gera_chave.getChavePublica();

        process = new Process(id, port, chave_publica, nomeProduto, idProduto, descProduto, precoProduto);

        InitSystem.processList.add(process);
        Controle controle = new Controle(idProduto, precoProduto);
        procesosInteresados.add(controle);
        ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
        ObjectOutputStream oos = new ObjectOutputStream(bos);

        oos.writeChar('N');
        oos.writeUTF(id);
        oos.writeUTF(port);
        oos.writeObject(chave_publica);
        oos.writeUTF(nomeProduto);
        oos.writeUTF(idProduto);
        oos.writeUTF(descProduto);
        oos.writeUTF(precoProduto);
        oos.flush();

        // *********************************************
        // Initializing multicast and unicast communication
        ReadingThread multCastComm = new ReadingThread(process, IP_MULTICAST, PORT_MULTICAST);
        multCastComm.start();
        UniCastServer uniCastComm = new UniCastServer(process, IP_MULTICAST, PORT_MULTICAST);
        uniCastComm.start();

        // *********************************************
        // Sending multicast notification of its presence.
        byte[] m = bos.toByteArray();
        DatagramPacket messageOut = new DatagramPacket(m, m.length, group, PORT_MULTICAST);

        System.out.println("\n[MULTICAST SEND] Sending information about this new process:");
        System.out.print("[MULTICAST SEND]");
        System.out.print(" ID do participante: " + id);
        System.out.print(", Porta: " + port);
        System.out.print(", Chave publica: - ");
        System.out.print(", Nome produto: " + nomeProduto);
        System.out.println(",ID Produto: " + idProduto);
        System.out.print(",Descricao do produto: " + descProduto);
        System.out.println(",Preco do produto: " + precoProduto);
        s.send(messageOut);

        // *********************************************
        // Interaction phase.
        while (true) {
            String cmd;

            System.out.println("MENU");
            System.out.println("Pressione a tecla desejada:");
            System.out.println("[P] Leiloar produto ");
            System.out.println("[L] Lista os processos ");
            System.out.println("[T] Listar transacoes efetuadas ");
            System.out.println("[E] to Exit");
            cmd = in.nextLine().trim().toUpperCase();
            System.out.println("");

            Iterator it;
            switch (cmd) {

                case "B":
                    // verifica quantidade de processos ativos
                    if (InitSystem.processList.size() < 2) {
                        System.out.println("Menos de dois Processos estao ativos");
                        break;
                    }
                    //recebe id processo
                    System.out.println("Selecione processo voce deseja dar o lance");
                    List<String> nomes = listarPartipantes(id);
                    String n = in.nextLine();
                    
                     // procura por processo 
                    Process paux = null;
                    for (Process p : processList) {
                        if (p.getId().equals(nomes.get(Integer.parseInt(n)))) {
                            paux = p;
                            break;
                        }
                    }
                    System.out.println("Qual o produto?");
                    System.out.println("Produto seleciona:"+paux.getIdProduto());
//                   String produtoId = in.nextLine();
                    String produtoId = paux.getIdProduto();
//                    s.send(messageOut);
      
//                    if (paux == null || !paux.getId().equals(produtoId)) {
//                        System.out.println("Process has not been found or self-buying, try again");
//                        break;
//                    }
                    
                    
                    String sid = paux.getId();
                    String sport = paux.getPort();
                    PublicKey sPubKey = paux.getChavePublica();
                    String sProduto = paux.getIdProduto();
                    String sNomeProducto = paux.getNomeProduto();
                    String sDescProduto = paux.getDescProduto();
                    String sPreco = paux.getPrecoProduto();
                    System.out.println("Valor Inicial:" + sPreco);

                    System.out.println("Qual valor do seu lance");
                    String lance = in.nextLine();

                    System.out.println("Seu lance:" + lance);
//                    if (Integer.parseInt(sPreco) > Integer.parseInt(lance)) {
//                        System.out.println("Seller has not enough coins to sell!");
//                        break;
//                    }

                    // *********************************************
                    //empacotando mensagem apra mandar unicast
                    ByteArrayOutputStream bos1 = new ByteArrayOutputStream(10);
                    ObjectOutputStream oos1 = new ObjectOutputStream(bos1);
                    oos1.writeChar('B');
                    oos1.writeUTF(process.getId());
                    oos1.writeUTF(process.getPort());
                    oos1.writeUTF(lance);
                    oos1.writeUTF(produtoId);

                    oos1.flush();

                    // sending unicast message to seller
                    byte[] output = bos1.toByteArray();
                    DatagramPacket messageOut1 = new DatagramPacket(output, output.length, InetAddress.getLocalHost(), Integer.parseInt(sport));
                    System.out.println("");
                    System.out.print("[UNICAST - SEND]");
                    System.out.print(" Enviando Lance " + paux.getId());
                    System.out.print(" Comprador  " + process.getId());

                    socket.send(messageOut1);
                    break;

                case "L":
                    listarProcessos();
                    
                    break;
                case "E":
                    System.out.println("Bye!");
                    s.leaveGroup(group);
                    s.close();
                    System.exit(0);
                    
                case "V":
                    
                     if(UniCastServer.isTeste()){
                         System.out.println("UniCastServer.isTeste() true");
                     }
                     UniCastServer.teste=true;
                     if(UniCastServer.teste)
                        System.out.println(" UniCastServer.teste=true true");
                     if(UniCastServer.isTeste()){
                         System.out.println("UniCastServer.isTeste() true");
                     }
                     UniCastServer.setTeste(true);
                       if(UniCastServer.isTeste()){
                         System.out.println("UniCastServer.isTeste() true");
                     }
                   break;
                     
                case "C":
                     UniCastServer.teste=true;
                     if(UniCastServer.teste)
                        System.out.println(" UniCastServer.teste=true true");
                     if(UniCastServer.isTeste()){
                         System.out.println("UniCastServer.isTeste() true");
                     }
                     UniCastServer.setTeste(true);
                       if(UniCastServer.isTeste()){
                         System.out.println("UniCastServer.isTeste() true");
                     }
                   break;                
             
            }
        }
    }
   
    
    public static void listarProcessos(){
          System.out.println("List of Process:");
                  
          for( Process p:processList ){
                 System.out.println(p.imprimaProcessos());
           }              
   }
    public static List<String> listarPartipantes( String nome){
        
           List<String> nomes = new ArrayList();    
          for( Process p:processList ){
              if(!p.getId().equals(nome)){
                   System.out.println(p.imprimaParticipantes());
                   nomes.add(p.getId());
                   
              }
         } 
         return nomes;
   }
     public static int toInt( String numero){

           int i = Integer.parseInt(numero);

        return i;
   }
     
    public static void listaProdutosUmProcesso(){
          System.out.println("Lista de Produtos:");
           int i =0;
          for( Process p:processList ){
                    i++;
                 System.out.println(i+":"+p.getNomeProduto());
           }              
   }
    public static List<String> selecioneProdutosUmProcesso(String nome){
          System.out.println("Participantes:");
          List<String> nomes = null;
          int i =0;
          for( Process p:processList ){
              if(!p.getId().equals(nome)){
                  i++;
                 System.out.println(i+":"+p.getNomeProduto());
                   nomes.add(p.getNomeProduto());
                   
              }
         } 
         return nomes;
   }

    public static char getTipo() {
        return tipo;
    }

    public static void setTipo(char tipo) {
        InitSystem.tipo = tipo;
    }
    
   
       
        
   
}
