package in.lekhai.core.account_master.service;

import in.lekhai.contract.model.DropdownItem;
import in.lekhai.core.account_master.domain.State;
import in.lekhai.core.account_master.repository.StateRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class StateService {

    private final StateRepository stateRepository;

    public StateService(StateRepository stateRepository) {
        this.stateRepository = stateRepository;
    }
    public List<DropdownItem> listStates() {
        return StreamSupport.stream(stateRepository.findAll().spliterator(), false)
                .map(state -> new DropdownItem().code(state.getStateCode()).label(state.getStateName()))
                .toList();
    }

    private DropdownItem mapToResponse(State state) {
        return new DropdownItem()
                .code(state.getStateCode())
                .label(state.getStateName());
    }
}
