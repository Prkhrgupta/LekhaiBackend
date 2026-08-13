package in.lekhai.voucher.entity;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.time.LocalDate;

@Table("voucher")
public class Voucher {
    @Id
    private Long id;
    private VoucherType voucherType;
    private Long voucherNumber;
    private LocalDate voucherDate;
    private String narration;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public Voucher(VoucherType voucherType, Long voucherNumber, LocalDate voucherDate, String narration) {
        this.voucherType = voucherType;
        this.voucherNumber = voucherNumber;
        this.voucherDate = voucherDate;
        this.narration = narration;
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

    public Long getVoucherNumber() {
        return voucherNumber;
    }

    public void setVoucherNumber(Long voucherNumber) {
        this.voucherNumber = voucherNumber;
    }

    public LocalDate getVoucherDate() {
        return voucherDate;
    }

    public void setVoucherDate(LocalDate voucherDate) {
        this.voucherDate = voucherDate;
    }

    public String getNarration() {
        return narration;
    }

    public void setNarration(String narration) {
        this.narration = narration;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
