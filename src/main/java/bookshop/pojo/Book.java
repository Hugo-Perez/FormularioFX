package bookshop.pojo;

import java.time.LocalDate;

public class Book {

    private String title, author, genre, ISBN, summary;
    private LocalDate date;
    private int pages;
    private String dateToString, pagesToString;

    public Book(String title, String autor, String genero, String ISBN, String sinopsis, LocalDate fecha, int paginas) {
        this.title = title;
        this.author = autor;
        this.genre = genero;
        this.ISBN = ISBN;
        this.summary = sinopsis;
        this.date = fecha;
        this.pages = paginas;
        this.dateToString = fecha.toString();
        this.pagesToString = String.valueOf(paginas);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getISBN() {
        return ISBN;
    }

    public void setISBN(String ISBN) {
        this.ISBN = ISBN;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public String getDateToString() {
        return dateToString;
    }

    public void setDateToString(String dateToString) {
        this.dateToString = dateToString;
    }

    public String getPagesToString() {
        return pagesToString;
    }

    public void setPagesToString(String pagesToString) {
        this.pagesToString = pagesToString;
    }

    @Override
    public String toString() {
        return "Book{" +
                "title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", genre='" + genre + '\'' +
                ", ISBN='" + ISBN + '\'' +
                ", summary='" + summary + '\'' +
                ", date=" + date +
                ", pages=" + pages +
                ", dateToString='" + dateToString + '\'' +
                ", pagesToString='" + pagesToString + '\'' +
                '}';
    }
}
