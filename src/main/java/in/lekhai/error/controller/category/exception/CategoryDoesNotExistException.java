package in.lekhai.error.controller.category.exception;

import in.lekhai.error.controller.LekhaiException;

public class CategoryDoesNotExistException extends LekhaiException {
    public CategoryDoesNotExistException(String category) {
        super(String.format("[%s] category does not exist", category));
    }
}
