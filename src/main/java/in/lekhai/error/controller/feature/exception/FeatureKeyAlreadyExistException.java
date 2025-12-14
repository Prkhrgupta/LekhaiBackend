package in.lekhai.error.controller.feature.exception;

import in.lekhai.error.controller.LekhaiException;

public class FeatureKeyAlreadyExistException extends LekhaiException {
    public FeatureKeyAlreadyExistException(String featureKey) {
        super(String.format("[%s] feature key already exist", featureKey));
    }
}
