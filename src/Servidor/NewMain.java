/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servidor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author juana
 */
public class NewMain {
    
    ConexionBD conexBD=new ConexionBD();
    
    public static void main(String[] args) throws SQLException, IOException {
        
        File f=new File("./fotos_usuarios");
        for(File ff:f.listFiles()){
            String fileNameWithOutExt = ff.getName().replaceFirst("[.][^.]+$", "");
                int n=Integer.parseInt(fileNameWithOutExt);
                
                System.out.println(ff.getPath());
                 System.out.println(ff.getAbsolutePath());
                  System.out.println(ff.getCanonicalPath());
                
        }
        
        /*ConexionBD conexion=new ConexionBD();
        String correo="cliente2@gmail.com";
        String dniCliente="39576918A";
        System.out.println(conexion.mostrarCliente(dniCliente));
        System.out.println(conexion.comprobarCorreoRegistrado(correo));
        System.out.println(conexion.comprobarContraseñaNoRegistrada(correo));
        System.out.println(conexion.loginTrabajadorBD("45380203F", "0402"));*/
        
        /*if(startConection(RUTA,USER,PW)){
            System.out.println("Se ha conectado");
            
            String correo="juanalbertobarba1997@gmail.com";
            String contraseña="canela123";
            logIn(correo,contraseña);
            
            stopConection(conexion);
        }*/
              
        
    }

    /*
    private static boolean startConection(String RUTA, String USER, String PW) {
        try{
            conexion=DriverManager.getConnection(RUTA,USER,PW);
            return true;
            
        } catch (SQLException ex) {
            System.out.println("Error de conexion");
            return false;
        }
    }
    
    
    private static void stopConection(Connection conexion) {
        try {
            conexion.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(NewMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    

    private static void logIn(String correo, String contraseña) throws SQLException {
        ///////////////////////////////////////////////////////////  IMPORTANTE /////////////////////////////////////////////////////
            String query = "SELECT nombre FROM cliente WHERE correo=? OR contraseña=? ;";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);

            preparedStatement.setString(1, correo);
            preparedStatement.setString(2, contraseña);

            ResultSet rs = preparedStatement.executeQuery();
            
            while (rs.next()) { 

                System.out.println(rs.getString(1));
            }
            System.out.println("---------------");

    }

    
*/
    
    
}
