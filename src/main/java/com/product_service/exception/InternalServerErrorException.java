package com.product_service.exception;

/**
 * Manejo de error InternalServerErrorException
 * 
 * @author Diego Alexander Villalba
 * @since Octubre 2022
 */
public class InternalServerErrorException extends RuntimeException {

	private static final long serialVersionUID = -8733576717869827852L;

	///////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Métodos de la clase
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////

	public InternalServerErrorException(String detalle) {
        super(detalle);
    }

}
