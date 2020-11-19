package Servidor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Protocol {

    //VARIABLES GLOBALES:
    //UTIL
    private static BufferedReader entradaTeclado = new BufferedReader(new InputStreamReader(System.in));
    private ConexionBD conexionBD=new ConexionBD();

    //ESTADOS
    private static final int INICIAL = 1;
    private static final int CLIENTE_LOGUEADO = 2;
    private static final int ENTRENADOR_LOGUEADO = 3;
    private static final int ADMIN_LOGUEADO = 4;
    private static final int ESPERA = 5;
    private static final int SESION_CADUCADA = 6;
    private int estado = INICIAL;

    //USUARIO LOGUEADO
    private String dniUsuario = "";
    private int nIntentosLoguear = 0;
    private int nEsperas = 0;
    private boolean sesionCaducada=false;
    EsperaThread esperaThread; //El hilo temporizador para poner penalizaciones de espera por fallar login repetidas veces 

    public String processInput(String theInput) {

        String theOutput = "S0-ERROR";

        //A) ESTADO INICIAL = Recibe peticiones de Log de clientes y trabajadores
        if (estado == INICIAL) {

            /// 1) LOG CLIENTE
            if (theInput.contains("C1-LOG_CLIENTE")) {
                String usuario = obtenerParametro(theInput, 1);
                String contraseña = obtenerParametro(theInput, 2);

                if (conexionBD.isConexionEstablecida()) {       

                    dniUsuario = conexionBD.loginClenteBD(usuario, contraseña);   

                    if (dniUsuario != null) {
                        theOutput = "S1-LOG_CORRECTO_CL";
                        estado = CLIENTE_LOGUEADO;
                    } else {
                        nIntentosLoguear++;
                        System.out.println("n intentos fallidos xD "+nIntentosLoguear);
                        if(nIntentosLoguear<3){
                            theOutput = "S3-LOG_INCORRECTO";
                        }else{                                           
                            //Para que halla penalizacion de 30 segundos cada 3 fallos
                            
                            nEsperas++;         //Aumenta exponencialmente el tiempo de espera por cada 3 fallos 
                            
                            //Se pondrá en estado de ESPERA y se lanzará un hilo a modo de temporizador que cuando acabe devolvera el estado a INICIAL
                            esperaThread=new EsperaThread(this, nEsperas);
                            esperaThread.start(); 
                            estado=ESPERA;
                            nIntentosLoguear=0;
                   
                            theOutput = "S7-LOG_INCORRECTO_ESPERA:" + 30 * nEsperas;
                        }

                    }

                } else {
                    theOutput = "S4-ERROR_CONEXION_BD";
                }

            }
            /// 2) REGISTRAR CONTRASEÑA CLIENTE
            else if(theInput.contains("C2-REGISTRAR_CONTRASEÑA")){
                String correo=obtenerParametro(theInput, 1);
                String contraseña=obtenerParametro(theInput, 2);
                
                int nResultado=conexionBD.registrarContraseña(correo, contraseña);
                //El resultado devolverá 0=ERROR  1=REGISTRO CORRECTO 2=CORREO NO REGISTRADO  3=CONTRASEÑA YA REGISTRADA
                if(nResultado==1){
                    theOutput="S9-REG_CORRECTO";
                }else{
                    theOutput="S8-REG_INCORRECTO_ERROR:"+nResultado;
                }
            }
            
            /// 3) LOG TRABAJADOR
            else if(theInput.contains("C4-LOG_TRABAJADOR")){
                String dni=obtenerParametro(theInput, 1);
                String clave=obtenerParametro(theInput, 2);
                
                String resultadoRolNombre=conexionBD.loginTrabajadorBD(dni,clave);
                if(resultadoRolNombre==null){
                    theOutput="S2-LOG_INCORRECTO";
                }else{
                    theOutput="S10-LOG_CORRECTO_TR:"+resultadoRolNombre;
                    estado=ADMIN_LOGUEADO;
                }
            }

        }
        //B) ESTADO ESPERA = Se entra por fallar 3 veces el log y mientras este en él, toda respuesta del server sera "nula" hasta que pase el tiempo de penalizacion.
        else if(estado==ESPERA){
            
            theOutput="S3-EN_ESPERA:"+esperaThread.getSegundosRestantesEspera();
        }
        
        //C) ESTADO ADMIN LOGUEADO
        else if(estado==ADMIN_LOGUEADO){
            ////TRABAJADORES ////
            //1) LISTAR TRABAJADORES
            if(theInput.contains("C6-LISTA_TRABAJADORES")){
                theOutput=conexionBD.listarTrabajadores();
                
            }
            //2) ALTA TRABAJADOR
            else if(theInput.contains("C8-REGISTRAR_TRABAJADOR")){
                String dni=obtenerParametro(theInput, 1);
                String nombre=obtenerParametro(theInput, 2);
                String apellidos=obtenerParametro(theInput, 3);
                float salario=Float.parseFloat(obtenerParametro(theInput, 4));    
                String correo=obtenerParametro(theInput, 5);
                int rol=Integer.parseInt(obtenerParametro(theInput,6));
                
                int resultado=conexionBD.altaTrabajador(dni, nombre, apellidos,correo, salario, rol);
                switch(resultado){
                    case 1: 
                        theOutput="S15-REGISTRO_COMPLETADO";
                        break;
                    case 2: 
                        theOutput="S16-ERROR_DNI";
                        break;
                    case 0:
                        theOutput="S0-ERROR";
                        break;
                }
            }
            //3) BAJA TRABAJADOR
            else if(theInput.contains("C7_ELIMINAR_TRABAJADOR")){
                String dni=obtenerParametro(theInput, 1);
                boolean eliminado=conexionBD.eliminarTrabajador(dni);
                if(eliminado) theOutput="S13-ELIMINACION_COMPLETADA";
                else theOutput="S14-ERROR_ELIMINACION";
            }
            
            //4) MODIFICAR TRABAJADOR
            else if(theInput.contains("C9-MODIFICAR_TRABAJADOR")){
                String dni=obtenerParametro(theInput, 1);
                String nombre=obtenerParametro(theInput, 2);
                String apellidos=obtenerParametro(theInput, 3);
                float salario=Float.parseFloat(obtenerParametro(theInput, 4));    
                String correo=obtenerParametro(theInput, 5);
                int rol=Integer.parseInt(obtenerParametro(theInput,6));
                
                int resultado=conexionBD.modificarTrabajador(dni, nombre, apellidos,correo, salario, rol);
                switch(resultado){
                    case 1: 
                        theOutput="S17-MODIFICACION_COMPLETADA";
                        break;
                    case 2: 
                        theOutput="S18-ERROR_CORREO";
                        break;
                    case 0:
                        theOutput="S0-ERROR";
                        break;
                }
            }
            //5) FILTRAR TRABAJADOR
            else if (theInput.contains("C10-LISTAR_BUSQUEDA_TRABAJADORES")) {
                
                String patronBusqueda = obtenerParametro(theInput, 1);
                System.out.println("Llega a modificar, patron:"+ patronBusqueda);
                theOutput = conexionBD.listarTrabajadoresFiltrados(patronBusqueda);
            }
            
            //6)FILTRAR ROL
            else if (theInput.contains("C11-FILTRAR_TRABAJADOR_ROL")) {
                
                int nRol =Integer.parseInt(obtenerParametro(theInput, 1));
                String patron=obtenerParametro(theInput, 2);
                theOutput = conexionBD.filtrarRol(nRol, patron);
            }
            
            /// ROLES Y CLAVES ///
            //7)LISTAR ROLES
            else if (theInput.contains("C12-LISTA_CLAVES")) {
                
                theOutput=conexionBD.listarClaves();
                
            }
            //8) ALTA ROL
            else if(theInput.contains("C13-REGISTRAR_ROL")){
                String nombre=obtenerParametro(theInput, 1);
                String clave=obtenerParametro(theInput, 2);
                
                boolean registrado=conexionBD.altaRol(nombre,clave);
                if(registrado) theOutput="S15-REGISTRO_COMPLETADO";
                else theOutput="S12-ERROR_QUERY";
            }
            //9) ELIMINAR ROL
            else if(theInput.contains("C14_ELIMINAR_ROL")){
                int id=Integer.parseInt(obtenerParametro(theInput, 1));
                boolean eliminado=conexionBD.bajaRol(id);
                if(eliminado) theOutput="S13-ELIMINACION_COMPLETADA";
                else theOutput="S14-ERROR_ELIMINACION";
                
            }
            
          
            
            

        }

        return theOutput;
    }
    
    
    

    /////////////// METODOS SECUNDARIOS  /////////////////////////////////////////////////////////////////
    private String obtenerParametro(String theInput, int nParametroBuscado) {
        //OBTIENE EL PARAMETRO indicado en la cadena indicada

        //Si es el primero estara entre los ":" y la ","
        //A partir del segundo se iran controlando por las ",", cuando lea una coma sabra que lo siguiente a leer es el siguiente parametro.
        int nParametro = 0;       //Mide por que parametro va, cuando lea un ":" o una "," -> nParametro++;

        char[] caracteres = theInput.toCharArray();
        String parametro = "";

        for (char c : caracteres) {

            if (c == ':') {
                nParametro++;
            } else if (c == ',') {
                nParametro++;
            } else if (nParametro == nParametroBuscado) {
                parametro += c;
            }

        }
        return parametro;
    }

    public void volverEstadoInicial(){
        //Metodo para que cuando pase el tiempo de espera de penalizacion por fallar log, vuelva al estado inicial, no pudiendo acceder a los demas estados.
        estado=INICIAL; 
    }
    
    

   
}
