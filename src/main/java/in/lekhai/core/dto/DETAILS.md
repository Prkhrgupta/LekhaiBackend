# core/dto — details

## Files & Roles

| File | Role |
|---|---|
| `admin/BaseUserEntity.java` | Shared fields for admin requests (name, phone, email). |
| `admin/AdminRegistrationRequest.java` / `AdminRegistrationResponse.java` | admin provisioning payloads. |
| `category/CategoryCreationRequest.java` / `CategoryCreationResponse.java` | category create flow. |
| `category/CategoryResponse.java` | category read model. |
| `category/EnableCategoryFeatureResponse.java` / `EnableCategoryWiseFeatures.java` | feature-enablement per category. |
| `feature/ScreenFeatureCreationRequest.java`, `FeatureResponse.java`, `FeatureCreationResponse.java` | feature/screen registry payloads. |
| `shop/BaseShopRequest.java` | shared shop create fields. |
| `shop/CreateShopExistingAdminRequest.java` / `CreateShopNewAdminRequest.java` | shop provisioning paths. |
| `shop/ShopCreationResponse.java` | shop create result. |
| `superadmin/SuperAdminRegistrationRequest.java` / `SuperAdminRegistrationResponse.java` | super-admin provisioning. |

## Behavioral Notes

- These are internal transfer types; controller request bodies should use contract models wherever the API spec defines them.
- Validation annotations (`@NotNull`, `@Size`, ...) belong on request DTOs; response DTOs stay plain.
- `BaseUserEntity` and `BaseShopRequest` are meant to be extended/embedded, not duplicated.