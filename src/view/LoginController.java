package view;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import model.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML
    private TextField txtNama;

    @FXML
    private TextField txtNoHP;

    @FXML
    private Button btnMasuk;

    @FXML
    public void initialize() {
        btnMasuk.setOnAction(event -> login());
    }

    private void login() {
        String nama = txtNama.getText().trim();
        String noHp = txtNoHP.getText().trim();

        if (nama.isEmpty() || noHp.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Semua field harus diisi.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {

            // Cek apakah user adalah karyawan
            String checkKaryawanSQL = "SELECT role FROM logkaryawan WHERE no_hp = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkKaryawanSQL);
            checkStmt.setString(1, noHp);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                switch (role.toLowerCase()) {
                    case "manajer":
                        showAlert(Alert.AlertType.INFORMATION, "Login berhasil sebagai Manajer");
                        // TODO: Arahkan ke halaman manajer
                        break;
                    case "staf_dapur":
                        showAlert(Alert.AlertType.INFORMATION, "Login berhasil sebagai Staf Dapur");
                        // TODO: Arahkan ke halaman staf dapur
                        break;
                    default:
                        showAlert(Alert.AlertType.WARNING, "Role tidak dikenali.");
                }
            } else {
                // Jika bukan karyawan, anggap sebagai pelanggan
                String insertSQL = "INSERT INTO logpelanggan (nama, no_hp) VALUES (?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertSQL);
                insertStmt.setString(1, nama);
                insertStmt.setString(2, noHp);
                insertStmt.executeUpdate();

                showAlert(Alert.AlertType.INFORMATION, "Login berhasil sebagai Pelanggan");
                // TODO: Arahkan ke halaman pelanggan
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Terjadi kesalahan: " + e.getMessage());
        }
    }


    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Informasi");
        alert.setContentText(message);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
