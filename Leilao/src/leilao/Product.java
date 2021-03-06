/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leilao;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 *
 * @author allan
 */
public class Product implements Serializable {
    
    //private static final long serialVersionUID;
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

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public void setPrecoInicial(String precoInicial) {
        this.precoInicial = precoInicial;
    }

    public void setTempoFinal(String tempoFinal) {
        this.tempoFinal = tempoFinal;
    }
    

   
    
}
