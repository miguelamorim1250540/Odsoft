package pt.psoft.g1.psoftg1.lendingmanagement.command.services;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SetLendingReturnedRequest {
    private final String commentary;
    private final Integer rating;
}
