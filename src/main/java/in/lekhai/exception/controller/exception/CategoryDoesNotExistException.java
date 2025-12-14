package in.lekhai.exception.controller.exception;

public class CategoryDoesNotExistException extends RuntimeException {
    public CategoryDoesNotExistException(String category) {
        super(String.format("%s category does not exits", category));
    }
}
