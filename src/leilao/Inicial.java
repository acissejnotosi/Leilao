/*
 * 
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
 * Autores: Allan e Jessica
 */
public class Inicial {

    static ArrayList<Processo> processList = new ArrayList<>();
    static List<Controle> procesosInteresados = new ArrayList<>();
    static List<String> produtosLancados = new ArrayList<>();
    static List<Produto> listaProdutos = new ArrayList<>();
    static Map<String, Autenticacao> assinatura = new HashMap<String, Autenticacao>();
    static PublicKey mychavePublica = null;
    static PrivateKey myChavePrivada = null;
    static boolean lance = false;
    static char tipo;

    public static void main(String[] args) throws InterruptedException, AWTException, NoSuchAlgorithmException, InvalidKeySpecException, UnknownHostException, IOException {

        int PORT_MULTICAST = 6789;
        String IP_MULTICAST = "228.5.6.7";
        MulticastSocket s = null;
        DatagramSocket socket = null;
        Processo process = null;
        Produto product = null;
        Chaves gera_chave = null;
        PrivateKey chave_privada = null;

        //********************************************
        //
        InetAddress group = InetAddress.getByName(IP_MULTICAST);
        s = new MulticastSocket(PORT_MULTICAST);
        s.joinGroup(group);
        socket = new DatagramSocket();

        // ********************************************
        // Recebe dados do usuário
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

        //imprime sequência de 4 números inteiros aleatórios
//        int k = gerador.nextInt(8) + 1;
//        String com = String.valueOf(k);
//        StringBuilder strBuilder = new StringBuilder(com);
//        for (int i = 0; i < 3; i++) {
//            k = gerador.nextInt(8) + 1;
//            strBuilder.append(String.valueOf(k));
//
//        }


        Scanner in = new Scanner(System.in);
        System.out.println("Informe o NOME do participante:");
        id =  in.nextLine();

        System.out.println("Informe a PORTA para comunicação UNICAST:");
        port = in.nextLine();
//        port = strBuilder.toString();

        System.out.println("Informe o NOME do produto:");
        nomeProduto = in.nextLine();
//        nomeProduto = strBuilder.toString();
        System.out.println("Informe o ID do produto:");
        idProduto = in.nextLine();
//        idProduto = strBuilder.toString();
        System.out.println("Informe DESCRIÇÃO do produto:");
        descProduto = in.nextLine();
//        descProduto = strBuilder.toString();
        System.out.println("Informe o PREÇO do produto:");
        precoProduto = in.nextLine();
//        precoProduto = strBuilder.toString();

        // ********************************************
        // Gera as chaves pra este processo.
        gera_chave = new Chaves();
        gera_chave.geraChave();
        myChavePrivada = gera_chave.getChavePrivada();
        mychavePublica = gera_chave.getChavePublica();

        //*********************************************
        //Cria um novo processo.
        process = new Processo(id, port, mychavePublica);

        //*********************************************
        //Adiciona o processo a lista de processos.
        Inicial.processList.add(process);

        //*********************************************
        //Cria um novo produto.
        product = new Produto(idProduto, nomeProduto, descProduto, precoProduto, tempoFinal, id);

        //*********************************************
        //Adiciona o produto a lista de produtos.
        Inicial.listaProdutos.add(product);

        Controle controle = new Controle(idProduto, precoProduto);
        procesosInteresados.add(controle);
        ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
        ObjectOutputStream oos = new ObjectOutputStream(bos);

        //*********************************************
        //Tipo N refere-se a dados enviados sobre este processo.
        oos.writeChar('N');
        oos.writeUTF(id);
        oos.writeUTF(port);
        oos.writeObject(mychavePublica);
        oos.writeObject(Inicial.listaProdutos);
        oos.flush();

        // *********************************************
        // Inicialização da comunicação Multicast
        ServidorMultiCast multCastComm = new ServidorMultiCast(process, IP_MULTICAST, PORT_MULTICAST);
        multCastComm.start();

        //**********************************************
        //Inicialização da comunicação Unicast
        ServidorUniCast uniCastComm = new ServidorUniCast(process, IP_MULTICAST, PORT_MULTICAST);
        uniCastComm.start();

        // *********************************************
        // Enviando através do multicast as informações sobre o processo
        byte[] m = bos.toByteArray();
        DatagramPacket messageOut = new DatagramPacket(m, m.length, group, PORT_MULTICAST);

        //***********************************************
        //Verificação se a mensagem está sendo enviada.
        System.out.println("\n[MULTICAST enviando] Enviando a informação sobre este novo processo:");
        System.out.print("[MULTICAST enviando]");
        System.out.print(" ID do participante: " + id);
        System.out.print(", Porta: " + port);
        System.out.print(", Chave publica: - ");
        System.out.print(", nomeProduto: " + listaProdutos.get(0).getName());
        System.out.print(", Tamanho da lista de produtos: " + listaProdutos.size() + "\n");
        s.send(messageOut);

        // *********************************************
        // Fase de interação do processo sobre o sistema.
        while (true) {
            String cmd;

            System.out.println("MENU");
            System.out.println("Pressione a tecla desejada:");
            System.out.println("[B] Dar um lance em um produto ");
            System.out.println("[L] Lista os processos ");
            System.out.println("[P] Anunciar um produto em leilão "); 
            System.out.println("[T] Lista dos leilões ");
            System.out.println("[S] Sair do programa 'Leilao' ");
            cmd = in.nextLine().trim().toUpperCase();
            System.out.println("");

            Iterator it;
            switch (cmd) {

                case "B":
                    // *******************************************
                    //Verifica quantidade de processos ativos
                    if (Inicial.processList.size() < 2) {
                        System.out.println("Atenção menos de dois Processos estao ativos");
                        break;
                    }
                    //recebe id processo
                    System.out.println("");
                    System.out.println("Atenção! Selecione UM ÍNDICE do processo que voce deseja dar o lance:");
                    List<String> nomes = listarPartipantes(id);
                    String n = in.nextLine();

                    //***********************************************************
                    // Procura pelo processo solicitado e salva na variável paux.
                    Processo paux = null;
                    for (Processo p : processList) {
                        if (p.getId().equals(nomes.get(Integer.parseInt(n)))) {
                            System.out.println("Nome processo:" + p.getId());
                            System.out.println("Nome processo igual: " + nomes.get(Integer.parseInt(n)));
                            paux = p;
                            break;
                        }
                    }

                    //*****************************************************************************
                    //Lista os produtos da lista do processo escolhido para se comprar um produto
                    List<String> nomeProdutos = selecionarProdutosUmProcesso(paux);
                    if (nomeProdutos.isEmpty()) {
                        System.out.println("Desculpe, O processo selecionado não está leiloando nenhum produto!");
                        break;
                    }
                    System.out.println("- Escolha um INDICE do produto desejado da lista a seguir:");
                    String stringNomeProd = in.nextLine();

                    //*************************************************
                    //Procura  id para o nome de produto correspondente
                    Produto produto = buscaUmProdutoPorId(nomeProdutos.get(Integer.parseInt(stringNomeProd)));
                    System.out.println("- Produto seleciona: " + produto.getName());
                    String produtoId = produto.getId(); //produto Id do leiloero
                    //******************************************************
                    //Informações do leiloero que estou dando lance
                    String sid = paux.getId();
                    String sport = paux.getPort();
                    PublicKey sPubKey = paux.getChavePublica();
                    String sPreco = produto.getPrecoInicial();

                    System.out.println("- Valor atual do produto: " + sPreco);
                    System.out.println("- Digite o valor do seu lance:");
                    String lance = in.nextLine();
                    System.out.println("- Seu lance foi de:" + lance + " reais ");
                    
                    // *********************************************
                    // encriptografando meu nome com minha privda
                     byte[] encryptedText = gera_chave.criptografa("kkkk",myChavePrivada);
            
  
                    // *********************************************
                    //empacotando mensagem apra mandar em unicast
                    ByteArrayOutputStream bos1 = new ByteArrayOutputStream(10);
                    ObjectOutputStream oos1 = new ObjectOutputStream(bos1);
                    oos1.writeChar('B');
                    oos1.writeUTF(process.getId());
                    oos1.writeUTF(process.getPort());
                    oos1.writeUTF(lance);
                    oos1.writeUTF(produtoId);
                    oos1.write(encryptedText.length);
                    oos1.write(encryptedText);
                    oos1.flush();

                    //*****************************************************
                    // Enviando a mensagem Unicast para o vendedor
                    byte[] output = bos1.toByteArray();
                    DatagramPacket messageOut1 = new DatagramPacket(output, output.length, InetAddress.getLocalHost(), Integer.parseInt(sport));
                    System.out.println("");
                    System.out.println("[UNICAST - Envia]");
                    System.out.print(" Enviando Lance " + paux.getId());
                    System.out.print(" para o comprador  " + process.getId());

                    socket.send(messageOut1);
                    break;

                case "L":
                    //**********************************************
                    //Lista os processos da lista de processos
                    listarProcessos();

                    break;
                case "E":
                    //**********************************************
                    //Sai do programa
                    System.out.println("Bye!");
                    s.leaveGroup(group);
                    s.close();
                    System.exit(0);

                    break;

            }
        }
    }
    


    public static void listarProcessos() {
        System.out.println("Lista de Processos:");

        for (Processo p : processList) {
            System.out.println(p.imprimaProcessos());
        }
    }

    public static List<String> listarPartipantes(String nome) {

        List<String> nomes = new ArrayList();
        int i = 0;
        for (Processo p : processList) {
            if (!p.getId().equals(nome)) {
                System.out.println(i + "-" + p.imprimaParticipantes());
                nomes.add(p.getId());
                i++;

            }
        }
        return nomes;
    }

    public static int toInt(String numero) {

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
        Inicial.tipo = tipo;
    }

//    public static List<String> selecionarProdutosUmProcesso2(Process paux) {
//
//        List<String> idProdutosPAUmProcesso = null;
//        System.out.println("Lista de produtos:");
//        int i = 0;
//        for (Product p : paux.getListaProdutos()) {
//            System.out.println(i + "- " + "Nome do produto desse processo:" + p.getName());
//            i++;
//        }
//
//        return idProdutosPAUmProcesso;
//
//    }
    public static List<String> selecionarProdutosUmProcesso(Processo paux) {

        List<String> idProdutosPAUmProcesso = new ArrayList<>();
        System.out.println("Lista de produtos:");
        int i = 0;
        for (Produto p : listaProdutos) {
            if (p.getIdProcesso().equals(paux.getId())) {
                System.out.println(i + "- " + "Nome do produto desse processo:" + p.getName());
                idProdutosPAUmProcesso.add(p.getId());
                i++;
            }

        }

        return idProdutosPAUmProcesso;
    }

    public static Produto buscaUmProdutoPorId(String id) {

        Produto pro = null;
        for (Produto p : listaProdutos) {
            if (p.getId().equals(id)) {
                pro = p;
            }

        }
        return pro;
    }
}
