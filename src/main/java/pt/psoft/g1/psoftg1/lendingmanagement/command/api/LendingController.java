package pt.psoft.g1.psoftg1.lendingmanagement.command.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.command.services.CreateLendingRequest;
import pt.psoft.g1.psoftg1.lendingmanagement.command.services.LendingService;
import pt.psoft.g1.psoftg1.lendingmanagement.command.services.SearchLendingQuery;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderService;
import pt.psoft.g1.psoftg1.shared.api.ListResponse;
import pt.psoft.g1.psoftg1.shared.services.ConcurrencyService;
import pt.psoft.g1.psoftg1.shared.services.Page;
import pt.psoft.g1.psoftg1.shared.services.SearchRequest;
import pt.psoft.g1.psoftg1.usermanagement.model.Librarian;
import pt.psoft.g1.psoftg1.usermanagement.model.User;
import pt.psoft.g1.psoftg1.usermanagement.services.UserService;
import pt.psoft.g1.psoftg1.lendingmanagement.command.services.SetLendingReturnedRequest;

import java.util.List;
import java.util.Objects;

@Tag(name = "Lendings", description = "Endpoints for managing Lendings")
@RestController
@RequestMapping("/api/lendings")
@RequiredArgsConstructor
public class LendingController {

    private final LendingService lendingService;
    private final ReaderService readerService;
    private final UserService userService;
    private final ConcurrencyService concurrencyService;
    private final LendingViewMapper lendingViewMapper;

    // ---------------- CREATE ----------------
    @Operation(summary = "Creates a new Lending")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<LendingView> create(@Valid @RequestBody CreateLendingRequest request) {
        Lending lending = lendingService.create(request);

        var newLendingUri = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .pathSegment(lending.getLendingNumber())
                .build()
                .toUri();

        return ResponseEntity.created(newLendingUri)
                .contentType(MediaType.parseMediaType("application/hal+json"))
                .eTag(Long.toString(lending.getVersion()))
                .body(lendingViewMapper.toLendingView(lending));
    }

    // ---------------- READ ----------------
    @Operation(summary = "Get a specific Lending by its Lending Number")
    @GetMapping("/{year}/{seq}")
    public ResponseEntity<LendingView> findByLendingNumber(
            Authentication authentication,
            @PathVariable("year") @Parameter(description = "Year of the Lending") Integer year,
            @PathVariable("seq") @Parameter(description = "Sequential number of the Lending") Integer seq) {

        String lendingNumber = year + "/" + seq;
        Lending lending = lendingService.findByLendingNumber(lendingNumber)
                .orElseThrow(() -> new NotFoundException(Lending.class, lendingNumber));

        User loggedUser = userService.getAuthenticatedUser(authentication);

        // Only allow access if librarian or the reader owning the lending
        if (!(loggedUser instanceof Librarian)) {
            ReaderDetails readerDetails = readerService.findByUsername(loggedUser.getUsername())
                    .orElseThrow(() -> new NotFoundException(ReaderDetails.class, loggedUser.getUsername()));

            if (!Objects.equals(readerDetails.getReaderNumber(), lending.getReaderDetails().getReaderNumber())) {
                throw new AccessDeniedException("Reader does not have permission to view this lending");
            }
        }

        var lendingUri = ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri();

        return ResponseEntity.ok()
                .location(lendingUri)
                .contentType(MediaType.parseMediaType("application/hal+json"))
                .eTag(Long.toString(lending.getVersion()))
                .body(lendingViewMapper.toLendingView(lending));
    }

    // ---------------- UPDATE ----------------
    @Operation(summary = "Mark a lending as returned")
    @PatchMapping("/{year}/{seq}")
    public ResponseEntity<LendingView> setLendingReturned(
            WebRequest request,
            Authentication authentication,
            @PathVariable("year") Integer year,
            @PathVariable("seq") Integer seq,
            @Valid @RequestBody SetLendingReturnedRequest resource) {

        String ifMatchValue = request.getHeader(ConcurrencyService.IF_MATCH);
        if (ifMatchValue == null || ifMatchValue.isEmpty() || ifMatchValue.equals("null")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You must issue a conditional PATCH using 'if-match'");
        }

        String lendingNumber = year + "/" + seq;
        Lending lending = lendingService.findByLendingNumber(lendingNumber)
                .orElseThrow(() -> new NotFoundException(Lending.class, lendingNumber));

        User loggedUser = userService.getAuthenticatedUser(authentication);

        ReaderDetails readerDetails = readerService.findByUsername(loggedUser.getUsername())
                .orElseThrow(() -> new NotFoundException(ReaderDetails.class, loggedUser.getUsername()));

        if (!Objects.equals(readerDetails.getReaderNumber(), lending.getReaderDetails().getReaderNumber())) {
            throw new AccessDeniedException("Reader does not have permission to edit this lending");
        }

        Lending updatedLending = lendingService.setReturned(lendingNumber, resource, concurrencyService.getVersionFromIfMatchHeader(ifMatchValue).longValue());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/hal+json"))
                .eTag(Long.toString(updatedLending.getVersion()))
                .body(lendingViewMapper.toLendingView(updatedLending));
    }

    // ---------------- STATS ----------------
    @Operation(summary = "Get average lendings duration")
    @GetMapping("/avgDuration")
    public ResponseEntity<LendingsAverageDurationView> getAvgDuration() {
        return ResponseEntity.ok(
                lendingViewMapper.toLendingsAverageDurationView(lendingService.getAverageDuration())
        );
    }

    // ---------------- LISTS ----------------
    @Operation(summary = "Get list of overdue lendings")
    @GetMapping("/overdue")
    public ListResponse<LendingView> getOverdueLendings(@Valid @RequestBody Page page) {
        List<Lending> overdueLendings = lendingService.getOverdue(page);
        if (overdueLendings.isEmpty()) {
            throw new NotFoundException("No lendings to show");
        }
        return new ListResponse<>(lendingViewMapper.toLendingView(overdueLendings));
    }

    @PostMapping("/search")
    @Operation(summary = "Search lendings by criteria")
    public ListResponse<LendingView> searchLendings(@RequestBody SearchRequest<SearchLendingQuery> request) {
        List<Lending> lendings = lendingService.searchLendings(request.getPage(), request.getQuery());
        return new ListResponse<>(lendingViewMapper.toLendingView(lendings));
    }

    /*
    // Optional endpoint for future use
    @GetMapping("/averageMonthlyPerReader")
    public ListResponse<ReaderLendingsAvgPerMonthView> getAverageMonthlyPerReader(
            @RequestParam("startDate") String start,
            @RequestParam("endDate") String end) {
        // implementation pending
    }
    */
}
