/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servidor;

import java.io.IOException;
import java.net.ServerSocket;

/**
 *
 * @author juana
 */
public class Server_Devuelve_Imagen extends Thread {

    ServerSocket serverSocket = null;
    boolean listening = true;
    int puerto;

    public Server_Devuelve_Imagen(int puerto) {
        this.puerto = puerto;
    }

    public void run() {
        try {
            serverSocket = new ServerSocket(puerto);

        } catch (IOException e) {
            System.err.println("Could not listen on port.");
            System.exit(-1);
        }

        try{
        while (listening) {

            new DevuelveImagenServerThread(serverSocket.accept()).start();
        }

        serverSocket.close();
        
        }catch(IOException ioe){
            System.err.println("Error al atender una peticion Socket para devolver imagenes.");
            
        }

    }

}
