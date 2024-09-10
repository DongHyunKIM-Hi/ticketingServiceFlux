package org.example.ticketingserviceflux.exception;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum ErrorCode {


    QUEUE_ALREADY_REGISTER_USER_ID(HttpStatus.CONFLICT, "code-01","Already register userID"),
    QUEUE_UNFORMATTED_REGISTER_USER_ID(HttpStatus.CONFLICT, "code-02","userID is %s");

    private HttpStatus httpStatus;
    private String code;
    private String reason;


    public ApiException build() {
        return new ApiException(httpStatus,code,reason);
    }

    public ApiException build(Object ...args) {
        return new ApiException(httpStatus,code,reason.formatted(args));
    }
}
