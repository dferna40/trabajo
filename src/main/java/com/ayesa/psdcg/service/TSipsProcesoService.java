package com.ayesa.psdcg.service;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.ayesa.psdcg.dao.TSipsProcesoDAO;
import com.ayesa.psdcg.excepciones.PSDCGException;


public class TSipsProcesoService
{
	public static void actualizar(Session sesion, Transaction transaccion, final String idCarga, final String codAgrupacion) throws PSDCGException
	{
		TSipsProcesoDAO.actualizar(sesion, transaccion, idCarga, codAgrupacion);	
	}
}