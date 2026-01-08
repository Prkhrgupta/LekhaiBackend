//package in.lekhai.core.domain.admin;
//
//
//import in.lekhai.core.dto.admin.BaseUserEntity;
//import in.lekhai.core.enums.Roles;
//import lombok.*;
//import org.springframework.data.annotation.CreatedDate;
//import org.springframework.data.annotation.Id;
//import org.springframework.data.annotation.LastModifiedDate;
//import org.springframework.data.relational.core.mapping.Table;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
//@Getter
//@Setter
//@Builder
//@AllArgsConstructor
//@NoArgsConstructor
//@Table("user_information")
//public class UserInformation implements BaseUserEntity {
//    @Id
//    private Long id;
//    private String uuid;
//    private Roles roleId;
//    private Long categoryId;
//    @Builder.Default
//    private List<Long> permissionBit = new ArrayList<>();
//    private Integer shopCode;
//    @CreatedDate
//    private LocalDateTime createdAt;
//    @LastModifiedDate
//    private LocalDateTime updatedAt;
//}
