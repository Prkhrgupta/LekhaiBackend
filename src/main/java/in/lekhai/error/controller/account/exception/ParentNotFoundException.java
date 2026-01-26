package in.lekhai.error.controller.account.exception;

import in.lekhai.error.controller.LekhaiException;

public class ParentNotFoundException extends LekhaiException {
    public ParentNotFoundException(Long parentId) {
        super(String.format("Parent id not found %s", parentId));
    }
}
