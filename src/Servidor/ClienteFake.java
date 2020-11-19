/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author juana
 */
public class ClienteFake {

    public static void main(String[] args) {

        
        //CONEXION SOCKET
        Socket conexionSocket;
        final String HOST = "192.168.0.11";
        final int PUERTO = 9002;
        BufferedReader inputSocket = null;
        PrintWriter outputSocket = null;
        

        try {
            //Replace below IP with the IP of that device in which server socket open.
            //If you change port then change the port number in the server side code also.
            conexionSocket = new Socket("192.168.0.11", 9002);
            outputSocket = new PrintWriter(conexionSocket.getOutputStream());
            inputSocket = new BufferedReader(new InputStreamReader(conexionSocket.getInputStream()));
            
            BufferedReader teclado=new BufferedReader(new InputStreamReader(System.in));

            
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
            

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
