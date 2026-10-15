package in.lekhai.voucher.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.lekhai.contract.model.PaymentVoucherRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VoucherRequestDeserializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void paymentRequest_deserializesNumericBillNo() throws Exception {
        String json = """
                {"voucher_date":"2025-06-15","payment_account_id":10,
                 "series_code":"CASH-1","bill_no":2,
                 "items":[{"account_id":20,"amount":500}]}""";

        PaymentVoucherRequest request = objectMapper.readValue(json, PaymentVoucherRequest.class);

        assertThat(request.getBillNo()).isEqualTo(2L);
        assertThat(request.getSeriesCode()).isEqualTo("CASH-1");
    }

    @Test
    void paymentRequest_absentBillNoDeserializesAsNull() throws Exception {
        String json = """
                {"voucher_date":"2025-06-15","payment_account_id":10,
                 "items":[{"account_id":20,"amount":500}]}""";

        PaymentVoucherRequest request = objectMapper.readValue(json, PaymentVoucherRequest.class);

        assertThat(request.getBillNo()).isNull();
    }

    @Test
    void paymentRequest_explicitNullBillNoDeserializesAsNull() throws Exception {
        String json = """
                {"voucher_date":"2025-06-15","payment_account_id":10,
                 "bill_no":null,
                 "items":[{"account_id":20,"amount":500}]}""";

        PaymentVoucherRequest request = objectMapper.readValue(json, PaymentVoucherRequest.class);

        assertThat(request.getBillNo()).isNull();
    }
}
