/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servidor;


import java.io.*;
import java.net.*;

/**
 *
 * @author juana
 */
public class ServerThread extends Thread {
    private Socket socketCliente=null;

    
    public ServerThread(Socket socketCliente) {
        super("Ahorcado1ServerThread");
        this.socketCliente = socketCliente;
    }

    public void run() {
        try {
            PrintWriter out = new PrintWriter(socketCliente.getOutputStream(), true);
            BufferedReader in = new BufferedReader( new InputStreamReader(socketCliente.getInputStream()));
            
            String inputLine, outputLine;
	    Protocol kkp = new Protocol();
	    
           
            
            while ((inputLine=in.readLine())!=null) {
                
                 System.out.println("llega:"+ inputLine);
		outputLine = kkp.processInput(inputLine);
                System.out.println("Server Thread: envia de vuelta"+ outputLine);
		out.println(outputLine);
                out.flush();
                
	    }
	    out.close();
	    in.close();
	    socketCliente.close();

        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
    }

}
