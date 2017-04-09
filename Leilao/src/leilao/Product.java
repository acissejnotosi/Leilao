/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leilao;

import java.util.Date;

/**
 *
 * @author allan
 */
public class Product {
    
    private String id;
    private String name;
    private String descricao;
    private String precoInicial;
    private String tempoFinal;

    public Product(String id, String name, String descricao, String precoInicial, String tempoFinal) {
        this.id = id;
        this.name = name;
        this.descricao = descricao;
        this.precoInicial = precoInicial;
        this.tempoFinal = tempoFinal;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getPrecoInicial() {
        return precoInicial;
    }

    public String getTempoFinal() {
        return tempoFinal;
    }
    

   
    
}
