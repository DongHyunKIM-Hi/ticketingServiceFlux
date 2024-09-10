package org.example.ticketingserviceflux.controller.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Setter
@Getter
public class ApiException extends RuntimeException{

    private HttpStatus httpStatus;
    private String code;
    private String reason;
}
