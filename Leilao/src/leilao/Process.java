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
    static ArrayList<leilao> listaLeilao = new ArrayList<>();

    public Process(String id, String port, PublicKey chavePublica, String nomeProduto, String idProduto, String descProduto, String precoProduto) {
        this.id = id;
        this.port = port;
        this.chavePublica = chavePublica;
        this.nomeProduto = nomeProduto;
        this.idProduto = idProduto;
        this.descProduto = descProduto;
        this.precoProduto = precoProduto;

    }

    public static void setListaLeilao(ArrayList<leilao> listaLeilao) {
        Process.listaLeilao = listaLeilao;
    }


    public String getId() {
        return id;
    }

    public String getPort() {
        return port;
    }

    public String getNomeProduto() {
        return nomeProduto;
    }

    public String getIdProduto() {
        return idProduto;
    }

    public String getDescProduto() {
        return descProduto;
    }

    public String getPrecoProduto() {
        return precoProduto;
    }

    public PublicKey getChavePublica() {
        return chavePublica;
    }
    public static ArrayList<leilao> getListaLeilao() {
        return listaLeilao;
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

    public void setNomeProduto(String nomeProduto) {
        this.nomeProduto = nomeProduto;
    }

    public void setIdProduto(String idProduto) {
        this.idProduto = idProduto;
    }

    public void setDescProduto(String descProduto) {
        this.descProduto = descProduto;
    }

    public void setPrecoProduto(String precoProduto) {
        this.precoProduto = precoProduto;
    }

    public void setChavePublica(PublicKey chavePublica) {
        this.chavePublica = chavePublica;
    }
    
    

    
}
