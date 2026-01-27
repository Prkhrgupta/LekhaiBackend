package in.lekhai.core.inventory_master.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record CommodityRequest(
        @JsonProperty("item_name") @NotBlank(message = "item name is required") String itemName,
        @JsonProperty("hsn_sac_code") String hsnSacCode,
        String description,
        String uom,
        @JsonProperty("gst_rate_sale") BigDecimal gstRateSale,
        @JsonProperty("gst_rate_purchase") BigDecimal gstRatePurchase,
        @JsonProperty("is_sale_purchase_active") Boolean isSalePurchaseActive,

        @JsonProperty("sales_ledger_config") SalesLedgerConfigDto salesLedgerConfig,
        @JsonProperty("purchase_ledger_config") PurchaseLedgerConfigDto purchaseLedgerConfig) {
    public record SalesLedgerConfigDto(
            @JsonProperty("in_state_account_id") Long inStateAccountId,
            @JsonProperty("cgst_percent") BigDecimal cgstPercent,
            @JsonProperty("sgst_percent") BigDecimal sgstPercent,
            @JsonProperty("cess_percent") BigDecimal cessPercent,
            @JsonProperty("round_off_account_id") Long roundOffAccountId,
            @JsonProperty("out_state_account_id") Long outStateAccountId,
            @JsonProperty("igst_percent") BigDecimal igstPercent,
            @JsonProperty("out_cess_percent") BigDecimal outCessPercent) {
    }

    public record PurchaseLedgerConfigDto(
            @JsonProperty("in_state_account_id") Long inStateAccountId,
            @JsonProperty("cgst_percent") BigDecimal cgstPercent,
            @JsonProperty("sgst_percent") BigDecimal sgstPercent,
            @JsonProperty("cess_percent") BigDecimal cessPercent,
            @JsonProperty("out_state_account_id") Long outStateAccountId,
            @JsonProperty("igst_percent") BigDecimal igstPercent,
            @JsonProperty("out_cess_percent") BigDecimal outCessPercent) {
    }
}
