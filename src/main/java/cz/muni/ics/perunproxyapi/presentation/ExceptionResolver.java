package cz.muni.ics.perunproxyapi.presentation;

import cz.muni.ics.perunproxyapi.persistence.exceptions.EntityNotFoundException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.InternalErrorException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.InvalidRequestParameterException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ExceptionResolver {

    // 4xx

    @ExceptionHandler(value = { InvalidRequestParameterException.class })
    public ResponseEntity<Object> exception400(Exception exception) {
        log.warn("Returning {}, caught {}", HttpStatus.BAD_REQUEST, exception.getClass(), exception);
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(value = { EntityNotFoundException.class })
    public ResponseEntity<Object> exception404(Exception exception) {
        log.debug("Returning {}, caught {}", HttpStatus.NOT_FOUND, exception.getClass());
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    // 5xx

    @ExceptionHandler(value = { InternalErrorException.class, PerunUnknownException.class,
            IllegalArgumentException.class, NullPointerException.class })
    public ResponseEntity<Object> exception500(Exception exception) {
        log.warn("Returning {}, caught {}", HttpStatus.INTERNAL_SERVER_ERROR, exception.getClass(), exception);
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = { PerunConnectionException.class })
    public ResponseEntity<Object> exception503(Exception exception) {
        log.warn("Returning {}, caught {}", HttpStatus.SERVICE_UNAVAILABLE, exception.getClass(), exception);
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
    }

}
