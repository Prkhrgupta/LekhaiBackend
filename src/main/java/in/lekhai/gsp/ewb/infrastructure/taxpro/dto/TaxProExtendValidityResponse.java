package in.lekhai.gsp.ewb.infrastructure.taxpro.dto;

public record TaxProExtendValidityResponse(
        String ewayBillNo,
        String updatedDate,
        String validUpto
) {}