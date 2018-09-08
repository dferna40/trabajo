package com.ayesa.psdcg.utiles;

import java.io.*;
import java.util.*;

import com.ayesa.psdcg.excepciones.jZipException;

/**
 * La clase toolsZip contiene todos los métodos que son de utilidad para la manipulación de ficheros y directorios
 * */

public class toolsZip {
	
	/////////////
	// MÉTODOS //
	/////////////
	
	//Crea un directorio con el nombre especificado por el parámetro namePath.
    static public boolean mkdir(String namePath) throws jZipException{
    	File file = new File(namePath);
    	boolean ret = false;
    	try {
    		if (file.exists()) {
    			if (file.isDirectory())
    				return true;
    			else
    				return false;
    		}else
    			ret = file.mkdirs();
    	}
    	catch(SecurityException ee) {
    		String str = "[mkdir]Exception " + ee.getMessage();         
    		throw new jZipException(str);   
    	};
    	
    	return ret;
    }

    //Tiene una doble utilidad: por un lado coge el el nombre del fichero  file  y traduce las barra de los directorios a las 
    //propias de la plataforma de ejecución y si full es false sólo retorna el nombre del fichero, si es true retorna todo el path.
    static public boolean createDirFromNameFile(String dirBase, String pathfile) throws jZipException{
    	String path; 
    	String file; 
    	int lenpath;
    	int lenfile;
   
    	//path completo sustituyendo caracteres no adecuados
    	path = getRealFileName(pathfile, true);    
    	file = getRealFileName(pathfile, false);
    	lenpath = path.length();
    	lenfile = file.length();
    	
    	if(lenpath  == lenfile && dirBase.length()==0)
    		return true;
      
    	file = path.substring(0, lenpath-lenfile);
    	
    	if(file.endsWith(File.separator)) {
    		path = file.substring(0, lenpath-lenfile-1);
    	}
    	else
    		path = file;
      
    	try {   
    		if(!mkdir(dirBase + path))
    			return false;
    	}
    	catch(Exception e) {
    		String str = "[mkdir]Exception " + e.getMessage();         
    		throw new jZipException(str);
    	}
    	
    	return true;
    } 
    
    //Crea el directorio donde se debe grabar el fichero teniendo en cuenta el directorio base y el nombre del directorio de la aplicación.
    static public String getRealFileName(String file, boolean full) {
    	String ret = file;
    	String aux = file.replace('/', File.separator.charAt(0));
    	if(full)
    		ret = aux; 
    	else {   
    		StringTokenizer st = new StringTokenizer(aux, File.separator);
            while(st.hasMoreTokens()) {
            	ret = st.nextToken();
            }
    	}   
        
    	return ret;
    }
}

