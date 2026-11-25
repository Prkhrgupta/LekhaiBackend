package in.lekhai.accountbooks.journalbook.controller;

import in.lekhai.accountbooks.journalbook.service.JournalBookService;
import in.lekhai.contract.api.JournalBookApi;
import in.lekhai.contract.model.JournalBookResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
public class JournalBookController implements JournalBookApi {

    private final JournalBookService journalBookService;

    public JournalBookController(JournalBookService journalBookService) {
        this.journalBookService = journalBookService;
    }

    @Override
    public ResponseEntity<JournalBookResponse> getJournalBook(LocalDate fromDate, LocalDate toDate) {
        JournalBookResponse response = journalBookService.getJournalBook(fromDate, toDate);
        return ResponseEntity.ok(response);
    }
}
