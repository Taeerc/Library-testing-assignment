package ac.il.bgu.qa;

import ac.il.bgu.qa.errors.*;
import ac.il.bgu.qa.services.*;

import org.junit.jupiter.api.*;

import org.junit.jupiter.params.*;
import org.mockito.*;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;

public class TestLibrary {

    @Mock DatabaseService databaseService;

    @Mock ReviewService reviewService;

    @Mock NotificationService mockNotificationService;

    Library library;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        library = new Library(databaseService, reviewService);
    }

    @Test
    //throwsIllegalArgumentException
    void addBook_whenBookIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> library.addBook(null));
    }

    @Test
    //throwsIllegalArgumentException
    void addBook_whenISBNInvalid() {
        Book bad = mock(Book.class);
        when(bad.getISBN()).thenReturn("123"); //invalid
        when(bad.getTitle()).thenReturn("Some Title");
        when(bad.getAuthor()).thenReturn("John Smith");
        when(bad.isBorrowed()).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> library.addBook(bad));
    }

    @Test
    //throwsIllegalArgumentException
    void addBook_whenTitleInvalid() {
        Book bad = mock(Book.class);
        when(bad.getISBN()).thenReturn("9780306406157"); //valid ISBN13
        when(bad.getTitle()).thenReturn(""); // invalid
        when(bad.getAuthor()).thenReturn("John Smith");
        when(bad.isBorrowed()).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> library.addBook(bad));
        verify(databaseService, never()).addBook(anyString(), any());
    }

    @Test
    //throwsIllegalArgumentException(
    void addBook_whenAuthorInvalid() {
        Book bad = mock(Book.class);
        when(bad.getISBN()).thenReturn("9780306406157");
        when(bad.getTitle()).thenReturn("Some Title");
        when(bad.getAuthor()).thenReturn("J--ohn"); // invalid: consecutive --
        when(bad.isBorrowed()).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> library.addBook(bad));
    }

    @Test
    //throwsIllegalArgumentException
    void addBook_whenBookAlreadyBorrowed() {
        Book bad = mock(Book.class);
        when(bad.getISBN()).thenReturn("9780306406157");
        when(bad.getTitle()).thenReturn("Some Title");
        when(bad.getAuthor()).thenReturn("John Smith");
        when(bad.isBorrowed()).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> library.addBook(bad));
    }

    @Test
    //throwsIllegalArgumentException
    void addBook_whenBookAlreadyExistsInDB() {
        Book good = mock(Book.class);
        when(good.getISBN()).thenReturn("9780306406157");
        when(good.getTitle()).thenReturn("Some Title");
        when(good.getAuthor()).thenReturn("John Smith");
        when(good.isBorrowed()).thenReturn(false);

        // BOOK ALREADY EXISTS → DB returns a non-null value
        when(databaseService.getBookByISBN("9780306406157")).thenReturn(mock(Book.class));

        assertThrows(IllegalArgumentException.class,
                () -> library.addBook(good));
    }

    @Test
    void addBook_validBook_addsBookToDatabase() {
        Book good = mock(Book.class);
        when(good.getISBN()).thenReturn("9780306406157");
        when(good.getTitle()).thenReturn("Some Title");
        when(good.getAuthor()).thenReturn("John Smith");
        when(good.isBorrowed()).thenReturn(false);

        // BOOK DOES NOT EXIST
        when(databaseService.getBookByISBN("9780306406157")).thenReturn(null);

        library.addBook(good);

        verify(databaseService).addBook("9780306406157", good);
    }

    @Test
    void registerUser_nullUser() {
        assertThrows(IllegalArgumentException.class,
                () -> library.registerUser(null));
    }

    @Test
    void registerUser_idNull() {
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(null);
        when(mockUser.getName()).thenReturn("Alice");
        when(mockUser.getNotificationService()).thenReturn(mockNotificationService);

        assertThrows(IllegalArgumentException.class,
                () -> library.registerUser(mockUser));
    }

    @Test
    void registerUser_idInvalidFormat() {
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn("12345");  //wrong
        when(mockUser.getName()).thenReturn("Alice");
        when(mockUser.getNotificationService()).thenReturn(mockNotificationService);

        assertThrows(IllegalArgumentException.class,
                () -> library.registerUser(mockUser));
    }

    @Test
    void registerUser_invalidName() {
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn("123456789012");
        when(mockUser.getName()).thenReturn("");
        when(mockUser.getNotificationService()).thenReturn(mockNotificationService);

        assertThrows(IllegalArgumentException.class,
                () -> library.registerUser(mockUser));
    }

    @Test
    void registerUser_notificationServiceNull() {
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn("123456789012");
        when(mockUser.getName()).thenReturn("Alice");
        when(mockUser.getNotificationService()).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> library.registerUser(mockUser));
    }

    @Test
    void registerUser_userAlreadyExists() {
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn("123456789012");
        when(mockUser.getName()).thenReturn("Alice");
        when(mockUser.getNotificationService()).thenReturn(mockNotificationService);

        when(databaseService.getUserById("123456789012"))
                .thenReturn(mock(User.class)); // user exists

        assertThrows(IllegalArgumentException.class,
                () -> library.registerUser(mockUser));
    }

    @Test
    void registerUser_validUser_registers() {
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn("123456789012");
        when(mockUser.getName()).thenReturn("Alice");
        when(mockUser.getNotificationService()).thenReturn(mockNotificationService);

        when(databaseService.getUserById("123456789012"))
                .thenReturn(null); // user does NOT exist

        library.registerUser(mockUser);

        verify(databaseService).registerUser("123456789012", mockUser);
    }

    @Test
    void borrowBook_invalidISBN() {
        assertThrows(IllegalArgumentException.class,
                () -> library.borrowBook("123", "123456789012"));
    }

    @Test
    void borrowBook_bookNotFound() {
        when(databaseService.getBookByISBN("9780306406157")).thenReturn(null);

        assertThrows(BookNotFoundException.class,
                () -> library.borrowBook("9780306406157", "123456789012"));
    }

    @Test
    void borrowBook_userIdInvalid() {
        Book book = mock(Book.class);
        when(databaseService.getBookByISBN("9780306406157")).thenReturn(book);

        assertThrows(IllegalArgumentException.class,
                () -> library.borrowBook("9780306406157", "12")); // too short
    }

    @Test
    void borrowBook_userNotRegistered() {
        Book book = mock(Book.class);
        when(databaseService.getBookByISBN("9780306406157")).thenReturn(book);
        when(databaseService.getUserById("123456789012")).thenReturn(null);

        assertThrows(UserNotRegisteredException.class,
                () -> library.borrowBook("9780306406157", "123456789012"));
    }

    @Test
    void borrowBook_bookAlreadyBorrowed() {
        Book book = mock(Book.class);
        when(databaseService.getBookByISBN("9780306406157")).thenReturn(book);
        when(databaseService.getUserById("123456789012")).thenReturn(mock(User.class));
        when(book.isBorrowed()).thenReturn(true);

        assertThrows(BookAlreadyBorrowedException.class,
                () -> library.borrowBook("9780306406157", "123456789012"));
    }

    @Test
    void borrowBook_validFlow_callsBorrowAndDatabase() {
        Book book = mock(Book.class);

        when(databaseService.getBookByISBN("9780306406157")).thenReturn(book);
        when(databaseService.getUserById("123456789012")).thenReturn(mock(User.class));
        when(book.isBorrowed()).thenReturn(false);

        library.borrowBook("9780306406157", "123456789012");

        verify(book).borrow();
        verify(databaseService).borrowBook("9780306406157", "123456789012");
    }

    @Test
    void returnBook_invalidISBN() {
        assertThrows(IllegalArgumentException.class,
                () -> library.returnBook("123"));  // invalid
    }

    @Test
    void returnBook_bookNotFound() {
        when(databaseService.getBookByISBN("9780306406157")).thenReturn(null);

        assertThrows(BookNotFoundException.class,
                () -> library.returnBook("9780306406157"));
    }

    @Test
    void returnBook_bookNotBorrowed() {
        Book mockBook = mock(Book.class);

        when(databaseService.getBookByISBN("9780306406157")).thenReturn(mockBook);
        when(mockBook.isBorrowed()).thenReturn(false); // not borrowed

        assertThrows(BookNotBorrowedException.class,
                () -> library.returnBook("9780306406157"));
    }

    @Test
    void returnBook_validFlow_callsReturnAndDatabase() {
        Book mockBook = mock(Book.class);

        when(databaseService.getBookByISBN("9780306406157")).thenReturn(mockBook);
        when(mockBook.isBorrowed()).thenReturn(true);

        library.returnBook("9780306406157");

        verify(mockBook).returnBook();
        verify(databaseService).returnBook("9780306406157");
    }

    @Test
    void notifyReviews_invalidISBN() {
        assertThrows(IllegalArgumentException.class,
                () -> library.notifyUserWithBookReviews("123", "123456789012"));
    }

    @Test
    void notifyReviews_invalidUserId() {
        assertThrows(IllegalArgumentException.class,
                () -> library.notifyUserWithBookReviews("9780306406157", "12"));
    }

    @Test
    void notifyReviews_bookNotFound() {
        when(databaseService.getBookByISBN("9780306406157")).thenReturn(null);

        assertThrows(BookNotFoundException.class,
                () -> library.notifyUserWithBookReviews("9780306406157", "123456789012"));
    }

    @Test
    void notifyReviews_userNotFound() {
        when(databaseService.getBookByISBN("9780306406157")).thenReturn(mock(Book.class));
        when(databaseService.getUserById("123456789012")).thenReturn(null);

        assertThrows(UserNotRegisteredException.class,
                () -> library.notifyUserWithBookReviews("9780306406157", "123456789012"));
    }


    @Test
    void notifyReviews_noReviewsFound() throws Exception {
        when(databaseService.getBookByISBN("9780306406157")).thenReturn(mock(Book.class));
        User user = mock(User.class);
        when(databaseService.getUserById("123456789012")).thenReturn(user);

        when(reviewService.getReviewsForBook("9780306406157"))
                .thenReturn(Collections.emptyList());

        assertThrows(NoReviewsFoundException.class,
                () -> library.notifyUserWithBookReviews("9780306406157", "123456789012"));

        verify(reviewService).close();
    }

    @Test
    void notifyReviews_successOnFirstTry() throws Exception {
        Book book = mock(Book.class);
        when(book.getTitle()).thenReturn("Some Title");

        when(databaseService.getBookByISBN("9780306406157")).thenReturn(book);
        User user = mock(User.class);
        when(databaseService.getUserById("123456789012")).thenReturn(user);

        when(reviewService.getReviewsForBook("9780306406157"))
                .thenReturn(Collections.singletonList("Great book!"));


        library.notifyUserWithBookReviews("9780306406157", "123456789012");

        verify(user, times(1)).sendNotification(anyString());
    }

    @Test
    void notifyReviews_succeedsOnFifthAttempt() throws Exception {
        Book book = mock(Book.class);
        when(book.getTitle()).thenReturn("Some Title");
        when(databaseService.getBookByISBN("9780306406157")).thenReturn(book);

        User user = mock(User.class);
        when(databaseService.getUserById("123456789012")).thenReturn(user);

        when(reviewService.getReviewsForBook("9780306406157"))
                .thenReturn(Collections.singletonList("Great book!"));

        doThrow(new NotificationException("x"))
                .doThrow(new NotificationException("x"))
                .doThrow(new NotificationException("x"))
                .doThrow(new NotificationException("x"))
                .doNothing()
                .when(user).sendNotification(anyString());

        library.notifyUserWithBookReviews("9780306406157", "123456789012");

        verify(user, times(5)).sendNotification(anyString());
    }

    @Test
    void notifyReviews_failsAllRetries() throws Exception {
        Book book = mock(Book.class);
        when(book.getTitle()).thenReturn("Some Title");
        when(databaseService.getBookByISBN("9780306406157")).thenReturn(book);

        User user = mock(User.class);
        when(databaseService.getUserById("123456789012")).thenReturn(user);

        when(reviewService.getReviewsForBook("9780306406157"))
                .thenReturn(Collections.singletonList("Great Book!"));

        doThrow(new NotificationException("fail"))
                .when(user).sendNotification(anyString());

        assertThrows(NotificationException.class,
                () -> library.notifyUserWithBookReviews("9780306406157", "123456789012"));

        verify(user, times(5)).sendNotification(anyString());
    }

    @Test
    void registerUser_invalidNameCharacters() {
        User u = mock(User.class);
        when(u.getId()).thenReturn("123456789012");
        when(u.getName()).thenReturn("Alic3!");  // invalid
        when(u.getNotificationService()).thenReturn(mockNotificationService);

        assertThrows(IllegalArgumentException.class,
                () -> library.registerUser(u));
    }


    @Test
    void notifyReviews_multipleReviewsFormatting() throws Exception {
        Book book = mock(Book.class);
        when(book.getTitle()).thenReturn("Some Title");
        when(databaseService.getBookByISBN("9780306406157")).thenReturn(book);

        User user = mock(User.class);
        when(databaseService.getUserById("123456789012")).thenReturn(user);

        when(reviewService.getReviewsForBook("9780306406157"))
                .thenReturn(Arrays.asList("Great!", "Amazing!", "10/10"));

        library.notifyUserWithBookReviews("9780306406157", "123456789012");

        ArgumentCaptor<String> msg = ArgumentCaptor.forClass(String.class);
        verify(user, times(1)).sendNotification(msg.capture());

        String text = msg.getValue();
        assertTrue(text.contains("Great!"));
        assertTrue(text.contains("Amazing!"));
        assertTrue(text.contains("10/10"));
    }

    @Test
    void addBook_isbnContainsNonDigits() {
        Book bad = mock(Book.class);
        when(bad.getISBN()).thenReturn("97803A6406157");  // non-digit
        when(bad.getTitle()).thenReturn("Good");
        when(bad.getAuthor()).thenReturn("John");
        when(bad.isBorrowed()).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> library.addBook(bad));
    }

    @Test
    void registerUser_validNameWithSpaces_registersSuccessfully() {
        User u = mock(User.class);
        when(u.getId()).thenReturn("123456789012");
        when(u.getName()).thenReturn("Alice Smith");
        when(u.getNotificationService()).thenReturn(mockNotificationService);

        when(databaseService.getUserById("123456789012")).thenReturn(null);

        library.registerUser(u);

        verify(databaseService).registerUser("123456789012", u);
    }

    @Test
    void notifyReviews_reviewServiceCalledExactlyOnce() throws Exception {
        Book b = mock(Book.class);
        User u = mock(User.class);

        when(databaseService.getBookByISBN("9780306406157")).thenReturn(b);
        when(databaseService.getUserById("123456789012")).thenReturn(u);

        when(reviewService.getReviewsForBook("9780306406157"))
                .thenReturn(Collections.singletonList("OK"));

        library.notifyUserWithBookReviews("9780306406157", "123456789012");

        verify(reviewService, times(1)).getReviewsForBook("9780306406157");
    }

    @Test
    void borrowBook_nullBorrowedFlag_treatedAsBorrowed() {
        Book b = mock(Book.class);
        when(databaseService.getBookByISBN("9780306406157")).thenReturn(b);
        when(databaseService.getUserById("123456789012")).thenReturn(mock(User.class));
        when(b.isBorrowed()).thenReturn(null);

        assertThrows(BookAlreadyBorrowedException.class,
                () -> library.borrowBook("9780306406157", "123456789012"));
    }


    





























































}