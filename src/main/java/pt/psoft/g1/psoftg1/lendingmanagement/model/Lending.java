package pt.psoft.g1.psoftg1.lendingmanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.hibernate.StaleObjectStateException;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;

/**
 * The {@code Lending} class associates a {@code Reader} and a {@code Book}.
 * It stores the date it was registered, the date it is supposed to
 * be returned, and the date it actually was returned.
 * It also stores an optional reader {@code commentary} and rating (0-10) upon return,
 * and the {@code Fine}, if applicable.
 */
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames={"LENDING_NUMBER"})})
public class Lending {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long pk;

    private LendingNumber lendingNumber;

    @NotNull
    @Getter
    @ManyToOne(fetch=FetchType.EAGER, optional = false)
    private Book book;

    @NotNull
    @Getter
    @ManyToOne(fetch=FetchType.EAGER, optional = false)
    private ReaderDetails readerDetails;

    @NotNull
    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.DATE)
    @Getter
    private LocalDate startDate;

    @NotNull
    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    @Getter
    private LocalDate limitDate;

    @Temporal(TemporalType.DATE)
    @Getter
    private LocalDate returnedDate;

    @Version
    @Getter
    private long version;

    @Size(min = 0, max = 1024)
    @Column(length = 1024)
    private String commentary = null;

    @Min(0)
    @Max(10)
    private Integer rating; // ✅ Student C

    @Transient
    private Integer daysUntilReturn;

    @Transient
    private Integer daysOverdue;

    @Getter
    private int fineValuePerDayInCents;

    public Lending(Book book, ReaderDetails readerDetails, int seq, int lendingDuration, int fineValuePerDayInCents){
        this.book = Objects.requireNonNull(book);
        this.readerDetails = Objects.requireNonNull(readerDetails);
        this.lendingNumber = new LendingNumber(seq);
        this.startDate = LocalDate.now();
        this.limitDate = LocalDate.now().plusDays(lendingDuration);
        this.returnedDate = null;
        this.fineValuePerDayInCents = fineValuePerDayInCents;
        setDaysUntilReturn();
        setDaysOverdue();
    }

    /**
     * Define o livro como devolvido, incluindo comentário e rating.
     * Rating só pode existir se returnedDate != null e só uma vez.
     */
    public void setReturned(long desiredVersion, String commentary, Integer rating) {
        if (this.returnedDate != null) {
            throw new IllegalStateException("Book already returned");
        }

        // Verifica versão (optimistic locking)
        if (this.version != desiredVersion) {
            throw new StaleObjectStateException("Object was already modified by another user", this.pk);
        }

        this.returnedDate = LocalDate.now();
        this.commentary = commentary;
        this.rating = rating; // rating pode ser null
    }

    private void setDaysUntilReturn() {
        int daysUntilReturn = (int) ChronoUnit.DAYS.between(LocalDate.now(), this.limitDate);
        this.daysUntilReturn = (this.returnedDate != null || daysUntilReturn < 0) ? null : daysUntilReturn;
    }

    private void setDaysOverdue() {
        int days = getDaysDelayed();
        this.daysOverdue = days > 0 ? days : null;
    }

    public int getDaysDelayed() {
        if (this.returnedDate != null) {
            return Math.max((int) ChronoUnit.DAYS.between(this.limitDate, this.returnedDate), 0);
        } else {
            return Math.max((int) ChronoUnit.DAYS.between(this.limitDate, LocalDate.now()), 0);
        }
    }

    public Optional<Integer> getDaysUntilReturn() {
        setDaysUntilReturn();
        return Optional.ofNullable(daysUntilReturn);
    }

    public Optional<Integer> getDaysOverdue() {
        setDaysOverdue();
        return Optional.ofNullable(daysOverdue);
    }

    public Optional<Integer> getFineValueInCents() {
        int days = getDaysDelayed();
        return days > 0 ? Optional.of(fineValuePerDayInCents * days) : Optional.empty();
    }

    public String getTitle() {
        return this.book.getTitle().toString(); // já é String
    }

    public String getLendingNumber() {
        return this.lendingNumber.toString();
    }

    public Optional<Integer> getRating() {
        return Optional.ofNullable(rating);
    }

    protected Lending() {} // Para ORM

    public static Lending newBootstrappingLending(Book book,
                                                  ReaderDetails readerDetails,
                                                  int year,
                                                  int seq,
                                                  LocalDate startDate,
                                                  LocalDate returnedDate,
                                                  int lendingDuration,
                                                  int fineValuePerDayInCents,
                                                  Integer rating){
        Lending lending = new Lending();

        lending.book = Objects.requireNonNull(book);
        lending.readerDetails = Objects.requireNonNull(readerDetails);
        lending.lendingNumber = new LendingNumber(year, seq);
        lending.startDate = startDate;
        lending.limitDate = startDate.plusDays(lendingDuration);
        lending.fineValuePerDayInCents = fineValuePerDayInCents;
        lending.returnedDate = returnedDate;
        lending.rating = rating;

        return lending;
    }
}
