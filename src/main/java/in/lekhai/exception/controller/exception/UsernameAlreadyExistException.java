package in.lekhai.exception.controller.exception;

public class UsernameAlreadyExistException extends RuntimeException {
  public UsernameAlreadyExistException(String username) {
    super(String.format("%s already exists", username));
  }
}
