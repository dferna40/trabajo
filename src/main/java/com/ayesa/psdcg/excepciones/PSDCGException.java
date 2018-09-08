package com.ayesa.psdcg.excepciones;

public class PSDCGException extends Exception
{
	
	///////////////
	// ATRIBUTOS //
	///////////////
	
	private static final long serialVersionUID = 8372082240533518368L;

	/////////////
	// MÉTODOS //
	/////////////
	
	public PSDCGException(final String message)
	{
		super(message);
	}

	public PSDCGException(final Throwable cause)
	{
		super(cause);
	}
}