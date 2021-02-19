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
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author juana
 */
public class NewMain {

    public  ConexionBD conexBD = new ConexionBD();

    public static void main(String[] args) throws SQLException, IOException {

        
        NewMain newMain=new NewMain();
        newMain.ejecutar();
        
    }
    public void ejecutar(){
        ArrayList<Object[]> listaEjercicios=new ArrayList<Object[]>();
        Object[] ejercicio1={1,1,10,3,"10 "};
        Object[] ejercicio2={1,25,null,null,"15 minutos"};
        Object[] ejercicio3={2,1,10,3,""};
        Object[] ejercicio4={1,2,6,3,""};
        listaEjercicios.add(ejercicio1);
        listaEjercicios.add(ejercicio2);
        listaEjercicios.add(ejercicio3);
        listaEjercicios.add(ejercicio4);
        
        
        conexBD.altaTablaModelo("Principiante", 2, listaEjercicios);
        //conexBD.eliminarFoto(".\\fotos_usuarios\\21.jpg");
//        System.out.println(conexBD.listarEjercicios());
    }


}
