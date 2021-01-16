/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servidor;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/**
 *
 * @author juana
 */
public class PruebaMostrarImagen {

    public PruebaMostrarImagen(String filePath) {

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

                JFrame frame = new JFrame(filePath);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                BufferedImage img = null;
                try {
                    ////

                    //CONEXION SOCKET
                    Socket conexionSocket;
                    Socket conexionSocketImagenes;
                    final String HOST = "192.168.0.11";
                    final int PUERTO_server = 9002;
                    final int PUERTO_imagenes = 9003;
                    BufferedReader inputSocket = null;
                    PrintWriter outputSocket = null;

                    PrintWriter outputImagenes = null;
                    InputStream inputImagenes = null;
                    conexionSocket = new Socket("192.168.0.11", 9002);
                    conexionSocketImagenes = new Socket(HOST, PUERTO_imagenes);

                    outputImagenes = new PrintWriter(conexionSocketImagenes.getOutputStream(), true);;
                    inputImagenes = conexionSocketImagenes.getInputStream();

                    BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));

                    String ruta = "./fotos_usuarios/alberto.jpg\n";
                    outputImagenes.write(ruta);
                    outputImagenes.flush();
                    
                    
                    byte[] bytesImagen = new byte[65536];   //La seccion de imagen recibida (maximo pasa de 65.536 en 65.536 bytes el flujo asi que hay que concatenar arrays para cargar la imagen completa
                    int nBytesImagen;                       //El numero de bytes que hay en la seccion recibida
                    int nBytesImagenTotales=0;              //El numero total de bytes total en todas las secciones recibidas
                    byte[] imagenTotalAnterior=new byte[0];             //La imagen total hasta el momento
                    byte[] imagenTotal=new byte[0];                     //La imagen total final.
                    
                    
                                       
                    
                    
                    while((nBytesImagen= inputImagenes.read(bytesImagen))!=-1){
                        System.out.println(bytesImagen);
                        System.out.println(nBytesImagen);
                        nBytesImagenTotales+=nBytesImagen;
                        imagenTotal=new byte[nBytesImagenTotales];
                        
                        System.arraycopy(imagenTotalAnterior, 0, imagenTotal, 0, imagenTotalAnterior.length);
                        System.arraycopy(bytesImagen, 0, imagenTotal, imagenTotalAnterior.length, nBytesImagen);
                        
                        imagenTotalAnterior=imagenTotal;
                        if(nBytesImagen<65536) break;
                        
                    }
                    System.out.println("sale");

                    ByteArrayInputStream bais = new ByteArrayInputStream(imagenTotal);
                     img = ImageIO.read(bais);
                    System.out.println(img);
                    
                    
                    JLabel lbl = new JLabel();
                    BufferedImage resizedImage=resizeImage(img,1000, 600);
                    lbl.setIcon(new ImageIcon(resizedImage));
                    frame.getContentPane().add(lbl, BorderLayout.CENTER);
                    frame.pack();
                    frame.setLocationRelativeTo(null);
                    frame.setVisible(true);
                    ////
                    
                            
                            
                } catch (IOException ex) {
                    Logger.getLogger(PruebaMostrarImagen.class.getName()).log(Level.SEVERE, null, ex);
                }

            }

            
            //resizear la imagen
            private BufferedImage resizeImage(BufferedImage img, int newW, int newH) {
                Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
                BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

                Graphics2D g2d = dimg.createGraphics();
                g2d.drawImage(tmp, 0, 0, null);
                g2d.dispose();

                return dimg;
            }

        });
    }

    public static void main(String[] args) {
        new PruebaMostrarImagen(".");
    }

}
