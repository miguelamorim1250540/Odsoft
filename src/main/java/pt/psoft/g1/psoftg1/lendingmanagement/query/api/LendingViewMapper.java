package pt.psoft.g1.psoftg1.lendingmanagement.query.api;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.shared.api.MapperInterface;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class LendingViewMapper extends MapperInterface {

    @Mapping(target = "lendingNumber", source = "lendingNumber")
    @Mapping(target = "bookTitle", source = "book.title")
    @Mapping(target = "fineValueInCents", expression = "java(lending.getFineValueInCents().orElse(null))")
    @Mapping(target = "daysUntilReturn", expression = "java(lending.getDaysUntilReturn().orElse(null))")
    @Mapping(target = "daysOverdue", expression = "java(lending.getDaysOverdue().orElse(null))")
    @Mapping(target = "rating", source = "rating")
    @Mapping(target = "_links.self", source = ".", qualifiedByName = "lendingLink")
    @Mapping(target = "_links.book", source = "book", qualifiedByName = "bookLink")
    @Mapping(target = "_links.reader", source = "readerDetails", qualifiedByName = "readerLink")
    public abstract LendingView toLendingView(Lending lending);

    public abstract List<LendingView> toLendingView(List<Lending> lendings);

    public abstract LendingsAverageDurationView toLendingsAverageDurationView(Double lendingsAverageDuration);
}
