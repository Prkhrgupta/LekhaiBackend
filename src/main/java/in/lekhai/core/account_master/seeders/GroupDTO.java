package in.lekhai.core.account_master.seeders;


import com.opencsv.bean.CsvBindByName;
import in.lekhai.common.AccountEntryType;

public class GroupDTO {
    @CsvBindByName(column = "account_group_name")
    private String accountGroupName;
    @CsvBindByName(column = "parent_group_name")
    private String parentGroupName;

    @CsvBindByName(column = "nature")
    private String nature;

    @CsvBindByName(column = "behaviour")
    private AccountEntryType behaviour;

    public String getAccountGroupName() {
        return accountGroupName;
    }

    public void setAccountGroupName(String accountGroupName) {
        this.accountGroupName = accountGroupName;
    }

    public String getParentGroupName() {
        return parentGroupName;
    }

    public void setParentGroupName(String parentGroupName) {
        this.parentGroupName = parentGroupName;
    }

    public String getNature() {
        return nature;
    }

    public void setNature(String nature) {
        this.nature = nature;
    }

    public AccountEntryType getBehaviour() {
        return behaviour;
    }

    public void setBehaviour(AccountEntryType behaviour) {
        this.behaviour = behaviour;
    }
}
