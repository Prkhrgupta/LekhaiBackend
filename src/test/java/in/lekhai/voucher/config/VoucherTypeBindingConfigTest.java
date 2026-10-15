package in.lekhai.voucher.config;

import java.time.LocalDate;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.lekhai.contract.model.ReceiptVoucherRequest;
import in.lekhai.contract.model.VoucherType;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import static org.assertj.core.api.Assertions.assertThat;

class VoucherTypeBindingConfigTest {

    private ObjectMapper springMapper() {
        Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.json();
        new VoucherTypeBindingConfig().voucherTypeJacksonCustomizer().customize(builder);
        return builder.build();
    }

    @Test
    void receiptVoucherDate_deserializesViaSpringMapper() throws Exception {
        ObjectMapper mapper = springMapper();
        String json = """
                {"voucher_date":"2026-10-13","receipt_account_id":11,
                 "items":[{"account_id":21,"amount":250}]}""";

        ReceiptVoucherRequest request = mapper.readValue(json, ReceiptVoucherRequest.class);

        assertThat(request.getVoucherDate()).isEqualTo(LocalDate.of(2026, 10, 13));
    }

    @Test
    void voucherType_lenientLowercaseStillWorks() throws Exception {
        ObjectMapper mapper = springMapper();

        assertThat(mapper.readValue("\"receipt\"", VoucherType.class)).isEqualTo(VoucherType.RECEIPT);
    }

    @Test
    void localDate_serializesAsIsoString() throws Exception {
        ObjectMapper mapper = springMapper();

        assertThat(mapper.writeValueAsString(LocalDate.of(2026, 10, 13)))
                .isEqualTo("\"2026-10-13\"");
    }
}
