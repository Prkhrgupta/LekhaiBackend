package in.lekhai.core.domain.category;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Table("categories")
public class Categories {
    @Id
    private Integer id;

    private String name;

    private List<Long> permissions = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public Categories() {
    }

    public Categories(Integer id, String name, List<Long> permissions, LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.permissions = permissions;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static CategoriesBuilder builder() {
        return new CategoriesBuilder();
    }

    public Integer getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public List<Long> getPermissions() {
        return this.permissions;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPermissions(List<Long> permissions) {
        this.permissions = permissions;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static class CategoriesBuilder {
        private Integer id;
        private String name;
        private List<Long> permissions = new ArrayList<>();
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        CategoriesBuilder() {
        }

        public CategoriesBuilder id(Integer id) {
            this.id = id;
            return this;
        }

        public CategoriesBuilder name(String name) {
            this.name = name;
            return this;
        }

        public CategoriesBuilder permissions(List<Long> permissions) {
            this.permissions = permissions;
            return this;
        }

        public CategoriesBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public CategoriesBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Categories build() {
            return new Categories(id, name, permissions, createdAt, updatedAt);
        }

        public String toString() {
            return "Categories.CategoriesBuilder(id=" + this.id + ", name=" + this.name + ", permissions="
                    + this.permissions + ", createdAt=" + this.createdAt + ", updatedAt=" + this.updatedAt + ")";
        }
    }
}
