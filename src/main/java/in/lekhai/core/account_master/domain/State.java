package in.lekhai.core.account_master.domain;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("state")
public class State {
    @Id
    private String stateCode;
    private String stateName;
    private String gstCode;
    private String type;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    public State() {
    }

    public State(String stateCode, String stateName, String gstCode, String type) {
        this.stateCode = stateCode;
        this.stateName = stateName;
        this.gstCode = gstCode;
        this.type = type;
    }

    public String getStateCode() {
        return stateCode;
    }

    public String getStateName() {
        return stateName;
    }

    public String getGstCode() {
        return gstCode;
    }

    public String getType() {
        return type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
