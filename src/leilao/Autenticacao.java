/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leilao;

import java.security.PublicKey;

/**
 *
 * @author allan
 */
public class Autenticacao {
    private PublicKey public_chave= null;
    private byte[] criptografado = null;

    public Autenticacao() {
    }

    public PublicKey getPublic_chave() {
        return public_chave;
    }

    public void setPublic_chave(PublicKey public_chave) {
        this.public_chave = public_chave;
    }

    public byte[] getCriptografado() {
        return criptografado;
    }

    public void setCriptografado(byte[] criptografado) {
        this.criptografado = criptografado;
    }

 
    
    


    
    
    
}
