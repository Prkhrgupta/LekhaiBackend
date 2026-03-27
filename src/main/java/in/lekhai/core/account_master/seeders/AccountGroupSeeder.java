package in.lekhai.core.account_master.seeders;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import in.lekhai.core.account_master.domain.AccountGroup;
import in.lekhai.core.account_master.repository.AccountGroupRepository;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class AccountGroupSeeder {

    private final ResourceLoader resourceLoader;
    private final AccountGroupRepository accountGroupRepository;

    private static final Logger log = LoggerFactory.getLogger(AccountGroupSeeder.class);

    public AccountGroupSeeder(ResourceLoader resourceLoader,
                              AccountGroupRepository accountGroupRepository) {
        this.resourceLoader = resourceLoader;
        this.accountGroupRepository = accountGroupRepository;
    }

    @ShopContextTransactional
    public void runSeeder() {
        long countOfEntries = accountGroupRepository.count();

        if (countOfEntries == 0) {
            seedAccountGroup();
        } else {
            log.info("Account Groups already present, Skipping seeding");
        }
    }

    private void seedAccountGroup() {
        List<GroupDTO> allAccountGroups = readCsv();

        // Step 1: Save primary groups
        List<AccountGroup> primaryGroups = allAccountGroups.stream()
                .filter(ag -> ag.getParentGroupName() == null)
                .map(ag -> new AccountGroup(
                        ag.getAccountGroupName(),
                        null,
                        ag.getNature(),
                        ag.getBehaviour(),
                        true
                ))
                .toList();

        List<AccountGroup> savedPrimaryGroups = accountGroupRepository.saveAll(primaryGroups);

        // Map: name -> entity
        Map<String, AccountGroup> groupMap = savedPrimaryGroups.stream()
                .collect(Collectors.toMap(AccountGroup::getName, Function.identity()));

        // Step 2: Process subgroups iteratively (supports any depth)
        List<GroupDTO> pending = allAccountGroups.stream()
                .filter(ag -> ag.getParentGroupName() != null)
                .collect(Collectors.toList());

        while (!pending.isEmpty()) {
            int processedInThisRound = 0;

            Iterator<GroupDTO> iterator = pending.iterator();

            while (iterator.hasNext()) {
                GroupDTO ag = iterator.next();

                AccountGroup parentGroup = groupMap.get(ag.getParentGroupName());

                if (parentGroup != null) {
                    AccountGroup group = new AccountGroup(
                            ag.getAccountGroupName(),
                            parentGroup.getId(),
                            parentGroup.getNature(),
                            parentGroup.getBehaviour(),
                            false
                    );

                    AccountGroup saved = accountGroupRepository.save(group);

                    groupMap.put(saved.getName(), saved);

                    iterator.remove();
                    processedInThisRound++;
                }
            }

            // Safety: detect broken hierarchy
            if (processedInThisRound == 0) {
                throw new RuntimeException(
                        "Invalid hierarchy! Could not resolve parent for: " +
                                pending.stream()
                                        .map(GroupDTO::getAccountGroupName)
                                        .collect(Collectors.toList())
                );
            }
        }

        log.info("Seeded Account groups successfully");
    }

    private List<GroupDTO> readCsv() {
        try (Reader reader = new InputStreamReader(
                resourceLoader.getResource("classpath:seeds/account_groups.csv")
                        .getInputStream(),
                StandardCharsets.UTF_8)) {

            CsvToBean<GroupDTO> csvToBean = new CsvToBeanBuilder<GroupDTO>(reader)
                    .withType(GroupDTO.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            List<GroupDTO> allAccountGroups = csvToBean.parse();

            // Normalize blank parent → null
            allAccountGroups.forEach(ag -> {
                if (ag.getParentGroupName() != null && ag.getParentGroupName().isBlank()) {
                    ag.setParentGroupName(null);
                }
            });

            return allAccountGroups;

        } catch (IOException e) {
            throw new RuntimeException("Failed to read CSV", e);
        }
    }
}