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
import java.security.PublicKey;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static leilao.InitSystem.chave_publica;
import static leilao.InitSystem.procesosInteresados;
import static leilao.InitSystem.processList;
import static leilao.InitSystem.produtosLancados;
import org.omg.PortableServer.LifespanPolicy;

/**
 *
 * @author Jessica
 */
public class UniCastServer extends Thread {

    static boolean teste = false;
    DatagramSocket socket = null;
    MulticastSocket s = null;
    InetAddress group = null;
    Process process = null;
    String MULT_IP = null;
    int MULT_PORT = 0;

    /*
    * @param p Process
    * @param MULT_IP Multicast IP
    * @param MULT_PORT Multicast Port
     */
    public UniCastServer(Process p, String MULT_IP, int MULT_PORT) {
        this.process = p;
        this.MULT_IP = MULT_IP;
        this.MULT_PORT = MULT_PORT;

        // ********************************************
        // Creates the UDP Socket in the port of the process.
        try {
            socket = new DatagramSocket(Integer.parseInt(process.getPort()));
        } catch (IOException ex) {
            System.out.println("Creation of socket: " + ex);
        }

        // ********************************************
        // Joining the multicast group.
        try {
            group = InetAddress.getByName(MULT_IP);
            s = new MulticastSocket(MULT_PORT);
            s.joinGroup(group);
        } catch (IOException e) {
            System.out.println("Joining Multicast:" + e.getMessage());
        }
    }

    @Override
    public void run() {

        byte[] buffer;
//        char type; // type of message
        DatagramPacket messageIn;
        ByteArrayInputStream bis;
        ObjectInputStream ois;

        while (true) {

            try {
                String pid;
                String port;
                char type;
                String idProduto;

                // ********************************************
                // Receiving an UDP message
                buffer = new byte[1024];
                messageIn = new DatagramPacket(buffer, buffer.length);
                socket.receive(messageIn);

                bis = new ByteArrayInputStream(buffer);
                ois = new ObjectInputStream(bis);
                type = ois.readChar();
//                InitSystem.setTipo(ois.readChar()); 

                switch (type) {
                    // ********************************************
                    // types supported:
                    // N --> Information of others process.
                    // B --> Buying request

                    case ('N'):
                        // *********************************************
                        // Descompactando messagem
                        pid = ois.readUTF();
                        port = ois.readUTF();
                        PublicKey chavePublica = (PublicKey) ois.readObject();
                        List<Product> listaProduto = (ArrayList<Product>) ois.readObject();

                        // *********************************************
                        // Cria um novo processo e adiciona a lista de processos
                        Process novoProcesso = new Process(pid, port, chavePublica, (ArrayList<Product>) listaProduto);
                        InitSystem.processList.add(novoProcesso);

                        System.out.println("");
                        System.out.print("[UNICAST - RECEIVE]");
                        System.out.print(" ID do participante: " + pid);
                        System.out.print(", Porta: " + port);
                        System.out.print(", Chave publica: - ");
                        for (Product p : listaProduto) {
                            System.out.println("Informações sobre o " + p.getName()+ " produto da lista do processo " + pid);
                                System.out.println(", ID Produto " + p.getName()+ ": " + p.getId());
                                System.out.println(", Descrição Produto " + p.getName()+ ": " + p.getDescricao());
                                System.out.println(", Preço Produto " + p.getName() + ": " + p.getPrecoInicial());
                        }

                        break;

                    case ('B'):
                        //***************************************************
                        //desenpacota mensagem de lance
                        pid = ois.readUTF();
                        port = ois.readUTF();
                        String lance = ois.readUTF();
                        idProduto = ois.readUTF(); //Id produto do processo atual(leiloero)

                        System.out.println("");
                        System.out.print("[UNICAST - RECEIVE]");
                        System.out.println("Requisicao de lance de processo: " + pid);

                        // verifica valor do lance maior de que valor do produto
                        if (Integer.parseInt(process.listaProdutos.get(Integer.parseInt(idProduto)).getPrecoInicial()) > Integer.parseInt(lance)) {
                            System.out.println("Valor do Lance não é suficiente!");
                            break;
                        }

                        //alguem ja deu um lance nesse produto
                        boolean lancar = true;
                        if (produtosLancados.isEmpty()) {
                            produtosLancados.add(idProduto);
                            lancar = true;
                        } else {
                            for (String c : InitSystem.produtosLancados) {
                                if (c.equals(idProduto)) {
                                    lancar = false;
                                    break;
                                }
                            }
                        }
                        long start = System.currentTimeMillis();
//                        long finish = System.currentTimeMillis();
//                        long total = finish - start;

                        //*****************************************************
                        //Set novo preco do produto idproduto da sua lista para o ultimo lance                       
                        process.listaProdutos.get(Integer.parseInt(idProduto)).setPrecoInicial(lance);

                        //*****************************************************
                        //Setar controlador de lances
                        Controle caux = null;
                        for (Controle c : procesosInteresados) {
                            if (c.getProdutoId().equals(idProduto)) {
                                c.setLancadorId(pid);
                                break;
                            }
                        }
                        
                        // Enviar multcast atualizando valor do produto 
                        if (lancar) {
                            Cronometro cro = new Cronometro(socket, idProduto);
                            cro.start();
                            System.out.println("Leilao Inicializado produtoID:" + idProduto);
                            atualizaValorCliente(pid, idProduto, lance);
                        } else {
                            System.out.println("Lancar Notificaçao para outro interesado");

                        }

                        break;
                    case ('F'):
                        //Finaliza Leilao tempo estorado
                        pid = ois.readUTF();
                        port = ois.readUTF();
                        idProduto = ois.readUTF();

                        System.out.print("[UNICAST - RECEIVE]");
                        System.out.print(" ID do participante: " + pid);
                        System.out.print(", Porta: " + port);
                        System.out.println("\nProduto arrematado com sucesso" + idProduto);

//                       for(Controle c:  procesosInteresados){
//                            if (c.isTempoFinalizado()) {
//                                 System.out.println("Compra Finalizada!!");
//                                 break;
//                            }
//                        }
                        break;

                }
                System.out.println("kkk");

//               switch (InitSystem.getTipo()) {
//                    // ********************************************
//                    // Tipos supported:
//                    // F --> Finaliza compra no leilo enviando msg para vencedor
//                    // B --> Buying request
//                   
//                   case ('F'):
//                        //Finaliza Leira tempo estorado
//                        System.out.println("saii");
//                           
//                       for(Controle c:  procesosInteresados){
//                            if (c.isTempoFinalizado()) {
//                                 System.out.println("Compra Finalizada!!");
//                                 break;
//                            }
//                        }
//                          
//                           
////                        ByteArrayOutputStream bos1 = new ByteArrayOutputStream(10);
////                        ObjectOutputStream oos1 = new ObjectOutputStream(bos1);
////                        oos1.writeUTF(pid);
////                        oos1.writeUTF(myport);
////                        oos1.writeUTF(nomeProduto);
////                        oos1.flush();
////
////                        
////                       byte[] output = bos1.toByteArray();
////                       DatagramPacket messageOut1 = new DatagramPacket(output, output.length, InetAddress.getLocalHost(), Integer.parseInt(hostport));
////                       System.out.println("");    
////                       System.out.print("[UNICAST - SEND]");
////                       System.out.print(" Voce venceu o Leilao " + pid);
////                       System.out.print(" Proudut0  " + nomeProduto);
////
////                       socket.send(messageOut1);
//                        
//                }
            } catch (IOException ex) {
                System.out.println("Unicast Exception");
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(UniCastServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static boolean isTeste() {
        return teste;
    }

    public static void setTeste(boolean teste) {
        UniCastServer.teste = teste;
    }

    public void atualizaValorCliente(String id, String idProduto, String novoValor) throws IOException {

        System.out.println("");
        System.out.print("[MULTICAST - SEND]");
        System.out.print("Atualiza valores de produto");

        // *********************************************
        // Packing transaction validation.
        ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeChar('A');
        oos.writeUTF(id);
        oos.writeUTF(idProduto);
        oos.writeUTF(novoValor);

        oos.flush();

        byte[] m1 = bos.toByteArray();
        DatagramPacket messageOut = new DatagramPacket(m1, m1.length, group, MULT_PORT);
        s.send(messageOut);

    }

}
