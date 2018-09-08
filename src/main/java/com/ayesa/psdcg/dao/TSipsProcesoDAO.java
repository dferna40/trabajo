package com.ayesa.psdcg.dao;

import javax.persistence.Query;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.ayesa.psdcg.excepciones.PSDCGException;


public class TSipsProcesoDAO
{
	private static Logger log = LogManager.getLogger(TSipsProcesoDAO.class);
	
	public static void actualizar(Session sesion, Transaction transaccion, final String idCarga, final String codAgrupacion) throws PSDCGException
	{
		try{
			final Query query = sesion.getNamedQuery("actualizar.idcarga.por.codAgrupacion");
			query.setParameter("idCarga", idCarga);
			query.setParameter("codAgrupacion", codAgrupacion);
			query.executeUpdate();		

			log.info("A los registros de la tabla t_sips_proceso con COD_AGRUPACION " + codAgrupacion + " se les ha actualizado el campo ID_CARGA a " + idCarga + " correctamente");
		}catch (final Exception e){
			log.error("Error al actualizar los registros de la tabla t_sips_proceso con COD_AGRUPACION " + codAgrupacion + " al valor ID_CARGA " + idCarga + "; Error: " + e.getMessage());
			throw new PSDCGException("Error al actualizar los registros de la tabla t_sips_proceso con COD_AGRUPACION " + codAgrupacion + " al valor ID_CARGA " + idCarga + "; Error: " + e.getMessage());
		}		
	}
}