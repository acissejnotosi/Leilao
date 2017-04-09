/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leilao;

import java.io.Serializable;
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
    static ArrayList<Product> listaProdutos = new ArrayList<>();

    public Process(String id, String port, PublicKey chavePublica, ArrayList<Product> listaProdutos) {
        this.id = id;
        this.port = port;
        this.chavePublica = chavePublica;
        this.listaProdutos = listaProdutos;

    }

    public static ArrayList<Product> getListaProdutos() {
        return listaProdutos;
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
