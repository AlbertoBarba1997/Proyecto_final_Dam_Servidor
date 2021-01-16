/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servidor;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import javax.imageio.ImageIO;

/**
 *
 * @author juana
 */
public class ClienteFake {

    public static void main(String[] args) throws IOException {

        
        
        
        
        //CONEXION SOCKET
        Socket conexionSocket;
        Socket conexionSocketImagenes;
        final String HOST = "192.168.0.11";
        final int PUERTO_server = 9002;
        final int PUERTO_imagenes = 9003;
        BufferedReader inputSocket = null;
        PrintWriter outputSocket = null;
        
        PrintWriter outputImagenes=null;
        InputStream inputImagenes=null;
        
        
        
        try {
            
        
        
            //Replace below IP with the IP of that device in which server socket open.
            //If you change port then change the port number in the server side code also.
            conexionSocket = new Socket("192.168.0.11", 9002);
            conexionSocketImagenes = new Socket(HOST, PUERTO_imagenes);
            
            outputImagenes= new PrintWriter(conexionSocketImagenes.getOutputStream(), true);;
            inputImagenes= conexionSocketImagenes.getInputStream();
            
            
            BufferedReader teclado=new BufferedReader(new InputStreamReader(System.in));
            
            
            String ruta="./fotos_usuarios/alberto.jpg\n";
            outputImagenes.write(ruta);
            outputImagenes.flush();
            byte[] bytesImagen=new byte[65536];
            
            int nBytesImagen;
                 
            while ((nBytesImagen = inputImagenes.read(bytesImagen)) != -1) {
                System.out.println(nBytesImagen);
            }
//            for(byte b:bytesImagen){
//                System.out.println(b);
//            }

             ByteArrayInputStream bais = new ByteArrayInputStream(bytesImagen);
             BufferedImage imagen=ImageIO.read(bais);
             System.out.println(imagen);
             
            
                /*
                System.out.println("Introduce usuario y cotraseña");
                String usuarioo = teclado.readLine();
                String contraseña = teclado.readLine();

                outputSocket.println("C4-LOG_TRABAJADOR:" + usuarioo + "," + contraseña);
                outputSocket.flush();
                final String mensajeEntrada = inputSocket.readLine();

                System.out.println("Mensaje del servidor:"+mensajeEntrada);
                
                 outputSocket.println("C6-LISTA_TRABAJADORES");
                 outputSocket.flush();
                 System.out.println("Segundo mensaje del servidor:"+inputSocket.readLine());
                
            
            outputSocket.close();
            inputSocket.close();
            conexionSocket.close();
            */

        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

}
