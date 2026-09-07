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
| `service/AdminProvisioningService` | Ensures bootstrap admin/super-admin rows exist on startup (idempotent). |
| `repository/UserAccountRepository` | Spring Data JDBC repo for `UserAccounts`. |
| `entity/UserAccounts` | Security principal entity: username, password hash, roles, shop access. |
| `model/JwtClaims` / `model/TenantContext` | Decoded claim structures consumed by converters/filters. |
| `utils/SecurityExpressions` | SPEL constants like `IS_SHOP_OWNER`, `HAS_ROLE_...` used across controllers. |
| `controller/LoginController` | Issues tokens (username/password → JWT). |

## Behavioral Notes

- Tokens are RSA-signed (private key), validated with the public key — keys from `resources/keys/` or env.
- `JwtAuthenticationConverter` is where role + shop + category info is injected into the `Authentication`; downstream filters/controllers rely on it.
- `ShopContext` (in `shop/`) is populated elsewhere (see `shop/context/filter/ShopContextFilter`), from this authentication result.
- `SecurityExpressions` strings are referenced by controllers in `core/`, `cash/`, etc. Renaming them is a cross-module change.