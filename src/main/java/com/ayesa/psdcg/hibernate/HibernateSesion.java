package com.ayesa.psdcg.hibernate;

import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import com.ayesa.psdcg.constantes.Constantes;
import com.ayesa.psdcg.excepciones.PSDCGException;
import com.ayesa.psdcg.utiles.AppContext;

public class HibernateSesion
{
	///////////////
	// ATRIBUTOS //
	///////////////
	
	private static final HibernateSesion instancia = new HibernateSesion();
	private static SessionFactory sessionFactory;
	private static Logger log = LogManager.getLogger(HibernateSesion.class);

	///////////////////
	// CONSTRUCTORES //
	///////////////////

	private HibernateSesion() {
		
	}

	/////////////////////////
	// GETTERS AND SETTERS //
	/////////////////////////
	public static HibernateSesion getInstancia() {
		return instancia;
	}

	/////////////
	// MÉTODOS //
	/////////////
	
	public static Session abrirSession() throws Exception
	{
		HibernateSesion.getInstancia();

		if(sessionFactory == null)
		{
			configurarSession();
		}

		return sessionFactory.openSession();
	}

	public static void cerrarSesion(final Session sesion)
	{
		if (sesion != null && sesion.isConnected())
		{
			sesion.disconnect();
		}
	}
	
	private static void configurarSession() throws Exception {

		String bdUrl = null;
		String bdUser = null;
		String bdPasswor = null;

		try {
			final Properties propiedades = new Properties();
			propiedades.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect"); 
			propiedades.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");

			//Recupero la configuración de la BD del fichero sips_load.properties
			bdUrl = AppContext.getInstance().getProperty(Constantes.BD_URL);
			bdUser = AppContext.getInstance().getProperty(Constantes.BD_USER);
			bdPasswor = AppContext.getInstance().getProperty(Constantes.BD_PASSWORD);
			
			propiedades.setProperty("hibernate.connection.url", bdUrl);
			propiedades.setProperty("hibernate.connection.username", bdUser);
			propiedades.setProperty("hibernate.connection.password", bdPasswor);
			propiedades.setProperty("hibernate.connection.autoReconnect", "true");

			final Configuration configuracion = new Configuration();
			configuracion.addAnnotatedClass(com.ayesa.psdcg.entities.TSipsProcesoEntity.class);
			 
			sessionFactory = configuracion.addProperties(propiedades).buildSessionFactory();			
		}
		catch(Exception e)
		{
			log.error("Error al inicializar la conexion con base de datos: " + e.getMessage());
			log.error("No existe conexion con base de datos, estos son los parametros de inicializacion:");
			log.error("URL de base de datos ....: " + bdUrl);
			log.error("Usuario empleado ........: " + bdUser);
			log.error("Password del usuario ....: " + bdPasswor);
			throw new PSDCGException(e);
		}
	}	
}
