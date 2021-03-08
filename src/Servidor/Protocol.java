package Servidor;

import HilosSecundarios.EsperaThread;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
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
            if (theInput.contains("M1-LOG_CLIENTE")) {
                String correo = obtenerParametro(theInput, 1);
                String contraseña = obtenerParametro(theInput, 2);

                if (conexionBD.isConexionEstablecida()) {

                    theOutput = conexionBD.loginClenteBD(correo, contraseña);
                    if(theOutput.contains("S1-LOG_CORRECTO")) estado=CLIENTE_LOGUEADO;

                }
                if (theOutput.contains("S2-LOG_INCORRECTO")) {
                    nIntentosLoguear++;
                    if (nIntentosLoguear < 3) {
                        //No hace nada por que no ha superado el maximo de intentos
                    } else {
                        //Para que halla penalizacion de 30 segundos cada 3 fallos

                        nEsperas++;         //Aumenta exponencialmente el tiempo de espera por cada 3 fallos 

                        //Se pondrá en estado de ESPERA y se lanzará un hilo a modo de temporizador que cuando acabe devolvera el estado a INICIAL
                        esperaThread = new EsperaThread(this, nEsperas);
                        esperaThread.start();
                        estado = ESPERA;
                        nIntentosLoguear = 0;

                        theOutput = "S4-LOG_BLOQUEADO:" + 30 * nEsperas;
                    }

                }

            }
            /// 1.5) ALTA CONTRASEÑA NUEVO CLIENTE
            else if (theInput.contains("M10-SING_UP_CLIENTE")) {
                String correo = obtenerParametro(theInput, 1);
                String contraseña = obtenerParametro(theInput, 2);
                
                theOutput=conexionBD.registrarContraseñaCliente(correo,contraseña);
                
                
            }
            
            
            /// 2) REGISTRAR CONTRASEÑA CLIENTE
            else if (theInput.contains("C2-REGISTRAR_CONTRASEÑA")) {
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
            else if(theInput.contains("C4-LOG_TRABAJADOR"))
            {
                String dni=obtenerParametro(theInput, 1);
                String clave=obtenerParametro(theInput, 2);
                
                String resultadoRolNombre=conexionBD.loginTrabajadorBD(dni,clave);
                if(resultadoRolNombre==null){
                    theOutput="S2-LOG_INCORRECTO";
                }else{
                    theOutput="S10-LOG_CORRECTO_TR:"+resultadoRolNombre;
                    int idTrabajador=Integer.parseInt(Utilidades.obtenerParametro(theOutput,1));
                    if(idTrabajador==1){
                        estado=ADMIN_LOGUEADO;
                    }else{
                        estado=ENTRENADOR_LOGUEADO;
                    }
                }
            }

        }
        //////////////////////////////////////////////////////////////////////// B) ESTADO ESPERA ////////////////////////////////////////////////////////////////////////////////////////////
        //Se entra por fallar 3 veces el log y mientras este en él, toda respuesta del server sera "nula" hasta que pase el tiempo de penalizacion.
        else if(estado==ESPERA)
        {
            
            theOutput="S5-ESPERA:"+esperaThread.getSegundosRestantesEspera();
        }
        
        //////////////////////////////////////////////////////////////////////// C)ESTADO : ADMIN LOGUEADO /////////////////////////////////////////////////////////////////////////////////
        else if(estado==ADMIN_LOGUEADO)
        {
            ////TRABAJADORES ////
            //1) LISTAR TRABAJADORES
            if(theInput.contains("C6-LISTA_TRABAJADORES")){
                theOutput=conexionBD.listarTrabajadores();
                
            }
            //2) ALTA TRABAJADOR
            else if(theInput.contains("C8-REGISTRAR_TRABAJADOR"))
            {
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
            else if(theInput.contains("C7_ELIMINAR_TRABAJADOR"))
            {
                String dni=obtenerParametro(theInput, 1);
                boolean eliminado=conexionBD.eliminarTrabajador(dni);
                if(eliminado) theOutput="S13-ELIMINACION_COMPLETADA";
                else theOutput="S14-ERROR_ELIMINACION";
            }
            
            //4) MODIFICAR TRABAJADOR
            else if(theInput.contains("C9-MODIFICAR_TRABAJADOR"))
            {
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
            else if (theInput.contains("C10-LISTAR_BUSQUEDA_TRABAJADORES")) 
            {
                
                String patronBusqueda = obtenerParametro(theInput, 1);
                theOutput = conexionBD.listarTrabajadoresFiltrados(patronBusqueda);
            }
            
            //6)FILTRAR ROL
            else if (theInput.contains("C11-FILTRAR_TRABAJADOR_ROL")) 
            {
                
                int nRol =Integer.parseInt(obtenerParametro(theInput, 1));
                String patron=obtenerParametro(theInput, 2);
                theOutput = conexionBD.filtrarRol(nRol, patron);
            }
            
            /// ROLES Y CLAVES ///
            //7)LISTAR ROLES
            else if (theInput.contains("C12-LISTA_CLAVES")) 
            {
                theOutput=conexionBD.listarClaves();
            }
            
            //8) ALTA ROL
            else if(theInput.contains("C13-REGISTRAR_ROL"))
            {
                String nombre=obtenerParametro(theInput, 1);
                String clave=obtenerParametro(theInput, 2);
                
                boolean registrado=conexionBD.altaRol(nombre,clave);
                if(registrado) theOutput="S15-REGISTRO_COMPLETADO";
                else theOutput="S12-ERROR_QUERY";
            }
            
            //9) ELIMINAR ROL
            else if(theInput.contains("C14_ELIMINAR_ROL"))
            {
                int id=Integer.parseInt(obtenerParametro(theInput, 1));
                boolean eliminado=conexionBD.bajaRol(id);
                if(eliminado) theOutput="S13-ELIMINACION_COMPLETADA";
                else theOutput="S14-ERROR_ELIMINACION";
                
            }else if (theInput.contains("C59_MODIFICAR_CLAVE")) 
            {
                int rol=Integer.parseInt(obtenerParametro(theInput, 1));
                String nuevaClave = obtenerParametro(theInput, 2);
                theOutput = conexionBD.actualizarClave(rol,nuevaClave);
            }
            
            
            /// CLIENTES ///
            //10) MOSTRAR CLIENTES
            else if(theInput.contains("C15-LISTA_CLIENTES"))
            {
                theOutput=conexionBD.listarClientes();
            }
            
            //11) FILTRAR CLIENTES
            else if(theInput.contains("C17-LISTAR_BUSQUEDA_CLIENTES")){
                String patronBusqueda = obtenerParametro(theInput, 1);
                theOutput = conexionBD.listarClientesFiltrados(patronBusqueda);
            }
            
             //12) MOSTRAR INFO CLIENTE
            else if(theInput.contains("C16-MOSTRAR_CLIENTE")){
                String dniCliente = obtenerParametro(theInput, 1);
                theOutput = conexionBD.mostrarCliente(dniCliente);
            }
            
            //13) ALTA CLIENTE
            else if(theInput.contains("C18-ALTA_CLIENTE")){
                String dniCliente = obtenerParametro(theInput, 1);
                String nombre = obtenerParametro(theInput, 2);
                String apellidos = obtenerParametro(theInput, 3);
                String correo = obtenerParametro(theInput, 4);
                int peso  = Integer.parseInt(obtenerParametro(theInput, 5));
                float altura= Float.parseFloat(obtenerParametro(theInput, 6));
                String rutaFoto=obtenerParametro(theInput,7);
                String notas=obtenerParametro(theInput, 8);
                
                
                theOutput = conexionBD.altaCliente(dniCliente,nombre, apellidos,correo,peso,altura,rutaFoto,notas);
            }
          
            //14) ELIMINAR CLIENTE
             else if(theInput.contains("C19_ELIMINAR_CLIENTE")){
                String dniCliente = obtenerParametro(theInput, 1);

                boolean eliminado=conexionBD.eliminarCliente(dniCliente);
                
                if(eliminado)   theOutput="S25-CLIENTE_ELIMINADO";
                else            theOutput="S26-CLIENTE_NO_ENCONTRADO";
            }
            //32) ELIMINAR FOTO
            else if(theInput.contains("C20_ELIMINAR_FOTO")){
                String rutaImg = obtenerParametro(theInput, 1);
                System.out.println("Servidor: entra en eliminar foto con:"+rutaImg);
                conexionBD.eliminarFoto(rutaImg);
                theOutput="S28-ELIMINADA";
            }
             //20) LISTAR HORARIOS
             else if(theInput.contains("C25-LISTAR_HORARIO")){

               theOutput=conexionBD.listarHorarios();
            }
            
            //23) ALTA HORARIO
            else if(theInput.contains("C28-ALTA_HORARIO")){
               
                int idClase  = Integer.parseInt(obtenerParametro(theInput, 1));
                int idEntrenador  = Integer.parseInt(obtenerParametro(theInput, 2));
                int nDia  = Integer.parseInt(obtenerParametro(theInput, 3));
                String hora=Utilidades.obtenerParametro(theInput, 4)+":00";
                System.out.println(hora);
                Time horaTime=Time.valueOf(hora);
                
                theOutput = conexionBD.altaHorario(idClase,idEntrenador,nDia,horaTime);
            }
            //24) RESTABLECER HORARIO
            else if (theInput.contains("C29-RESTBLECER_HORARIO")) {
               theOutput=conexionBD.restablecerHorario();
               
            }
            
            //25) ELIMINAR HORARIO SELECCIONADO
            else if (theInput.contains("C30_ELIMINAR_HORARIO")) {
                int id = Integer.parseInt(Utilidades.obtenerParametro(theInput, 1));
                theOutput = conexionBD.eliminarHorario(id);

            }
            
             //17) MOSTRAR CLASES
            else if(theInput.contains("C22-LISTAR_CLASES"))
            {
                theOutput=conexionBD.listarClases();
            }
             //18) MOSTRAR INFO CLASES
            else if(theInput.contains("C23-MOSTRAR_CLASE"))
            {
                String nombreClase = obtenerParametro(theInput, 1);
                theOutput = conexionBD.mostrarClase(nombreClase);
            }
            //21) LISTAR CLASE
            else if (theInput.contains("C26-LISTAR_CLASE_DISPONIBLE")) {
                theOutput = conexionBD.listarClasesId();
            } 
            //16) ALTA CLASE
            else if(theInput.contains("C21-ALTA_CLASE")){
               
                String nombre = obtenerParametro(theInput, 1);
                int aforo  = Integer.parseInt(obtenerParametro(theInput, 2));
                String rutaImg = obtenerParametro(theInput, 3);
                String descripcion = obtenerParametro(theInput, 4);
                
                theOutput = conexionBD.altaClase(nombre,aforo,rutaImg,descripcion);
            }
            //19) ELIMINAR CLASES
             else if(theInput.contains("C24_ELIMINAR_CLASE")){
                String nombreCliente = obtenerParametro(theInput, 1);

                boolean eliminado=conexionBD.eliminarClase(nombreCliente);
                
                if(eliminado)   theOutput="S34-CLASE_ELIMINADA";
                else            theOutput="S33-CLASE_NO_ENCONTRADA";
            }
            //22) LISTAR ENTRENADORES
            else if (theInput.contains("C27-LISTAR_ENTRENADORES")) {
                theOutput = conexionBD.listarEntrenadores();
            }
            //49) RESETEAR RESERVA
            else if (theInput.contains("C46-RESETEAR_RESERVAS")) {
                theOutput = conexionBD.resetearReservas();
            }
            
            
        //////////////////////////////////////////////////////////////////////// ESTADO : COACH LOGUEADO /////////////////////////////////////////////////////////////////////////////////     
        }else if(estado==ENTRENADOR_LOGUEADO){
             
            /// CLIENTES ///
            //10) MOSTRAR CLIENTES
            if(theInput.contains("C15-LISTA_CLIENTES"))
            {
                theOutput=conexionBD.listarClientes();
            }
            
            //11) FILTRAR CLIENTES
            else if(theInput.contains("C17-LISTAR_BUSQUEDA_CLIENTES")){
                String patronBusqueda = obtenerParametro(theInput, 1);
                theOutput = conexionBD.listarClientesFiltrados(patronBusqueda);
            }
            
             //12) MOSTRAR INFO CLIENTE
            else if(theInput.contains("C16-MOSTRAR_CLIENTE")){
                String dniCliente = obtenerParametro(theInput, 1);
                theOutput = conexionBD.mostrarCliente(dniCliente);
            } 
            
            
            //15) ELIMINAR FOTO
            else if(theInput.contains("C20_ELIMINAR_FOTO")){
                String rutaImg = obtenerParametro(theInput, 1);
                System.out.println("Servidor: entra en eliminar foto con:"+rutaImg);
                conexionBD.eliminarFoto(rutaImg);
                theOutput="S28-ELIMINADA";
            }
            
            //16) ALTA CLASE
            else if(theInput.contains("C21-ALTA_CLASE")){
               
                String nombre = obtenerParametro(theInput, 1);
                int aforo  = Integer.parseInt(obtenerParametro(theInput, 2));
                String rutaImg = obtenerParametro(theInput, 3);
                String descripcion = obtenerParametro(theInput, 4);
                
                theOutput = conexionBD.altaClase(nombre,aforo,rutaImg,descripcion);
            }
            //17) MOSTRAR CLASES
            else if(theInput.contains("C22-LISTAR_CLASES"))
            {
                theOutput=conexionBD.listarClases();
            }
             //18) MOSTRAR INFO CLASES
            else if(theInput.contains("C23-MOSTRAR_CLASE"))
            {
                String nombreClase = obtenerParametro(theInput, 1);
                theOutput = conexionBD.mostrarClase(nombreClase);
            }
            
            
            
            //20) LISTAR HORARIOS
             else if(theInput.contains("C25-LISTAR_HORARIO")){

               theOutput=conexionBD.listarHorarios();
            }
            //21) LISTAR CLASE
            else if (theInput.contains("C26-LISTAR_CLASE_DISPONIBLE")) {
                theOutput = conexionBD.listarClasesId();
            } 
            
            //22) LISTAR ENTRENADORES
            else if (theInput.contains("C27-LISTAR_ENTRENADORES")) {
                theOutput = conexionBD.listarEntrenadores();
            }
            //23) ALTA HORARIO
            else if(theInput.contains("C28-ALTA_HORARIO")){
               
                int idClase  = Integer.parseInt(obtenerParametro(theInput, 1));
                int idEntrenador  = Integer.parseInt(obtenerParametro(theInput, 2));
                int nDia  = Integer.parseInt(obtenerParametro(theInput, 3));
                String hora=Utilidades.obtenerParametro(theInput, 4)+":00";
                System.out.println(hora);
                Time horaTime=Time.valueOf(hora);
                
                theOutput = conexionBD.altaHorario(idClase,idEntrenador,nDia,horaTime);
            }
            //24) RESTABLECER HORARIO
            else if (theInput.contains("C29-RESTBLECER_HORARIO")) {
               theOutput=conexionBD.restablecerHorario();
               
            }
            
            //25) ELIMINAR HORARIO SELECCIONADO
            else if (theInput.contains("C30_ELIMINAR_HORARIO")) {
                int id = Integer.parseInt(Utilidades.obtenerParametro(theInput, 1));
                theOutput = conexionBD.eliminarHorario(id);

            }
            
            //26) LISTAR EJERCICIOS
            else if (theInput.contains("C31-LISTAR_EJERCICIOS")) {
                theOutput = conexionBD.listarEjercicios();
            }
            //27) LISTAR MUSUCULOS
            else if (theInput.contains("C32-LISTAR_MUSCULOS")) {
                theOutput = conexionBD.listarMusculos();
            }
            //28) LISTAR EJERCICIOS FILTRADOS
            else if (theInput.contains("C33-FILTRAR_EJERCICIOS")) {
                String tipo=Utilidades.obtenerParametro(theInput, 1);
                String musculo=Utilidades.obtenerParametro(theInput, 2);
                String nombreBusqueda=Utilidades.obtenerParametro(theInput, 3);

                theOutput = conexionBD.listarEjerciciosFiltrados(tipo,musculo,nombreBusqueda);
            }
            //28) LISTAR EJERCICIOS FILTRADOS
            else if (theInput.contains("C35-FILTRAR_EJERCICIOS_CARDIO")) {
                String resultado = "S42-LISTA_EJERCICIOS:";
                String nombreBusqueda=Utilidades.obtenerParametro(theInput, 1);
                    
                theOutput = resultado+conexionBD.listarEjerciciosFiltradosCardio(nombreBusqueda);
            }
            //30) ELIMINAR EJERCICIO
            else if (theInput.contains("C34_ELIMINAR_EJERCICIO")) {
                int id=Integer.parseInt(Utilidades.obtenerParametro(theInput, 1));
                theOutput = conexionBD.eliminarEjercicio(id);
            }
            
            //31) ALTA MUSCULO
            else if (theInput.contains("C36-ALTA_MUSCULO")) {
                String nombreMusculo=Utilidades.obtenerParametro(theInput, 1);
                theOutput = conexionBD.altaMusculo(nombreMusculo);
            }
            //32) ELIMINAR FOTO
            else if(theInput.contains("C20_ELIMINAR_FOTO")){
                String rutaImg = obtenerParametro(theInput, 1);
                System.out.println("Servidor: entra en eliminar foto con:"+rutaImg);
                conexionBD.eliminarFoto(rutaImg);
                theOutput="S28-ELIMINADA";
            }
            //33) ALTA EJERCICIO
            else if (theInput.contains("C37-ALTA_EJERCICIO")) {
                String nombreEjercicio=Utilidades.obtenerParametro(theInput, 1);
                String tipo=Utilidades.obtenerParametro(theInput, 2);
                String descripcion=Utilidades.obtenerParametro(theInput, 3);
                String rutaImg=Utilidades.obtenerParametro(theInput, 4);
                String rutaVideo=Utilidades.obtenerParametro(theInput, 5);
                ArrayList<String> listaMusculos=obtenerlistaMusculos(Utilidades.obtenerParametro(theInput, 6));
                
                theOutput = conexionBD.altaEjercicio(nombreEjercicio, tipo, descripcion, rutaImg, rutaVideo, listaMusculos);
            }
            //34) BAJA MUSCULO
            else if (theInput.contains("C38_ELIMINAR_MUSCULO")) {
                String nombre=Utilidades.obtenerParametro(theInput, 1);
                theOutput = conexionBD.bajaMusculo(nombre);
            }
            //35) ALTA TABLA MODELO
            else if (theInput.contains("C39-ALTA_TABLA_MODELO")) {
                String datosTabla=Utilidades.obtenerParametro(theInput, 1);
                System.out.println("atributo 1:"+Utilidades.obtenerAtributo(datosTabla, 0,'&')+"  atributo2:"+Utilidades.obtenerAtributo(datosTabla, 1,'&'));
                String nombreTabla=Utilidades.obtenerAtributo(datosTabla,0 ,'&');
                int nDias=Integer.parseInt(Utilidades.obtenerAtributo(datosTabla, 1,'&'));
                
                ArrayList<Object[]> listaEjercicios=obtenerListaEjercicios(theInput);
                theOutput = conexionBD.altaTablaModelo(nombreTabla,nDias,listaEjercicios);
            }
            //36) ALTA TABLA PERSONALIZADA
            else if (theInput.contains("C40-ALTA_TABLA_PERSONALIZADA")) {
                String datosTabla=Utilidades.obtenerParametro(theInput, 1);
                System.out.println("atributo 1:"+Utilidades.obtenerAtributo(datosTabla, 0,'&')+"  atributo2:"+Utilidades.obtenerAtributo(datosTabla, 1,'&'));
                String nombreTabla=Utilidades.obtenerAtributo(datosTabla,0 ,'&');
                int nDias=Integer.parseInt(Utilidades.obtenerAtributo(datosTabla, 1,'&'));
                String dniClienteAsociado=Utilidades.obtenerAtributo(datosTabla,2 ,'&');
                
                System.out.println("DNI QUE LLEGA :"+ dniClienteAsociado);
                ArrayList<Object[]> listaEjercicios=obtenerListaEjercicios(theInput);
                theOutput = conexionBD.altaTablaPersonalizada(nombreTabla, nDias, dniClienteAsociado, listaEjercicios);
            }
            
            //37) LISTAR TABLAS MODELOS
            else if(theInput.contains("C41-LISTA_TABLAS_MODELO")){
                theOutput=conexionBD.listarTablas(null);
                 
            }
            //38) LISTAR TABLAS MODELOS
            else if(theInput.contains("C42-LISTA_TABLAS_CLIENTE")){
                String idCliente=Utilidades.obtenerParametro(theInput, 1);
                theOutput=conexionBD.listarTablas(idCliente);
                 
            }
            //39) ASOCIAR TABLA A UN USUARIO
            else if(theInput.contains("C43-ASOCIAR-TABLA-A-CLIENTE")){
                String dniCliente=Utilidades.obtenerParametro(theInput, 1);
                int idTabla = Integer.parseInt(Utilidades.obtenerParametro(theInput, 2));
                theOutput=conexionBD.asociarTablaAUsuario(dniCliente,idTabla);
                 
            }
            //40) OBTENER 
            else if(theInput.contains("C45-CARGAR_EJERCICIOS")){
                int idTabla = Integer.parseInt(Utilidades.obtenerParametro(theInput, 1));
                theOutput=conexionBD.listarEjerciciosTabla(idTabla);
                 
            }
        
        
        }
            
 
        // D) CLIENTE LOGUEADO    
        else if(estado==CLIENTE_LOGUEADO){
            if(theInput.contains("M3-LISTAR_HORARIOS")){
                
                theOutput=conexionBD.listarHorariosMovil();
                 
            }
            else if(theInput.contains("M4-RESERVAR")){
                int idCliente=Integer.parseInt(Utilidades.obtenerParametro(theInput, 1));
                int idHorario=Integer.parseInt(Utilidades.obtenerParametro(theInput, 2));
                theOutput=conexionBD.reservarHorario(idCliente,idHorario);
                 
            }
            else if(theInput.contains("M5-LOG_OUT")){
                estado=INICIAL;
                theOutput="S1-CORRECTO";
                 
            }
            else if(theInput.contains("M6-LISTAR_RESERVAS")){
                int idCliente=Integer.parseInt(Utilidades.obtenerParametro(theInput, 1));
                theOutput=conexionBD.listarReservas(idCliente);
                 
            }
            else if(theInput.contains("M7-CANCELAR_RESERVA")){
                int idReserva=Integer.parseInt(Utilidades.obtenerParametro(theInput, 1));
                theOutput=conexionBD.cancelarReserva(idReserva);
            }
            else if(theInput.contains("M8-LISTAR_TABLAS_EJERCICIOS")){
                int idCliente=Integer.parseInt(Utilidades.obtenerParametro(theInput, 1));
                theOutput=conexionBD.listarTablasCliente(idCliente);
            }
            
            else if(theInput.contains("M9-LISTAR_EJERCICIOS_TABLA")){
                int idTabla=Integer.parseInt(Utilidades.obtenerParametro(theInput, 1));
                theOutput=conexionBD.listarEjerciciosTabla(idTabla);
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

    private ArrayList<String> obtenerlistaMusculos(String musculosCadena) {
        char[] cadenaChar=musculosCadena.toCharArray();
        ArrayList<String> listaMusculos=new ArrayList<>();
        String musculo="";
        boolean primerMusculo=false;
        for (char c : cadenaChar) {
            if (c == '$') {
                if (primerMusculo == false) {
                    primerMusculo = true;
                } else {
                    listaMusculos.add(musculo);
                    musculo = "";
                }

            } else {
                musculo += c;
            }
        }
        listaMusculos.add(musculo);
        return listaMusculos;
    }

    private ArrayList<Object[]> obtenerListaEjercicios(String theImput) {
        ArrayList<Object[]> listaEjercicios=new ArrayList<Object[]>();
        
        for(int i=1; i<Utilidades.contarParametros(theImput); i++){
            //Empieza en 1 y no en 0 por que el parametro 1 son los datos basicos, no ejercicios
            String datosEjercicioParametro=Utilidades.obtenerParametro(theImput, i+1);
            
            int nDia=Integer.parseInt(Utilidades.obtenerAtributo(datosEjercicioParametro,0,'&'));
            int idEjercicio=Integer.parseInt(Utilidades.obtenerAtributo(datosEjercicioParametro,1,'&'));
            int reps = 0;
            int series = 0;
            try {
                reps = Integer.parseInt(Utilidades.obtenerAtributo(datosEjercicioParametro, 2,'&'));
                series = Integer.parseInt(Utilidades.obtenerAtributo(datosEjercicioParametro, 3,'&'));
            } catch (Exception e) {
                //Si vienen vacias pues se quedan en 0 y ya está
            }
            String tiempo=Utilidades.obtenerAtributo(datosEjercicioParametro,4,'&');
            System.out.println("nDia:"+ nDia+ " idEjercicio:"+ idEjercicio+" reps:"+reps+ " series:"+series+ " tiempo:"+ tiempo);
            Object[] datosEjercicio={nDia,idEjercicio,reps,series,tiempo};
            listaEjercicios.add(datosEjercicio);
        }
        return listaEjercicios;
       
    }
    
    

   
}
