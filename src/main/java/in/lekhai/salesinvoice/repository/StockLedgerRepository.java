package in.lekhai.salesinvoice.repository;

import in.lekhai.salesinvoice.entity.StockLedger;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface StockLedgerRepository extends ListCrudRepository<StockLedger, Long> {

    @Query("SELECT COALESCE(SUM(in_qty), 0) FROM stock_ledger WHERE stock_item_id = :stockItemId")
    BigDecimal sumInQty(@Param("stockItemId") Long stockItemId);

    @Query("SELECT COALESCE(SUM(out_qty), 0) FROM stock_ledger WHERE stock_item_id = :stockItemId")
    BigDecimal sumOutQty(@Param("stockItemId") Long stockItemId);
}
