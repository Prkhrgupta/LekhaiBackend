package in.lekhai.error.controller.user.exception;

import in.lekhai.error.controller.LekhaiException;

public class UserAlreadyExistException extends LekhaiException {
  public UserAlreadyExistException(String username) {
    super(String.format("[%s] already exist", username));
  }
}
