# authentication

Security context: Spring Security config, JWT issuing/validation, login, and user account management.

## Public API

- `config/SecurityConfig` — resource server config, `@EnableMethodSecurity` (drives `@PreAuthorize`).
- `config/CorsConfig` — CORS allowed origins.
- `config/RsaKeyFactory`, `config/properties/RsaKeyConfigProperties`, `RsaKeyProperties` — RSA key loading from PEM/env.
- `converter/JwtAuthenticationConverter` — JWT → `Authentication` with roles/preferences.
- `service/JwtTokenService` — token create/validate/decode.
- `service/UserService` — user account lookup/management.
- `service/AdminProvisioningService` — boot-time admin/super-admin provisioning.
- `model/TenantContext`, `model/JwtClaims` — decoded claim models.
- `entity/UserAccounts` — user account entity (impl in `authentication`, not `core`).
- `repository/UserAccountRepository` — Spring Data JDBC repo.
- `utils/SecurityExpressions` — SPEL strings (`IS_SHOP_OWNER`) for `@PreAuthorize`.
- `controller/LoginController` — login endpoint.

## Dependencies

- `common/` (`JwtConstants`), `error/`, `core/` (users/roles for provisioning), `shop/`

## How to Extend

- New protected endpoint: annotate with `@PreAuthorize(SecurityExpressions.X)`.
- New claim in token: update `JwtTokenService` + `JwtClaims`, keep `JwtConstants` in sync.
- New login flow: extend `LoginController`/`UserService`, don't bypass `SecurityConfig`'s filter chain.

## Deep Dive

> Read `DETAILS.md` only when: wiring security filters, changing token claims/signature, RSA key handling, or provisioning logic.