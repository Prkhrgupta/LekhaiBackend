package in.lekhai.voucher.util;

import java.util.EnumMap;
import java.util.Map;

import in.lekhai.contract.model.VoucherType;

/**
 * Legacy per-type display prefixes (e.g. {@code PY-7}, {@code RC-3}).
 *
 * <p>Source of truth for voucher <em>names</em> is the API contract
 * ({@code openapi/voucher/components.yaml#/VoucherType}); this map only owns the
 * legacy-counter display strings, which are deliberately not part of the contract.
 *
 * <p><b>When adding a new voucher type:</b> add the value in the spec, regen the
 * contract, then add its prefix entry here (display numbering breaks without it).
 */
public final class VoucherTypePrefixes {

    private static final Map<VoucherType, String> PREFIXES = new EnumMap<>(VoucherType.class);

    static {
        PREFIXES.put(VoucherType.PAYMENT, "PY");
        PREFIXES.put(VoucherType.RECEIPT, "RC");
        PREFIXES.put(VoucherType.CONTRA, "CNT");
        PREFIXES.put(VoucherType.JOURNAL, "JN");
        PREFIXES.put(VoucherType.PURCHASE, "PR");
        PREFIXES.put(VoucherType.SALES, "SV");
        PREFIXES.put(VoucherType.CREDIT_NOTE, "CN");
        PREFIXES.put(VoucherType.DEBIT_NOTE, "DN");
    }

    private VoucherTypePrefixes() {
    }

    public static String get(VoucherType type) {
        String prefix = PREFIXES.get(type);
        if (prefix == null) {
            throw new IllegalStateException("Missing legacy prefix for voucher type: " + type);
        }
        return prefix;
    }
}
