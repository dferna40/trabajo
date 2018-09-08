package com.ayesa.psdcg.entities;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;

@NamedQueries({
	@NamedQuery(name = "actualizar.idcarga.por.codAgrupacion", query = "UPDATE TSipsProcesoEntity SET idCarga=:idCarga where codAgrupacion = :codAgrupacion")})

@Entity(name = "TSipsProcesoEntity")
@Table(name = "sips_owner.T_SIPS_PROCESO")
public class TSipsProcesoEntity implements Serializable
{
	private static final long serialVersionUID = -8527860484476755323L;

	@Id
	@Column(name = "ID_PROCESO")
	private Long idProceso;

	@Column(name = "PROCESO")
	private String proceso;

	@Column(name = "COD_AGRUPACION")
	private String codAgrupacion;

	@Column(name = "ID_CARGA")
	private String idCarga;

	@Column(name = "ACTIVO")
	private String activo;

	@Column(name = "ENTRADA_S3")
	private String entradaS3;

	@Column(name = "SALIDA_S3")
	private String salidaS3;

	@Column(name = "FECHA")
	private String fecha;

	@Column(name = "PARM1")
	private String parm1;

	@Column(name = "PARM2")
	private String parm2;
	
	@Column(name = "PARM3")
	private String parm3;
	
	@Column(name = "PARM4")
	private String parm4;

	@Column(name = "PARM5")
	private String parm5;
	
	public Long getIdProceso() {
		return idProceso;
	}
	
	public void setIdProceso(Long idProceso) {
		this.idProceso = idProceso;
	}

	public String getProceso() {
		return proceso;
	}

	public void setProceso(String proceso) {
		this.proceso = proceso;
	}

	public String getCodAgrupacion() {
		return codAgrupacion;
	}

	public void setCodAgrupacion(String codAgrupacion) {
		this.codAgrupacion = codAgrupacion;
	}

	public String getIdCarga() {
		return idCarga;
	}

	public void setIdCarga(String idCarga) {
		this.idCarga = idCarga;
	}

	public String getActivo() {
		return activo;
	}

	public void setActivo(String activo) {
		this.activo = activo;
	}

	public String getEntradaS3() {
		return entradaS3;
	}

	public void setEntradaS3(String entradaS3) {
		this.entradaS3 = entradaS3;
	}

	public String getSalidaS3() {
		return salidaS3;
	}

	public void setSalidaS3(String salidaS3) {
		this.salidaS3 = salidaS3;
	}

	public String getFecha() {
		return fecha;
	}

	public void setFecha(String fecha) {
		this.fecha = fecha;
	}

	public String getParm1() {
		return parm1;
	}

	public void setParm1(String parm1) {
		this.parm1 = parm1;
	}

	public String getParm2() {
		return parm2;
	}

	public void setParm2(String parm2) {
		this.parm2 = parm2;
	}

	public String getParm3() {
		return parm3;
	}

	public void setParm3(String parm3) {
		this.parm3 = parm3;
	}

	public String getParm4() {
		return parm4;
	}

	public void setParm4(String parm4) {
		this.parm4 = parm4;
	}

	public String getParm5() {
		return parm5;
	}

	public void setParm5(String parm5) {
		this.parm5 = parm5;
	}
}