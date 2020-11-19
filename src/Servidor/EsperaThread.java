/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servidor;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author juana
 */
public class EsperaThread extends Thread{
    
    private final int TIEMPO_ESPERA=30000; //30 segundos por espera
    Protocol protocoloPadre; 
    private int tiempoTotalEspera;
    private int tiempoEsperado;
    
    
    public EsperaThread(Protocol protocoloPadre, int nVecesEnEspera){
        this.protocoloPadre=protocoloPadre;
        this.tiempoTotalEspera=TIEMPO_ESPERA*nVecesEnEspera;
        this.tiempoEsperado=0;
    }
    
    public int getSegundosRestantesEspera(){
        return (tiempoTotalEspera-tiempoEsperado)/1000;
    }
    
    public void run(){
        /*Duermo 5 segundos y no todo el tiempo para ir llevando la contabilidad del tiempo que falta de espera
         para informar en tiempo real de los segundos que faltan*/
        while (tiempoEsperado < tiempoTotalEspera) {
            
            try {
                this.sleep(5000);                               
                tiempoEsperado = tiempoEsperado + 5000;

            } catch (InterruptedException ex) {

            }
        }
        
        protocoloPadre.volverEstadoInicial();
    }
    
    
    
}
