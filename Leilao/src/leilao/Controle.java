/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leilao;

/**
 *
 * @author allan
 */
public class Controle {
    
   private String produtoId;
   private String lancadorId;
   private long tempo;
   private boolean tempoFinalizado = false;
   private String ultimo;


    public Controle(String produtoId, String ultimoLance) {
        this.produtoId = produtoId;
        this.ultimo = ultimoLance;
    }
   

    public String getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(String produtoId) {
        this.produtoId = produtoId;
    }

    public String getLancadorId() {
        return lancadorId;
    }

    public void setLancadorId(String lancadorId) {
        this.lancadorId = lancadorId;
    }

    public long getTempo() {
        return tempo;
    }

    public void setTempo(long tempo) {
        this.tempo = tempo;
    }

    
    public String getUltimo() {
        return ultimo;
    }

    public void setUltimo(String ultimo) {
        this.ultimo = ultimo;
    }

    public boolean isTempoFinalizado() {
        return tempoFinalizado;
    }

    public void setTempoFinalizado(boolean tempoFinalizado) {
        this.tempoFinalizado = tempoFinalizado;
    }
    
    
   
   
    
    
}
