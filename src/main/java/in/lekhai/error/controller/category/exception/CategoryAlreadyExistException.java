package in.lekhai.error.controller.category.exception;

import in.lekhai.error.controller.LekhaiException;

public class CategoryAlreadyExistException extends LekhaiException {
    public CategoryAlreadyExistException(String category) {
        super(String.format("[%s] category already exist", category));
    }
}
