/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leilao;

import java.util.logging.Level;
import java.util.logging.Logger;
import static leilao.InitSystem.procesosInteresados;

/**
 *
 * @author allan
 */
public class Temporizador extends Thread{
    String pid;
    String idProduto;

    public Temporizador(String pid, String idProduto) {
        this.pid = pid;
        this.idProduto = idProduto;
    }   
    @Override
    public void run() {
      int i=0;
      while (i<2) {          
       i++;
       try { 
           Thread.sleep(10000);
       } catch (InterruptedException ex) {
        Logger.getLogger(Temporizador.class.getName()).log(Level.SEVERE, null, ex);
      }
    }  
        System.out.println("Tempo Finalizado!!");
      for(Controle c:  procesosInteresados){
         if (c.getProdutoId().equals(idProduto)) {
                      c.setTempoFinalizado(true);
                      break;
                }
        }      
        String chart = "F";
        InitSystem.setTipo(chart.charAt(0)); 
        System.out.println("Tempo Finalizado!!");
                             
    
   }
    
    
    
    
}
