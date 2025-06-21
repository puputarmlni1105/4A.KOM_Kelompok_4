package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TambahStafController {

    // Field input untuk nama dan no HP staf
    @FXML
    private TextField tfNama, tfNoHP;

    // ComboBox untuk memilih role (staf_dapur atau manajer)
    @FXML
    private ComboBox<String> cbRole;

    // Tombol untuk simpan dan hapus isian
    @FXML
    private Button btnSimpan, btnClear;

    // Variabel untuk menyimpan ID jika sedang mode edit (default -1 artinya tambah baru)
    private int selectedId = -1;

    // Fungsi otomatis dijalankan saat form dibuka
    @FXML
    public void initialize() {
        // Tambahkan pilihan role ke ComboBox
        cbRole.getItems().addAll("staf_dapur", "manajer");

        // Atur aksi saat tombol diklik
        btnSimpan.setOnAction(e -> saveOrUpdate());
        btnClear.setOnAction(e -> clearForm());
    }

    // Fungsi menyimpan data baru atau mengupdate data jika sedang edit
    private void saveOrUpdate() {
        // Ambil input dari field
        String nama = tfNama.getText().trim();
        String no_hp = tfNoHP.getText().trim();
        String role = cbRole.getValue();

        // Validasi: semua field wajib diisi
        if (nama.isEmpty() || no_hp.isEmpty() || role == null) {
            showAlert("Validasi", "Semua field wajib diisi!", Alert.AlertType.WARNING);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Jika ID belum dipilih (mode tambah staf)
            if (selectedId == -1) {
                // Cek apakah nomor HP sudah ada di database
                PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM logkaryawan WHERE no_hp = ?");
                check.setString(1, no_hp);
                ResultSet rs = check.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    showAlert("Validasi", "Nomor HP sudah terdaftar.", Alert.AlertType.WARNING);
                    return;
                }

                // Simpan data baru ke tabel logkaryawan
                PreparedStatement ps = conn.prepareStatement("INSERT INTO logkaryawan (nama, no_hp, role) VALUES (?, ?, ?)");
                ps.setString(1, nama);
                ps.setString(2, no_hp);
                ps.setString(3, role);
                ps.executeUpdate();

                showAlert("Sukses", "Karyawan berhasil ditambahkan.", Alert.AlertType.INFORMATION);
            } else {
                // Jika ID tersedia (mode edit), update data
                PreparedStatement ps = conn.prepareStatement("UPDATE logkaryawan SET nama=?, no_hp=?, role=? WHERE id=?");
                ps.setString(1, nama);
                ps.setString(2, no_hp);
                ps.setString(3, role);
                ps.setInt(4, selectedId);
                ps.executeUpdate();

                showAlert("Sukses", "Data karyawan diperbarui.", Alert.AlertType.INFORMATION);
            }

            // Bersihkan form setelah simpan atau update
            clearForm();
        } catch (Exception e) {
            // Tampilkan pesan error jika ada kesalahan
            showAlert("Database Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Fungsi untuk mengosongkan semua input
    private void clearForm() {
        tfNama.clear();
        tfNoHP.clear();
        cbRole.setValue(null);
        selectedId = -1; // Kembali ke mode tambah baru
    }

    // Fungsi untuk kembali ke halaman sebelumnya (ManajemenAkunStaf)
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ManajemenAkunStaf.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tfNama.getScene().getWindow();
            stage.setScene(new Scene(root, 400, 600));
            stage.setTitle("Manajemen Akun Staf");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Fungsi untuk menampilkan alert pop up
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Fungsi ini digunakan saat mode edit, mengisi form berdasarkan ID staf yang diklik
    public void setEditData(int idStaf) {
        this.selectedId = idStaf;

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Ambil data berdasarkan ID staf
            String query = "SELECT * FROM logkaryawan WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, idStaf);
            ResultSet rs = stmt.executeQuery();

            // Isi form dengan data yang diambil
            if (rs.next()) {
                tfNama.setText(rs.getString("nama"));
                tfNoHP.setText(rs.getString("no_hp"));
                cbRole.setValue(rs.getString("role"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal memuat data staf.", Alert.AlertType.ERROR);
        }
    }
}
