# authentication

The **front door and access control** of the accounting system — who may sign in, as whom, and into which firm's books. In Tally terms this is the user/security setup: a shop owner, an accountant, and a data-entry operator should each see and do exactly what their role allows, and nothing more.

## Business Goal

The books of a firm are sensitive, and different people in the firm have different authority. This module decides who gets in and what they may do once inside:

- **Sign in** — a user presents username/password and receives a signed token (JWT, RSA-signed) that carries their identity, their **role(s)**, and the **shop/category** they belong to. Subsequent requests are statelessly authenticated from that token.
- **Enforce authority** — method-level security (`@PreAuthorize`, e.g. `IS_SHOP_OWNER`) guards actions endpoint-by-endpoint, so only authorised roles can, say, provision a shop or post a voucher. Role + shop + category preferences are loaded into the security context by the JWT converter for downstream use.
- **Firm identity wiring** — the authenticated result feeds the `shop/` module, which turns the principal's firm into the RLS-scoped database context for the whole request.
- **Bootstrap trust** — on startup, admin/super-admin accounts are provisioned idempotently so the platform always has an owner to manage shops from.
- **Login endpoint** — your standard "login screen" that turns credentials into a usable session token.

## Public API

- `config/SecurityConfig` — OAuth2 resource-server + JWT filter chain, method security enabled.
- `config/CorsConfig` — allowed origins for web/mobile clients.
- `config/RsaKeyFactory`, `config/properties/RsaKeyConfigProperties`, `config/properties/RsaKeyProperties` — RSA signing/verification key loading (PEM or env).
- `converter/JwtAuthenticationConverter` — token claims → Spring `Authentication` with roles + shop/category preferences.
- `service/JwtTokenService` — sign/validate/decode tokens.
- `service/UserService` — user account management (credentials, hashing).
- `service/AdminProvisioningService` — idempotent bootstrap of admin/super-admin accounts.
- `model/TenantContext`, `model/JwtClaims` — decoded token/tenant structures.
- `entity/UserAccounts`, `repository/UserAccountRepository` — the security principal (username, password hash, roles, shop access).
- `utils/SecurityExpressions` — the `@PreAuthorize` expressions used across controllers.
- `controller/LoginController` — the login endpoint.

## Dependencies

- `common/` (`JwtConstants`), `error/`, `core/` (users/roles used during provisioning), `shop/` (principal → RLS shop context).

## How to Extend

- **New protected endpoint:** annotate with `@PreAuthorize(SecurityExpressions.X)`; don't reinvent authentication.
- **New claim in the token:** update `JwtTokenService` + `JwtClaims`, and keep `JwtConstants` in sync.
- **New login flow:** extend `LoginController` / `UserService`, but don't bypass the `SecurityConfig` filter chain.

## Deep Dive

> Read `DETAILS.md` only when: wiring security filters, changing token claims/signature, RSA key handling, or provisioning logic.