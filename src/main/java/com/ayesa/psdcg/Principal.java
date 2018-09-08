package com.ayesa.psdcg;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import com.ayesa.psdcg.constantes.Constantes;
import com.ayesa.psdcg.excepciones.PSDCGException;
import com.ayesa.psdcg.hibernate.HibernateSesion;
import com.ayesa.psdcg.service.TSipsProcesoService;
import com.ayesa.psdcg.utiles.AppLock;
import com.ayesa.psdcg.utiles.Util;



public class Principal
{
	private static Logger log = LogManager.getLogger(Principal.class);
			
	public static void main(final String[] args)
	{	
		boolean existeOtraInstanciaEnEjecucion = false;
		boolean resultadoEjecucion = false;
		int codigoSalidaBatch = -1;
		Session sesion = null;
		Transaction transaccion = null;
		String idCarga = null;
		
		
		if (args.length==4){
		
			try{
				log.info("Comienza la ejecución del proceso de preparación del fichero PSDCG para el código de agrupación 0060");
						
				//Se eliminan los ficheros de logs que tengan una antigüedad superior a 180 días
				String directorioLog = Util.obtenerConfiguracion(Constantes.DIRECTORIO_LOG);			
				Util.eliminarFicherosAntiguosDelDirectorio(directorioLog, 180, Constantes.PARTE_NOMBRE_FICH_LOG);
				
				//Comprobacion instancia única
				if (!AppLock.setLock(Constantes.PROCESO)) {
					existeOtraInstanciaEnEjecucion = true;								
					log.error("Ya existe una instancia del programa en ejecución. Se detiene la ejecución actual");
				} else {
					log.info("Comprobación de instancia única en ejecución OK");
										
					// Tanto bucket como s3pathFileConf llegarán como parámetros enviados por lambda					 
					String bucket = args[0]; //"enel-noprod-loib-ap02145-sips";
					String s3pathFileConf = args[1]; //"DEV/configuration/sips_load.properties";
					String directorioCarga = args[2]; //"DEV/cargas/0060/"
					String directorioDescarga = args[3]; //"DEV/cargas/0060/descarga/"
					
					//Listamos los ficheros localizados en S3 dentro del directorio de descargas
					List<String> listadoRutasFicherosS3 = Util.getRutasFicherosS3(bucket, directorioDescarga);
					
					if(!listadoRutasFicherosS3.isEmpty()){
						//Recuperar el fichero de configuración sips_load.properties almacenado en S3
						log.info("Cargo en el contexto de la aplicación el fichero sips_load.properties");
						Util.loadAppContext(bucket, s3pathFileConf);
					
						//Descargar los ficheros localizados en S3 dentro del directorio de descargas
						File dirTemporal = Util.descargarFicherosS3(bucket, listadoRutasFicherosS3);
							
						//Descomprimir fichero/s
						Util.descomprimrFicherosS3(dirTemporal, listadoRutasFicherosS3);
						
						//Generamos el fichero de salida concatenando en un único fichero los ficheros descargados anteriormente.
						File ficheroSalida = Util.preparaFicheroSalida(dirTemporal);
						
						//Borro los ficheros localizados en S3 dentro del directorio de descargas
						borrarFicherosS3(bucket, listadoRutasFicherosS3);
						
						//obtener idCarga
						Date fechaActual = new Date();
						idCarga =  Util.parseDateToString(fechaActual) + Constantes.ID_CARGA_GAS;
						
						//Abrir conexión a BD
						sesion = HibernateSesion.abrirSession();
						transaccion = sesion.beginTransaction();
						
						TSipsProcesoService.actualizar(sesion, transaccion, idCarga, Constantes.CODIGO_AGRUPACION);
						
						//Subimos a S3 dentro del directorio de cargas el fichero generado anteriormente y los ficheros .zip
						subirFicheroS3(bucket, ficheroSalida, dirTemporal, directorioCarga);
						
						//Borro el directorio temporal de trabajo
						Util.eliminarDirectorio(ficheroSalida.getParent());
						
					}else{
						log.info("No hay ficheros que procesar. Se detiene la ejecución actual");
						codigoSalidaBatch = 0; //Se considera que la ejecución ha terminado OK
					}
					
					resultadoEjecucion = true;				
				}
			}catch (PSDCGException e){
				log.error("Error al ejecutar el proceso de preparación del fichero PSDCG para el código de agrupación 0060: " + e.getMessage());
			}catch (Exception e){
				log.error("Se ha producido un error no controlado al ejecutar el proceso de preparación del fichero PSDCG para el código de agrupación 0060: " + e.getMessage());		
			} finally{ 
				
				//Liberar el proceso
				if (!existeOtraInstanciaEnEjecucion) {
					AppLock.releaseLock(); 
				}			
				
				//Formalizar la transacción
				if (resultadoEjecucion) {
					if (transaccion != null && transaccion.isActive()){
						transaccion.commit();
						log.info("Se realiza commit de la transacción");
						codigoSalidaBatch = 0;  //Todo el proceso ha terminado OK
					}
				}else{
					if (transaccion != null && transaccion.isActive()){
						transaccion.rollback();
						log.info("Se realiza rollback de la transacción");
					}
				}
				
				//Cerrar conexión a BD
				HibernateSesion.cerrarSesion(sesion);
				
				log.info("Fin de la ejecución del proceso de preparación del fichero PSDCG para el código de agrupación 0060. CODIGO SALIDA: " + codigoSalidaBatch);
				System.exit(codigoSalidaBatch);			
			}		
		}else {
			log.error("No se han informado todos los argumentos de entrada");
		}
	}	
	
	
	//Borra los ficheros pasados como parámetros localizados en la ruta bucketName + file 
	private static void borrarFicherosS3(String bucketName, List<String> files) throws PSDCGException
	{	
		boolean error = false;
		
		for(String fich : files){
			try{
				Util.deleteS3Object(bucketName, fich);
				log.info("Se ha borrado de S3 el fichero " + bucketName + File.separator + fich);
			}catch(IOException e){
				error = true;
				String nombreCompletoFich = bucketName + File.separator + fich;
				log.error("Error mientras borraba el fichero " + nombreCompletoFich + " de S3: " + e.getMessage());
			}
		}
		
		if(error){		
			throw new PSDCGException("Error mientras borraba los ficheros de S3");
		}
	} 
	
	//Subimos a S3 dentro del directorio de cargas el fichero generado anteriormente, tanto a la ruta del identificador
	//de carga como a la principal (sin olvidar los ficheros .zip en la ruta idCarga)
	private static void subirFicheroS3(String bucket, File ficheroSalida, File dirTemporal, String directorioCarga) throws PSDCGException{
		log.info("Subimos a S3 el fichero generado");
		
		Date fechaActual = new Date();
		String idCarga =  Util.parseDateToString(fechaActual) + Constantes.ID_CARGA_GAS;
		
		String directorioSalida1 = directorioCarga;
		String directorioSalida2 = directorioSalida1 + idCarga + File.separator;
						
		Util.putS3Object(bucket, directorioSalida1+ficheroSalida.getName(), ficheroSalida);
		Util.putS3Object(bucket, directorioSalida2+ficheroSalida.getName(), ficheroSalida);
		
		for(File fich : dirTemporal.listFiles()){
			if(fich.getName().contains(Constantes.EXTENSION_FICH_ZIP)){
				Util.putS3Object(bucket, directorioSalida2+fich.getName(), fich);
			}
		}
				
		log.info("Los ficheros se han subido correctamente a los directorios: " + bucket + File.separator + directorioSalida1 + " y " + bucket + File.separator + directorioSalida2);
		
		//return idCarga;
	}
}