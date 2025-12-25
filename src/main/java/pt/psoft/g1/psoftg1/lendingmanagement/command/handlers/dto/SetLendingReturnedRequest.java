package pt.psoft.g1.psoftg1.lendingmanagement.command.handlers.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for marking a Lending as returned")
public class SetLendingReturnedRequest {
    @Size(max = 1024)
    private String commentary;
    private Integer rating;
}
