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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
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
        //Obtener TODOS los datos del usuario, para desplegarlos automaticamente con foto y todo.
        String resultado = "S2-LOG_INCORRECTO";
        System.out.println("correo:" + usuario);
        System.out.println("contraseña" + contraseña);

        try {
            String query = "SELECT id, DNI, nombre, apellido, correo, contraseña, rutaImg, peso, altura  FROM usuario WHERE correo=? AND contraseña=?";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);

            preparedStatement.setString(1, usuario);
            preparedStatement.setString(2, contraseña);

            ResultSet rs = preparedStatement.executeQuery();
            
            if(rs.next()!=false){
                int id=rs.getInt(1);
                String DNI=rs.getString(2);
                String nombre=rs.getString(3);
                String apellido=rs.getString(4);
                String correo=rs.getString(5);
                contraseña=rs.getString(6);
                String rutaImg=rs.getString(7);
                int peso=rs.getInt(8);
                float altura=rs.getFloat(9);
                
                resultado="S1-LOG_CORRECTO:"+id+"&"+DNI+"&"+nombre+"&"+apellido+"&"+correo+"&"+contraseña+"&"+rutaImg+"&"+peso+"&"+altura;
                
            }else{
                resultado="S2-LOG_INCORRECTO";
            }   

            preparedStatement.close();
            rs.close();

        } catch (SQLException ex) {
            resultado="S3-ERROR_DB";
            Logger.getLogger(Protocol.class.getName()).log(Level.SEVERE, null, ex);
        }

        return resultado;
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
            else resultado=2; //La unica razon por la que puede no registrarse es por que el DNI ya este registrado, pero puede taambien saltar en este caso sql exception duplicate
            
            
           
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            if(ex.getMessage().contains("Duplicate")) return 2;
            resultado=0;
        }
        
        return resultado;
    }
    
    

    public int modificarTrabajador(String dni, String nombre, String apellidos, String correo, float salario, int rol) {
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
            if(ex.getMessage().contains("Duplicate")) return 2;
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

    String listarTablas(String dniCliente) {
        String resultado = "S53-LISTA_TABLAS";
        try {
            String query;
            PreparedStatement preparedStatement;
            if(dniCliente==null){
                query = "SELECT t.id, t.nombre,t.dias FROM tabla t WHERE t.tabla_base=?;"; //(El rol 3 son los clientes)
                preparedStatement = conexion.prepareStatement(query);
                preparedStatement.setBoolean(1, true);

            }else{
                query = "SELECT t.id, t.nombre,t.dias FROM tabla t INNER JOIN tabla_usuario_mtm ut ON t.id=ut.id_tabla INNER JOIN usuario u ON ut.id_usuario=u.id WHERE u.DNI=?;"; //(El rol 3 son los clientes)
                preparedStatement = conexion.prepareStatement(query);
                preparedStatement.setString(1, dniCliente);
            }
            

            ResultSet rs = preparedStatement.executeQuery();

            int registros = 0;

            while (rs.next()) {
                
                int id = rs.getInt(1);
                String nombre = rs.getString(2);
                int nDias = rs.getInt(3);


                if (registros < 1) {
                    resultado += ":";  //Para asi saber si hay almenos 1 tabla registrada
                } else {
                    resultado += ",";  //Delimitador entre tabla y tabla
                }
                resultado += id + "$" + nombre + "$" + nDias;
                registros++;
                //Ira añadiendo al mensaje resultado todos los registros de tablas, separando sus atributos con "/"

            }

        } catch (SQLException ex) {
            resultado = "S12-ERROR_QUERY";
        }

        return resultado;
    }

    String asociarTablaAUsuario(String dniCliente, int idTabla) {
        String resultado;
        System.out.println("---------Dni:"+dniCliente+"  IdTabla:"+idTabla);
        int idCliente;

        try {
            String query1 = "SELECT id FROM usuario WHERE dni=?;";
            PreparedStatement preparedStatement1 = conexion.prepareStatement(query1);
            preparedStatement1.setString(1, dniCliente);
            ResultSet rs = preparedStatement1.executeQuery();
            rs.next();
            idCliente = rs.getInt(1);
            System.out.println("-------idCliente:"+idCliente);
            try {
                String query3 = "INSERT INTO tabla_usuario_mtm (id_tabla, id_usuario) VALUES (?,?);";
                PreparedStatement preparedStatement3 = conexion.prepareStatement(query3);
                preparedStatement3.setInt(1, idTabla);
                preparedStatement3.setInt(2, idCliente);
                preparedStatement3.executeUpdate();
                return "S54-CORRECTO";
            } catch (MySQLIntegrityConstraintViolationException ex) {
                return "S55-INCORRECTO:duplicate";
            }
        } catch (SQLException ex) {
            System.out.println(ex);
            return "S12-ERROR_QUERY";
        }

        
    }

    String listarHorariosMovil() {
        String resultado = "S35-HORARIO";
        try {
            String query = "SELECT h.id,c.nombre,u.nombre, h.dia, h.hora, c.rutaImg, c.aforo_maximo, c.descripcion FROM horario h  INNER JOIN clase c ON h.id_clase=c.id INNER JOIN usuario u ON h.id_entrenador=u.id ORDER BY h.hora ASC;";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);

            ResultSet rs = preparedStatement.executeQuery();

            int registros = 0;

            while (rs.next()) {

                int id = rs.getInt(1);
                String nombreClase = rs.getString(2);
                String nombreEntrenador = rs.getString(3);
                int dia = rs.getInt(4);
                Time horaTime = rs.getTime(5);
                String rutaImg=rs.getString(6);
                int aforoMaximo= rs.getInt(7);
                String descripcion=rs.getString(8);
                int horas = horaTime.getHours();
                int minutos = horaTime.getMinutes();
                String hora = horas + ":" + minutos;
                
                //Calcular afoto actual
                int aforoActual;
                try {
                    String query2 = "SELECT count(id_usuario) FROM reserva_usu_horario WHERE id_horario=?";
                    PreparedStatement preparedStatement2 = conexion.prepareStatement(query2);
                    preparedStatement2.setInt(1, id);
                    ResultSet rs2 = preparedStatement2.executeQuery();
                    rs2.next();
                    aforoActual=rs2.getInt(1);
                } catch (Exception e) {
                    aforoActual=0;
                }
                

                
                if (minutos == 0) {
                    hora += "0"; //Esto por que si es 0 imprime 14:0 en lugar de 14:00
                }
                

                if (registros < 1) {
                    resultado += ":";  //Para asi saber si hay almenos 1 trabajador registrado
                } else {
                    resultado += "&";  //Delimitador entre trabajador y trabajador
                }
                resultado += id + "$" + nombreClase + "$" + nombreEntrenador + "$" + dia + "$" + hora + "$" + rutaImg + "$" + aforoMaximo + "$" + aforoActual + "$" + descripcion;
                registros++;
                //Ira añadiendo al mensaje resultado todos los registros de trabajadores, separando sus atributos con "/"
            }
        } catch (SQLException ex) {
            resultado = "S12-ERROR_QUERY";
        }

        return resultado;
    }

    String reservarHorario(int idCliente, int idHorario) {
        String resultado = "S60-ERROR";
        try {
            if (comprobarAforo(idHorario)) {
                String query = "INSERT INTO reserva_usu_horario (id_usuario,id_horario) VALUES (?,?)";
                PreparedStatement preparedStatement = conexion.prepareStatement(query);
                preparedStatement.setInt(1, idCliente);
                preparedStatement.setInt(2, idHorario);

                int registrado = preparedStatement.executeUpdate();
                if (registrado > 0) {
                    resultado = "S57-RESERVADA";
                }

            } else {
                resultado= "S58-AFORO_COMPLETO";
            }

        } catch (SQLException ex) {
            if(ex.getMessage().contains("Duplicate")){
                resultado= "S59-YA_RESERVADA";
            }else{
                resultado = "S60-ERROR";
            }
        }

        return resultado;
    }

    
    
    private boolean comprobarAforo(int idHorario) {
        boolean disponible=false;
        try {
           
                String query="SELECT c.aforo_maximo, COUNT(r.id_usuario)  From horario h INNER JOIN clase c ON h.id_clase=c.id INNER JOIN reserva_usu_horario r ON h.id= r.id_horario WHERE h.id=?;";
                PreparedStatement preparedStatement = conexion.prepareStatement(query);
                preparedStatement.setInt(1, idHorario);
                

                ResultSet rs= preparedStatement.executeQuery();
                rs.next();
                int aforoMaximo=rs.getInt(1);
                int aforoActual=rs.getInt(2);
                disponible=aforoActual<aforoMaximo;
            

        } catch (SQLException ex) {
            return false;
        }
        
        return disponible;
    }

    String listarReservas(int idCliente) {
        String resultado = "S61-LISTA_RESERVAS";
        try {
           
            PreparedStatement preparedStatement;
            
            String query = "SELECT r.id, c.nombre, h.dia,h.hora FROM reserva_usu_horario r INNER JOIN horario h on r.id_horario=h.id INNER JOIN clase c ON h.id_clase=c.id WHERE r.id_usuario=?;"; //(El rol 3 son los clientes)
            preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setInt(1, idCliente);

            ResultSet rs = preparedStatement.executeQuery();

            int registros = 0;

            while (rs.next()) {
                
                int idReserva = rs.getInt(1);
                String nombreClase=rs.getString(2);
                int dia = rs.getInt(3);
                Time horaTime=rs.getTime(4);                
                
                int horas=horaTime.getHours();
                int minutos=horaTime.getMinutes();
                String hora=horas+":"+minutos;
                if(minutos==0) hora+="0"; //Esto por que si es 0 imprime 14:0 en lugar de 14:00


                if (registros < 1) {
                    resultado += ":";  //Para asi saber si hay almenos 1 tabla registrada
                } else {
                    resultado += "&";  //Delimitador entre tabla y tabla
                }
                resultado += idReserva + "$" + nombreClase + "$" + dia + "$" + hora;
                registros++;
                //Ira añadiendo al mensaje resultado todos los registros de tablas, separando sus atributos con "/"

            }

        } catch (SQLException ex) {
            resultado = "S60-ERROR";
        }

        return resultado;    
    }

    public String cancelarReserva(int idReserva) {
        String resultado="S60-ERROR";
        try {
            String query = "DELETE FROM reserva_usu_horario WHERE id=?;";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setInt(1, idReserva);

            int filasModificadas= preparedStatement.executeUpdate();
            
            if(filasModificadas>0) resultado="S62-CANCELACION_CORRECTA";
            
            
           
        } catch (SQLException ex) {
            resultado="S60-ERROR";
        }
        
        return resultado;
    }

    String listarTablasCliente(int idCliente) {
        String resultado = "S63-LISTA_TABLAS";
        try {
           
            PreparedStatement preparedStatement;
            
            String query = "SELECT t.id,t.nombre,t.dias FROM tabla t INNER JOIN tabla_usuario_mtm tu ON t.id=tu.id_tabla WHERE tu.id_usuario=?;"; //(El rol 3 son los clientes)
            preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setInt(1, idCliente);

            ResultSet rs = preparedStatement.executeQuery();

            int registros = 0;

            while (rs.next()) {
                
                int idTabla = rs.getInt(1);
                String nombreTabla=rs.getString(2);
                int nDias = rs.getInt(3);
               
                if (registros < 1) {
                    resultado += ":";  //Para asi saber si hay almenos 1 tabla registrada
                } else {
                    resultado += "&";  //Delimitador entre tabla y tabla
                }
                resultado += idTabla + "$" + nombreTabla + "$" + nDias;
                registros++;
                //Ira añadiendo al mensaje resultado todos los registros de tablas, separando sus atributos con "/"

            }

        } catch (SQLException ex) {
            resultado = "S60-ERROR";
        }

        return resultado;   

    }

    
    public String listarEjerciciosTabla(int idTabla) {
        String resultado = "S64-LISTA_EJERCICIOS_DE_TABLA";
        try {
           
            PreparedStatement preparedStatement;
            
            String query = "SELECT e.id,et.dia ,e.nombre,e.descripcion,e.tipo,e.rutaImg,et.series,et.repeticiones,et.tiempo  FROM ejercicio e INNER JOIN ej_tabla_mtm et ON e.id=et.id_ejercicio WHERE et.id_tabla=?;";
            preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setInt(1, idTabla);

            ResultSet rs = preparedStatement.executeQuery();

            int registros = 0;

            while (rs.next()) {
                
                int idEjercicio = rs.getInt(1);
                int dia= rs.getInt(2);
                String nombre=rs.getString(3);
                String descripcion=rs.getString(4);
                String tipo=rs.getString(5);
                String rutaImg=rs.getString(6);
                int series=rs.getInt(7);
                int repeticiones=rs.getInt(8);
                String tiempo=rs.getString(9);
               
                         

                if (registros < 1) {
                    resultado += ":";  //Para asi saber si hay almenos 1 tabla registrada
                } else {
                    resultado += "&";  //Delimitador entre tabla y tabla
                }
                resultado += idEjercicio + "$" + dia + "$" + nombre + "$" + descripcion+ "$" +tipo+ "$" +rutaImg+ "$" +series+ "$" +repeticiones+ "$" +tiempo;
                registros++;
                //Ira añadiendo al mensaje resultado todos los registros de tablas, separando sus atributos con "/"

            }

        } catch (SQLException ex) {
            resultado = "S60-ERROR";
        }

        return resultado;       }

    String registrarContraseñaCliente(String correo, String contraseña) {
        String resultado = "S66-ERROR_REGISTRO";
        //1. Comprobar que halla un usuario con ese correo y que su contraseña sea null, para establecerla por primera vez
        try {

            PreparedStatement preparedStatement;

            String query = "SELECT contraseña FROM usuario WHERE correo=?";
            preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setString(1, correo);

            ResultSet rs = preparedStatement.executeQuery();

            if (rs.next()) {
                try {
                    String contraseñaa = rs.getString(1);
                    if (contraseñaa.isEmpty()) {
                        //Esta vacia, puede hacer cambiar la contraseña
                         
                        try {
                            String query2 = "UPDATE usuario SET contraseña=? WHERE correo=?";
                            PreparedStatement preparedStatement2 = conexion.prepareStatement(query);
                            preparedStatement2.setString(1, contraseña);
                            preparedStatement2.setString(2, correo);

                            int filasModificadas = preparedStatement.executeUpdate();
                            //System.out.println("Filas modificadas(Eliminando):"+filasModificadas);

                            if (filasModificadas > 0) {
                                resultado = "S65-REGISTRO_CORRECTO";
                            }else{
                                resultado = "S66-ERROR_REGISTRO:Error de insercion. Servidor en mantenimiento.";
                            }

                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            resultado = "S60-ERROR";
                        }

                    } else {
                        resultado = "S66-ERROR_REGISTRO:Este correo ya esta registrado y tiene una contraseña asignada.";
                    }

                } catch (NullPointerException ne) {
                    //Es null,  puede hacer cambiar la contraseña
                    try {
                            String query2 = "UPDATE usuario SET contraseña= ? WHERE correo= ?;";
                            PreparedStatement preparedStatement2 = conexion.prepareStatement(query2);
                            preparedStatement2.setString(1, contraseña);
                            preparedStatement2.setString(2, correo);

                            int filasModificadas = preparedStatement2.executeUpdate();
                            //System.out.println("Filas modificadas(Eliminando):"+filasModificadas);

                            if (filasModificadas > 0) {
                                resultado = "S65-REGISTRO_CORRECTO";
                            }else{
                                resultado = "S66-ERROR_REGISTRO:Error de insercion. Servidor en mantenimiento.";
                            }

                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            resultado = "S60-ERROR";
                        }
                    
                }
            } else {
                resultado = "S66-ERROR_REGISTRO:No existe ningun usuario registrado con este correo.";
            }
            
        } catch (SQLException ex) {
            resultado = "S60-ERROR";
        }
        
        return resultado;
        
    }

    public String altaTablaPersonalizada(String nombreTabla, int nDias,String dni, ArrayList<Object[]> listaEjercicios) {
       String resultado;
       
       int idTablaCreada=0;
       int idCliente=0;
        try {
            //1.Crear Tabla
            String query = "INSERT INTO tabla(nombre, dias, tabla_Base) VALUES (?, ?, ?);";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            
           
            preparedStatement.setString(1, nombreTabla);
            preparedStatement.setInt(2, nDias);
            preparedStatement.setBoolean(3, false);            

            int filasModificadas= preparedStatement.executeUpdate();
            if(filasModificadas>0)  resultado=registrarEjerciciosDeLaTabla(nombreTabla,listaEjercicios);
            else                    resultado="S47-ERROR_REGISTRO: Error de insercion. Actualize los parametros seleccionados (musulos, tipos...). Pueden haber sido modificados/eliminados.";
            
            //2. Obtener id de la tabla para asociarselo al cliente
            query = "SELECT id FROM tabla WHERE nombre=?";
            preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setString(1, nombreTabla);

            ResultSet rs = preparedStatement.executeQuery();

            rs.next();
            idTablaCreada=rs.getInt(1);
            
            //3. Obtener id del cliente
            query = "SELECT id FROM usuario WHERE dni=?";
            preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setString(1, dni);

            rs = preparedStatement.executeQuery();

            rs.next();
            idCliente=rs.getInt(1);
            
            //System.out.println("ID CLIENTE:"+idCliente);
            //System.out.println("ID TABLA:"+idTablaCreada);
            
            //4. Asociar tabla a usuario
            query = "INSERT INTO tabla_usuario_mtm(id_usuario, id_tabla) VALUES (?,?);";
            preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setInt(1, idCliente);
            preparedStatement.setInt(2, idTablaCreada);
            

            int filasAfectadas = preparedStatement.executeUpdate();
            
            if(filasAfectadas>0){
                resultado="S52-TABLA_REGISTRADA";
            }
            
            
            
            
            
        } catch (SQLException ex) {
            if(ex.toString().contains("Duplicate")) return resultado="S47-ERROR_REGISTRO:duplicate";            
            
            resultado="S47-ERROR_REGISTRO: Error de insercion. Actualize los parametros seleccionados (musulos, tipos...). Pueden haber sido modificados/eliminados.";
            ex.printStackTrace();
        }
        
        return resultado; 
    }

    public String resetearReservas() {
        String resultado="S68-RESETEADAS";
        try {
            String query = "DELETE FROM reserva_usu_horario;;";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);

            int filasModificadas = preparedStatement.executeUpdate();

            
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            resultado="S69-ERROR_RESETEO";
        }
        
        return resultado;
    }

    public void eliminarReservasAntiguas() {
        //Elimina todas las reservas hechas antes de 2 dias atras 
        //(Ya que solo se puede reservar el mismo dia o al siguiente, y en la BD se almacena cuando se realiza la reserva, no para cuando es)
        
        //1. Obtiene la fecha de 2 dias atras con LocalDate
        LocalDate fechaDosDiasAtras=LocalDate.now().minusDays(2);
        
        
        //2. Transforma el LocalDate a Date
        ZoneId defaultZoneId = ZoneId.systemDefault();
        Date fecha = Date.from(fechaDosDiasAtras.atStartOfDay(defaultZoneId).toInstant());
        
               
        try {
            //3. Realiza la Query que elimina todas las reservas que hallan sido realizadas hace dos dias o antes
            String query = "DELETE FROM reserva_usu_horario WHERE fecha<=?";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setDate(1, new java.sql.Date(fecha.getTime()));
            
            int reservasEliminadas=preparedStatement.executeUpdate();
            System.out.println("RESERVAS ELIMINADAS:"+reservasEliminadas);
            

            
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            
        }
        
        
    }

    String actualizarClave(int rol, String nuevaClave) {
        String resultado="S0-ERROR";
        try {
            String query = "UPDATE rol SET clave=? WHERE id=?;";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            
            preparedStatement.setString(1, nuevaClave);
            preparedStatement.setInt(2, rol);

            int filasModificadas = preparedStatement.executeUpdate();

            if (filasModificadas > 0) {
                resultado = "S13-CLAVE_ACTUALIZADA";
            } 


        } catch (SQLException ex) {
           
        }
        
        return resultado;
    }
    
    

    


}

    
