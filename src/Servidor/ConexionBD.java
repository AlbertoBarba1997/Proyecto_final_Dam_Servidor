/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servidor;

import java.io.File;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    protected void eliminarFoto(String rutaImg) {
        
        File imagenFile=new File(rutaImg);
        if(imagenFile.exists()){
            System.out.println("ConexionDB: entra qui a eliminar: existe el File");
            //Comprobar si es una foto
            String mimetype = new MimetypesFileTypeMap().getContentType(imagenFile);
            String type = mimetype.split("/")[0];
            if (type.equals("image")) {
                //System.out.println("Intenta eliminarla");
                //System.out.println("Eliminada:"+imagenFile.delete());
            }
            
        }
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
    
}
