package in.lekhai.core.account_master.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

/**
 * A TDS section (nature of payment) with its statutory rates and thresholds,
 * e.g. 194C — payment to contractors. System-level like {@link State}: seeded by
 * Flyway, shared by every shop, not RLS-isolated. A {@code null} limit means the
 * section has no threshold of that kind.
 */
@Table("tds_section")
public class TdsSection {

    @Id
    private Long id;
    private String code;
    private String description;
    private BigDecimal rateIndividualHuf;
    private BigDecimal rateOthers;
    private BigDecimal rateNoPan;
    private BigDecimal singleTransactionLimit;
    private BigDecimal monthlyLimit;
    private BigDecimal annualLimit;
    private Boolean isActive;

    public TdsSection() {
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getRateIndividualHuf() {
        return rateIndividualHuf;
    }

    public BigDecimal getRateOthers() {
        return rateOthers;
    }

    public BigDecimal getRateNoPan() {
        return rateNoPan;
    }

    public BigDecimal getSingleTransactionLimit() {
        return singleTransactionLimit;
    }

    public BigDecimal getMonthlyLimit() {
        return monthlyLimit;
    }

    public BigDecimal getAnnualLimit() {
        return annualLimit;
    }

    public Boolean getActive() {
        return isActive;
    }

    /** Label shown to users, e.g. "194Q - Purchase of goods". */
    public String getLabel() {
        return code + " - " + description;
    }
}
