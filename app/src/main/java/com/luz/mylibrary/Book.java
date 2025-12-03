package com.luz.mylibrary;

public class Book {
    private String id;
    private String title;
    private String author;
    private String year;
    private String coverUrl;
    private String bookLink;
    private String generos;
    private String isbn;
    private String paginas;

    // 🔹 CONSTRUCTOR VACÍO
    public Book() {
        this.id = "";
        this.title = "";
        this.author = "";
        this.year = "";
        this.coverUrl = "";
        this.bookLink = "";
        this.generos = "";
        this.isbn = "";
        this.paginas = "";
    }

    // 🔹 CONSTRUCTOR CON 3 PARÁMETROS
    public Book(String id, String title, String author) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.year = "";
        this.coverUrl = "";
        this.bookLink = "";
        this.generos = "";
        this.isbn = "";
        this.paginas = "";
    }

    // 🔹 CONSTRUCTOR CON 4 PARÁMETROS
    public Book(String id, String title, String author, String year) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.year = year;
        this.coverUrl = "";
        this.bookLink = "";
        this.generos = "";
        this.isbn = "";
        this.paginas = "";
    }

    // 🔹 GETTERS
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getYear() {
        return year;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public String getBookLink() {
        return bookLink;
    }

    public String getGeneros() {
        return generos;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getPaginas() {
        return paginas;
    }

    // 🔹 SETTERS
    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public void setBookLink(String bookLink) {
        this.bookLink = bookLink;
    }

    public void setGeneros(String generos) {
        this.generos = generos;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public void setPaginas(String paginas) {
        this.paginas = paginas;
    }

    // 🔹 MÉTODO toString
    @Override
    public String toString() {
        if (year != null && !year.isEmpty() && !year.equals("Año desconocido")) {
            return title + " - " + author + " (" + year + ")";
        }
        return title + " - " + author;
    }
}
