package com.ayesa.psdcg.excepciones;

public class jZipException extends Exception {
	
	///////////////
	// ATRIBUTOS //
	///////////////
	
	private static final long serialVersionUID = 227089359707963556L;

	/////////////
	// MÉTODOS //
	/////////////	
	
	public jZipException(String strError) {
		super(strError);
	}
}
