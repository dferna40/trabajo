package com.ayesa.psdcg.utiles;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ayesa.psdcg.excepciones.PSDCGException;

public class AppContext {

	///////////////
	// ATRIBUTOS //
	///////////////
	
	private static Logger log = LogManager.getLogger(AppContext.class);
    private static AppContext appContext = new AppContext();
    private LinkedHashMap<String, String> props;
    
	///////////////////
	// CONSTRUCTORES //
	///////////////////

    private AppContext(){
    	props = new LinkedHashMap<>();
    }    
    
	/////////////
	// MÉTODOS //
	/////////////
    
    
    public static AppContext getInstance(){
    	return appContext;
    }
    
    public String getProperty(String propertyName){
        return props.get(propertyName);
    }
    
    public List<String> getPropertyNames(){
		List<String> propsNames = new ArrayList<>();
		
		Iterator<String> keys = props.keySet().iterator();
		while (keys.hasNext()) {
			propsNames.add(keys.next());
		}
		
		return propsNames;
	}
  
    //Recupera las propiedades registradas en el contexto
    public void load(InputStream is) throws PSDCGException, IOException{
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ( (line = br.readLine()) != null ) {
                if (!line.trim().startsWith("#")) {
                    String[] propParts = line.split("=");
                    if (propParts.length == 2) {
                        props.put(propParts[0], propParts[1]);
                    }
                }
            }
        }catch(IOException e) {
      	  log.error("Error cargando el fichero de propiedades: " + e.getMessage());
      	  throw new PSDCGException("Error cargando el fichero de propiedades: " + e.getMessage());
        } finally {
        	 if (br != null){
             	log.info("Buffer abierto. Se procede a cerrarlo");
             	br.close();
             	log.info("Se cierra el buffer");
             }
        }
    }
    
	public void setProperty(String name, String value){
		props.put(name, value);
	}
}