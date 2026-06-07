package in.lekhai.core.account_master.repository;

import in.lekhai.contract.model.LedgerSummaryItem;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Repository for fetching paginated, searchable, and sortable ledger summaries.
 * Uses NamedParameterJdbcTemplate with a JOIN query across ledger, ledger_address,
 * state, area, account_group, and gst_details tables.
 */
@Repository
public class LedgerSummaryRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    // Columns that are valid for search_field parameter
    private static final Set<String> SEARCHABLE_FIELDS = Set.of(
            "name", "state", "area", "accountGroup", "gstin"
    );

    // Maps API sort_by values to SQL column expressions
    private static final Map<String, String> SORT_COLUMN_MAP = Map.of(
            "id", "l.id",
            "name", "l.name",
            "state", "COALESCE(s.state_name, '')",
            "area", "COALESCE(a.area_name, '')",
            "accountGroup", "COALESCE(ag.name, '')",
            "gstin", "COALESCE(gd.gstin_or_uin, '')"
    );

    // Maps API search_field values to SQL column expressions for ILIKE
    private static final Map<String, String> SEARCH_COLUMN_MAP = Map.of(
            "name", "l.name",
            "state", "s.state_name",
            "area", "a.area_name",
            "accountGroup", "ag.name",
            "gstin", "gd.gstin_or_uin"
    );

    private static final String BASE_FROM_CLAUSE = """
            FROM ledger l
            LEFT JOIN ledger_address la ON la.ledger_id = l.id
            LEFT JOIN state s ON s.state_code = la.state_id
            LEFT JOIN area a ON a.id = l.default_area_id
            LEFT JOIN account_group ag ON ag.id = l.account_group_id
            LEFT JOIN gst_details gd ON gd.ledger_id = l.id
            """;

    public LedgerSummaryRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Counts total items matching the given search criteria.
     */
    public long countSummaries(String search, String searchField) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        String whereClause = buildWhereClause(search, searchField, params);

        String sql = "SELECT COUNT(*) " + BASE_FROM_CLAUSE + whereClause;

        Long count = jdbcTemplate.queryForObject(sql, params, Long.class);
        return count != null ? count : 0;
    }

    /**
     * Fetches a page of ledger summary items with search and sort applied.
     */
    public List<LedgerSummaryItem> findSummaries(
            String search,
            String searchField,
            String sortBy,
            String sortOrder,
            int offset,
            int limit
    ) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        String whereClause = buildWhereClause(search, searchField, params);
        String orderClause = buildOrderClause(sortBy, sortOrder);

        String sql = """
                SELECT
                    l.id,
                    l.name,
                    COALESCE(s.state_name, '') AS state,
                    COALESCE(a.area_name, '') AS area,
                    COALESCE(ag.name, '') AS account_group,
                    COALESCE(gd.gstin_or_uin, '') AS gstin
                """
                + BASE_FROM_CLAUSE
                + whereClause
                + orderClause
                + " LIMIT :limit OFFSET :offset";

        params.addValue("limit", limit);
        params.addValue("offset", offset);

        return jdbcTemplate.query(sql, params, (rs, rowNum) ->
                new LedgerSummaryItem()
                        .id(rs.getLong("id"))
                        .name(rs.getString("name"))
                        .state(rs.getString("state"))
                        .area(rs.getString("area"))
                        .accountGroup(rs.getString("account_group"))
                        .gstin(rs.getString("gstin"))
        );
    }

    /**
     * Builds the WHERE clause for search filtering.
     * Supports both global search (across all searchable columns) and
     * single-column search (when searchField is specified).
     */
    private String buildWhereClause(String search, String searchField, MapSqlParameterSource params) {
        if (search == null || search.isBlank()) {
            return "";
        }

        String searchPattern = "%" + search.toLowerCase() + "%";
        params.addValue("searchPattern", searchPattern);

        if (searchField != null && !searchField.isBlank() && SEARCHABLE_FIELDS.contains(searchField)) {
            // Single-column search
            String column = SEARCH_COLUMN_MAP.get(searchField);
            return " WHERE LOWER(COALESCE(" + column + ", '')) LIKE :searchPattern ";
        }

        // Global search across all searchable columns (OR)
        List<String> conditions = new ArrayList<>();
        for (String column : SEARCH_COLUMN_MAP.values()) {
            conditions.add("LOWER(COALESCE(" + column + ", '')) LIKE :searchPattern");
        }
        return " WHERE (" + String.join(" OR ", conditions) + ") ";
    }

    /**
     * Builds the ORDER BY clause from validated sort parameters.
     * Falls back to "l.name ASC" if parameters are invalid.
     */
    private String buildOrderClause(String sortBy, String sortOrder) {
        String column = SORT_COLUMN_MAP.getOrDefault(sortBy, "l.name");
        String direction = "desc".equalsIgnoreCase(sortOrder) ? "DESC" : "ASC";
        return " ORDER BY " + column + " " + direction + " ";
    }
}
