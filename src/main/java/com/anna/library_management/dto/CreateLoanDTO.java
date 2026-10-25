package com.anna.library_management.dto;

import jakarta.validation.constraints.*;

public class CreateLoanDTO {

    @NotNull(message = "ID книги обязателен")
    private Long bookId;

    @NotNull(message = "ID пользователя обязателен")
    private Long userId;

    @Positive(message = "Срок выдачи должен быть положительным")
    private Integer days;

    public CreateLoanDTO() {}

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Integer getDays() { return days; }
    public void setDays(Integer days) { this.days = days; }
}
