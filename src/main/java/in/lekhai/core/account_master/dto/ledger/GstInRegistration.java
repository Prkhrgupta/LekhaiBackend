package in.lekhai.core.account_master.dto.ledger;

public class GstInRegistration {

    public static enum RegistrationType {
        COMPOSITION, CUSTOMER, REGULAR, UNREGISTERED
    }

    public enum PartyType {
        NOT_APPLICABLE, DEEMED_EXPORT, GOVERNMENT_ENTITY, SEZ
    }
}
