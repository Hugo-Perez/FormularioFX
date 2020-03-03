package bookshop.controller;

import bookshop.db.DBConnector;
import bookshop.pojo.Book;
import com.itextpdf.text.DocumentException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;



public class Controller {
    @FXML
    public TextField titleField, authorField, pagesField, ISBNField;
    public ComboBox genreSelector;
    public DatePicker dateSelector;
    public TextArea summaryField;
    public TableView dataTable;

    ObservableList<String> genres;

    bookshop.db.DBConnector DBConnector = new DBConnector();

    public void initialize() {
        //TODO: Buscar mejor forma para manejar/añadir géneros
        genres = FXCollections.observableArrayList(
                        "Drama",
                        "Romance",
                        "Misterio",
                        "Ciencia Ficcion",
                        "Fantasía",
                        "Histórico",
                        "No ficción",
                        "Filosofía",
                        "Estudio");
        genreSelector.setItems(genres);

        //Initial SQL and Filling table with data
        try {
            DBConnector.executeInitialSQL();
            fillTable();
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "ERROR SQL: Comprueba que el servidor SQL esta funcionando!").showAndWait();
            System.exit(0);
            e.printStackTrace();
        }

        //Adding context menu to table elements
        MenuItem menuSearch = new MenuItem("Buscar Libro");
        menuSearch.setOnAction((ActionEvent event) -> {
            Book selectedBook = (Book)dataTable.getSelectionModel().getSelectedItem();
            searchBook(selectedBook);
        });

        MenuItem menuLoad = new MenuItem("Cargar Libro");
        menuLoad.setOnAction((ActionEvent event) -> {
            Book selectedBook = (Book)dataTable.getSelectionModel().getSelectedItem();
            loadFields(selectedBook);
        });

        MenuItem menuPdf = new MenuItem("Generar PDF");
        menuPdf.setOnAction((ActionEvent event) -> {
            Book selectedBook = (Book)dataTable.getSelectionModel().getSelectedItem();
            try {
                generatePDF(selectedBook);
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Error en el guardado");
            }
        });

        ContextMenu menu = new ContextMenu();
        menu.getItems().addAll(menuSearch, menuLoad, menuPdf);
        dataTable.setContextMenu(menu);
    }

    public void addBook() {
        //comprobarCampos() devuelve true si no hay nada vacío
        if (checkFields()) {
            try {
                Book generatedBook = generateBook();
                if (generatedBook == null) {
                    return;
                } //El mensaje de error salta desde generarLibro()
                DBConnector.addBook(generatedBook);
                new Alert(Alert.AlertType.CONFIRMATION, "Libro introducido correctamente!").showAndWait();
                fillTable();
            } catch (MySQLIntegrityConstraintViolationException ex) {
                new Alert(Alert.AlertType.WARNING, "AVISO SQL: Ya existe un libro con esa ISBN!").showAndWait();
                ex.printStackTrace();
            }   catch (SQLException e) {
                new Alert(Alert.AlertType.ERROR, "ERROR SQL: Comprueba que todos los campos están correctos!").showAndWait();
                e.printStackTrace();
            }
        } else {
            //Enviamos mensaje al usuario
            new Alert(Alert.AlertType.WARNING, "Comprueba que todos los campos están cubiertos!").showAndWait();
        }
    }

    public void deleteBook() {
        TextInputDialog ISBNDialog = new TextInputDialog();
        ISBNDialog.setTitle("Introduce ISBN");
        ISBNDialog.setHeaderText("Introduce el ISBN del libro a eliminar:");
        ISBNDialog.setContentText("ISBN:");
        Optional<String> dialogInput = ISBNDialog.showAndWait();
        if (dialogInput.isPresent()) {
            try {
                boolean isDeleted = DBConnector.deleteBook(dialogInput.get());
                if (isDeleted) {
                    new Alert(Alert.AlertType.CONFIRMATION, "Libro eliminado correctamente!").showAndWait();
                } else {
                    new Alert(Alert.AlertType.WARNING, "No se ha encontrado el libro con el ISBN especificado!").showAndWait();
                }
                fillTable();
            } catch (SQLException e) {
                new Alert(Alert.AlertType.ERROR, "ERROR SQL: Comprueba que todos los campos están correctos!").showAndWait();
                e.printStackTrace();
            }
        }
    }

    public void updateBook() {
        if (checkFields()) {
            try {
                Book generatedBook = generateBook();
                if (generatedBook == null) { return; } //El mensaje de error salta desde generarLibro()
                boolean isUpdated = DBConnector.updateBook(generatedBook);
                if (isUpdated) {
                    new Alert(Alert.AlertType.CONFIRMATION, "Libro actualizado correctamente!").showAndWait();
                } else {
                    new Alert(Alert.AlertType.WARNING, "Libro NO encontrado, comprueba ISBN!").showAndWait();
                }
                fillTable();
            } catch (SQLException e) {
                new Alert(Alert.AlertType.ERROR, "ERROR SQL: Comprueba que todos los campos están correctos!").showAndWait();
                e.printStackTrace();
            }
        } else {
            //Enviamos mensaje al usuario
            new Alert(Alert.AlertType.WARNING, "Comprueba que todos los campos están cubiertos!").showAndWait();
        }
    }

    public void clearFields() {
        //Vaciamos los campos
        titleField.setText("");
        authorField.setText("");
        pagesField.setText("");
        ISBNField.setText("");
        summaryField.clear();
        genreSelector.getSelectionModel().clearSelection();
        dateSelector.getEditor().clear();
        dateSelector.setValue(null);
        fillTable();
    }

    private boolean checkFields() {
        if (titleField.getText().equals("")) {
            return false;
        } if (authorField.getText().equals("")) {
            return false;
        } if (pagesField.getText().equals("")) {
            return false;
        } if (ISBNField.getText().equals("")) {
            return false;
        } if (summaryField.getText().equals("")) {
            return false;
        } if (genreSelector.getSelectionModel().isEmpty()) {
            return false;
        } if (dateSelector.getValue() == null){
            return false;
        }
        return true;
    }

    private Book generateBook() {
        String title = titleField.getText(), author = authorField.getText(), isbn = ISBNField.getText(), summary = summaryField.getText().trim();
        String genre = (String) genreSelector.getSelectionModel().getSelectedItem();
        LocalDate date = dateSelector.getValue();
        int pages;
        try {
            pages = Integer.parseInt(pagesField.getText());
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.WARNING, "Escribe solo números en la cantidad de páginas!").showAndWait();
            return null;
        }
        return new Book(title, author, genre, isbn, summary, date, pages);
    }

    public void loadBook() {
        TextInputDialog ISBNDialog = new TextInputDialog();
        ISBNDialog.setTitle("Introduce ISBN");
        ISBNDialog.setHeaderText("Introduce el ISBN del libro a cargar:");
        ISBNDialog.setContentText("ISBN:");
        Optional<String> dialogInput = ISBNDialog.showAndWait();
        if (dialogInput.isPresent()) {
            try {
                Book loadedBook = DBConnector.loadBook(dialogInput.get());
                if (loadedBook != null) {
                    loadFields(loadedBook);
                    new Alert(Alert.AlertType.CONFIRMATION, "Libro cargado correctamente!").showAndWait();
                } else {
                    new Alert(Alert.AlertType.WARNING, "No se ha encontrado el libro con el ISBN especificado!").showAndWait();
                }
            } catch (SQLException e) {
                new Alert(Alert.AlertType.ERROR, "ERROR SQL: Comprueba que todos los campos están correctos!").showAndWait();
                e.printStackTrace();
            }
        }
    }

    private void loadFields(Book loadedBook) {
        titleField.setText(loadedBook.getTitle());
        authorField.setText(loadedBook.getAuthor());
        genreSelector.getSelectionModel().select(loadedBook.getGenre());
        dateSelector.setValue(loadedBook.getDate());
        pagesField.setText("" + loadedBook.getPages());
        ISBNField.setText(loadedBook.getISBN());
        summaryField.setText(loadedBook.getSummary());
    }

    public void fillTable() {
        try {
            ResultSet resultSet = DBConnector.readBooks();
            ObservableList<Book> tempList = FXCollections.observableArrayList();

            while (resultSet.next()) {
                Book row = new Book(resultSet.getString(2), resultSet.getString(3), resultSet.getString(4),
                        resultSet.getString(7), resultSet.getString(8), resultSet.getDate(5).toLocalDate(),
                        resultSet.getInt(6));
                tempList.add(row);
            }

            dataTable.setItems(tempList);
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "ERROR SQL: Comprueba que el servidor esta funcionando!").showAndWait();
        }
    }

    public void showGraphics(ActionEvent actionEvent) {
        Parent sub = null;
        try {
            sub = FXMLLoader.load(getClass().getClassLoader().getResource("graph.fxml"));
            Stage subStage = new Stage();
            subStage.setScene(new Scene(sub));
            subStage.setTitle("Gráficas");
            subStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void searchBook(Book book) {
        Stage subStage = new Stage();
        WebView browser = new WebView();
        WebEngine webEngine = browser.getEngine();
        String url = "https://www.google.com/search?q=" + book.getTitle().replace(' ', '+');
        webEngine.load(url);
        browser.autosize();
        subStage.setScene(new Scene(browser));
        subStage.setTitle("Navegador");
        subStage.setWidth(1280);
        subStage.setHeight(720);
        subStage.show();
    }

    public void generatePDF(Book book) throws IOException, DocumentException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Elige ruta para guardar");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialFileName(book.getTitle());
        File fileToSave = fileChooser.showSaveDialog(null);

        if (fileToSave != null) {
            File htmlTemplateFile = new File("src/main/resources/html/HtmlPlaceHolder.html");
            String htmlString = new String(Files.readAllBytes(htmlTemplateFile.toPath()), "UTF-8");
            htmlString = htmlString.replaceAll("BOOKTITLE", book.getTitle());
            htmlString = htmlString.replace("BOOKAUTHOR", book.getAuthor());
            htmlString = htmlString.replace("BOOKSUMMARY", book.getSummary());
            htmlString = htmlString.replace("BOOKGENRE", book.getGenre());
            htmlString = htmlString.replace("BOOKDATE", book.getDateToString());
            htmlString = htmlString.replace("BOOKPAGES", book.getPagesToString());
            htmlString = htmlString.replace("BOOKISBN", book.getISBN());

            File tempHtml = new File("src/main/resources/html/temp.html");
            BufferedWriter writer = Files.newBufferedWriter(tempHtml.toPath());
            writer.flush();
            writer.write(htmlString);
            writer.close();

            OutputStream os = new FileOutputStream(fileToSave);
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocument(tempHtml);
            renderer.layout();
            renderer.createPDF(os);

            os.close();
        }
    }

    public void menuGeneratePDF() {
        TextInputDialog ISBNDialog = new TextInputDialog();
        ISBNDialog.setTitle("Introduce ISBN");
        ISBNDialog.setHeaderText("Introduce el ISBN del libro a exportar:");
        ISBNDialog.setContentText("ISBN:");
        Optional<String> dialogInput = ISBNDialog.showAndWait();
        if (dialogInput.isPresent()) {
            try {
                Book loadedBook = DBConnector.loadBook(dialogInput.get());
                if (loadedBook != null) {
                    generatePDF(loadedBook);
                } else {
                    new Alert(Alert.AlertType.WARNING, "No se ha encontrado el libro con el ISBN especificado!").showAndWait();
                }
            } catch (SQLException e) {
                new Alert(Alert.AlertType.ERROR, "ERROR SQL: Comprueba que todos los campos están correctos!").showAndWait();
                e.printStackTrace();
            } catch (IOException | DocumentException ex) {
                new Alert(Alert.AlertType.ERROR, "Error a la hora de generar pdf, comprueba la ruta").show();
            }
        }
    }
}
