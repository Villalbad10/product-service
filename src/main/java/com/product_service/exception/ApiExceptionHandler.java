package com.product_service.exception;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Manejador de errores centralizado
 * 
 * @author Diego Alexander Villalba
 * @since Octubre 2022
 */
@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler {
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Métodos de la clase
	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Maneja errores de validación de argumentos en los métodos del controlador.
	 * 
	 * @param request - solicitud HTTP recibida
	 * @param ex - excepción de validación
	 * @return objeto con el detalle del error
	 */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler({
		org.springframework.web.bind.MethodArgumentNotValidException.class
	})
	@ResponseBody
	public MensajeError handleValidationExceptions(HttpServletRequest request, MethodArgumentNotValidException ex) {
		MensajeError error = new MensajeError();
		error.setPath(request.getRequestURI());
		
		List<String> errores = ex.getBindingResult().getAllErrors()
			.stream().map(errorObject -> {
				String fieldName = ((FieldError) errorObject).getField();
				String errorMessage = errorObject.getDefaultMessage();
				return fieldName + ": " + errorMessage;
			})
			.collect(Collectors.toList());
		
		error.setMensaje(errores);
		return error;
	}
	
	/**
	 * Maneja errores tipo NOT_FOUND.
	 * 
	 * @param request - solicitud HTTP recibida
	 * @param exception - excepción de validación
	 * @return objeto con el detalle del error
	 */
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler({
			com.product_service.exception.NotFoundException.class,
			com.product_service.exception.ResourceNotFoundException.class
	})
	@ResponseBody
	public MensajeError notFoundRequest(HttpServletRequest request, Exception exception) {
		return new MensajeError(List.of(exception.getMessage()), request.getRequestURI());
	}
	
	/**
	 * Maneja errores tipo BAD_REQUEST comunes.
	 * 
	 * @param request - solicitud HTTP recibida
	 * @param exception - excepción de validación
	 * @return objeto con el detalle del error
	 */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler({
		com.product_service.exception.BadRequestException.class,
		org.springframework.dao.DuplicateKeyException.class,
		org.springframework.web.HttpRequestMethodNotSupportedException.class,
		org.springframework.web.bind.MissingRequestHeaderException.class,
		org.springframework.web.bind.MissingServletRequestParameterException.class,
		org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
		org.springframework.http.converter.HttpMessageNotReadableException.class,
		IllegalArgumentException.class
	})
	@ResponseBody
	public MensajeError badRequest(HttpServletRequest request, Exception exception) {
		return new MensajeError(List.of(exception.getMessage()), request.getRequestURI());
	}
	
	/**
	 * Maneja errores tipo FORBIDDEN.
	 * 
	 * @param request - solicitud HTTP recibida
	 * @param exception - excepción de validación
	 * @return objeto con el detalle del error
	 */
	@ResponseStatus(HttpStatus.FORBIDDEN)
	@ExceptionHandler({
			com.product_service.exception.ForbiddenException.class
	})
	@ResponseBody
	public MensajeError forbiddenRequest(HttpServletRequest request, Exception exception) {
		return new MensajeError(List.of(exception.getMessage()), request.getRequestURI());
	}
	
	/**
	 * Maneja errores de integridad de datos (CONFLICT).
	 * 
	 * @param request - solicitud HTTP recibida
	 * @param exception - excepción de validación
	 * @return objeto con el detalle del error
	 */
	@ResponseStatus(HttpStatus.CONFLICT)
	@ExceptionHandler({
		org.springframework.dao.DataIntegrityViolationException.class,
		com.product_service.exception.ConflictException.class
	})
	@ResponseBody
	public MensajeError conflictRequest(HttpServletRequest request, Exception exception) {
		log.error(exception.getMessage());
		return new MensajeError(List.of("Las claves no pueden ser duplicadas."), request.getRequestURI());
	}
	
	/**
	 * Maneja errores de autorización (UNAUTHORIZED).
	 */
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	@ExceptionHandler({
			com.product_service.exception.UnauthorizedException.class
	})
	public void unauthorizedRequest() { }
	
	/**
	 * Maneja errores internos del servidor (INTERNAL_SERVER_ERROR).
	 * 
	 * @param request - solicitud HTTP recibida
	 * @param exception - excepción de validación
	 * @return objeto con el detalle del error
	 */
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler({
		Exception.class,
		IOException.class
	})
	@ResponseBody
	public MensajeError fatalErrorUnexpectedRequest(HttpServletRequest request, Exception exception) {
		log.error(exception.getMessage());
		exception.printStackTrace();
		return new MensajeError(List.of("Contacte con un administrador"), request.getRequestURI());
	}
	
	/** timeout base de datos desde @Transactional */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler({
		org.springframework.orm.jpa.JpaSystemException.class
	})
	@ResponseBody
	public MensajeError badRequestManualTransactional(HttpServletRequest request) {
		return new MensajeError(List.of("Se agoto el tiempo de respuesta en la transacción."), request.getRequestURI());
	}
	
}
