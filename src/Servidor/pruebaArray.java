/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servidor;



/**
 *
 * @author juana
 */
public class pruebaArray {

    
    public static void main(String[] args) {
        int[] numeros1={10,20,30,40};
        int[] numeros2={50,60,70};
        int[] numeros3={80,90,1000};
        int[] numeros=new int[10];
        System.arraycopy(numeros1, 0,numeros, 0, numeros1.length);
        System.arraycopy(numeros2, 0,numeros, numeros1.length, numeros2.length);
        
        
        for(int n:numeros){
            System.out.println(n);
        }
    }
    
}
