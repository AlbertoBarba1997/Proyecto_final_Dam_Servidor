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
public class Server {

    public static void main(String[] args) throws IOException {
       ServerSocket serverSocket = null;
       boolean listening = true;
       
       try {
            serverSocket = new ServerSocket(9002);   //quizas 9002
        } catch (IOException e) {
            System.err.println("Could not listen on port: 4444.");
            System.exit(-1);
        }
        
       
       while (listening){
           new  ServerThread(serverSocket.accept()).start();
       }
       
       serverSocket.close();
    }
    
}
