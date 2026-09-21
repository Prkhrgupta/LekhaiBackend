package in.lekhai.core.account_master.domain;

import in.lekhai.common.domain.ShopAwareEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

/**
 * Shop-level ledger defaults shared by sale and purchase flows: freight,
 * round-off, TDS, TCS and the output/input cess ledgers (rates come from
 * the commodity). One active row per shop.
 */
@Table("general_ledger_setting_master")
public class GeneralLedgerSetting extends ShopAwareEntity {

    @Id
    private Long id;

    private Long freightPackingLedgerId;
    private Long roundOffLedgerId;
    private BigDecimal tdsPercentage;
    private Long tdsLedgerId;
    private BigDecimal tcsPercentage;
    private Long tcsLedgerId;
    private Long outputCessLedgerId;
    private Long inputCessLedgerId;

    private Boolean isDeleted = Boolean.FALSE;

    public GeneralLedgerSetting() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFreightPackingLedgerId() {
        return freightPackingLedgerId;
    }

    public void setFreightPackingLedgerId(Long freightPackingLedgerId) {
        this.freightPackingLedgerId = freightPackingLedgerId;
    }

    public Long getRoundOffLedgerId() {
        return roundOffLedgerId;
    }

    public void setRoundOffLedgerId(Long roundOffLedgerId) {
        this.roundOffLedgerId = roundOffLedgerId;
    }

    public BigDecimal getTdsPercentage() {
        return tdsPercentage;
    }

    public void setTdsPercentage(BigDecimal tdsPercentage) {
        this.tdsPercentage = tdsPercentage;
    }

    public Long getTdsLedgerId() {
        return tdsLedgerId;
    }

    public void setTdsLedgerId(Long tdsLedgerId) {
        this.tdsLedgerId = tdsLedgerId;
    }

    public BigDecimal getTcsPercentage() {
        return tcsPercentage;
    }

    public void setTcsPercentage(BigDecimal tcsPercentage) {
        this.tcsPercentage = tcsPercentage;
    }

    public Long getTcsLedgerId() {
        return tcsLedgerId;
    }

    public void setTcsLedgerId(Long tcsLedgerId) {
        this.tcsLedgerId = tcsLedgerId;
    }

    public Long getOutputCessLedgerId() {
        return outputCessLedgerId;
    }

    public void setOutputCessLedgerId(Long outputCessLedgerId) {
        this.outputCessLedgerId = outputCessLedgerId;
    }

    public Long getInputCessLedgerId() {
        return inputCessLedgerId;
    }

    public void setInputCessLedgerId(Long inputCessLedgerId) {
        this.inputCessLedgerId = inputCessLedgerId;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }
}
