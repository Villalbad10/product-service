package com.product_service.exception;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import feign.Response;
import feign.codec.ErrorDecoder;
/**
 * Clase encargada de la captura de los posibles errores a un feingClient
 * @author Diego Alexander Villalba
 * @since Noviembre 2024
 */
public class FeingClientErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();
        String body = "";
        if(response.body() != null) {
            try (InputStream inputStream = response.body().asInputStream()) {
                body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
                body = "Error al leer el cuerpo de la respuesta";
            }
        }

        if(status == 404 ) {
            return new NotFoundException("Recurso no encontrado del cliente: " + body);
        } else if(status == 500 ) {
            return new InternalServerErrorException("Error interno en el servidor del cliente: " + body);
        } else if(status == 502 ) {
            return new InternalServerErrorException("Bad Gate del cliente: " + body);
        } else if(status >= 400 && status <= 499) {
            return new BadRequestException("Petición incorrecta al servicio del cliente: " + body);
        }
        return defaultErrorDecoder.decode(methodKey, response);
    }


}