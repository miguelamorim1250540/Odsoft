package pt.psoft.g1.psoftg1.lendingmanagement.command.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.exceptions.LendingForbiddenException;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.FineRepository;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.LendingRepository;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.shared.services.Page;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@PropertySource("classpath:config/library.properties")
public class LendingServiceImpl implements LendingService {

    private final LendingRepository lendingRepository;
    private final FineRepository fineRepository;
    private final BookRepository bookRepository;
    private final ReaderRepository readerRepository;

    @Value("${lendingDurationInDays}")
    private int lendingDurationInDays;

    @Value("${fineValuePerDayInCents}")
    private int fineValuePerDayInCents;

    @Override
    public Optional<Lending> findByLendingNumber(String lendingNumber) {
        return lendingRepository.findByLendingNumber(lendingNumber);
    }

    @Override
    public List<Lending> listByReaderNumberAndIsbn(String readerNumber, String isbn, Optional<Boolean> returned) {
        List<Lending> lendings = lendingRepository.listByReaderNumberAndIsbn(readerNumber, isbn);

        if (returned.isPresent()) {
            lendings.removeIf(l -> (l.getReturnedDate() == null) == returned.get());
        }

        return lendings;
    }

    @Override
    public Lending create(CreateLendingRequest resource) {
        int count = 0;

        Iterable<Lending> lendingList = lendingRepository.listOutstandingByReaderNumber(resource.getReaderNumber());
        for (Lending lending : lendingList) {
            if (lending.getDaysDelayed() > 0) {
                throw new LendingForbiddenException("Reader has book(s) past their due date");
            }
            count++;
            if (count >= 3) {
                throw new LendingForbiddenException("Reader has three books outstanding already");
            }
        }

        final var book = bookRepository.findByIsbn(resource.getIsbn())
                .orElseThrow(() -> new NotFoundException("Book not found"));

        final var reader = readerRepository.findByReaderNumber(resource.getReaderNumber())
                .orElseThrow(() -> new NotFoundException("Reader not found"));

        int seq = lendingRepository.getCountFromCurrentYear() + 1;
        final Lending lending = new Lending(book, reader, seq, lendingDurationInDays, fineValuePerDayInCents);

        return lendingRepository.save(lending);
    }

    @Override
    public Lending setReturned(String lendingNumber, SetLendingReturnedRequest resource, long desiredVersion) {
        Lending lending = lendingRepository.findByLendingNumber(lendingNumber)
                .orElseThrow(() -> new RuntimeException("Lending not found"));

        // Usa o método da entidade Lending
        lending.setReturned(desiredVersion, resource.getCommentary(), resource.getRating());

        return lendingRepository.save(lending);
    }

    @Override
    public Double getAverageDuration() {
        Double avg = lendingRepository.getAverageDuration();
        return Double.valueOf(String.format(Locale.US, "%.1f", avg));
    }

    @Override
    public List<Lending> getOverdue(Page page) {
        if (page == null) page = new Page(1, 10);
        return lendingRepository.getOverdue(page);
    }

    @Override
    public Double getAvgLendingDurationByIsbn(String isbn) {
        Double avg = lendingRepository.getAvgLendingDurationByIsbn(isbn);
        return Double.valueOf(String.format(Locale.US, "%.1f", avg));
    }

    @Override
    public List<Lending> searchLendings(Page page, SearchLendingQuery query) {
        if (page == null) page = new Page(1, 10);
        if (query == null) query = new SearchLendingQuery("", "", null,
                LocalDate.now().minusDays(10L).toString(), null);

        LocalDate startDate = null;
        LocalDate endDate = null;
        try {
            if (query.getStartDate() != null) startDate = LocalDate.parse(query.getStartDate());
            if (query.getEndDate() != null) endDate = LocalDate.parse(query.getEndDate());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Expected format is YYYY-MM-DD");
        }

        return lendingRepository.searchLendings(page,
                query.getReaderNumber(),
                query.getIsbn(),
                query.getReturned(),
                startDate,
                endDate);
    }
}
