package in.lekhai.gsp.ewb.infrastructure.taxpro.exceptions;

public class TaxProUnauthorizedException extends RuntimeException{
    public TaxProUnauthorizedException(String message) {
        super(message);
    }
}
