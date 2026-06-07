package in.lekhai.core.account_master.repository;

import in.lekhai.contract.model.LedgerSummaryItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LedgerSummaryRepositoryTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @InjectMocks
    private LedgerSummaryRepository ledgerSummaryRepository;

    @Test
    void testCountSummaries_globalSearch() {
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> paramCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);

        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(42L);

        long count = ledgerSummaryRepository.countSummaries("foo", null);

        assertEquals(42L, count);
        verify(jdbcTemplate).queryForObject(sqlCaptor.capture(), paramCaptor.capture(), eq(Long.class));

        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains("SELECT COUNT(*)"));
        assertTrue(sql.contains("FROM ledger l"));
        assertTrue(sql.contains("WHERE ("));
        assertTrue(sql.contains("LOWER(COALESCE(l.name, '')) LIKE :searchPattern"));
        assertTrue(sql.contains("LOWER(COALESCE(s.state_name, '')) LIKE :searchPattern"));
        assertTrue(sql.contains("LOWER(COALESCE(a.area_name, '')) LIKE :searchPattern"));
        assertTrue(sql.contains("LOWER(COALESCE(ag.name, '')) LIKE :searchPattern"));
        assertTrue(sql.contains("LOWER(COALESCE(gd.gstin_or_uin, '')) LIKE :searchPattern"));

        MapSqlParameterSource params = paramCaptor.getValue();
        assertEquals("%foo%", params.getValue("searchPattern"));
    }

    @Test
    void testCountSummaries_singleFieldSearch() {
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> paramCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);

        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(10L);

        long count = ledgerSummaryRepository.countSummaries("Bar", "state");

        assertEquals(10L, count);
        verify(jdbcTemplate).queryForObject(sqlCaptor.capture(), paramCaptor.capture(), eq(Long.class));

        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains("SELECT COUNT(*)"));
        assertTrue(sql.contains("WHERE LOWER(COALESCE(s.state_name, '')) LIKE :searchPattern"));

        MapSqlParameterSource params = paramCaptor.getValue();
        assertEquals("%bar%", params.getValue("searchPattern"));
    }

    @Test
    void testFindSummaries_sortingAndPagination() {
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> paramCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);

        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.of(new LedgerSummaryItem().id(1L).name("Test")));

        List<LedgerSummaryItem> results = ledgerSummaryRepository.findSummaries(
                "abc", "gstin", "gstin", "desc", 20, 10
        );

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Test", results.get(0).getName());

        verify(jdbcTemplate).query(sqlCaptor.capture(), paramCaptor.capture(), any(RowMapper.class));

        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains("SELECT"));
        assertTrue(sql.contains("l.id"));
        assertTrue(sql.contains("ORDER BY COALESCE(gd.gstin_or_uin, '') DESC"));
        assertTrue(sql.contains("LIMIT :limit OFFSET :offset"));

        MapSqlParameterSource params = paramCaptor.getValue();
        assertEquals("%abc%", params.getValue("searchPattern"));
        assertEquals(10, params.getValue("limit"));
        assertEquals(20, params.getValue("offset"));
    }
}
