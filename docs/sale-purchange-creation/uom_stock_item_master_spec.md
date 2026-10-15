# UOM & Stock Item Master Spec

Common branch across all three repos: `feature/uom-stock-item-master`.
Companion proposal (backend input): `uom_stock_item_master_specifications.md` in the same folder.
This spec is mirrored under the same filename in `lekhai-apispec` and `lekhaiFrontend` so every repo's agent works from identical context.

Glossary (canon, mirrors `CONTEXT.md` additions on implement): **UOM** is a sellable unit;
**Primary UOM** is the stock and rate unit of a Stock Item; **Alternate UOM** is an optional
packing or display unit; **Conversion Factor** is how many Alternate units sit inside 1 Primary unit (fixed).

## Problem Statement

Shops in different industries sell in different units — pieces, meters, boxes, bags, kilos, litres —
but the Stock Item master only understands pieces and meters. A shop selling boxes of shirts, gunny bags
of grain, or strips of tablets cannot record opening stock, price, or quantity in its real unit, so books
either misstate stock or force users into wrong-unit workarounds that corrupt GST reporting.

## Solution

Replace the hardcoded piece/meter fields on the Stock Item with a generic shop-owned UOM master.
Each shop defines its own unit labels mapped to an official GST quantity-code enum; each Stock Item
points at one Primary UOM (its accounting unit) and optionally one Alternate UOM with a fixed conversion
factor. Rates and values always stay in the Primary UOM; the Alternate quantity is derived at runtime.
A dedicated UOM master screen plus updated Stock Item screens expose this across the product.

## User Stories

1. As a shop owner, I want to create my own units (e.g. Gunny Bag mapped to BAG), so that my items speak my industry's language.
2. As a shop owner, I want the quantity-code picked from the official GST list, so that my e-invoices and e-way bills never get rejected for a bad unit code.
3. As a shop owner, I want to set one Primary UOM per item, so that there is never ambiguity about which unit my rate and stock are in.
4. As a wholesaler, I want an optional Alternate UOM with a fixed factor (1 Box = 10 Pcs), so that I can buy in boxes and reason in pieces.
5. As a retailer with single-unit items, I want to skip the Alternate UOM entirely, so that simple items stay simple.
6. As a shop owner, I want the rate and opening value always interpreted per Primary UOM, so that my stock valuation is never silently wrong.
7. As a shop owner, I want the Alternate quantity auto-derived from the Primary quantity, so that I never maintain two truths.
8. As a shop owner, I want to rename a unit label without touching its GST code, so that wording fixes don't endanger filed returns.
9. As a shop owner, I want deletion of a unit that items still use to be blocked with a clear message, so that I never orphan my catalogue.
10. As a shop accountant, I want a live conversion hint while defining an item (1 BOX = 10 PCS), so that factor entry mistakes are caught immediately.
11. As a shop accountant, I want the Stock Item form to offer my units in a dropdown, so that I never type a free-text unit that GST rejects.
12. As a shop accountant, I want opening stock entered once in the Primary unit, so that migration from pieces/meters is a straight copy.
13. As a GST auditor, I want bill lines to eventually freeze the unit and code at posting time, so that later master edits never rewrite history (tracked as follow-up, see Out of Scope).
14. As a multi-branch shop, I want my units isolated to my shop, so that another firm's labels never leak into my dropdown.
15. As a new shop, I want to start with an empty unit list and add what I need, so that onboarding is not cluttered with irrelevant units.
16. As a developer, I want the quantity-code enum owned by the API contract, so that backend and frontend validate identically.
17. As a developer, I want factor validation (positive when Alternate present, absent otherwise, Alternate distinct from Primary) enforced at every layer, so that no client can persist a zero-factor divide-by-zero bomb.

## Implementation Decisions

- The UOM master is shop-scoped like every other master; there are no global seed rows. Row isolation follows the existing shop-isolation pattern already used by the inventory masters.
- The API contract owns the closed GST quantity-code enum and the UOM create/update payloads plus the revised Stock Item payloads. The old piece/meter enum and fields are removed as an intended breaking contract change under the shared feature branch.
- The backend edits the not-yet-released Stock Item table definition in place (clean-run waiver, confirmed: no production or staging database exists) instead of adding an additive migration: drop the rate-per indicator and both opening-quantity columns, add Primary UOM reference (required), Alternate UOM reference (optional), conversion factor, and a single opening quantity.
- The conversion factor is fixed per item for this release. Variable conversions (a fabric roll whose meters vary per piece, weighed goods) are explicitly out of scope and must not be modelled with fudge factors.
- The factor column is nullable: null means single-unit item; any stored factor must be positive. This is enforced in the database, the domain service, and the contract validation alike.
- Rates and opening values are always denominated in the Primary UOM. The Alternate opening quantity is not stored; it is derived by multiplication at read time.
- Quantity-code validation lives at application and contract level only, not as a database check, so GST list updates ship as contract releases rather than migrations.
- Unit deletion is a soft-delete blocked by usage: any unit referenced by a Stock Item cannot be removed and the backend reports a client error the UI surfaces as a toast. Renaming the display label is allowed; changing the GST code of a used unit is not.
- The UOM master follows the established four-layer master pattern of its module (domain, repository, service, thin controller) and reuses the shared shop-context and error-handling conventions; no new architectural pattern is introduced.
- The frontend ships a standalone UOM master screen (list, create, edit with GST-code dropdown, delete with toast on blocked) and revises the Stock Item form (Primary dropdown required, Alternate plus factor optional, single opening quantity, live conversion hint). Inline quick-add of units from the item form is deferred.
- Contract version bump ships with this change since Stock Item payloads break backward compatibility.

## Testing Decisions

- Test external behavior at the highest seam in each repo, one seam per repo: the OpenAPI contract surface in the spec repo (schema review plus generated-client round trip), the domain service boundary in the backend (create/update/delete rules, validation rejections, delete-blocked-when-referenced, shop isolation), and the screen level in the frontend (master CRUD flows, item form validation, conversion hint, blocked-delete toast).
- Good tests assert observable outcomes (persisted item with correct Primary unit, rejected zero factor, blocked delete message) rather than internal implementation details.
- Prior art: the existing master-service tests for the neighbouring catalogue masters in the backend, and the existing feature-folder widget tests in the frontend, are the patterns to copy. If a repo lacks such a test today, the first test added here establishes the pattern.
- Proposed seams assume the teams agree the service boundary (backend) and screen boundary (frontend) are the right level; flag if a lower seam is preferred before implementing.

## Out of Scope

- UOM snapshots frozen onto sale and purchase lines (unit name, code, and factor copied at posting for GST audit immunity). Noted as a requirement for the upcoming line-table work, not built here.
- Variable or lot-wise conversions, three-level unit chains (box to strip to tablet), and unit-specific decimal precision rules.
- Data migration tooling for released databases (unneeded: clean-run waiver confirmed).
- Inline quick-add of units from the Stock Item form (deferred to a follow-up).
- Any change to tax, ledger, voucher posting, or e-invoice filing flows beyond accepting the new unit references.

## Further Notes

- The original proposal suggested seeding global units on application startup and storing an Alternate opening quantity; both were rejected during review (startup seeding conflicts with migration discipline and shop isolation; stored Alternate quantity invites divergence — derive it instead).
- Once GST notifies a quantity-code list change, the update path is a contract enum release; no database migration is involved by design.
- Issue-tracker publication is pending workspace setup (run `/setup-matt-pocock-skills`); this mirrored doc is the source of truth until then.
