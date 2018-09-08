package com.ayesa.psdcg.utiles;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ayesa.psdcg.excepciones.jZipException;

public class jUnzip {
	
	///////////////
	// ATRIBUTOS //
	///////////////
	
	private static Logger log = LogManager.getLogger(jUnzip.class);

	/////////////
	// MÉTODOS //
	/////////////
	
	// Este método tiene dos parámetros:
	// •String nombreZIP: es el nombre del fichero ZIP con su ruta.
	// •String dirBase: es la ruta donde se descomprimen los archivos.
	//
	// En el caso de que haya algún problema se lanzará una excepción de tipo jZipException.	
	static public boolean decompressFile(String nombreZIP, String dirBase, String nombreDescomprimido) throws jZipException, IOException {
		ZipFile zf = null;
		byte buffer[] = new byte[4048];/* tamaño del buffer de descompresión */
		int lenToWrite;
		boolean flagDirBase;
		String separator = "/";
		
		if (dirBase == null) {
			flagDirBase = false;
			dirBase = "";
		} else {
			flagDirBase = true;
			if (!dirBase.endsWith(File.separator) && dirBase.length() > 0) {
				separator = File.separator;
			}
		}
		
		FileOutputStream fTarget = null;
		
		try {
			zf = new ZipFile(nombreZIP);
			Enumeration e = zf.entries();
			fTarget = new FileOutputStream(dirBase + separator + nombreDescomprimido);
			
			for (; e.hasMoreElements();) {
				ZipEntry z = (ZipEntry) e.nextElement();
				log.debug(z.getName() + " -> " + dirBase + separator + nombreDescomprimido);
				InputStream in = zf.getInputStream(z);
				
				if (flagDirBase)
					toolsZip.createDirFromNameFile(dirBase, z.getName());
							
				while ((lenToWrite = in.read(buffer)) != -1) {
					fTarget.write(buffer, 0, lenToWrite);
				}
				
				fTarget.close();
			}
		}catch (IOException e) {
			throw new jZipException("[decompressFile] Exception " + e.getMessage());
		}finally {
			if (zf != null) {
				log.info("ZipFile abierto. Se procede a cerrarlo");
				zf.close();
				log.info("Se cierra ZipFile");
			}
			
			if (fTarget != null) {
				log.info("fTarget abierto. Se procede a cerrarlo");
                fTarget.close();
                log.info("Se cierra fTarget");
			}
		}
		return true;
	}
}