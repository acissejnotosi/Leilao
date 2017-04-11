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
                PublicKey pubKey;
                String nomeProduto;
                String idProduto;
                String descProduto;
                String precoProduto;

                // ********************************************
                // Receiving an UDP message
                buffer = new byte[1024];
                messageIn = new DatagramPacket(buffer, buffer.length);
                socket.receive(messageIn);
                bis = new ByteArrayInputStream(buffer);
                ois = new ObjectInputStream(bis);
                type = ois.readChar();

                System.out.println();
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
                        // Creating new process and add in the list of process
                        Process novoProcesso = new Process(pid, port, chavePublica, (ArrayList<Product>) listaProduto);

                        InitSystem.processList.add(novoProcesso);
                        System.out.println("");
                        System.out.print("[UNICAST - RECEIVE]");
                        System.out.print(" ID do participante: " + pid);
                        System.out.print(", Porta: " + port);
                        System.out.print(", Chave publica: - ");
                        for (Product p : listaProduto) {
                            System.out.println("Informações sobre o " + p.getName() + " produto da lista do processo " + pid);
                            System.out.println(", ID Produto " + p.getName() + ": " + p.getId());
                            System.out.println(", Descrição Produto " + p.getName() + ": " + p.getDescricao());
                            System.out.println(", Preço Produto " + p.getName() + ": " + p.getPrecoInicial());
                        }
                        ;

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
                        System.out.println("Requisicao de lance do processo: " + pid);
                        System.out.print(", valor do lance: " + lance);

                        // verifica valor do lance maior de que valor do produto
                        List<String> nomeProdutos = selecionarProdutosUmProcesso2(process);

                        if (Integer.parseInt(process.getListaProdutos().get(0).getPrecoInicial()) > Integer.parseInt(lance)) {
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
                        //atualiza valor do proiduto local
                        for (Process p : processList) {
                            if (p.getId().equals(pid)) {
                                 for(Product pro : p.getListaProdutos()){
                                        if(pro.getId().equals(idProduto)){
                                            pro.setPrecoInicial(lance);
                                            break;
                                        }
                                 }
                            }
                        }

                        // Enviar multcast atualizando valor do produto 
                        if (lancar) {
                        //setar controlador de lances
                            adiconaProcessoInteresado(pid, idProduto);
                            Cronometro cro = new Cronometro(socket, idProduto,process,s,group,MULT_PORT);
                            cro.start();
                            System.out.println("Leilao Inicializado produtoID:" + idProduto);
                            /// Enviando atualizacao de preco para todo multicast 
                            atualizaValorCliente(pid, idProduto, lance);
                        } else {
                            /// Enviando atualizacao de preco para todo multicast 
                            System.out.println("Lancar Notificaçao para outro interesado");
//                            notificacaParaCliente(pid,port,idProduto,lance);
                            for (Controle c : procesosInteresados) {
                                if (c.getProdutoId().equals(idProduto)) {
                                    for (String ids : c.getLancadorId()) {
                                        if (!ids.equals(pid)) {
                                            Process p = procuraUmprocesso(ids);
                                            notificacaParaCliente(pid, p.getPort(), idProduto, lance);
                                        }
                                    }
                                }
                            }
                            //setar controlador de lances
                            adiconaProcessoInteresado(pid, idProduto);
                            atualizaValorCliente(pid, idProduto, lance);
                        }

                        break;
                    case ('F'):
                        //Finaliza Leilao tempo estorado
                        System.out.println("cheguei");
                        String leiloeiroID = ois.readUTF();
                        pid = ois.readUTF();
                        port = ois.readUTF();
                        Product meuProduto = (Product) ois.readObject();
                        
                        System.out.println("depois");
                    
                        System.out.print("[UNICAST - RECEIVE]");
                        System.out.print(" ID do participante: " + pid);
                        System.out.print(", Porta: " + port);
                        System.out.println("\nProduto arrematado com sucesso: " + meuProduto.getName());
                        procuraUmprocessoAdicionaProdutoComprado(pid,meuProduto);
                        System.out.println("depois");
                        //remove produto do cliente
//                        Process leiloeiro = procuraUmprocesso(leiloeiroID);
//                        leiloeiro.getListaProdutos().remove(meuProduto);
                       

                         break;

                    case ('U'):
                        ////Notificaçao de lance maior 

                        pid = ois.readUTF();
                        idProduto = ois.readUTF();
                        String novoValor = ois.readUTF();
                        System.out.println("     Notificaço recebido com sucesso!!!!!");
                        System.out.print("[UNICAST - RECEIVE]");
                        System.out.print(" ID do participante deu lance maior: " + pid);
                        System.out.print(" Valor do ultimo lance:  " + novoValor);

                        break;

                }

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

    public void notificacaParaCliente(String id, String port, String idProduto, String novoValor) throws IOException {

        System.out.println("");
        System.out.print("[UNIACAST - SEND]");
        System.out.print("Outro participante deu um lance valor:" + novoValor);
        System.out.println("Porta:" + port);
        // *********************************************
        // Packing transaction validation.
        ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeChar('U');
        oos.writeUTF(id);
        oos.writeUTF(idProduto);
        oos.writeUTF(novoValor);
        oos.flush();

        byte[] output = bos.toByteArray();
        DatagramPacket request = new DatagramPacket(output, output.length, InetAddress.getLocalHost(), Integer.parseInt(port));
        socket.send(request);

    }

    public static Process procuraUmprocesso(String id) {

        Process process = null;

        for (Process p : processList) {
            if (p.getId().equals(id)) {
                process = p;
            }
        }
        return process;

    }
    
    public static void procuraUmprocessoAdicionaProdutoComprado(String id, Product product) {

        Process process = null;

        for (Process p : processList) {
            if (p.getId().equals(id)) {
                process.getListaProdutos().add(product);
            }
        }


    }
   

    public static void adiconaProcessoInteresado(String idProcesso, String idProduto) {

        for (Controle c : procesosInteresados) {
            if (c.getProdutoId().equals(idProduto)) {
                c.setUltimo(idProcesso);
                if (c.getLancadorId() != null) {
                    for (String ids : c.getLancadorId()) {
                        if (!ids.equals(idProcesso)) {
                            c.getLancadorId().add(idProcesso);
                            break;
                        }
                    }
                } else {
                    c.setLancadorId(new ArrayList<>());
                }
                c.getLancadorId().add(idProcesso);
            }
        }
    }

    public static List<String> selecionarProdutosUmProcesso2(Process paux) {

        List<String> idProdutosPAUmProcesso = null;
        System.out.println("Lista de produtos:");
        int i = 0;
        for (Product p : paux.getListaProdutos()) {
            System.out.println(i + "- " + "Nome do produto desse processo:" + p.getName());
            i++;
        }

        return idProdutosPAUmProcesso;

    }
}