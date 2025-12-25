package pt.psoft.g1.psoftg1.lendingmanagement.query.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "A Fine")
public class FineView implements Serializable {
    @PositiveOrZero
    private int centsValue;

    @NotNull
    private LendingView lending;
}
