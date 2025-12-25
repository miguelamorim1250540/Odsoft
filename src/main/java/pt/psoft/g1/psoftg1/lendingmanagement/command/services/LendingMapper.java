package pt.psoft.g1.psoftg1.lendingmanagement.command.services;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import pt.psoft.g1.psoftg1.bookmanagement.services.BookService;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderService;

/**
 * Mapper for Lending command operations
 * Guides:
 *  - https://www.baeldung.com/mapstruct
 *  - https://medium.com/@susantamon/mapstruct-a-comprehensive-guide-in-spring-boot-context-1e7202da033e
 */
@Mapper(componentModel = "spring", uses = {BookService.class, ReaderService.class})
public abstract class LendingMapper {

    /**
     * Partial update of Lending for setting as returned
     */
    public abstract void update(SetLendingReturnedRequest request, @MappingTarget Lending lending);

}
