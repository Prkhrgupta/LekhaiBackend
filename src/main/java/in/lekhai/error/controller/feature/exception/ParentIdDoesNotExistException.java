package in.lekhai.error.controller.feature.exception;

import in.lekhai.error.controller.LekhaiException;

public class ParentIdDoesNotExistException extends LekhaiException {
    public ParentIdDoesNotExistException(Long parentId) {
        super(String.format("[%s] parent id does not exist", parentId));
    }
}
