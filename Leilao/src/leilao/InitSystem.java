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
    static List<Product> listaProdutos = new ArrayList<>();
    static PublicKey chave_publica = null;
    static boolean lance = false;
    static char tipo;

    public static void main(String[] args) throws InterruptedException, AWTException, NoSuchAlgorithmException, InvalidKeySpecException, UnknownHostException, IOException {

        int PORT_MULTICAST = 6789;
        String IP_MULTICAST = "228.5.6.7";
        MulticastSocket s = null;
        DatagramSocket socket = null;
        Process process = null;
        Product product = null;
        GeraChave gera_chave = null;
        PrivateKey chave_privada = null;

        InetAddress group = InetAddress.getByName(IP_MULTICAST);
        s = new MulticastSocket(PORT_MULTICAST);
        s.joinGroup(group);
        socket = new DatagramSocket();

        // ********************************************
        // Receve dados do usuário
        String id; // ID do processo.
        String port; // Porta de comunicação Unicast para o produto
        String nomeProduto; // nome do Produto
        String idProduto; // ID do produto
        String descProduto; // descrição do Pruduto
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
       //************************************************
       // Recebe os dados do usuário
        Scanner in = new Scanner(System.in);
        System.out.println("Informe a ID do participante:");
        id = name;

        System.out.println("Informe a porta para comunicação UNICAST:");
        port = strBuilder.toString();

        System.out.println("Informe o nome do produto:");
        nomeProduto = strBuilder.toString();
        
        System.out.println("Informe o id do produto:");
        idProduto = strBuilder.toString();
        
        System.out.println("Informe descricao do produto:");
        descProduto = strBuilder.toString();
        
        System.out.println("Informe o preço do produto:");
        precoProduto = strBuilder.toString();
        
        
        // ********************************************
        // Gera as chaves do processo
        gera_chave = new GeraChave();
        gera_chave.geraChave();
        chave_privada = gera_chave.getChavePrivada();
        chave_publica = gera_chave.getChavePublica();

      
        
        //*********************************************
        //Cria um novo produto
        product = new Product(idProduto, nomeProduto, descProduto, precoProduto, tempoFinal);
        
        //*********************************************
        //Adiciona o produto a lista de produtos
        InitSystem.listaProdutos.add(product);

          //*********************************************
        //Cria um novo processo
        process = new Process(id, port, chave_publica, (ArrayList<Product>) InitSystem.listaProdutos);
        
        //*********************************************
        //Adiciona o processo a lista de processos
        InitSystem.processList.add(process);
     
        //*********************************************
        //Cria o controlador para os lances
        Controle controle = new Controle(idProduto, precoProduto);
        procesosInteresados.add(controle);
        ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
        ObjectOutputStream oos = new ObjectOutputStream(bos);

        oos.writeChar('N');
        oos.writeUTF(id);
        oos.writeUTF(port);
        oos.writeObject(chave_publica);
        oos.writeObject(InitSystem.listaProdutos);
        oos.flush();

        // *********************************************
        // Inicialização da comunicação Multicast
        ReadingThread multCastComm = new ReadingThread(process, IP_MULTICAST, PORT_MULTICAST);
        multCastComm.start();
        
        //**********************************************
        //Inicialização da comunicação Unicast
        UniCastServer uniCastComm = new UniCastServer(process, IP_MULTICAST, PORT_MULTICAST);
        uniCastComm.start();

        // *********************************************
        // Enviando através do multicast as informações sobre o processo
        byte[] m = bos.toByteArray();
        DatagramPacket messageOut = new DatagramPacket(m, m.length, group, PORT_MULTICAST);

        System.out.println("\n[MULTICAST SEND] Enviando a informação sobre este novo processo:");
        System.out.print("[MULTICAST SEND]");
        System.out.print(" ID do participante: " + id);
        System.out.print(", Porta: " + port);
        System.out.print(", Chave publica: - ");
        System.out.print(", nomeProduto: " + process.getListaProdutos().get(0).getName());
        System.out.print(", Tamanho da lista de produtos: " + listaProdutos.size() + "\n");

        s.send(messageOut);

        // *********************************************
        // Menu de interação com o usuário
        while (true) {
            String cmd;

            System.out.println("MENU");
            System.out.println("Pressione a tecla desejada:");
            System.out.println("[B] Dar um lance em um produto ");
            System.out.println("[L] Lista os processos ");
            System.out.println("[E] to Exit");
            cmd = in.nextLine().trim().toUpperCase();
            System.out.println("");

           // Iterator it;
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
                    
                    
                    List<String> nomeProdutos = selecionarProdutosUmProcesso2(paux);
                    
                    System.out.println("Qual o índice desejado?");
                    String stringNomeProd = in.nextLine();
                    
                    //*************************************************
                    //Procura  id para o nome de produto correspondente
                  
                   System.out.println("Produto seleciona: " + paux.listaProdutos.get(Integer.parseInt(stringNomeProd)).getName());
                    String produtoId = paux.listaProdutos.get(Integer.parseInt(stringNomeProd)).getId(); //produto Id do leiloero
                    //******************************************************
                    //Informações do leiloero que estou dando lance
                    String sid = paux.getId();
                    String sport = paux.getPort();
                    PublicKey sPubKey = paux.getChavePublica();
                    String sPreco = paux.listaProdutos.get(Integer.parseInt(stringNomeProd)).getPrecoInicial();
                    
                    
                    System.out.println("Valor Inicial:" + sPreco);
                    System.out.println("Qual valor do seu lance");
                    String lance = in.nextLine();
                    System.out.println("Seu lance:" + lance);

                    // *********************************************
                    //empacotando mensagem apra mandar em unicast
                    ByteArrayOutputStream bos1 = new ByteArrayOutputStream(10);
                    ObjectOutputStream oos1 = new ObjectOutputStream(bos1);
                    oos1.writeChar('B');
                    oos1.writeUTF(process.getId());
                    oos1.writeUTF(process.getPort());
                    oos1.writeUTF(lance);
                    oos1.writeUTF(produtoId);

                    oos1.flush();

                    // Enviando mensagem de lance para o leiloero através do Unicast
                    byte[] output = bos1.toByteArray();
                    DatagramPacket messageOut1 = new DatagramPacket(output, output.length, InetAddress.getLocalHost(), Integer.parseInt(sport));
                    System.out.println("");
                    System.out.print("[UNICAST - ENVIANDO]");
                    System.out.print(" Enviando Lance " + lance + " para o processo de ID igual a " + paux.getId());
                  
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
                    
                    break;          
             
            }
        }
    }
   
    
    
    public static List<String> selecionarProdutosUmProcesso2(Process paux){
          
         List<String> idProdutosPAUmProcesso = null;
        System.out.println("Lista de produtos:");
          int i=0;        
          for( Product p: paux.getListaProdutos() ){
                System.out.println( i+ "- " + "Nome do produto desse processo:" + p.getName());
                i++;
           }         
          
          return idProdutosPAUmProcesso;
          
   }
    
    public static void listarProcessos(){
          System.out.println("List of Process:");
                  
          for( Process p:processList ){
                 System.out.println(p.imprimaProcessos());
           }              
   }
    public static List<String> listarPartipantes( String id){
        
           List<String> nomes = new ArrayList();
          int i=0; 
          
          System.out.println("size: " + processList.size());
          for( Process p:processList ){
              
              if(p.getId().equals(id)){
                 System.out.println("entrar"); 
                  i++;
              }
              
              if(!p.getId().equals(id)){
                   System.out.println(i + "- " +p.imprimaParticipantes());
                   nomes.add(p.getId());
                   i++;
              }
         } 
         return nomes;
   }
     public static int toInt( String numero){

           int i = Integer.parseInt(numero);

        return i;
   }
     
//    public static void listaProdutosUmProcesso(){
//          System.out.println("Lista de Produtos:");
//           int i =0;
//          for( Process p:processList ){
//                    i++;
//                 System.out.println(i+":"+p.getNomeProduto());
//           }              
//   }
//    public static List<String> selecioneProdutosUmProcesso(String nome){
//          System.out.println("Participantes:");
//          List<String> nomes = null;
//          int i =0;
//          for( Process p:processList ){
//              if(!p.getId().equals(nome)){
//                  i++;
//                 System.out.println(i+":"+p.getNomeProduto());
//                   nomes.add(p.getNomeProduto());
//                   
//              }
//         } 
//         return nomes;
//   }

    public static char getTipo() {
        return tipo;
    }

    public static void setTipo(char tipo) {
        InitSystem.tipo = tipo;
    }
    
    public int procuraIndexListaProdutos(String nomeProduto, Process paux){
     
        int index;
            for(Product l : paux.getListaProdutos())
                    {
                        if(l.getName().equals(nomeProduto)){
                          
                        }       
                    }
        
     return -1;               
        
    }
       
        
   
}
