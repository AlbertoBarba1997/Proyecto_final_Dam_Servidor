/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servidor;

import HilosAtiendeImagen.Server_Devuelve_Imagen;
import HilosAtiendeImagen.Server_Recibe_Imagen;
import java.io.IOException;
import java.net.ServerSocket;


public class Server {

  
    
    public static void main(String[] args) throws IOException {
       ServerSocket serverSocket = null;
       final int PUERTO_PRINCIPAL=9002;
       
       boolean listening = true;
       
       //Lanzar Hilos para atender las peticiones de los diferentes Socket para recibir imagenes como de enviarlas y poder mostrarlas desde la interfaz (Diferentes socket por que tendra diferentes flujos).
       lanzarHilosAtiendeClienteImagenes();
       lanzarHiloEliminacionPeriodicaReservas();
       
       
       //Atender Socket principal (funciones BD)
       try {
            serverSocket = new ServerSocket(PUERTO_PRINCIPAL);  
            //System.out.println(serverSocket.get);
            
            
        } catch (IOException e) {
            System.err.println("Could not listen on port.");
            System.exit(-1);
        }
        
       
       while (listening){
           
           new  ServerThread(serverSocket.accept()).start();
          
          
       }
       
       serverSocket.close();
       
    }
    

    private static void lanzarHilosAtiendeClienteImagenes() {
       int PUERTO_RECIBIR_IMAGENES=9004;
       
       int PUERTO_DEVOLVER_IMAGENES=9003;
       
       Server_Devuelve_Imagen devolverImagenThread=new Server_Devuelve_Imagen(PUERTO_DEVOLVER_IMAGENES);
       Server_Recibe_Imagen recibirImagenThread=new Server_Recibe_Imagen(PUERTO_RECIBIR_IMAGENES);
       
       devolverImagenThread.setDaemon(true);
       recibirImagenThread.setDaemon(true);
       
       devolverImagenThread.start();
       recibirImagenThread.start();
       
        
    }

    private static void lanzarHiloEliminacionPeriodicaReservas() {
        EliminarReservasPeriodicamente_Thread hiloEliminarReservas=new EliminarReservasPeriodicamente_Thread();
        hiloEliminarReservas.setDaemon(true);
        hiloEliminarReservas.start();
    }
    
}
