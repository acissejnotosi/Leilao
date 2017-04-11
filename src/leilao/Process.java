
package leilao;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

/**
 *
 * @author Jessica
 */
class Process implements Serializable {

    private  String id;
    private  String port;
    private  String nomeProduto;
    private  String idProduto;
    private  String descProduto;
    private  String precoProduto;
    private  PublicKey chavePublica;
    private  PrivateKey chavePrivada;

    
    
     // ********************************************
     // Construtor do processo recebidos multcast
    public Process(String id, String port, PublicKey chavePublica) {
        this.id = id;
        this.port = port;
        this.chavePublica = chavePublica;


    }
     // ********************************************
     // Construtor do processo meu processo
    public Process(String id, String port, PublicKey chavePublica, PrivateKey chavePrivada) {
        this.id = id;
        this.port = port;
        this.chavePublica = chavePublica;
        this.chavePrivada = chavePrivada;
    }
   
   
    public String getId() {
        return id;
    }

    public String getPort() {
        return port;
    }

    public PublicKey getChavePublica() {
        return chavePublica;
    }

  
    public String imprimaProcessos() {
        return "Participante: " + id + ", Porta: " + port;
    }
    public String imprimaParticipantes() {
        return "Participante: " + id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPort(String port) {
        this.port = port;
    }


    public void setChavePublica(PublicKey chavePublica) {
        this.chavePublica = chavePublica;
    }
    
    

    
}
