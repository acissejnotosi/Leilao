/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leilao;

/**
 *
 * @author Jessica
 */
public class leilao {
    private long leilaoId;
    private int compradorId; 
    private int leiloeroId;
    private int lance;
    private int valorAtual;
   
    private String status; // status of transaction
    
    public leilao(long leilaoId, int compradorId, int leiloeroId ) {
        this.leilaoId    = leilaoId;
        this.compradorId = compradorId;
        this.leiloeroId  = leiloeroId;
       
    }
    
    // C   --> Confirmed
    // NC  --> Not Confirmed
    // I   --> Invalid
    public boolean setStatus(String status) {
        if (status.equals("C") || status.equals("NC") || status.equals("I")) {
            this.status = status;
            return true;
        } else {
            return false;
        }
    }
    
    public long getLeilaoId() {
        return leilaoId;
    }

    public int getCompradorId() {
        return compradorId;
    }

    public int getLeiloeroId() {
        return leiloeroId;
    }

    public int getLance() {
        return lance;
    }

    public void setLance(int lance) {
        this.lance = lance;
    }

    public int getValorAtual() {
        return valorAtual;
    }

    public void setValorAtual(int valorAtual) {
        this.valorAtual = valorAtual;
    }
   
    
    public String getStatus() {
        return status;
    }
    
    public void printTransaction() {
        System.out.print("Leilao ID: " + leilaoId);
        System.out.print(", Comprador ID: " + compradorId);
        System.out.print(", Leiloero ID: " + leiloeroId);
        System.out.println(", Status: " + status);
    }
}