# authentication — details

## Files & Roles

| File | Role |
|---|---|
| `config/SecurityConfig` | `SecurityFilterChain` for OAuth2 resource server + JWT; stateless; method security enabled. |
| `config/CorsConfig` | CORS source for web/mobile clients (env-driven origins). |
| `config/RsaKeyFactory` | Builds `RSAPublicKey`/`RSAPrivateKey` from `RsaKeyConfigProperties` (PEM paths or inline). |
| `config/properties/RsaKeyConfigProperties.java` | Typed `@ConfigurationProperties` wrapper. |
| `config/properties/RsaKeyProperties.java` | Key material holder (`publicKey`, `privateKey`). |
| `converter/JwtAuthenticationConverter` | `JwtAuthenticationConverter` impl: maps claims → authorities (role + shop/category prefs). |
| `service/JwtTokenService` | Sign tokens with RSA, parse/validate, expose claims. |
| `service/UserService` | CRUD + static password/hashing for `UserAccounts`. |
| `service/AdminProvisioningService` | Ensures bootstrap admin/super-admin rows exist on startup (idempotent), then ORs the configured route bits into the `SUPER_ADMIN` category and its `SUPER_ADMIN` role row (additive, never clears). |
| `service/SuperAdminRouteProvisioner` | For each `super-admin.routes` entry: find feature by route, else create missing parents (no bit/route) and the screen (next free bit). Takes `pg_advisory_xact_lock` first so concurrent instances don't pick the same bit; requires an active transaction. |
| `config/properties/SuperAdminRoutesProperties` | `@ConfigurationProperties("super-admin")` record exposing `routes`. |
| `repository/UserAccountRepository` | Spring Data JDBC repo for `UserAccounts`. |
| `entity/UserAccounts` | Security principal entity: username, password hash, roles, shop access. |
| `model/JwtClaims` / `model/TenantContext` | Decoded claim structures consumed by converters/filters. |
| `utils/SecurityExpressions` | SPEL constants like `IS_SHOP_OWNER`, `HAS_ROLE_...` used across controllers. |
| `controller/LoginController` | Issues tokens (username/password → JWT). |

## Behavioral Notes

- Only the default super admin is affected: the `SUPER_ADMIN` category is assigned solely to the user created from `super-admin.username`; super admins registered via the API get no category.
- `title`/`icon` are applied only when a route's feature is created; editing them in config later does not update an existing feature. A blank `title` falls back to the prettified last segment; parent features get prettified titles and an empty icon. An existing parent row is reused as-is, so a pre-existing `NULL` icon must be fixed with a one-off SQL update.
- Routes removed from `super-admin.routes` are not revoked. A configured route whose feature has no bit position fails startup (`InvalidSuperAdminRouteException`).
- `CategoryService.enableFeaturesForCategory` overwrites category bits; the next startup restores the configured routes.

- Tokens are RSA-signed (private key), validated with the public key — keys from `resources/keys/` or env.
- `JwtAuthenticationConverter` is where role + shop + category info is injected into the `Authentication`; downstream filters/controllers rely on it.
- `ShopContext` (in `shop/`) is populated elsewhere (see `shop/context/filter/ShopContextFilter`), from this authentication result.
- `SecurityExpressions` strings are referenced by controllers in `core/`, `cash/`, etc. Renaming them is a cross-module change.