/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servidor;

import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;
import java.io.File;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.MimetypesFileTypeMap;

public class ConexionBD {

    //DATOS BD 
    private static final String RUTA = "jdbc:mysql://localhost:3306/gimnasio";
    private static final String USER = "root";
    private static final String PW = "";
    private static Connection conexion = null;
    private static CallableStatement callableEstado = null;
    //CONEXION
    public boolean conexionEstablecida = false;

    
    public ConexionBD() {
        conexionEstablecida = startConection(RUTA, USER, PW);
    }
    

    private static boolean startConection(String RUTA, String USER, String PW) {
        try {
            conexion = DriverManager.getConnection(RUTA, USER, PW);
            return true;

        } catch (SQLException ex) {
            System.out.println("Error de conexion con BD.");
            return false;
        }
    }

    private static boolean stopConection(Connection conection) {
        try {
            conection.close();
            return true;

        } catch (SQLException ex) {
            Logger.getLogger(Protocol.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public String loginClenteBD(String usuario, String contraseña) {

        String dni = null;
        System.out.println("correo:" + usuario);
        System.out.println("contraseña" + contraseña);

        try {
            String query = "SELECT DNI FROM Usuario WHERE correo=? AND contraseña=? ;";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);

            preparedStatement.setString(1, usuario);
            preparedStatement.setString(2, contraseña);

            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                dni=rs.getString(1);
                System.out.println("ConexionDB.java DNI:"+dni); //Prueba
            }

            preparedStatement.close();
            rs.close();

        } catch (SQLException ex) {
            Logger.getLogger(Protocol.class.getName()).log(Level.SEVERE, null, ex);
        }

        return dni;
    }
    
    
    public int registrarContraseña(String correo, String contraseña) {
        
        int resultado = 0;
        //0= Error Desconocido,  1=Registrado Exitosamente,  2=Error, correo no registrado,  3=Error,contraseña registrada anteriormente

        if (!comprobarCorreoRegistrado(correo)) {
            resultado = 1;
        } else {
            if (!comprobarContraseñaNoRegistrada(correo)) {
                resultado = 2;
            } else {
                //Si existe el correo, y la contraseña aun no ha sido registrada (null), intenta registrar la contraseña nueva:
                try {
                    String query = "UPDATE usuario SET contraseña=? WERE correo=? ";
                    PreparedStatement preparedStatement = conexion.prepareStatement(query);

                    preparedStatement.setString(1, contraseña);
                    preparedStatement.setString(2, correo);

                    int nrows=preparedStatement.executeUpdate(); 

                    if(nrows>0) resultado=3;

                    preparedStatement.close();
                    

                } catch (SQLException ex) {
                    return 0;
                }

                

            }
        }
        return resultado;
    }
    
    

    public boolean isConexionEstablecida() {
        return conexionEstablecida;
    }
    

    
    
    
    public boolean comprobarCorreoRegistrado(String correo){
        //Compruba si existe un ususuario con ese correo
        boolean correoRegistrado = false;

        try {
            String query = "SELECT contraseña FROM usuario WHERE correo=?";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);

            preparedStatement.setString(1, correo);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.next()) {
                correoRegistrado=true;
            }

            preparedStatement.close();
            rs.close();

        } catch (SQLException ex) {
            Logger.getLogger(Protocol.class.getName()).log(Level.SEVERE, null, ex);
        }

        return correoRegistrado;
        
    }
    
    
    
    public boolean comprobarContraseñaNoRegistrada(String correo){
        //Compruba si existe un ususuario con ese correo
        boolean contraseñaNoRegistrada = false;
        String contraseña=null;

        try {
            String query = "SELECT contraseña FROM usuario WHERE correo=?";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);

            preparedStatement.setString(1, correo);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                contraseña=rs.getString(1);
                if(contraseña==null){
                    contraseñaNoRegistrada=true;
                }
            }

            preparedStatement.close();
            rs.close();

        } catch (SQLException ex) {
            Logger.getLogger(Protocol.class.getName()).log(Level.SEVERE, null, ex);
        }

        return contraseñaNoRegistrada;
        
    }
    
    

    String loginTrabajadorBD(String dni, String clave) {
        String resultado = null;
       

        try {
            String query = "SELECT Usuario.nombre, Usuario.apellido, Rol.id FROM Usuario, Rol WHERE Usuario.dni=? AND Usuario.id_rol=Rol.id AND Rol.clave=? ;";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);

            preparedStatement.setString(1, dni);
            preparedStatement.setString(2, clave);

            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                String nombre= rs.getString(1);
                String apellidos= rs.getString(2);
                String nombreCompleto=nombre+" "+ apellidos;
                int nRol= rs.getInt(3);
                //System.out.println("ConexionDB.java:"+nRol+nombreCompleto); //Prueba
                
                resultado=nRol+","+nombreCompleto;
            }

            preparedStatement.close();
            rs.close();

        } catch (SQLException ex) {
            Logger.getLogger(Protocol.class.getName()).log(Level.SEVERE, null, ex);
        }

        return resultado;
    }
    
    

    protected String listarTrabajadores() {
        String resultado = "S11-LISTA_TRABAJADORES";
        try {
            String query = "SELECT u.id, u.dni, u.nombre, u.apellido, u.salario,u.correo, r.nombre from usuario u, rol r where u.id_rol=r.id && u.id_rol!=3 ;"; //El rol 3 son los clientes, los unicos excluidos en esta consulta.
            PreparedStatement preparedStatement = conexion.prepareStatement(query);

            ResultSet rs = preparedStatement.executeQuery();

            int registros = 0;

            while (rs.next()) {
                int id = rs.getInt(1);
                String DNI = rs.getString(2);
                String nombre = rs.getString(3);
                String apellido = rs.getString(4);
                float salario = rs.getFloat(5);
                String correo = rs.getString(6);
                String nombreRol = rs.getString(7);

                if (registros < 1) {
                    resultado += ":";  //Para asi saber si hay almenos 1 trabajador registrado
                } else {
                    resultado += ",";  //Delimitador entre trabajador y trabajador
                }
                resultado += id + "/" + DNI + "/" + nombre + "/" + apellido + "/" + salario + "/" + correo + "/" + nombreRol;
                registros++;
                //Ira añadiendo al mensaje resultado todos los registros de trabajadores, separando sus atributos con "/"

            }

        } catch (SQLException ex) {
            resultado = "S12-ERROR_QUERY";
        }

        return resultado;
    }
    
    

    boolean eliminarTrabajador(String dni) {
        boolean eliminado=false;
        try {
            String query = "DELETE FROM usuario WHERE dni=?;";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setString(1, dni);

            int filasModificadas= preparedStatement.executeUpdate();
            //System.out.println("Filas modificadas(Eliminando):"+filasModificadas);
            
            if(filasModificadas>0) eliminado=true;
            
            
           
        } catch (SQLException ex) {
            
        }
        
        return eliminado;
    }
    
    

    int altaTrabajador(String dni, String nombre, String apellidos, String correo, float salario, int rol) {
        int resultado=0;
        try {
            String query = "INSERT INTO USUARIO (DNI, nombre, apellido, correo, salario, id_rol) VALUES" +
                "(?, ?, ?, ?, ?, ?);";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setString(1, dni);
            
            preparedStatement.setString(1, dni);
            preparedStatement.setString(2, nombre);
            preparedStatement.setString(3, apellidos);
            preparedStatement.setString(4, correo);
            preparedStatement.setFloat(5, salario);
            preparedStatement.setInt(6, rol);

            int filasModificadas= preparedStatement.executeUpdate();
           
            
            if(filasModificadas>0) resultado=1;
            else resultado=2; //La unica razon por la que puede no registrarse es por que el DNI ya este registrado
            
            
           
        } catch (SQLException ex) {
            resultado=0;
        }
        
        return resultado;
    }
    
    

    int modificarTrabajador(String dni, String nombre, String apellidos, String correo, float salario, int rol) {
        int resultado=0;
        try {
            String query = "UPDATE usuario SET nombre = ?, apellido = ?, correo = ?, salario = ?, id_rol=? WHERE DNI=?;";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            
            preparedStatement.setString(1, nombre);
            preparedStatement.setString(2, apellidos);
            preparedStatement.setString(3, correo);
            preparedStatement.setFloat(4, salario);
            preparedStatement.setInt(5, rol);

            preparedStatement.setString(6, dni);

            int filasModificadas = preparedStatement.executeUpdate();

            if (filasModificadas > 0) {
                resultado = 1;
            } else {
                resultado = 2; //La unica razon por la que puede no registrarse es por que el DNI ya este registrado
            }


        } catch (SQLException ex) {
            resultado=0;
        }
        
        return resultado;
    }

    
    
    protected String listarTrabajadoresFiltrados(String patron) {
        String resultado = "S11-LISTA_TRABAJADORES";
        try {
            String query = "SELECT u.id, u.dni, u.nombre, u.apellido, u.salario,u.correo, r.nombre from usuario u, rol r where u.id_rol=r.id AND u.id_rol!=3 AND ((u.DNI LIKE '%"+patron+"%') OR (u.correo LIKE '%"+patron+"%') OR (u.nombre LIKE '%"+patron+"%') OR (u.apellido LIKE '%"+patron+"%'))"; //El rol 3 son los clientes, los unicos excluidos en esta consulta.
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            
           
            ResultSet rs = preparedStatement.executeQuery();
            
            int registros=0;
            
            while(rs.next()){
                int id=rs.getInt(1);
                String DNI=rs.getString(2);
                String nombre=rs.getString(3);
                String apellido=rs.getString(4);
                float salario=rs.getFloat(5);
                String correo=rs.getString(6);
                String nombreRol=rs.getString(7);
                
                if(registros<1){
                    resultado+=":";  //Para asi saber si hay almenos 1 trabajador registrado
                }else{
                    resultado+=",";  //Delimitador entre trabajador y trabajador
                }   
                resultado+=id+"/"+DNI+"/"+nombre+"/"+apellido+"/"+salario+"/"+correo+"/"+nombreRol;
                registros++;
                //Ira añadiendo al mensaje resultado todos los registros de trabajadores, separando sus atributos con "/"
                
                
            }

           
        } catch (SQLException ex) {
            resultado="S12-ERROR_QUERY";
            ex.printStackTrace();
        }
        
        return resultado;
    }

    
    
    protected String filtrarRol(int nRol, String patron) {
        String resultado = "S11-LISTA_TRABAJADORES";
        try {
            String query;
            PreparedStatement preparedStatement;
            
            if(nRol<3){
                query = "SELECT u.id, u.dni, u.nombre, u.apellido, u.salario,u.correo, r.nombre from usuario u, rol r where u.id_rol=r.id AND u.id_rol=? AND ((u.DNI LIKE '%"+patron+"%') OR (u.correo LIKE '%"+patron+"%') OR (u.nombre LIKE '%"+patron+"%') OR (u.apellido LIKE '%"+patron+"%'))";
                preparedStatement = conexion.prepareStatement(query);
                preparedStatement.setInt(1, nRol);
            } 
            else{
                query= "SELECT u.id, u.dni, u.nombre, u.apellido, u.salario,u.correo, r.nombre from usuario u, rol r where u.id_rol=r.id AND u.id_rol!=1 AND u.id_rol!=2 AND u.id_rol!=3  AND ((u.DNI LIKE '%"+patron+"%') OR (u.correo LIKE '%"+patron+"%') OR (u.nombre LIKE '%"+patron+"%') OR (u.apellido LIKE '%"+patron+"%')) ORDER BY u.id_rol";
                preparedStatement = conexion.prepareStatement(query);
            }
   
            ResultSet rs = preparedStatement.executeQuery();
            
            int registros=0;
            
            while(rs.next()){
                int id=rs.getInt(1);
                String DNI=rs.getString(2);
                String nombre=rs.getString(3);
                String apellido=rs.getString(4);
                float salario=rs.getFloat(5);
                String correo=rs.getString(6);
                String nombreRol=rs.getString(7);
                
                if(registros<1){
                    resultado+=":";  //Para asi saber si hay almenos 1 trabajador registrado
                }else{
                    resultado+=",";  //Delimitador entre trabajador y trabajador
                }   
                resultado+=id+"/"+DNI+"/"+nombre+"/"+apellido+"/"+salario+"/"+correo+"/"+nombreRol;
                registros++;
                //Ira añadiendo al mensaje resultado todos los registros de trabajadores, separando sus atributos con "/"
                
                
            }

           
        } catch (SQLException ex) {
            resultado="S12-ERROR_QUERY";
            ex.printStackTrace();
        }
        
        return resultado;
    }
    
    

    String listarClaves() {
        String resultado = "S19-LISTA_CLAVES";
        try {
            String query = "SELECT id, nombre, clave from rol;"; //El rol 3 son los clientes, los unicos excluidos en esta consulta.
            PreparedStatement preparedStatement = conexion.prepareStatement(query);

            ResultSet rs = preparedStatement.executeQuery();

            int registros = 0;

            while (rs.next()) {
                int id = rs.getInt(1);
                String nombre = rs.getString(2);
                String clave = rs.getString(3);

                if (registros < 1) {
                    resultado += ":";  //Para asi saber si hay almenos 1 trabajador registrado
                } else {
                    resultado += ",";  //Delimitador entre trabajador y trabajador
                }
                resultado += id + "/" + nombre + "/" + clave;
                registros++;
                //Ira añadiendo al mensaje resultado todos los registros de trabajadores, separando sus atributos con "/"

            }

        } catch (SQLException ex) {
            resultado = "S12-ERROR_QUERY";
        }
        
        return resultado;    }

    
    
    
    boolean altaRol(String nombre, String clave) {
        boolean registrado=false;
        try {
            String query = "INSERT INTO rol (nombre, clave)VALUES" + "(?, ?);";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);

            preparedStatement.setString(1, nombre);
            preparedStatement.setString(2, clave);

            int filasModificadas = preparedStatement.executeUpdate();

            if (filasModificadas > 0) {
                registrado = true;
            }

           
        } catch (SQLException ex) {
            registrado=false;
            ex.printStackTrace();
        }
        
        return registrado;
    }

    
    
    boolean bajaRol(int id) {
        boolean eliminado = false;
        if (id > 3) {
            try {
                String query = "DELETE FROM rol WHERE id=?;";
                PreparedStatement preparedStatement = conexion.prepareStatement(query);
                preparedStatement.setInt(1, id);

                int filasModificadas = preparedStatement.executeUpdate();

                if (filasModificadas > 0) {
                    eliminado = true;
                }

            } catch (SQLException ex) {

            }
        }
        return eliminado;
    }

    String listarClientes() {
        String resultado = "S20-LISTA_CLIENTES";
        try {
            String query = "SELECT u.dni, u.nombre, u.apellido, u.correo from usuario u, rol r where u.id_rol=r.id && u.id_rol=3 ;"; //(El rol 3 son los clientes)
            PreparedStatement preparedStatement = conexion.prepareStatement(query);

            ResultSet rs = preparedStatement.executeQuery();

            int registros = 0;

            while (rs.next()) {
                
                String DNI = rs.getString(1);
                String nombre = rs.getString(2);
                String apellido = rs.getString(3);
                String correo = rs.getString(4);

                if (registros < 1) {
                    resultado += ":";  //Para asi saber si hay almenos 1 trabajador registrado
                } else {
                    resultado += ",";  //Delimitador entre trabajador y trabajador
                }
                resultado += DNI + "/" + nombre + "/" + apellido + "/" + correo;
                registros++;
                //Ira añadiendo al mensaje resultado todos los registros de trabajadores, separando sus atributos con "/"

            }

        } catch (SQLException ex) {
            resultado = "S12-ERROR_QUERY";
        }

        return resultado;
    }

    String listarClientesFiltrados(String patron) {
         String resultado = "S20-LISTA_CLIENTES";
        try {
            String query = "SELECT u.dni, u.nombre, u.apellido, u.correo from usuario u, rol r where u.id_rol=r.id AND u.id_rol=3 AND ((u.DNI LIKE '%"+patron+"%') OR (u.correo LIKE '%"+patron+"%') OR (u.nombre LIKE '%"+patron+"%') OR (u.apellido LIKE '%"+patron+"%'))"; 
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            
           
            ResultSet rs = preparedStatement.executeQuery();
            
            int registros=0;
            
            while(rs.next()){
               
                String DNI=rs.getString(1);
                String nombre=rs.getString(2);
                String apellido=rs.getString(3);
                String correo=rs.getString(4);
                
                if(registros<1){
                    resultado+=":";  //Para asi saber si hay almenos 1 trabajador registrado
                }else{
                    resultado+=",";  //Delimitador entre trabajador y trabajador
                }   
                resultado+=DNI+"/"+nombre+"/"+apellido+"/"+correo;
                registros++;
                //Ira añadiendo al mensaje resultado todos los registros de trabajadores, separando sus atributos con "/"
                
                
            }

           
        } catch (SQLException ex) {
            resultado="S12-ERROR_QUERY";
            ex.printStackTrace();
        }
        
        return resultado;
    }

    String mostrarCliente(String dniCliente) {
         String resultado = "S21-INFO_CLIENTE";
        try {
            String query = "SELECT u.dni, u.nombre, u.apellido,u.fecha, u.peso, u.altura,u.notas, u.rutaImg from usuario u, rol r where u.id_rol=r.id AND u.id_rol=3 AND u.dni=?"; 
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            
            preparedStatement.setString(1, dniCliente);
            ResultSet rs = preparedStatement.executeQuery();
            
            int registros=0;
            
            while(rs.next()){
               
                String DNI=rs.getString(1);
                String nombre=rs.getString(2);
                String apellido=rs.getString(3);
                Date fecha=rs.getDate(4);
                int peso=rs.getInt(5);
                float altura=rs.getFloat(6);
                String notas=rs.getString(7);
                String ruta=rs.getString(8);
                
               
                
                if(registros<1){
                    resultado+=":";  //Para asi saber si hay almenos 1 trabajador registrado
                }else{
                    resultado+=",";  //Delimitador entre trabajador y trabajador
                }   
                resultado+=DNI+"&"+nombre+"&"+apellido+"&"+fecha+"&"+peso+"&"+altura+"&"+notas+"&"+ruta;
                registros++;
                //Ira añadiendo al mensaje resultado todos los registros de trabajadores, separando sus atributos con "/"
                
                
            }

           
        } catch (SQLException ex) {
            resultado="S12-ERROR_QUERY";
            ex.printStackTrace();
        }
        
        return resultado;
    }

    String altaCliente(String dniCliente, String nombre, String apellidos, String correo, int peso, float altura, String rutaFoto, String notas) {
        String resultado;
        try {
            String query = "INSERT INTO USUARIO (DNI, nombre, apellido, correo, peso, altura, rutaImg, notas) VALUES" +
                "(?, ?, ?, ?, ?, ?, ?, ?);";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            
            preparedStatement.setString(1, dniCliente);
            preparedStatement.setString(2, nombre);
            preparedStatement.setString(3, apellidos);
            preparedStatement.setString(4, correo);
            preparedStatement.setInt(5, peso);
            preparedStatement.setFloat(6, altura);
            preparedStatement.setString(7, rutaFoto);
            preparedStatement.setString(8, notas);
           

            int filasModificadas= preparedStatement.executeUpdate();
            if(filasModificadas>0)  resultado="S22-CLIENTE_REGISTRADO";
            else                    resultado="S23-CLIENTE_REPETIDO";
            
            
           
        } catch (SQLException ex) {
                                    resultado="S24-ERROR_SUBIDA_CLIENTE";
        }
        
        return resultado;
    }

    protected boolean eliminarFoto(String rutaImg) {
        boolean eliminada=false;
        File imagenFile=new File(rutaImg);
        if(imagenFile.exists()){
            System.out.println("ConexionDB: entra qui a eliminar: existe el File");
            //Comprobar si es una foto
            String mimetype = new MimetypesFileTypeMap().getContentType(imagenFile);
            String type = mimetype.split("/")[0];
            if (type.equals("image")) {
                eliminada=imagenFile.delete();
            }
            
        }
        return eliminada;
    }
    protected boolean eliminarCliente(String dni) {

        boolean eliminado = false;
        try {
            String query = "DELETE FROM usuario WHERE dni=? AND id_rol=3;";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setString(1, dni);

            int filasModificadas = preparedStatement.executeUpdate();

            if(filasModificadas>0) eliminado=true;
            
        } catch (SQLException ex) {
            eliminado=false;
        }
        
        return eliminado;
    }

    String altaClase(String nombre, int aforo, String rutaImg, String descripcion) {
        String resultado;
        try {
            String query = "INSERT INTO CLASE (nombre, aforo_maximo, rutaImg, descripcion) VALUES" +
                "(?, ?, ?, ?);";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            
            preparedStatement.setString(1, nombre);
            preparedStatement.setInt(2, aforo);
            preparedStatement.setString(3, rutaImg);
            preparedStatement.setString(4, descripcion);

            int filasModificadas= preparedStatement.executeUpdate();
            if(filasModificadas>0)  resultado="S30-CLASE_REGISTRADA";
            else                    resultado="S31-CLASE_REPETIDA";
            
    
        } catch (SQLException ex) {
                                    resultado="S24-ERROR_SUBIDA_CLIENTE";
        }
        
        return resultado;
    }
    
    
    String listarClases() {
        String resultado = "S31-LISTA_CLASES";
        try {
            String query = "SELECT nombre from clase;"; //(El rol 3 son los clientes)
            PreparedStatement preparedStatement = conexion.prepareStatement(query);

            ResultSet rs = preparedStatement.executeQuery();

            int registros = 0;

            while (rs.next()) {
                
                String nombre = rs.getString(1);
                
                if (registros < 1) {
                    resultado += ":";  //Para asi saber si hay almenos 1 trabajador registrado
                } else {
                    resultado += ",";  //Delimitador entre trabajador y trabajador
                }
                resultado += nombre;
                registros++;

            }

        } catch (SQLException ex) {
            resultado = "S12-ERROR_QUERY";
        }

        return resultado;
    }

    String mostrarClase(String nombreClase) {
         String resultado = "S32-INFO_CLASE";
        try {
            String query = "SELECT nombre, aforo_maximo,descripcion, id, rutaImg from clase where nombre=?"; 
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            
            preparedStatement.setString(1, nombreClase);
            ResultSet rs = preparedStatement.executeQuery();
            
            int registros=0;
            
            while(rs.next()){

                String nombre=rs.getString(1);
                int aforo=rs.getInt(2);
                String descripcion=rs.getString(3);                
                int id=rs.getInt(4);
                String rutaImg=rs.getString(5);

                
                if(registros<1){
                    resultado+=":";  //Para asi saber si hay almenos 1 trabajador registrado
                }else{
                    resultado+=",";  //Delimitador entre trabajador y trabajador
                }   
                resultado+=nombre+"&"+aforo+"&"+descripcion+"&"+id+"&"+rutaImg;
                registros++;
                //Ira añadiendo al mensaje resultado todos los registros de trabajadores, separando sus atributos con "/"
                
                
            }

           
        } catch (SQLException ex) {
            resultado="S12-ERROR_QUERY";
            ex.printStackTrace();
        }
        
        return resultado;
    }

    boolean eliminarClase(String nombreClaseEliminada) {
        boolean eliminado = false;
        try {
            String query = "DELETE FROM clase WHERE nombre=?";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setString(1, nombreClaseEliminada);

            int filasModificadas = preparedStatement.executeUpdate();

            if(filasModificadas>0) eliminado=true;
            
        } catch (SQLException ex) {
            eliminado=false;
        }
        
        return eliminado;
    }
    
    
    String listarHorarios() {
        String resultado = "S35-HORARIO";
        try {
            String query = "SELECT horario.id,horario.dia,horario.hora, clase.nombre, usuario.nombre from horario INNER JOIN clase ON horario.id_clase=clase.id INNER JOIN usuario ON horario.id_entrenador=usuario.id ORDER BY horario.hora ASC;"; 
            PreparedStatement preparedStatement = conexion.prepareStatement(query);

            ResultSet rs = preparedStatement.executeQuery();

            int registros = 0;

            while (rs.next()) {
                
                int id = rs.getInt(1);
                int dia = rs.getInt(2);
                Time horaTime=rs.getTime(3);
                String nombreClase = rs.getString(4);
                String nombreEntrenador= rs.getString(5);
                
                
                int horas=horaTime.getHours();
                int minutos=horaTime.getMinutes();
                String hora=horas+":"+minutos;
                if(minutos==0) hora+="0"; //Esto por que si es 0 imprime 14:0 en lugar de 14:00
                System.out.println("Hora:"+hora);

                if (registros < 1) {
                    resultado += ":";  //Para asi saber si hay almenos 1 trabajador registrado
                } else {
                    resultado += ",";  //Delimitador entre trabajador y trabajador
                }
                resultado += id + "&" + dia + "&" + hora + "&" + nombreClase+ "&" + nombreEntrenador;
                registros++;
                //Ira añadiendo al mensaje resultado todos los registros de trabajadores, separando sus atributos con "/"

            }

        } catch (SQLException ex) {
            resultado = "S12-ERROR_QUERY";
        }

        return resultado;
    }
    String listarClasesId() {
        String resultado = "S36-LISTA_CLASES";
        try {
            String query = "SELECT id, nombre from clase ORDER BY nombre ASC;"; 
            PreparedStatement preparedStatement = conexion.prepareStatement(query);

            ResultSet rs = preparedStatement.executeQuery();

            int registros = 0;

            while (rs.next()) {
                
                int id = rs.getInt(1);
                String nombreClase = rs.getString(2);
                
                if (registros < 1) {
                    resultado += ":";  //Para asi saber si hay almenos 1 trabajador registrado
                } else {
                    resultado += ",";  //Delimitador entre trabajador y trabajador
                }
                resultado += id + "&" + nombreClase;
                registros++;
                //Ira añadiendo al mensaje resultado todos los registros de trabajadores, separando sus atributos con "/"

            }

        } catch (SQLException ex) {
            resultado = "S12-ERROR_QUERY";
        }

        return resultado;
    }
    String listarEntrenadores() {
        String resultado = "S37-LISTA_ENTRENADORES";
        try {
            String query = "SELECT id, nombre, apellido from usuario WHERE id_rol=2 ORDER BY nombre ASC"; 
            PreparedStatement preparedStatement = conexion.prepareStatement(query);

            ResultSet rs = preparedStatement.executeQuery();
            
            int registros = 0;

            while (rs.next()) {
                
                int id = rs.getInt(1);
                String nombre = rs.getString(2);
                String apellido= rs.getString(3);

                if (registros < 1) {
                    resultado += ":";  //Para asi saber si hay almenos 1 trabajador registrado
                } else {
                    resultado += ",";  //Delimitador entre trabajador y trabajador
                }
                resultado += id + "&" + nombre + "&" + apellido;
                registros++;
                //Ira añadiendo al mensaje resultado todos los registros de trabajadores, separando sus atributos con "/"

            }

        } catch (SQLException ex) {
            resultado = "S12-ERROR_QUERY";
        }

        return resultado;
    }

    public String altaHorario(int idClase, int idEntrenador, int nDia, Time hora) {
       String resultado;
        try {
            String query = "INSERT INTO HORARIO (id_clase, id_entrenador, hora, dia) VALUES" +
                "(?, ?, ?, ?);";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            
            preparedStatement.setInt(1, idClase);
            preparedStatement.setInt(2, idEntrenador);
            preparedStatement.setTime(3, hora);
            preparedStatement.setInt(4, nDia);
            

            int filasModificadas= preparedStatement.executeUpdate();
            if(filasModificadas>0)  resultado="S38-HORARIO_REGISTRADO";
            else                    resultado="S39-ERROR_REGISTRO:Error de registro. Es posible que la clase o el entrenador hayan sido modificadas/eliminadas. Intentelo de nuevo.";
            
    
        } catch (SQLException ex) {
            if(ex.toString().contains("Duplicate")) return resultado="S39-ERROR_REGISTRO:Registro duplicado. El entrenador imparte otra clase en ese mismo horario";            
            
            resultado="S39-ERROR_REGISTRO: Error de insercion, revise los parametros seleccionados. Pueden haber sido modificados/eliminados.";
            ex.printStackTrace();
        }
        
        return resultado;
    }

    String restablecerHorario() {
      String resultado;
        try {
            String query = "DELETE FROM horario";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);            

            int filasModificadas= preparedStatement.executeUpdate();
            if(filasModificadas>0)  resultado="S40-HORARIO_RESTABLECIDO";
            else                    resultado="S41-ERROR_RESTABLECER";
            
    
        } catch (SQLException ex) {
            ex.printStackTrace();
            resultado="S0-ERROR";
        }
        
        return resultado;
    }

    String eliminarHorario(int id) {
        
        String resultado;
        try {
            String query = "DELETE FROM horario WHERE id=?";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setInt(1, id);

            int filasModificadas= preparedStatement.executeUpdate();
            if(filasModificadas>0)  resultado="S40-HORARIO_ELIMINADO";
            else                    resultado="S41-ERROR_ELIMINACION:No se ha podido eliminar. La clase ha debido de ser modificada/eliminada ya.";
            
    
        } catch (SQLException ex) {
            ex.printStackTrace();
            resultado="S41-ERROR_ELIMINACION:No se ha podido eliminar. La clase ha debido de ser modificada/eliminada ya.";
        }
        
        return resultado;
        
    }

    String listarEjercicios() {
        String resultado = "S42-LISTA_EJERCICIOS";
        try {
            String query = "SELECT id, nombre, descripcion, tipo, rutaImg, rutaVideo from ejercicio ORDER BY nombre ASC"; 
            PreparedStatement preparedStatement = conexion.prepareStatement(query);

            ResultSet rs = preparedStatement.executeQuery();
            
            int registros = 0;

            while (rs.next()) {
                
                int id = rs.getInt(1);
                String nombre = rs.getString(2);
                String descripcion= rs.getString(3);
                String tipo= rs.getString(4);
                String musculos=listarMusculosDeEjercicio(id);
                String rutaImg=rs.getString(5);
                String rutaVideo=rs.getString(6);

                if (registros < 1) {
                    resultado += ":";  //Para asi saber si hay almenos 1 trabajador registrado
                } else {
                    resultado += ",";  //Delimitador entre trabajador y trabajador
                }
                resultado += id + "&" + nombre + "&" + descripcion+ "&" + tipo+ "&" + musculos+ "&" + rutaImg+ "&" + rutaVideo;
                registros++;
                
                
            }

        } catch (SQLException ex) {
            resultado = "S12-ERROR_QUERY";
        }

        return resultado;
    }

    public String listarMusculosDeEjercicio(int id) {
        String resultado = " ";
        try {
            String query = "SELECT musculo.nombre FROM musculo INNER JOIN ejercicio_musculo_mtm ON musculo.id=ejercicio_musculo_mtm.id_musculo WHERE ejercicio_musculo_mtm.id_ejercicio=?;";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setInt(1, id);
            ResultSet rs = preparedStatement.executeQuery();  

            int registros = 0;

            while (rs.next()) {

                String nombre = rs.getString(1);

                if (registros < 1) {
                    resultado = "@";
                } else {
                    resultado += "@";
                }
                resultado += nombre;
                registros++;

            }

        } catch (SQLException ex) {
            resultado = " ";
        }

        return resultado;
    }

    String listarMusculos() {
        String resultado = "S43-LISTA_MUSCULOS";
        try {
            String query = "SELECT nombre FROM musculo ORDER BY nombre ASC"; 
            PreparedStatement preparedStatement = conexion.prepareStatement(query);

            ResultSet rs = preparedStatement.executeQuery();
            
            int registros = 0;

            while (rs.next()) {
                String nombre = rs.getString(1);

                if (registros < 1) {
                    resultado += ":";  //Para asi saber si hay almenos 1 trabajador registrado
                } else {
                    resultado += ",";  //Delimitador entre trabajador y trabajador
                }
                resultado += nombre;
                registros++;
                
                
            }

        } catch (SQLException ex) {
            resultado = "S12-ERROR_QUERY";
        }

        return resultado;
    }

    String listarEjerciciosFiltrados(String tipo, String musculo, String nombreBusqueda) {
        String resultado = "S42-LISTA_EJERCICIOS";
        try {
            String query = "SELECT e.id, e.nombre, e.descripcion, e.tipo, e.rutaImg, e.rutaVideo from ejercicio e INNER JOIN ejercicio_musculo_mtm em ON e.id=em.id_ejercicio INNER JOIN musculo m ON m.id=em.id_musculo "
                    + "WHERE m.nombre LIKE '%"+musculo+"%'AND e.tipo LIKE '%"+tipo+"%' AND e.nombre LIKE '%"+nombreBusqueda+"%' GROUP BY e.nombre"; 
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            ResultSet rs = preparedStatement.executeQuery();
            
            int registros = 0;

            while (rs.next()) {
                
                int id = rs.getInt(1);
                String nombre = rs.getString(2);
                String descripcion= rs.getString(3);
                String tip= rs.getString(4);
                String musculos=listarMusculosDeEjercicio(id);
                String rutaImg=rs.getString(5);
                String rutaVideo=rs.getString(6);

                if (registros < 1) {
                    resultado += ":";  //Para asi saber si hay almenos 1 trabajador registrado
                } else {
                    resultado += ",";  //Delimitador entre trabajador y trabajador
                }
                resultado += id + "&" + nombre + "&" + descripcion+ "&" + tip+ "&" + musculos+ "&" + rutaImg+ "&" + rutaVideo;
                registros++;
                
                
            }
            if(tipo.equals("")){
                //Si filtra por tipo "todos" los de cardio no van a salir por que no tienen musculos, y los iner join en esta consulta no funcionan, por lo que se hacen en otra consulta y se añaden
                String listaEjerciciosCardio=listarEjerciciosFiltradosCardio(nombreBusqueda);
                if(!listaEjerciciosCardio.equals("")){
                    resultado+=","+listaEjerciciosCardio;
                }
            }

        } catch (SQLException ex) {
            resultado = "S12-ERROR_QUERY";
        }

        return resultado;
    }

    String eliminarEjercicio(int id) {
        String resultado;
        try {
            String query = "DELETE FROM ejercicio WHERE id=?";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setInt(1, id);

            int filasModificadas= preparedStatement.executeUpdate();
            if(filasModificadas>0)  resultado="S44-EJERCICIO_ELIMINADOS";
            else                    resultado="45-ERROR_ELIMINACION: No se ha podido eliminar. El ejercicio ha debido de ser modificada/eliminada ya, actualice el sistema.";
            
    
        } catch (SQLException ex) {
            ex.printStackTrace();
            resultado="S44-EJERCICIO_ELIMINADO: error'"+ex.getMessage()+"'";
        }
        
        return resultado;
    }

    String listarEjerciciosFiltradosCardio(String nombreBusqueda) {
        String resultado="";
        try {
            String query = "SELECT e.id, e.nombre, e.descripcion, e.tipo, e.rutaImg, e.rutaVideo from ejercicio e "
                    + "WHERE e.tipo LIKE '%cardio%' AND e.nombre LIKE '%"+nombreBusqueda+"%' GROUP BY e.nombre"; 
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            ResultSet rs = preparedStatement.executeQuery();
            
            int registros = 0;

            while (rs.next()) {
                
                int id = rs.getInt(1);
                String nombre = rs.getString(2);
                String descripcion= rs.getString(3);
                String tip= rs.getString(4);
                String musculos="";
                String rutaImg=rs.getString(5);
                String rutaVideo=rs.getString(6);

                 if (registros>1) {
                    resultado += ",";  //Delimitador entre trabajador y trabajador
                }
                resultado += id + "&" + nombre + "&" + descripcion+ "&" + tip+ "&" + musculos+ "&" + rutaImg+ "&" + rutaVideo;
                registros++;
                
                
            }

        } catch (SQLException ex) {
            resultado = "";
        }

        return resultado;
    }

    String altaMusculo(String nombreMusculo) {
        String resultado;
        try {
            String query = "INSERT INTO musculo (nombre) VALUES (?);";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            
            preparedStatement.setString(1, nombreMusculo);

            int filasModificadas= preparedStatement.executeUpdate();
            if(filasModificadas>0)  resultado="S46-CLASE_REGISTRADA";
            else                    resultado="S47-ERROR_REGISTRO:Error de insercion. Actualice los parametros seleccionados (musulos, tipos...). Pueden haber sido modificados/eliminados.";
            
    
        } catch (SQLException ex) {
            if(ex.toString().contains("Duplicate")) return resultado="S47-ERROR_REGISTRO:Registro duplicad";            
            
            resultado="S39-ERROR_REGISTRO: Error de insercion. Actualize los parametros seleccionados (musulos, tipos...). Pueden haber sido modificados/eliminados.";
            ex.printStackTrace();
        }
        
        return resultado;
    }



    String altaEjercicio(String nombreEjercicio, String tipo, String descripcion, String rutaImg, String rutaVideo, ArrayList<String> listaMusculos) {
        String resultado;
        try {
            String query = "INSERT INTO ejercicio (nombre,descripcion,tipo, rutaImg, rutaVideo) VALUES (?, ?, ?, ?, ?);";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            
            preparedStatement.setString(1, nombreEjercicio);
            preparedStatement.setString(2, descripcion);
            preparedStatement.setString(3, tipo);
            preparedStatement.setString(4, rutaImg);
            preparedStatement.setString(5, rutaVideo);
            

            int filasModificadas= preparedStatement.executeUpdate();
            if(filasModificadas>0)  resultado=registrarMusculosDelEjercicio(nombreEjercicio,listaMusculos);
            else                    resultado="S47-ERROR_REGISTRO: Error de insercion. Actualize los parametros seleccionados (musulos, tipos...). Pueden haber sido modificados/eliminados.";
            
    
        } catch (SQLException ex) {
            if(ex.toString().contains("Duplicate")) return resultado="S47-ERROR_REGISTRO:duplicate";            
            
            resultado="S47-ERROR_REGISTRO: Error de insercion. Actualize los parametros seleccionados (musulos, tipos...). Pueden haber sido modificados/eliminados.";
            ex.printStackTrace();
        }
        
        return resultado; 
    }

    private String registrarMusculosDelEjercicio(String nombreEjercicio, ArrayList<String> listaMusculos) {
        String resultado;
        
        
        int idEjercicio=0;
        
        try {
            String query1 = "Select id From ejercicio where nombre=?;";
            PreparedStatement preparedStatement1 = conexion.prepareStatement(query1);
            preparedStatement1.setString(1, nombreEjercicio);
            ResultSet rs = preparedStatement1.executeQuery();
            rs.next();
            idEjercicio=rs.getInt(1);
           
            for(String nombreMusculo: listaMusculos){
                String query2 = "SELECT id FROM musculo WHERE nombre=?;";
                PreparedStatement preparedStatement2 = conexion.prepareStatement(query2);
                preparedStatement2.setString(1, nombreMusculo);
                ResultSet rs2 = preparedStatement2.executeQuery();
                rs2.next();
                int idMusculo=rs2.getInt(1);
                
                String query3 = "insert into ejercicio_musculo_mtm (id_ejercicio,id_musculo) VALUES (?,?);";
                PreparedStatement preparedStatement3 = conexion.prepareStatement(query3);
                preparedStatement3.setInt(1, idEjercicio);
                preparedStatement3.setInt(2, idMusculo);
                preparedStatement3.executeUpdate();
      
            }

        } catch (SQLException ex) {
            System.out.println(ex);
        }

        return "S49-MUSCULO_REGISTRADO";
    }

    String bajaMusculo(String nombre) {
         String resultado;
        try {
            String query = "DELETE FROM musculo WHERE nombre=?";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setString(1, nombre);

            int filasModificadas= preparedStatement.executeUpdate();
            if(filasModificadas>0)  resultado="S51-MUSCULO_ELIMINADO";
            else                    resultado="S50-MUSCULO_NO_ENCONTRADO";
            
    
        } catch (SQLException ex) {
            ex.printStackTrace();
            resultado="S0-ERROR:El musculo no ha podido ser eliminado por razones desconocidas. (contacte con un tecnico)";
        }
        
        return resultado;
    }

    String altaTablaModelo(String nombreTabla, int nDias, ArrayList<Object[]> listaEjercicios) {
        String resultado;
        try {
            String query = "INSERT INTO tabla(nombre, dias, tabla_Base) VALUES (?, ?, ?);";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            
           
            preparedStatement.setString(1, nombreTabla);
            preparedStatement.setInt(2, nDias);
            preparedStatement.setBoolean(3, true);            

            int filasModificadas= preparedStatement.executeUpdate();
            if(filasModificadas>0)  resultado=registrarEjerciciosDeLaTabla(nombreTabla,listaEjercicios);
            else                    resultado="S47-ERROR_REGISTRO: Error de insercion. Actualize los parametros seleccionados (musulos, tipos...). Pueden haber sido modificados/eliminados.";
            
    
        } catch (SQLException ex) {
            if(ex.toString().contains("Duplicate")) return resultado="S47-ERROR_REGISTRO:duplicate";            
            
            resultado="S47-ERROR_REGISTRO: Error de insercion. Actualize los parametros seleccionados (musulos, tipos...). Pueden haber sido modificados/eliminados.";
            ex.printStackTrace();
        }
        
        return resultado; 
    }
    
    
    private String registrarEjerciciosDeLaTabla(String nombreTabla, ArrayList<Object[]> listaEjercicios) {
        String resultado;
        
        int idTabla=0;
        
        try {
            String query1 = "Select id From tabla where nombre=?;";
            PreparedStatement preparedStatement1 = conexion.prepareStatement(query1);
            preparedStatement1.setString(1, nombreTabla);
            ResultSet rs = preparedStatement1.executeQuery();
            rs.next();
            idTabla=rs.getInt(1);
           
            for(Object[] datosEjercicio: listaEjercicios){
                int nDia=(int)datosEjercicio[0];
                int idEjercicio=(int)datosEjercicio[1];
                
                int reps=0;
                int series=0;
                try{
                    reps=(int)datosEjercicio[2];
                    series=(int)datosEjercicio[3];
                }catch(Exception e){
                    //Si vienen vacias pues se quedan en 0 y ya está
                }
                String tiempo=(String)datosEjercicio[4];
                
                try{
                String query3 = "insert into ej_tabla_mtm (id_ejercicio,id_tabla, dia,series, repeticiones,tiempo) VALUES (?,?,?,?,?,?);";
                PreparedStatement preparedStatement3 = conexion.prepareStatement(query3);
                preparedStatement3.setInt(1, idEjercicio);
                preparedStatement3.setInt(2, idTabla);
                preparedStatement3.setInt(3, nDia);
                preparedStatement3.setInt(4, reps);
                preparedStatement3.setInt(5, series);
                preparedStatement3.setString(6, tiempo);
                preparedStatement3.executeUpdate();
                
                }catch(MySQLIntegrityConstraintViolationException ex){
                    //Si hay algun registro de ejercicio duplicado idEjercicio/dia/idTabla (Que no deberia por que esta controlado en la app cliente) no pete todo.
                }
            }

        } catch (SQLException ex) {
            System.out.println(ex);
        }

        return "S52-TABLA_REGISTRADA";
    }

    
    
}
