package in.lekhai.core.account_master.service;

import in.lekhai.contract.model.DropdownItem;
import in.lekhai.core.account_master.repository.TdsSectionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TdsSectionService {

    private final TdsSectionRepository tdsSectionRepository;

    public TdsSectionService(TdsSectionRepository tdsSectionRepository) {
        this.tdsSectionRepository = tdsSectionRepository;
    }

    public List<DropdownItem> listTdsSections() {
        return tdsSectionRepository.findAllActive().stream()
                .map(section -> new DropdownItem()
                        .id(section.getId())
                        .code(section.getCode())
                        .label(section.getLabel()))
                .toList();
    }
}
