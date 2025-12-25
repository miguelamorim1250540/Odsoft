package pt.psoft.g1.psoftg1.lendingmanagement.command.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import pt.psoft.g1.psoftg1.lendingmanagement.command.services.SetLendingReturnedRequest;
import pt.psoft.g1.psoftg1.lendingmanagement.command.services.LendingService;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;

@Component
@RequiredArgsConstructor
public class SetLendingReturnedHandler {

    private final LendingService lendingService;

    public Lending handle(String lendingNumber, long version,
            pt.psoft.g1.psoftg1.lendingmanagement.command.handlers.dto.SetLendingReturnedRequest request) {

        var commandRequest = new pt.psoft.g1.psoftg1.lendingmanagement.command.services.SetLendingReturnedRequest(
            request.getCommentary(),
            request.getRating()
        );

        return lendingService.setReturned(lendingNumber, commandRequest, version);
    }
}