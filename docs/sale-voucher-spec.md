# Sale voucher tables to support Sale Invoice

## Problem Statement

Shops need to record sales as GST-compliant bills with correct bill numbers, tax splits, and stock effects, but today only Payment, Receipt, Contra, and Journal postings exist, so sales cannot be recorded without breaking the books or GST filings.

## Solution

A sale creation flow that posts one balanced Voucher of Voucher Type Sales while issuing one Sale Invoice in the chosen Sale Series with its own yearly Bill Number, freezing per-line tax as a Tax Snapshot and tracking E-Invoice and E-Way Bill identity separately so later master edits never rewrite history.

## User Stories

1. As a shop owner, I want to create a GST Sale Invoice for a Party, so that the buyer gets a legal bill.
2. As a shop owner, I want Retail and GST bills numbered in separate Sale Series, so that each series stays GST-compliant.
3. As a shop owner, I want the Bill Number to restart each financial year while the Voucher Number stays continuous, so that audits and GST filings both pass.
4. As an accountant, I want intra-state bills to split into CGST plus SGST and inter-state into IGST automatically, so that I do not pick tax ledgers manually.
5. As an accountant, I want multi-rate bills (for example 5 percent fabric plus 12 percent yarn) to total correctly, so that mixed carts work.
6. As an accountant, I want single-rate bills to work the same way as multi-rate ones, so that I do not learn two flows.
7. As an accountant, I want the Tax Snapshot frozen at posting, so that later GST-rate or ledger remaps do not rewrite filed bills.
8. As a shop owner, I want to attach a Broker to a sale, so that commission can be tracked later.
9. As a shop owner, I want to record transporter, vehicle, and GR details on a sale, so that goods movement is traceable.
10. As a shop owner, I want E-Invoice identity (IRN and acknowledgement) linked to the bill, so that GSTN registration is provable.
11. As a shop owner, I want an E-Way Bill linked to the bill with retry support, so that transport delays do not block billing.
12. As a shop owner, I want cash sales tied to a cash or bank ledger, so that counter sales balance.
13. As a shop owner, I want credit sales to debit the Party balance by the net amount, so that receivables stay correct.
14. As a godown keeper, I want each Sale Line to record pieces, meters, rate-per, rate, and discount, so that stock issues are auditable.
15. As a godown keeper, I want a warning (not a block) on negative stock, so that urgent sales are not stopped.
16. As an accountant, I want freight, round-off, cess, TDS, and TCS resolved automatically at posting, so that net matches the printed bill.
17. As an auditor, I want header totals to always equal the sum of lines and Tax Breakup splits, so that books stay balanced.
18. As an auditor, I want every sale traceable to its balanced double-entry legs, so that debits always equal credits.
19. As a shop owner with multiple branches, I want branch-wise series defaults (bank, sale ledger), so that billing auto-fills.
20. As a GST filer, I want Credit Note and Debit Note bills in their own numbered series, so that returns and corrections file correctly.

## Implementation Decisions

- One sale series master owns legal numbering: series code, Voucher Type binding (GST to Sales, CN to Credit Note, DN to Debit Note), prefix, next bill number, current financial year, yearly-reset flag, branch number, and default bank and sale ledgers. Internal voucher numbering stays in the existing per-type counter.
- Sale persistence is four new records plus reuse of two existing ones: series advance, voucher header plus balanced entry legs, invoice header (commercial plus tax totals snapshot), invoice lines (quantity, rate, amount plus GST-rate snapshot), per-line tax splits (type, rate, amount, tax ledger), and a separate GST filing record for E-Invoice and E-Way Bill lifecycle.
- All commercial references are nullable foreign keys to masters (customer ledger, broker, transporter, cash ledger, stock item); legacy zero codes migrate to null.
- Tax ledgers resolve at posting from sale-ledger settings (CGST, SGST, IGST) and general settings (cess, freight, round-off), with shop-state versus party-state comparison deciding CGST plus SGST versus IGST; resolved rate, amount, and ledger are snapshotted.
- Filing lifecycle is decoupled from posting: the filing record starts as pending; external GSTN and E-Way Bill calls fill it later and must not hold the posting transaction open.
- Yearly reset rule uses the April to March financial year (same rule as voucher date validation); series uniqueness is scoped to shop, series, financial year, and bill number.
- The API surface follows the existing intake pattern (validate, build posting request, post, respond) with a new sale path; request and response shapes come from the generated contract jar, never hand-written.

## Testing Decisions

- Good tests assert external behavior only: bill and voucher numbers issued, legs balance, snapshots frozen, filing pending — not internal SQL or mapper calls.
- Test at a single intake seam: creating a Sale Invoice through the voucher intake (validate, build posting request, post, respond). Avoid per-repository unit tests unless the seam proves too coarse.
- Prior art: existing intake and posting tests for payment, receipt, contra, and journal balance checks, counter atomicity tests, and container-backed persistence tests for ledger postings.

## Out of Scope

- Purchase vouchers, sales returns flow beyond a separate Credit Note series, linkage of credit notes to original bills, stock valuation method changes, a separate on-hand stock ledger table, async GSP and E-Way Bill gateway integration, bulk reorder of series, and per-shop feature ordering.

## Further Notes

- Respects the dual-numbering ADR and the project glossary; legacy names (invoice type, sale file, stock files) are retired in favor of Sale Series, Sale Invoice, and Sale Line.
- Gotchas carried forward: dual-number atomicity and gap handling, totals invariant enforced in service, legacy file migration mapping, and inter-state detection source of truth.
