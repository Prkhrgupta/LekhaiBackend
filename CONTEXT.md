# Lekhai Context

Shared business language for the Lekhai B2B accounting product. All module docs reference these terms instead of redefining them.

## Language

**Voucher**:
A balanced double-entry record of money moving into, out of, or between ledgers.
_Avoid_: transaction, entry, sale_file

**Voucher Type**:
The accounting effect of a **Voucher**: Payment, Receipt, Contra, Journal, Sales, Purchase, Credit Note, Debit Note.
_Avoid_: invoice_type, voucher category

**Sale Series**:
A numbered bill series within one **Voucher Type**, e.g. GST bills or Retail bills under Sales.
_Avoid_: invoice_type, bill type

**Sale Invoice**:
The commercial bill given to a buyer for goods sold, with totals, tax and transport details.
_Avoid_: sale_file, bill, sale voucher

**Sale Line**:
One stock item sold on a **Sale Invoice**, with quantity, rate and amount.
_Avoid_: stock_file, stock entry

**Tax Breakup**:
The per-line split of GST into CGST, SGST, IGST and cess amounts.
_Avoid_: tax columns, gst fields

**Tax Snapshot**:
The frozen rate, amount and ledger copied onto a posted bill so later master edits never rewrite history.
_Avoid_: live tax lookup

**Bill Number**:
The legal printed number of a **Sale Invoice** within its **Sale Series**, reset each financial year.
_Avoid_: vno, bill string, voucher number

**Voucher Number**:
The internal continuous sequence of a **Voucher** within its **Voucher Type**, never reset.
_Avoid_: bill number, vno

**Party**:
The customer ledger a **Sale Invoice** is billed to.
_Avoid_: account, buyer code

**Broker**:
A commission agent attached to a sale for brokerage tracking.
_Avoid_: agent, brok

**E-Invoice**:
The GSTN-registered identity of a **Sale Invoice**: IRN, acknowledgement number and date.
_Avoid_: irn fields, ack fields

**E-Way Bill**:
The government permit to move goods, linked to a **Sale Invoice** with transporter, vehicle and GR details.
_Avoid_: ewb fields, grno, tpn
