package in.lekhai.cucumber.steps;

import in.lekhai.cucumber.context.TestContext;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoucherSteps {

    @Autowired
    private TestContext testContext;

    @When("a payment voucher is posted on {string} from payment account {string} to ledger {string} with amount {double} and narration {string}")
    public void aPaymentVoucherIsPosted(
            String dateStr,
            String paymentAccountName,
            String targetLedgerName,
            double amount,
            String narration) {
        String token = testContext.get("shopScopedToken");
        Long paymentAccountId = resolveLedgerId(paymentAccountName);
        Long targetLedgerId = resolveLedgerId(targetLedgerName);

        Map<String, Object> item = new HashMap<>();
        item.put("account_id", targetLedgerId);
        item.put("amount", amount);
        item.put("remarks", narration);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("voucher_date", dateStr);
        requestBody.put("payment_account_id", paymentAccountId);
        requestBody.put("narration", narration);
        requestBody.put("items", List.of(item));

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/payment-voucher");

        testContext.setResponse(response);
    }

    @When("a receipt voucher is posted on {string} into receipt account {string} from ledger {string} with amount {double} and narration {string}")
    public void aReceiptVoucherIsPosted(
            String dateStr,
            String receiptAccountName,
            String targetLedgerName,
            double amount,
            String narration) {
        String token = testContext.get("shopScopedToken");
        Long receiptAccountId = resolveLedgerId(receiptAccountName);
        Long targetLedgerId = resolveLedgerId(targetLedgerName);

        Map<String, Object> item = new HashMap<>();
        item.put("account_id", targetLedgerId);
        item.put("amount", amount);
        item.put("remarks", narration);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("voucher_date", dateStr);
        requestBody.put("receipt_account_id", receiptAccountId);
        requestBody.put("narration", narration);
        requestBody.put("items", List.of(item));

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/receipt-voucher");

        testContext.setResponse(response);
    }

    @When("a contra voucher is posted on {string} debiting {string} and crediting {string} with amount {double} and narration {string}")
    public void aContraVoucherIsPosted(
            String dateStr,
            String debitAccountName,
            String creditAccountName,
            double amount,
            String narration) {
        String token = testContext.get("shopScopedToken");
        Long debitId = resolveLedgerId(debitAccountName);
        Long creditId = resolveLedgerId(creditAccountName);

        Map<String, Object> debitItem = new HashMap<>();
        debitItem.put("account_id", debitId);
        debitItem.put("amount", amount);
        debitItem.put("remarks", narration);

        Map<String, Object> creditItem = new HashMap<>();
        creditItem.put("account_id", creditId);
        creditItem.put("amount", amount);
        creditItem.put("remarks", narration);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("voucher_date", dateStr);
        requestBody.put("narration", narration);
        requestBody.put("debit_entries", List.of(debitItem));
        requestBody.put("credit_entries", List.of(creditItem));

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/contra-voucher");

        testContext.setResponse(response);
    }

    @When("a journal voucher is posted on {string} debiting {string} and crediting {string} with amount {double} and narration {string}")
    public void aJournalVoucherIsPosted(
            String dateStr,
            String debitAccountName,
            String creditAccountName,
            double amount,
            String narration) {
        String token = testContext.get("shopScopedToken");
        Long debitId = resolveLedgerId(debitAccountName);
        Long creditId = resolveLedgerId(creditAccountName);

        Map<String, Object> debitItem = new HashMap<>();
        debitItem.put("account_id", debitId);
        debitItem.put("amount", amount);
        debitItem.put("remarks", narration);

        Map<String, Object> creditItem = new HashMap<>();
        creditItem.put("account_id", creditId);
        creditItem.put("amount", amount);
        creditItem.put("remarks", narration);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("voucher_date", dateStr);
        requestBody.put("narration", narration);
        requestBody.put("debit_entries", List.of(debitItem));
        requestBody.put("credit_entries", List.of(creditItem));

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/journal-voucher");

        testContext.setResponse(response);
    }

    @When("an unbalanced journal voucher is posted on {string} debiting {string} for {double} and crediting {string} for {double}")
    public void anUnbalancedJournalVoucherIsPosted(
            String dateStr,
            String debitAccountName,
            double debitAmount,
            String creditAccountName,
            double creditAmount) {
        String token = testContext.get("shopScopedToken");
        Long debitId = resolveLedgerId(debitAccountName);
        Long creditId = resolveLedgerId(creditAccountName);

        Map<String, Object> debitItem = new HashMap<>();
        debitItem.put("account_id", debitId);
        debitItem.put("amount", debitAmount);

        Map<String, Object> creditItem = new HashMap<>();
        creditItem.put("account_id", creditId);
        creditItem.put("amount", creditAmount);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("voucher_date", dateStr);
        requestBody.put("narration", "Unbalanced test");
        requestBody.put("debit_entries", List.of(debitItem));
        requestBody.put("credit_entries", List.of(creditItem));

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/journal-voucher");

        testContext.setResponse(response);
    }

    @When("an account ledger statement is requested for {string} from {string} to {string}")
    public void anAccountLedgerStatementIsRequested(String ledgerName, String fromDate, String toDate) {
        String token = testContext.get("shopScopedToken");
        Long ledgerId = resolveLedgerId(ledgerName);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .queryParam("fromDate", fromDate)
                .queryParam("toDate", toDate)
                .contentType(ContentType.JSON)
                .when()
                .get("/account-ledger/" + ledgerId);

        testContext.setResponse(response);
    }

    private Long resolveLedgerId(String nameOrId) {
        Long id = testContext.get("ledgerId:" + nameOrId);
        if (id != null) return id;
        try {
            return Long.parseLong(nameOrId);
        } catch (NumberFormatException e) {
            Long lastId = testContext.get("lastCreatedLedgerId");
            if (lastId != null) return lastId;
            throw new IllegalArgumentException("Cannot resolve Ledger ID for: " + nameOrId);
        }
    }
}
