/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leilao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;

/**
 *
 * @author Jessica
 */
public class GeraChave {
    
    public PrivateKey chavePrivada;
    public PublicKey chavePublica;
    
    
    public static final String ALGORITHM = "RSA";
   
    /**
     * Gera a chave que contém um par de chave Privada e Pública usando 1025
     * bytes. Armazena o conjunto de chaves nos arquivos private.key e
     * public.key
     */
    public  void geraChave() {
        try {
            
            final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
            keyGen.initialize(1024);
            final KeyPair key = keyGen.generateKeyPair();

            this.chavePrivada = key.getPrivate();//.getEncoded();
            this.chavePublica = key.getPublic();//.getEncoded();
            //File chavePrivadaFile = new File(PATH_CHAVE_PRIVADA);
            //File chavePublicaFile = new File(PATH_CHAVE_PUBLICA);
            
           
           
        } catch (NoSuchAlgorithmException e) {
        }

    }

  
    /**
     * Criptografa o texto puro usando chave pública.
     */
    public  byte[] criptografa(String texto, PublicKey chave) {
        byte[] cipherText = null;

        try {
            final Cipher cipher = Cipher.getInstance(ALGORITHM);
            // Criptografa o texto puro usando a chave Púlica
            cipher.init(Cipher.ENCRYPT_MODE, chave);
            cipherText = cipher.doFinal(texto.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cipherText;
    }

    /**
     * Decriptografa o texto puro usando chave privada.
     */
    public  String decriptografa(byte[] texto, PrivateKey chave) {
        byte[] dectyptedText = null;

        try {
            final Cipher cipher = Cipher.getInstance(ALGORITHM);
            // Decriptografa o texto puro usando a chave Privada
            cipher.init(Cipher.DECRYPT_MODE, chave);
            dectyptedText = cipher.doFinal(texto);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new String(dectyptedText);
    }

    public PrivateKey getChavePrivada() {
        return chavePrivada;
    }

    public PublicKey getChavePublica() {
        return chavePublica;
    }

   
    
    public PublicKey carregaChavePublica(byte[] bytes) throws NoSuchAlgorithmException, InvalidKeySpecException{
        PublicKey publicKey = 
        KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(bytes));
        return publicKey;
    }
    
   public PrivateKey carregaChavePrivate(byte[] bytes) throws NoSuchAlgorithmException, InvalidKeySpecException{
        PrivateKey privateKey = 
        KeyFactory.getInstance("RSA").generatePrivate(new X509EncodedKeySpec(bytes));
        return  privateKey;
    }
}
