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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author juana
 */
public class NewMain {

    public static ConexionBD conexBD = new ConexionBD();

    public static void main(String[] args) throws SQLException, IOException {

        Time hora = Time.valueOf("18:00:00");
        conexBD.altaHorario(3, 2, 1, hora);
    }


}
