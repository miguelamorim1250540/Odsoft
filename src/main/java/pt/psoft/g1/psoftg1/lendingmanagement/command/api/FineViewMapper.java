package pt.psoft.g1.psoftg1.lendingmanagement.command.api;

import org.mapstruct.Mapper;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Fine;
import java.util.Optional;

/**
 * Mapper for Fine → FineView
 */
@Mapper(componentModel = "spring")
public abstract class FineViewMapper {

    public abstract FineView toFineView(Fine fine);

    public abstract Iterable<FineView> toFineView(Iterable<Fine> fines);

    // método auxiliar para Optional<Integer> → Integer
    protected Integer map(Optional<Integer> value) {
        return value.orElse(null);
    }
}