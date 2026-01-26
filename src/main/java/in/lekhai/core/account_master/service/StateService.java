package in.lekhai.core.account_master.service;

import in.lekhai.core.account_master.domain.State;
import in.lekhai.core.account_master.dto.StateResponse;
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
    public List<StateResponse> listStates() {
        return StreamSupport.stream(stateRepository.findAll().spliterator(), false)
                .map(this::mapToResponse)
                .toList();
    }

    private StateResponse mapToResponse(State state) {
        return new StateResponse(state.getStateCode(), state.getStateName());
    }
}
