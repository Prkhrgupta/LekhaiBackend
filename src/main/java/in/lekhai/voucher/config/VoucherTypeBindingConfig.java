package in.lekhai.voucher.config;

import java.io.IOException;

import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

import in.lekhai.contract.model.VoucherType;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Lenient binding for {@link VoucherType}: trims and uppercases incoming values
 * so legacy callers sending {@code "sales"} keep working after the contract
 * moved {@code voucher_type} from plain string to enum (exact-match by default).
 */
@Configuration
public class VoucherTypeBindingConfig {

    @Bean
    Converter<String, VoucherType> voucherTypeQueryParamConverter() {
        return new VoucherTypeQueryParamConverter();
    }

    static class VoucherTypeQueryParamConverter implements Converter<String, VoucherType> {
        @Override
        public VoucherType convert(String source) {
            return parseLenient(source);
        }
    }

    @Bean
    Jackson2ObjectMapperBuilderCustomizer voucherTypeJacksonCustomizer() {
        return builder -> {
            // why: builder.modules() replaces Boot's auto-registered modules (drops JavaTimeModule
            // and breaks LocalDate binding); modulesToInstall() appends instead. It also overwrites any earlier
            // modulesToInstall() call, so every module we need (JsonNullable for generated contract models) must
            // be listed here — a separate Module @Bean would be silently dropped.
            builder.modulesToInstall(new JavaTimeModule(), new JsonNullableModule(), new SimpleModule() {
                {
                    addDeserializer(VoucherType.class, new JsonDeserializer<VoucherType>() {
                        @Override
                        public VoucherType deserialize(JsonParser p, DeserializationContext ctxt)
                                throws IOException {
                            VoucherType parsed = parseLenient(p.getValueAsString());
                            if (parsed == null) {
                                return null;
                            }
                            return parsed;
                        }
                    });
                }
            });
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        };
    }

    static VoucherType parseLenient(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return VoucherType.fromValue(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid voucher_type: " + raw);
        }
    }
}
