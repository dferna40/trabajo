package com.ayesa.psdcg.utiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.ayesa.psdcg.constantes.Constantes;
import com.ayesa.psdcg.excepciones.PSDCGException;
import com.ayesa.psdcg.excepciones.jZipException;

public class Util {
	
	///////////////
	// ATRIBUTOS //
	///////////////
	private static Logger log = LogManager.getLogger(Util.class);
	public static final AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
	

	/////////////
	// MÉTODOS //
	/////////////
		
	//Borra los ficheros pasados como parámetros localizados en la ruta s3BucketName + s3ObjectPath
	public static void deleteS3Object(String s3BucketName, String s3ObjectPath)	throws IOException {
		s3Client.deleteObject(s3BucketName, s3ObjectPath);
	}
	
	//Descarga los ficheros de S3 en una ruta temporal de trabajo
	public static File descargarFicherosS3(String bucketName, List<String> files){		
		File dirTemporal = new File("tmpPSDCG");
		dirTemporal.mkdirs();
		String[] partesRutaFich = null;
		String nombreFich = null; 		
				
		log.info("Descargamos los ficheros en la ruta: " + dirTemporal.getAbsolutePath());
		
		for(String fich : files){
			partesRutaFich = fich.split(File.separator); 
			nombreFich = partesRutaFich[partesRutaFich.length - 1];
			
			s3Client.getObject(
			        new GetObjectRequest(bucketName, fich),
			        new File(dirTemporal,nombreFich)
			);
		}
		
		log.info("Los ficheros se han descargado correctamente");
		
		return dirTemporal;
	}
	
	//Descomprimir los ficheros localizados en la ruta temporal de trabajo
	public static void descomprimrFicherosS3(File dirTemporal, List<String> files) throws PSDCGException{
		String[] partesRutaFich = null;
		String nombreFich = null; 
		String nombreFichSinExt = null;
		
		log.info("Procedemos a descomprimir los ficheros anteriormente descargados");
		
		for(String fich : files){
			partesRutaFich = fich.split(File.separator); 
			nombreFich = partesRutaFich[partesRutaFich.length - 1];
			nombreFichSinExt = (nombreFich.split("\\."))[0]; 
			
			try {							
				jUnzip.decompressFile(dirTemporal.getPath().concat(File.separator).concat(nombreFich), dirTemporal.getPath(), nombreFichSinExt.concat(Constantes.EXTENSION_FICH_XML));
				log.info("Se completa la descarga del fichero: " + dirTemporal.getAbsolutePath().concat(File.separator).concat(nombreFich));
			} catch (jZipException e) {
				log.error("Error al descomprimir el fichero: " + dirTemporal.getPath().concat(nombreFich));
				throw new PSDCGException("Error al descomprimir el fichero: " + dirTemporal.getAbsolutePath().concat(nombreFich)); 
			}catch (IOException e) {
				log.error("[decompressFile] Exception in close file: " + e.getMessage());
				throw new PSDCGException("[decompressFile] Exception in close file: " + e.getMessage()); 
			}
		}
		
		log.info("Los ficheros se han descomprimido correctamente");
	}
	
	
	//Elimina todos los directorios existentes en la ruta indicada
	public static void eliminarDirectorio(String directorio) 
	{
		File d1 = new File(directorio);		
		String nombre = File.separator + directorio;
			
		for(File f1: d1.listFiles()) {
			//Borro el contenido del directorio			
			f1.delete();
			log.info("Se ha eliminado el fichero: " + nombre + File.separator + f1.getName());
		}
			
		//Borro el directorio
		d1.delete();
		log.info("Se ha eliminado el directorio: " + nombre);
	} 	 
	 
	//Elimina del directorio los ficheros que tengan una antigüedad superior a numDiasFicheros días
	public static boolean eliminarFicherosAntiguosDelDirectorio(String directorio, int numDiasFicheros, String parteNomFich) throws PSDCGException{
		boolean res = false;
					
		Calendar c = obtenerCalendarioFechaActual();
		c.add(Calendar.DATE,-numDiasFicheros);
				
		try {		
			File f = new File(directorio);
			
			//Listamos los ficheros del directorio
			for(File fi:f.listFiles()) {
				
				if(parteNomFich != null){
					//Si se ha informado parte del nombre de los ficheros que deseamos borrar, entonces  
					//filtramos los ficheros que corresponden a nuestro proceso y que superen numDiasFicheros días
					if (fi.getName().contains(Constantes.PARTE_NOMBRE_FICH_LOG) &&
						superaNumDias(fi.lastModified(), c.getTime())){
						log.info("Eliminando fichero anterior a " + numDiasFicheros + " días: " + fi.getName());
						fi.delete();
					}
					
				}else{
					//Si no se ha informado parte del nombre de los ficheros que deseamos borrar, entonces
					// filtramos todos los ficheros que superen numDiasFicheros días
					if (superaNumDias(fi.lastModified(), c.getTime())){
						log.info("Eliminando fichero anterior a " + numDiasFicheros + " días: " + fi.getName());
						fi.delete();
					}
				}				
			}
			
			res = true;
			
		} catch(SecurityException e) {
			log.error("Fallo eliminando ficheros de logs que tengan una antigüedad superior a "+numDiasFicheros+" días: " + e.getMessage());
			throw new PSDCGException("Fallo eliminando ficheros de logs que tengan una antigüedad superior a "+numDiasFicheros+" días: " + e.getMessage());
		}
	
		return res;
	 }
	
	//Recuperamos las rutas de los ficheros localizados en un directorio dentro de S3
	public static List<String> getRutasFicherosS3(String bucketName, String s3ConfigFile){

	    log.info("Obtenemos los ficheros almacendos en S3 en la ruta: " + bucketName + File.separator+ s3ConfigFile);

	    ObjectListing listing;
	    List<String> listadoRutasFicheros = new LinkedList<>();
	    ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName).withPrefix(s3ConfigFile);

	    do {
	    	listing = s3Client.listObjects(listObjectsRequest);
	    
	    	for (S3ObjectSummary object : listing.getObjectSummaries()){
	    		if(!object.getKey().equals(s3ConfigFile)){
	    			//Si no es el directorio raíz, obtenemos el nombre del fichero contenido en la ruta 
	    			log.info("FICHERO: " + bucketName + File.separator + object.getKey());
	    			listadoRutasFicheros.add(object.getKey());
	    		}
	    	}
	    } while (listing.isTruncated());

	    log.info("Encontrados " + listadoRutasFicheros.size() + " ficheros almacenados");
	    
	    return listadoRutasFicheros;
	}
	
	//Cargo el contexto con el fichero sips_load.properties tratado
	public static void loadAppContext(String s3BucketName, String s3ConfigFile) throws PSDCGException, IOException {		
		//Cargo en el contexto el fichero sips_load.properties localizado en el bucket dentro de la ruta especificada
		loadAppContextFromConfigFile(s3BucketName, s3ConfigFile);
		AppContext ctxt = AppContext.getInstance();
		
		//Recupero todas las propiedades del fichero
		List<String> keys = ctxt.getPropertyNames();
		
		//Recorro las propiedades del fichero
		for (String key : keys) {
			String value = ctxt.getProperty(key);
			
			//Posición que ocupa el carácter { dentro del valor de la propiedad
			int start = value.indexOf("{");
			
			//Comprueba si existe el caracter { dentro del valor de la propiedad para tratarla 
			if (start != -1) {
				//Posición que ocupa el carácter } dentro del valor de la propiedad
				int end = value.indexOf("}");
				//Primera parte del valor de la propiedad hasta el caracter { 
				String firstLinePart = value.substring(0, start);
				//última parte del valor de la propiedad a partir del carácter }
				String lastLinePart = value.substring(end + 1);
				//Parte del valor de la propiedad comprendida entre {} a partir de la posición del caracter $ 
				String propertyName = value.substring(start + 2, end); // +2 due to $ position
				
				//El valor real de la propiedad se compone de la concatenación de todas las partes
				String result = firstLinePart + ctxt.getProperty(propertyName) + lastLinePart;
				ctxt.setProperty(key, result);
			}//Comprueba si el valor de la propiedad comieza por $ para tratarla 
			else if (value.startsWith("$")) {
				String valueKey = value.substring(1, value.length());

				//El valor real de la propiedad es el valor almacenado en otra propiedad
				ctxt.setProperty(key, ctxt.getProperty(valueKey));
			}
		}
					
		log.info("Fichero de configuración cargado correctamente");
	}
		
	//Recuperamos el fichero sips_load.properties de S3 Amazon
	public static void loadAppContextFromConfigFile(String s3BucketName, String s3ConfigFile) throws PSDCGException, IOException {
		S3Object s3Obj = s3Client.getObject(new GetObjectRequest(s3BucketName, s3ConfigFile));
		AppContext.getInstance().load(s3Obj.getObjectContent());
	}	
	
	//Obtenemos la fecha actual en formato Calendar
	public static Calendar obtenerCalendarioFechaActual()
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		return cal;
	}
	
	//Accedemos al fichero de configuración para obtener el valor asociado al parámetro que se pasa
	//como argumento
	public static String obtenerConfiguracion(String parametro) throws PSDCGException
	{
		String valor = null;

		try
		{
			//Accedemos al fichero configuracion.properties
			final ResourceBundle rs = ResourceBundle.getBundle(Constantes.FICHERO_CONFIGURACION);
			//Recuperamos el valor asociado al parámetro que se le pasa
			valor = rs.getString(parametro);
		}catch (final NullPointerException e){
			log.error("Se ha obtenido un NULL al buscar el parametro " + parametro + ": " + e.getMessage());
			throw new PSDCGException("Se ha obtenido un NULL al buscar el parametro " + parametro + ": " + e.getMessage());
		}catch (final MissingResourceException e){
			log.error("No se ha encontrado el recurso: " + e.getMessage());
			throw new PSDCGException("No se ha encontrado el recurso: " + e.getMessage());
		}catch (final ClassCastException e){
			log.error("Se ha producido un error al hacer el casting del parametro " + parametro + ": " + e.getMessage());
			throw new PSDCGException("Se ha producido un error al hacer el casting del parametro " + parametro + ": " + e.getMessage());
		}

		return valor;
	}
	
	
	//Permite convertir una fecha (Date) en String formateado yyyyMMdd
	public static String parseDateToString(Date fecha) 
	{
		SimpleDateFormat formato = new SimpleDateFormat("yyyyMMdd");
		String fechaString = formato.format(fecha);

		return fechaString;
	}
		
	//Preparamos el fichero compuesto por la concatenación del contenido de los 
	//ficheros descargados en el directorio temporal de trabajo 
	public static File preparaFicheroSalida(File dirTemporal) throws PSDCGException, IOException
	{	
		log.info("Preparamos el fichero de salida");
		
		File destino = new File(dirTemporal,"PS_NEW");
		OutputStream out = null;
		
				
		try {
			destino.createNewFile();
			out = new FileOutputStream(destino, true); //El segundo parámetro indica que escribe a continuación de lo que haya
		
			for(File fich : dirTemporal.listFiles()){
				if(fich.getName().contains(Constantes.EXTENSION_FICH_XML)){
					tratarFichero(out, fich);
				}
			}			
			out.close();			
		}catch (IOException e){				
			log.error("Error mientras se preparaba el fichero de salida: " + e.getMessage());
			throw new PSDCGException("Error mientras se preparaba el fichero de salida: " + e.getMessage());
		}finally {
			if (out != null) {
				log.info("FileOutputStream abierto. Se procede a cerrarlo");
				out.close();
				log.info("Se cierra FileOutputStream");
			}
		}
		
		log.info("El fichero se ha generado correctamente");
		
		return destino;			
	}	
	
	//Subir un fichero a S3
	public static void putS3Object(String bucket, String key, File file) {
		s3Client.putObject(bucket, key, file);
	}
	
	//Comprueba si una fecha dada es anterior a una fecha límite
	public static boolean superaNumDias(long fechaUltModificacion, Date fechaLimite){
		return new Date(fechaUltModificacion).compareTo(fechaLimite) < 0;
	}
	
	//Se copia el contenido del fichero de entrada en el fichero de salida.
	public static void tratarFichero(OutputStream out, File fich) throws IOException{
		InputStream in = null;
		
		try{
			in = new FileInputStream(fich);				
			byte[] buf = new byte[1024];
			int len;
			
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);					
			}
		
			in.close();
		}finally{
			if(in !=null){
				log.info("FileInputStream abierto. Se procede a cerrarlo");
				in.close();
				log.info("Se cierra FileInputStream");
			}
		}
	}
}