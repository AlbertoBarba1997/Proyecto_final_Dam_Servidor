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

/**
 *
 * @author juana
 */
public class DevuelveImagenServerThread extends Thread {

    private Socket socketCliente=null;
    
    DevuelveImagenServerThread(Socket accept) {
        super("ImagenesServerThread");
        this.socketCliente = accept;
    }
    
    public void run() {
        
        try {
            
            BufferedReader in = new BufferedReader( new InputStreamReader(socketCliente.getInputStream()));
            BufferedOutputStream out=new BufferedOutputStream(socketCliente.getOutputStream());
            
            
            
            
            String inputLine;
	    Protocol kkp = new Protocol();
            
            byte[] theOutput;
	    
//            System.out.println("Entra en DevuelveImagenServerThread, in: "+ in);
            
            while ((inputLine=in.readLine())!=null) {
                
               
                //Llegara un String con la ruta de la imagen
		String rutaImagen=inputLine;
                //FileInputStream fileInputStream = new FileInputStream( rutaImagen );
                int nBytes;
                File imagen=new File(rutaImagen);
                
                
                if(!imagen.exists()){ 
                    theOutput=new byte[]{ (byte)-1 };
                }else{
        
//                    while ((nBytes = fileInputStream.read(theOutput)) != -1) {
//                        networkOutputStream.write(data, 0, nRead);
    }
                    theOutput=Files.readAllBytes(imagen.toPath());
//                    System.out.println("Tamaño imagen:"+ theOutput.length);


                   
//                }
               
                
                
                
		out.write(theOutput,0,theOutput.length);
                out.flush();
                
                
	    }
            System.out.println("Acaba hilo ImagenServerThread.");
	    out.close();
	    in.close();
	    socketCliente.close();

        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
    }
    
    
    
    
}
