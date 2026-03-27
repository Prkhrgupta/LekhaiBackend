package in.lekhai.core.account_master.seeders;

import in.lekhai.shop.context.model.ShopContext;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class GroupSeeder implements ApplicationRunner {
    private final AccountGroupSeeder accountGroupSeeder;

    public GroupSeeder(AccountGroupSeeder accountGroupSeeder) {
        this.accountGroupSeeder = accountGroupSeeder;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            ShopContext.setShopCode(0);
            accountGroupSeeder.runSeeder();
            ShopContext.clear();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
