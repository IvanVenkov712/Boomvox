package bg.fmi.uni.boomvox.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String entityName, Long id) {
        super(entityName + " not found with id: " + id);
    }
}
