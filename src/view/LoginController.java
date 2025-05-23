package view;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import model.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    public ImageView logoImage;
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
        // Ambil input dari field nama dan nomor HP
        String nama = txtNama.getText().trim();
        String noHp = txtNoHP.getText().trim();

        // Validasi input: jika kedua field kosong
        if (nama.isEmpty() && noHp.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Nama dan Nomor HP harus diisi.");
            return;
        }
        // Validasi jika hanya nama yang kosong
        else if (nama.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Nama harus diisi.");
            return;
        }
        // Validasi jika hanya nomor HP yang kosong
        else if (noHp.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Nomor HP harus diisi.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {

            // Cek apakah nomor HP tersebut terdaftar sebagai karyawan
            String checkKaryawanSQL = "SELECT role FROM logkaryawan WHERE no_hp = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkKaryawanSQL);
            checkStmt.setString(1, noHp);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // Jika ditemukan, ambil peran karyawan (role)
                String role = rs.getString("role");

                switch (role.toLowerCase()) {
                    case "manajer":
                        // Jika role adalah manajer, tampilkan notifikasi
                        showAlert(Alert.AlertType.INFORMATION, "Login berhasil sebagai Manajer");
                        // TODO: Arahkan ke halaman manajer
                        break;

                    case "staf_dapur":
                        // Jika role adalah staf dapur, tampilkan notifikasi
                        showAlert(Alert.AlertType.INFORMATION, "Login berhasil sebagai Staf Dapur");
                        // TODO: Arahkan ke halaman staf dapur
                        break;

                    default:
                        // Role tidak dikenali
                        showAlert(Alert.AlertType.WARNING, "Role tidak dikenali.");
                }

            } else {
                // Jika nomor HP tidak ditemukan di tabel logkaryawan, maka dianggap pelanggan baru

                // Simpan data pelanggan ke tabel logpelanggan
                String insertSQL = "INSERT INTO logpelanggan (nama, no_hp) VALUES (?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertSQL);
                insertStmt.setString(1, nama);
                insertStmt.setString(2, noHp);
                insertStmt.executeUpdate();

                // Tampilkan pesan sambutan khusus pelanggan
                showAlert(Alert.AlertType.INFORMATION, "Halo " + nama + ", selamat datang di Ramen House!");
                // TODO: Arahkan ke halaman pelanggan
            }

        } catch (Exception e) {
            // Tangani jika ada error saat koneksi atau query database
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
