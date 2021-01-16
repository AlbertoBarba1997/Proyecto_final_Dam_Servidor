/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servidor;

import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JLabel;

/**
 *
 * @author juana
 */
public class Utilidades {
    
    public Utilidades() {
    }
    
    
    public static String obtenerParametro(String theInput, int nParametroBuscado) {
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
    
    
     public static int contarParametros(String respuestaServidor) {
        //CUENTA CUANTOS PARAMETROS HAY EN UNA SECUENCIA String
        final String caracterDelimitador=",";
        int nParametros=0;
        
        if(respuestaServidor.contains(":")){
            nParametros=1;
            
            if(respuestaServidor.contains(caracterDelimitador)){
                char[] arrayCaracteres=respuestaServidor.toCharArray();
                for(char c:arrayCaracteres){
                    if(c==',') nParametros++;
                }
            }
        }
        return nParametros;
        
        
    }
     
     public static Object[] obtenerTrabajador(String parametroTrabajador){
         Object[] atributosTrabajador=new Object[6];
         //atributosTrabajador[0] = Integer.parseInt(obtenerAtributo(parametroTrabajador, 0));
         atributosTrabajador[0] = obtenerAtributo(parametroTrabajador, 1);
         atributosTrabajador[1] = obtenerAtributo(parametroTrabajador, 2);
         atributosTrabajador[2] = obtenerAtributo(parametroTrabajador, 3);
         atributosTrabajador[3] = Float.parseFloat(obtenerAtributo(parametroTrabajador, 4));
         atributosTrabajador[4] = obtenerAtributo(parametroTrabajador, 5);
         atributosTrabajador[5] = obtenerAtributo(parametroTrabajador, 6);
         
         
         return atributosTrabajador;
         
     }
     
      public static Object[] obtenerAtributosClave(String parametroClave) {
        Object[] atributosTrabajador=new Object[6];
         atributosTrabajador[0] = Integer.parseInt(obtenerAtributo(parametroClave, 0));
         atributosTrabajador[1] = obtenerAtributo(parametroClave, 1);
         atributosTrabajador[2] = obtenerAtributo(parametroClave, 2);

         return atributosTrabajador;
    } 
     
     
     public static String obtenerAtributo(String cadena, int nAtributoBuscado) {
        int nParametro = 0;       //Mide por que parametro va
        final char caracterDelimitador = '/';
        char[] caracteres = cadena.toCharArray();
        String parametro = "";

        for (char c : caracteres) {

            if (c == caracterDelimitador) {
                nParametro++;
            } else if (nParametro == nAtributoBuscado) {
                parametro += c;
            }

        }
        return parametro;

    }
     
     
     
     public static void opcionFocusGained(java.awt.event.MouseEvent evt) {
        JLabel labelFocus = (JLabel) evt.getSource();
        Font font = labelFocus.getFont();
        Map<TextAttribute, Object> attributes = new HashMap<>(font.getAttributes());
        attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        labelFocus.setFont(font.deriveFont(attributes));
    }

    public static void opcionFocusLost(java.awt.event.MouseEvent evt) {
        JLabel labelFocus = (JLabel) evt.getSource();
        Font font = labelFocus.getFont();
        Map<TextAttribute, Object> attributes = new HashMap<>(font.getAttributes());
        attributes.put(TextAttribute.UNDERLINE, -1);
        labelFocus.setFont(font.deriveFont(attributes));
    }
    
    
}
