package view;

import controller.MenuPelangganController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.DatabaseConnection;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class LoginController implements javafx.fxml.Initializable {

    @FXML
    private TextField txtNama;

    @FXML
    private TextField txtNoHP;

    @FXML
    private Button btnMasuk;

    @FXML
    private VBox formContainer;

    @FXML
    private StackPane rootPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rootPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            double scale = Math.min(newVal.doubleValue() / 400.0, 1.2);
            formContainer.setScaleX(scale);
        });

        rootPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            double scale = Math.min(newVal.doubleValue() / 600.0, 1.2);
            formContainer.setScaleY(scale);
        });

        btnMasuk.setOnAction(event -> login());
    }

    private void login() {
        String nama = txtNama.getText().trim();
        String noHp = txtNoHP.getText().trim();

        if (nama.isEmpty() && noHp.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Nama dan Nomor HP wajib diisi.");
            return;
        } else if (nama.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Nama wajib diisi.");
            return;
        } else if (noHp.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Nomor HP wajib diisi.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Cek apakah nomor HP milik karyawan
            String checkKaryawanSQL = "SELECT role FROM logkaryawan WHERE no_hp = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkKaryawanSQL);
            checkStmt.setString(1, noHp);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                switch (role.toLowerCase()) {
                    case "manajer":
                        showAlert(Alert.AlertType.INFORMATION, "Login berhasil sebagai Manajer");
                        switchScene("ManagerDashboard.fxml", "Dashboard Manajer");
                        break;
                    case "staf_dapur":
                        showAlert(Alert.AlertType.INFORMATION, "Login berhasil sebagai Staf Dapur");
                        switchScene("StafDashboard.fxml", "Dashboard Staf Dapur");
                        break;
                    default:
                        showAlert(Alert.AlertType.WARNING, "Role tidak dikenali.");
                        break;
                }
            } else {
                // Cek apakah pelanggan sudah terdaftar
                String checkPelangganSQL = "SELECT id FROM logpelanggan WHERE nama = ? AND no_hp = ?";
                PreparedStatement cekPelangganStmt = conn.prepareStatement(checkPelangganSQL);
                cekPelangganStmt.setString(1, nama);
                cekPelangganStmt.setString(2, noHp);
                ResultSet rsPelanggan = cekPelangganStmt.executeQuery();

                if (rsPelanggan.next()) {
                    int idPelanggan = rsPelanggan.getInt("id");
                    showAlert(Alert.AlertType.INFORMATION, "Selamat datang kembali, " + nama + "!");
                    switchToMenuPelanggan(idPelanggan);
                } else {
                    // Insert pelanggan baru
                    String insertSQL = "INSERT INTO logpelanggan (nama, no_hp) VALUES (?, ?)";
                    PreparedStatement insertStmt = conn.prepareStatement(insertSQL);
                    insertStmt.setString(1, nama);
                    insertStmt.setString(2, noHp);
                    insertStmt.executeUpdate();

                    // Ambil ID yang baru saja dibuat
                    String getIdSQL = "SELECT id FROM logpelanggan WHERE nama = ? AND no_hp = ?";
                    PreparedStatement getIdStmt = conn.prepareStatement(getIdSQL);
                    getIdStmt.setString(1, nama);
                    getIdStmt.setString(2, noHp);
                    ResultSet getIdResult = getIdStmt.executeQuery();

                    if (getIdResult.next()) {
                        int idPelangganBaru = getIdResult.getInt("id");
                        showAlert(Alert.AlertType.INFORMATION, "Selamat datang, " + nama + " di Ramen House!");
                        switchToMenuPelanggan(idPelangganBaru);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Terjadi kesalahan: " + e.getMessage());
        }
    }

    // Kirim ID pelanggan ke controller MenuPelanggan
    private void switchToMenuPelanggan(int idPelanggan) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MenuPelanggan.fxml"));
            Parent root = loader.load();

            MenuPelangganController controller = loader.getController();
            controller.setIdPelanggan(idPelanggan);

            Stage stage = (Stage) btnMasuk.getScene().getWindow();
            stage.setTitle("Menu Ramen House");
            stage.setScene(new Scene(root, 400, 600));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Gagal membuka halaman Menu: " + e.getMessage());
        }
    }

    private void switchScene(String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/" + fxmlFile));
            Parent root = loader.load();
            Stage stage = (Stage) btnMasuk.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(new Scene(root, 400, 600));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Gagal membuka halaman: " + e.getMessage());
        }
    }


    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Informasi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
