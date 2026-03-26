package in.lekhai.core.enums;

public enum Roles {
    // Used for developers and maintainers, Eg : Create new screen, change SHOP_OWNER permission
    SUPER_ADMIN,
    // Lekhai Customer (who bought an id), Eg : used for most FE and customers facing APIs
    SHOP_OWNER,
    // For future, If Lekhai Customer wants a higher level of privilege on some features
    ADMIN,
    // For future, for down stream customers ( who purchase from actual shop) to view their invoices list etc.
    USER
    ;
}
