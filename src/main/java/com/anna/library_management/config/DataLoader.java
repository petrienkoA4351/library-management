package com.anna.library_management.config;

import com.anna.library_management.entity.*;
import com.anna.library_management.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Тестовые данные загружаются только в пустую базу,
        // иначе при повторном запуске нарушится уникальность name и email
        if (categoryRepository.count() == 0) {
            loadData();
            System.out.println("Тестовые данные загружены успешно!");
        } else {
            System.out.println("Тестовые данные уже есть в базе, загрузка пропущена.");
        }
        encodeLegacyPasswords();
        printDemo();
    }

    private void loadData() {
        // Создание категорий
        Category fiction = new Category("Художественная литература",
                "Романы, повести, рассказы");
        Category science = new Category("Научная литература", "Учебники, научные труды");
        Category history = new Category("История", "Исторические книги");

        categoryRepository.save(fiction);
        categoryRepository.save(science);
        categoryRepository.save(history);

        // Создание авторов
        Author tolstoy = new Author("Лев", "Толстой");
        tolstoy.setBirthDate(LocalDate.of(1828, 9, 9));
        tolstoy.setBiography("Русский писатель и мыслитель");

        Author pushkin = new Author("Александр", "Пушкин");
        pushkin.setBirthDate(LocalDate.of(1799, 6, 6));
        pushkin.setBiography("Русский поэт, драматург и прозаик");

        authorRepository.save(tolstoy);
        authorRepository.save(pushkin);

        // Создание книг
        Book warAndPeace = new Book("Война и мир", "978-5-17-123456-7",
                LocalDate.of(1869, 1, 1), 5);
        warAndPeace.setAuthor(tolstoy);
        warAndPeace.setCategory(fiction);

        Book eugeneOnegin = new Book("Евгений Онегин", "978-5-17-123457-4",
                LocalDate.of(1833, 1, 1), 3);
        eugeneOnegin.setAuthor(pushkin);
        eugeneOnegin.setCategory(fiction);

        bookRepository.save(warAndPeace);
        bookRepository.save(eugeneOnegin);

        // Создание пользователей
        User admin = new User();
        admin.setEmail("admin@library.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setFirstName("Администратор");
        admin.setLastName("Системы");
        admin.setCreatedAt(LocalDateTime.now());

        User librarian = new User();
        librarian.setEmail("librarian@library.com");
        librarian.setPassword(passwordEncoder.encode("librarian123"));
        librarian.setFirstName("Библиотекарь");
        librarian.setLastName("Главный");
        librarian.setCreatedAt(LocalDateTime.now());

        userRepository.save(admin);
        userRepository.save(librarian);

        // Просроченная выдача для проверки запросов по займам
        Loan overdueLoan = new Loan(warAndPeace, admin,
                LocalDateTime.now().minusDays(30),
                LocalDateTime.now().minusDays(16),
                LoanStatus.ACTIVE);
        loanRepository.save(overdueLoan);
    }

    // Шифрование паролей, сохранённых ранее в открытом виде (BCrypt-хеши начинаются с "$2")
    private void encodeLegacyPasswords() {
        int count = 0;
        for (User user : userRepository.findAll()) {
            String password = user.getPassword();
            if (password != null && !password.startsWith("$2")) {
                user.setPassword(passwordEncoder.encode(password));
                userRepository.save(user);
                count++;
            }
        }
        if (count > 0) {
            System.out.println("Пароли пользователей зашифрованы (BCrypt): " + count);
        }
    }

    // Демонстрация работы репозиториев (результат выводится в консоль)
    private void printDemo() {
        System.out.println("=== Проверка репозиториев ===");
        System.out.println("Категорий: " + categoryRepository.count());
        System.out.println("Авторов: " + authorRepository.count());
        System.out.println("Книг: " + bookRepository.count());
        System.out.println("Пользователей: " + userRepository.count());
        System.out.println("Выдач: " + loanRepository.count());
        System.out.println("Поиск книг по названию 'Война': "
                + titles(bookRepository.findByTitleContainingIgnoreCase("Война")));
        System.out.println("Поиск книги по ISBN: "
                + bookRepository.findByIsbn("978-5-17-123457-4")
                .map(Book::getTitle).orElse("не найдена"));
        System.out.println("Доступные книги: "
                + titles(bookRepository.findByAvailableCopiesGreaterThan(0)));
        System.out.println("Книги с просроченным возвратом: "
                + titles(bookRepository.findOverdueBooks()));
        System.out.println("Пользователь существует (admin@library.com): "
                + userRepository.existsByEmail("admin@library.com"));
        System.out.println("Просроченных выдач: "
                + loanRepository.findOverdueLoans(LocalDateTime.now()).size());
        System.out.println("Для входа в API: admin@library.com / admin123");
    }

    private List<String> titles(List<Book> books) {
        return books.stream().map(Book::getTitle).toList();
    }
}
