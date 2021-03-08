/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servidor;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;
import Servidor.ConexionBD;


/**
 *
 * @author juana
 */
public class EliminarReservasPeriodicamente_Thread extends Thread {

    
    public  ConexionBD conexBD;
    
    
    EliminarReservasPeriodicamente_Thread() {
        conexBD = new ConexionBD();
    }
    
    public void run() {
      
        //Periodicamente (cada 12 horas) este hilo se encargará de eliminar las reservas antiguas

        
        
        while(true){
            
            conexBD.eliminarReservasAntiguas();
            
            try {
                Thread.sleep(10*60*60*1000);        //horas*minutos*segundos*milisegundos)
                //Thread.sleep(2*60*1000);        //Prueba para presentacion (2 minutos)
                
            } catch (InterruptedException ex) {
                Logger.getLogger(EliminarReservasPeriodicamente_Thread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
    
    
    
}
