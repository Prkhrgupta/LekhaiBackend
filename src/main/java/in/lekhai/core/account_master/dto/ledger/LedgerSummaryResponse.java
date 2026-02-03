package in.lekhai.core.account_master.dto.ledger;

import java.util.List;

public record LedgerSummaryResponse(
                List<Column> columns,
                List<Item> data) {
        public record Column(String name, String type, int width) {
        }

        public record Item(
                        Long id,
                        String name,
                        String state,
                        String area,
                        String accountGroup,
                        String gstin) {
        }
}
