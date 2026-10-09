
package com.library.model;

public class Book {
    private int id;
    private String title;
    private String author;
    private String isbn;
    private int totalCopies;
    private int availableCopies;

    public Book(int id, String title, String author, String isbn,
                int totalCopies, int availableCopies) {
        if (totalCopies < 0 || availableCopies < 0
                || availableCopies > totalCopies) {
            throw new IllegalArgumentException("Invalid copy counts");
        }

        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
    public int getTotalCopies() { return totalCopies; }
    public int getAvailableCopies() { return availableCopies; }

    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public void setTotalCopies(int totalCopies) {
        if (totalCopies < availableCopies || totalCopies < 0) {
            throw new IllegalArgumentException("Invalid total copies");
        }
        this.totalCopies = totalCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        if (availableCopies < 0 || availableCopies > totalCopies) {
            throw new IllegalArgumentException("Invalid available copies");
        }
        this.availableCopies = availableCopies;
    }
}
