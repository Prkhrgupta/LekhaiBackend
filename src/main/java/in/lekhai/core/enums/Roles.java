package in.lekhai.core.enums;

public enum Roles {
    SUPER_ADMIN("SAD"),
    ADMIN("ADM"),
    USER("USR");

    private final String acronym;

    Roles(String acronym) {
        this.acronym = acronym;
    }

    public String getAcronym() {
        return this.acronym;
    }
}
