package pt.psoft.g1.psoftg1.lendingmanagement.command.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import pt.psoft.g1.psoftg1.lendingmanagement.command.handlers.dto.CreateLendingCommand;
import pt.psoft.g1.psoftg1.lendingmanagement.command.services.CreateLendingRequest;
import pt.psoft.g1.psoftg1.lendingmanagement.command.services.LendingService;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;

@Component
@RequiredArgsConstructor
public class CreateLendingHandler {

    private final LendingService lendingService;

    public Lending handle(CreateLendingCommand command) {
        CreateLendingRequest request = new CreateLendingRequest(command.getIsbn(), command.getReaderNumber());
        return lendingService.create(request);
    }
}
