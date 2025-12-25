package pt.psoft.g1.psoftg1.lendingmanagement.query.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.query.services.LendingService;
import pt.psoft.g1.psoftg1.shared.api.ListResponse;
import pt.psoft.g1.psoftg1.shared.services.Page;
import pt.psoft.g1.psoftg1.lendingmanagement.query.services.SearchLendingQuery;

import java.util.List;

@Tag(name = "Lendings (Query)", description = "Endpoints for querying Lendings")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/query/lendings")
public class LendingQueryController {

    private final LendingService lendingService;
    private final LendingViewMapper lendingViewMapper;

    @Operation(summary = "Get Lending by lending number")
    @GetMapping("/{year}/{seq}")
    public ResponseEntity<LendingView> getLendingByNumber(
            @PathVariable Integer year,
            @PathVariable Integer seq) {

        String lendingNumber = year + "/" + seq;
        Lending lending = lendingService.findByLendingNumber(lendingNumber)
                .orElseThrow(() -> new NotFoundException(Lending.class, lendingNumber));

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(lendingViewMapper.toLendingView(lending));
    }

    @Operation(summary = "List overdue lendings")
    @GetMapping("/overdue")
    public ListResponse<LendingView> getOverdueLendings(
            @RequestBody(required = false) Page page) {

        if (page == null) page = new Page(1, 10);

        List<Lending> overdue = lendingService.getOverdue(page);

        if (overdue.isEmpty())
            throw new NotFoundException("No overdue lendings found");

        return new ListResponse<>(lendingViewMapper.toLendingView(overdue));
    }

    @Operation(summary = "Search lendings")
    @PostMapping("/search")
    public ListResponse<LendingView> searchLendings(
            @RequestBody SearchLendingQuery query,
            @RequestParam(required = false) Page page) {

        if (page == null) page = new Page(1, 10);

        List<Lending> results = lendingService.searchLendings(page, query);

        if (results.isEmpty())
            throw new NotFoundException("No lendings found for given search criteria");

        return new ListResponse<>(lendingViewMapper.toLendingView(results));
    }

    @Operation(summary = "Get average lending duration")
    @GetMapping("/avgDuration")
    public ResponseEntity<LendingsAverageDurationView> getAverageDuration() {
        Double avgDuration = lendingService.getAverageDuration();
        return ResponseEntity.ok(lendingViewMapper.toLendingsAverageDurationView(avgDuration));
    }
}
