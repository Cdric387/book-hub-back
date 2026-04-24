package com.bookhub.service;

import com.bookhub.dto.book.BookRequest;
import com.bookhub.entity.Book;
import com.bookhub.exception.BusinessException;
import com.bookhub.exception.ResourceNotFoundException;
import com.bookhub.repository.BookRepository;
import com.bookhub.repository.LoanRepository;
import com.bookhub.repository.RatingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires du service de gestion du catalogue.
 */
@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock private BookRepository bookRepository;
    @Mock private LoanRepository loanRepository;
    @Mock private RatingRepository ratingRepository;

    @InjectMocks
    private BookService bookService;

    private Book book;
    private BookRequest bookRequest;

    @BeforeEach
    void setUp() {
        book = Book.builder()
                .id(1L)
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("978-0132350884")
                .totalCopies(3)
                .availableCopies(3)
                .build();

        bookRequest = new BookRequest();
        bookRequest.setTitle("Clean Code");
        bookRequest.setAuthor("Robert C. Martin");
        bookRequest.setIsbn("978-0132350884");
        bookRequest.setTotalCopies(3);
    }

    // ---- findById ----

    @Test
    @DisplayName("findById retourne le livre si trouvé")
    void findById_found() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(ratingRepository.findAverageStarsByBookId(1L)).thenReturn(4.5);

        var response = bookService.findById(1L);

        assertThat(response.getTitle()).isEqualTo("Clean Code");
        assertThat(response.getAuthor()).isEqualTo("Robert C. Martin");
        assertThat(response.getAverageRating()).isEqualTo(4.5);
    }

    @Test
    @DisplayName("findById lance ResourceNotFoundException si livre absent")
    void findById_notFound_throwsException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ---- create ----

    @Test
    @DisplayName("create sauvegarde le livre avec availableCopies = totalCopies")
    void create_setsAvailableCopiesEqualToTotal() {
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> {
            Book b = inv.getArgument(0);
            b.setId(1L);
            return b;
        });
        when(ratingRepository.findAverageStarsByBookId(any())).thenReturn(null);

        var response = bookService.create(bookRequest);

        assertThat(response.getTotalCopies()).isEqualTo(3);
        assertThat(response.getAvailableCopies()).isEqualTo(3);
        verify(bookRepository).save(any(Book.class));
    }

    // ---- delete ----

    @Test
    @DisplayName("delete supprime si aucun exemplaire emprunté")
    void delete_success_whenAllCopiesAvailable() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        assertThatCode(() -> bookService.delete(1L)).doesNotThrowAnyException();
        verify(bookRepository).delete(book);
    }

    @Test
    @DisplayName("delete échoue si des exemplaires sont en cours d'emprunt")
    void delete_fails_whenCopiesBorrowed() {
        book.setAvailableCopies(1); // 1 exemplaire emprunté sur 3
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> bookService.delete(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("emprunté");

        verify(bookRepository, never()).delete(any());
    }

    // ---- findAll ----

    @Test
    @DisplayName("findAll retourne une page de livres")
    void findAll_returnsPaginatedResults() {
        var pageable = PageRequest.of(0, 20);
        when(bookRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(book)));
        when(ratingRepository.findAverageStarsByBookId(any())).thenReturn(null);

        var page = bookService.findAll(pageable);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getTitle()).isEqualTo("Clean Code");
    }

    // ---- isAvailable ----

    @Test
    @DisplayName("isAvailable est vrai si availableCopies > 0")
    void bookIsAvailable_whenCopiesRemaining() {
        assertThat(book.isAvailable()).isTrue();
    }

    @Test
    @DisplayName("isAvailable est faux si availableCopies = 0")
    void bookIsNotAvailable_whenNoCopiesRemaining() {
        book.setAvailableCopies(0);
        assertThat(book.isAvailable()).isFalse();
    }
}
