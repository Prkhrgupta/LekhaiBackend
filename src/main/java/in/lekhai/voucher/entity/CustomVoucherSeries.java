package in.lekhai.voucher.entity;

import in.lekhai.common.domain.ShopAwareEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("custom_voucher_series")
public class CustomVoucherSeries extends ShopAwareEntity {

    @Id
    private Long id;
    private VoucherType voucherType;
    private String seriesCode;
    private String financialYear;
    private String prefix;
    private String suffix;
    private Boolean allowManualEntry = Boolean.FALSE;
    private String printMetadata;
    private Long nextNumber = 1L;

    public CustomVoucherSeries() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public VoucherType getVoucherType() {
        return voucherType;
    }

    public void setVoucherType(VoucherType voucherType) {
        this.voucherType = voucherType;
    }

    public String getSeriesCode() {
        return seriesCode;
    }

    public void setSeriesCode(String seriesCode) {
        this.seriesCode = seriesCode;
    }

    public String getFinancialYear() {
        return financialYear;
    }

    public void setFinancialYear(String financialYear) {
        this.financialYear = financialYear;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public Boolean getAllowManualEntry() {
        return allowManualEntry;
    }

    public void setAllowManualEntry(Boolean allowManualEntry) {
        this.allowManualEntry = allowManualEntry;
    }

    public String getPrintMetadata() {
        return printMetadata;
    }

    public void setPrintMetadata(String printMetadata) {
        this.printMetadata = printMetadata;
    }

    public Long getNextNumber() {
        return nextNumber;
    }

    public void setNextNumber(Long nextNumber) {
        this.nextNumber = nextNumber;
    }
}
