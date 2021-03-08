/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package HilosAtiendeImagen;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;


public class RecibeImagenServerThread extends Thread {

    
    //ESTA ES LA QUE HAY QUE CAMBIAR.
    private final String RUTA_CARPETA_IMAGENES="./fotos_usuarios";
    private Socket socketCliente=null;
    
    
    RecibeImagenServerThread(Socket accept) {
        super("ImagenesServerThread");
        this.socketCliente = accept;
        
    }
    
    public void run() {
        try {
            //1. CREA FLUJOS
            BufferedInputStream in =new BufferedInputStream( socketCliente.getInputStream());
            PrintWriter out = new PrintWriter(socketCliente.getOutputStream());;

            //2. RECIBE LA IMAGEN POR BYTES del flujo de entrada
            byte[] bytesImagen = new byte[65536];   //La seccion de imagen recibida (maximo pasa de 65.536 en 65.536 bytes el flujo asi que hay que concatenar arrays para cargar la imagen completa
            int nBytesImagen;                       //El numero de bytes que hay en la seccion recibida
            int nBytesImagenTotales = 0;              //El numero total de bytes total en todas las secciones recibidas
            byte[] imagenTotalAnterior = new byte[0];             //La imagen total hasta el momento
            byte[] imagenTotal = new byte[0];                     //La imagen total final.

            while ((nBytesImagen = in.read(bytesImagen)) != -1) {

                nBytesImagenTotales += nBytesImagen;
                imagenTotal = new byte[nBytesImagenTotales];

                System.arraycopy(imagenTotalAnterior, 0, imagenTotal, 0, imagenTotalAnterior.length);
                System.arraycopy(bytesImagen, 0, imagenTotal, imagenTotalAnterior.length, nBytesImagen);

                imagenTotalAnterior = imagenTotal;
                if (nBytesImagen < 65536) {
                    break;                      // Si llega un conjunto de bytes que no es de 65536, significa que es el ultimo conjunto de bytes, por lo que sale.
                }

            }
            
            
            //3. Una vez tiene la imagen en bytes[] tendra que ASIGNARLE un numero como NOMBRE a la imagen
            String nombreImagen=generarNombreImagen();
            
            //4. CREAR FILE con el nombre de esta imagen y copiar los bytes[] de la imagen recibida anteriormente 
            File imagen=new File(RUTA_CARPETA_IMAGENES+"/"+nombreImagen);
            
            boolean creada=imagen.createNewFile();
            if(!creada){
                out.write("S23-ERROR_SUBIDA\n");
                out.flush();
                
            } else {
                try (FileOutputStream fos = new FileOutputStream(imagen)) {
                    fos.write(imagenTotal);
                    
                    out.write("S22-IMAGEN_SUBIDA:" + imagen.getPath() + "\n");
                    out.close();
                    

                } catch (IOException ioe) {
                    out.write("S23-ERROR_SUBIDA\n");
                    out.flush();
                    return;
                }
            }
            
            
            

        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
    }

    private String generarNombreImagen() {
        //Genera el nombre de un archivo (los archivos son nombrados con numeros secuenciales), segun el numero de la ultima imagen, le asigna el numero como nombre (+extension).
        String nombreImagenFinal="0.jpg";
        int minimo=0;
        File carpetaImagenes=new File(RUTA_CARPETA_IMAGENES);
        for(File img:carpetaImagenes.listFiles()){
            String nombreImgSinExtension=img.getName().replaceFirst("[.][^.]+$", "");
            
            try{
                int numeroFoto=Integer.parseInt(nombreImgSinExtension);
                if(numeroFoto>=minimo) minimo=numeroFoto+1;
            }catch(NumberFormatException nfe){
                //Si no se puede transformar a integer ni lo tnedrá en cuenta
            }    
            
        }
        
        nombreImagenFinal=Integer.toString(minimo)+".jpg";
        return nombreImagenFinal;
    }
    
    
    
    
}
