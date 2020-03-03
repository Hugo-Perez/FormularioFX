package bookshop.db;

import bookshop.pojo.Book;

import java.sql.*;

public class DBConnector {

    private final String URL = "jdbc:mysql://localhost/librosHPC";
    private Connection connection;

    private Connection connectToDB() throws SQLException {
        connection = DriverManager.getConnection(URL, "root", "");
        return connection;
    }

    public void addBook(Book generatedBook) throws SQLException {
        String query = "INSERT INTO libro(titulo, autor, genero, fecha, paginas, ISBN, sinopsis) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement sqlInput = connectToDB().prepareStatement(query);
        sqlInput.setString(1, generatedBook.getTitle());
        sqlInput.setString(2, generatedBook.getAuthor());
        sqlInput.setString(3, generatedBook.getGenre());
        sqlInput.setDate(4, Date.valueOf(generatedBook.getDate()));
        sqlInput.setInt(5, generatedBook.getPages());
        sqlInput.setString(6, generatedBook.getISBN());
        sqlInput.setString(7, generatedBook.getSummary());
        sqlInput.execute();
    }

    public boolean updateBook(Book generatedBook) throws SQLException {
        String query = "UPDATE libro SET titulo = ?, autor = ?, genero = ?, fecha = ?, paginas = ?, sinopsis = ?\n" +
                "WHERE ISBN = ?";
        PreparedStatement sqlInput = connectToDB().prepareStatement(query);
        sqlInput.setString(1, generatedBook.getTitle());
        sqlInput.setString(2, generatedBook.getAuthor());
        sqlInput.setString(3, generatedBook.getGenre());
        sqlInput.setDate(4, Date.valueOf(generatedBook.getDate()));
        sqlInput.setInt(5, generatedBook.getPages());
        sqlInput.setString(6, generatedBook.getSummary());
        sqlInput.setString(7, generatedBook.getISBN());
        int actualizado = sqlInput.executeUpdate();
        if (actualizado > 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean deleteBook(String ISBN) throws SQLException {
        PreparedStatement sqlDelete = connectToDB().prepareStatement("DELETE FROM libro\n WHERE ISBN = ?");
        sqlDelete.setString(1, ISBN);
        int deleted = sqlDelete.executeUpdate();
        if (deleted > 0) {
            return true;
        } else {
            return false;
        }
    }

    public void executeInitialSQL() throws SQLException {
        connection = DriverManager.getConnection("jdbc:mysql://localhost/", "root", "");
        Statement initialSQL = connection.createStatement();
        initialSQL.execute("CREATE SCHEMA IF NOT EXISTS librosHPC;");
        initialSQL.execute("USE librosHPC;");
        initialSQL.execute("CREATE TABLE IF NOT EXISTS libro (" +
                "id INT PRIMARY KEY AUTO_INCREMENT," +
                "titulo VARCHAR(100) NOT NULL," +
                "autor VARCHAR(100) NOT NULL," +
                "genero VARCHAR(50) NOT NULL," +
                "fecha DATE NOT NULL," +
                "paginas INT NOT NULL," +
                "ISBN VARCHAR(13) UNIQUE NOT NULL," +
                "sinopsis TEXT);");
    }

    public Book loadBook(String ISBN) throws SQLException {
        PreparedStatement sqlLoad = connectToDB().prepareStatement("SELECT * FROM libro WHERE ISBN = ?");
        sqlLoad.setString(1, ISBN);
        ResultSet result = sqlLoad.executeQuery();
        if (result.next()) {
            return new Book(result.getString(2), result.getString(3), result.getString(4),
                    result.getString(7),result.getString(8), result.getDate(5).toLocalDate(), result.getInt(6));
        } else {
            return null;
        }
    }

    public ResultSet readBooks() throws SQLException {
        Statement sqlSelect = connectToDB().createStatement();
        return sqlSelect.executeQuery("SELECT * FROM libro");
    }

    public ResultSet readBooksByGenre() throws SQLException {
        Statement sqlGenres = connectToDB().createStatement();
        return sqlGenres.executeQuery("SELECT COUNT(*) AS cantidad, genero FROM libro GROUP BY genero");
    }
}
