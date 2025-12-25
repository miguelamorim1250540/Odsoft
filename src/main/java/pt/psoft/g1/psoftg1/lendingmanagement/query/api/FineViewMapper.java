package pt.psoft.g1.psoftg1.lendingmanagement.query.api;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Fine;
import pt.psoft.g1.psoftg1.lendingmanagement.command.api.LendingViewMapper;
import pt.psoft.g1.psoftg1.lendingmanagement.command.api.LendingView;

import java.util.List;
import java.util.Optional;

@Mapper(componentModel = "spring", uses = {LendingViewMapper.class})
public abstract class FineViewMapper {

    @Mapping(target = "centsValue", expression = "java(mapCentsValue(fine.getCentsValue()))")
    @Mapping(target = "lending", source = "lending")
    public abstract FineView toFineView(Fine fine);

    public abstract List<FineView> toFineView(List<Fine> fines);

    // Método auxiliar para converter Optional<Integer> → Integer
    protected Integer mapCentsValue(Optional<Integer> value) {
        return value.orElse(null);
    }

    // Se precisares de outros Optional no futuro, podes criar métodos semelhantes
    // protected Integer mapDaysOverdue(Optional<Integer> value) { return value.orElse(null); }
}
