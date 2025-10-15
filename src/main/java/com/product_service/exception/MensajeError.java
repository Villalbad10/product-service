package com.product_service.exception;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Mensaje de retorno para el manjeo de errores centralizado
 * 
 * @author Diego Alexander Villalba
 * @since Octubre 2022
 */
@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MensajeError {
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Atributos de la clase
	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** mensaje que sera presentado al usuario final */
	private List<String> mensaje;
	/** path en el cual esta ocurriendo el error */
	private String path;
}
