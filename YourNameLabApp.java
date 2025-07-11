import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;

public class YourNameLabApp extends Application {

    // DB connection details - change these
    private final String DB_URL = "jdbc:mysql://localhost:3306/your_database";
    private final String DB_USER = "root";
    private final String DB_PASSWORD = "";

    private TableView<Employee> tableView;
    private TextField tfId, tfName, tfEmail;

    private ObservableList<Employee> data;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        // Labels + Input Fields
        tfId = new TextField();
        tfId.setPromptText("ID (for Update/Delete)");

        tfName = new TextField();
        tfName.setPromptText("Name");

        tfEmail = new TextField();
        tfEmail.setPromptText("Email");

        // Buttons
        Button btnView = new Button("View Data");
        Button btnInsert = new Button("Insert");
        Button btnUpdate = new Button("Update");
        Button btnDelete = new Button("Delete");

        btnView.setOnAction(e -> loadData());
        btnInsert.setOnAction(e -> insertData());
        btnUpdate.setOnAction(e -> updateData());
        btnDelete.setOnAction(e -> deleteData());

        // TableView Setup
        tableView = new TableView<>();
        tableView.setPrefWidth(600);

        TableColumn<Employee, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Employee, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Employee, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        tableView.getColumns().addAll(colId, colName, colEmail);

        // Layout
        HBox inputBox = new HBox(10, tfId, tfName, tfEmail);
        HBox buttonBox = new HBox(10, btnView, btnInsert, btnUpdate, btnDelete);
        VBox root = new VBox(10, inputBox, buttonBox, tableView);
        root.setPadding(new Insets(15));

        Scene scene = new Scene(root, 650, 400);

        primaryStage.setTitle("YourName - StudentID - Date - JavaFX MySQL CRUD");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Initialize data list
        data = FXCollections.observableArrayList();
    }

    private Connection connectDB() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private void loadData() {
        data.clear();
        try (Connection conn = connectDB()) {
            String query = "SELECT * FROM employees";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                data.add(new Employee(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email")
                ));
            }
            tableView.setItems(data);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error Loading Data", e.getMessage());
        }
    }

    private void insertData() {
        String name = tfName.getText();
        String email = tfEmail.getText();

        if (name.isEmpty() || email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Name and Email are required.");
            return;
        }

        try (Connection conn = connectDB()) {
            String insertSQL = "INSERT INTO employees(name, email) VALUES (?, ?)";
            PreparedStatement ps = conn.prepareStatement(insertSQL);
            ps.setString(1, name);
            ps.setString(2, email);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Record inserted.");
                loadData();
                clearFields();
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Insert Error", e.getMessage());
        }
    }

    private void updateData() {
        String idStr = tfId.getText();
        String name = tfName.getText();
        String email = tfEmail.getText();

        if (idStr.isEmpty() || name.isEmpty() || email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "ID, Name, and Email are required.");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            try (Connection conn = connectDB()) {
                String updateSQL = "UPDATE employees SET name=?, email=? WHERE id=?";
                PreparedStatement ps = conn.prepareStatement(updateSQL);
                ps.setString(1, name);
                ps.setString(2, email);
                ps.setInt(3, id);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Record updated.");
                    loadData();
                    clearFields();
                } else {
                    showAlert(Alert.AlertType.WARNING, "Not Found", "No record with ID = " + id);
                }
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "ID must be an integer.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Update Error", e.getMessage());
        }
    }

    private void deleteData() {
        String idStr = tfId.getText();
        if (idStr.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "ID is required for delete.");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            try (Connection conn = connectDB()) {
                String deleteSQL = "DELETE FROM employees WHERE id=?";
                PreparedStatement ps = conn.prepareStatement(deleteSQL);
                ps.setInt(1, id);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Record deleted.");
                    loadData();
                    clearFields();
                } else {
                    showAlert(Alert.AlertType.WARNING, "Not Found", "No record with ID = " + id);
                }
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "ID must be an integer.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Delete Error", e.getMessage());
        }
    }

    private void clearFields() {
        tfId.clear();
        tfName.clear();
        tfEmail.clear();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Employee Data Model class
    public static class Employee {
        private final IntegerProperty id;
        private final StringProperty name;
        private final StringProperty email;

        public Employee(int id, String name, String email) {
            this.id = new SimpleIntegerProperty(id);
            this.name = new SimpleStringProperty(name);
            this.email = new SimpleStringProperty(email);
        }

        public int getId() { return id.get(); }
        public void setId(int id) { this.id.set(id); }
        public IntegerProperty idProperty() { return id; }

        public String getName() { return name.get(); }
        public void setName(String name) { this.name.set(name); }
        public StringProperty nameProperty() { return name; }

        public String getEmail() { return email.get(); }
        public void setEmail(String email) { this.email.set(email); }
        public StringProperty emailProperty() { return email; }
    }
}
