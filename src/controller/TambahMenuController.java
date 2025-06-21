package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.DatabaseConnection;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TambahMenuController {

    // Komponen tampilan dari FXML
    @FXML private Label logoLabel;
    @FXML private TextField tfNamaMenu;
    @FXML private TextField tfJenisMenu;
    @FXML private TextField tfHarga;
    @FXML private TextField tfStok;
    @FXML private TextField tfStatus;
    @FXML private Label labelNamaGambar;

    // Variabel penyimpanan gambar dan ID menu jika mode edit
    private File gambarFile;
    private Integer editIdMenu = null;

    // Fungsi untuk menangani klik logo (kembali ke halaman MenuDanStok.fxml)
    @FXML
    private void handleLogoClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MenuDanStok.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) logoLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 400, 600));
            stage.setTitle("Menu dan Stok");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Gagal kembali ke halaman Menu dan Stok.");
        }
    }

    // Fungsi untuk memilih gambar dari file explorer
    @FXML
    private void pilihGambar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih Gambar Menu");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            gambarFile = selectedFile;
            labelNamaGambar.setText(selectedFile.getName());
        }
    }

    // Fungsi utama untuk menyimpan atau mengupdate data menu
    @FXML
    private void simpanMenu() {
        String nama = tfNamaMenu.getText();
        String jenis = tfJenisMenu.getText();
        String hargaText = tfHarga.getText();
        String stokText = tfStok.getText();
        String gambar = (gambarFile != null) ? gambarFile.getName() : labelNamaGambar.getText();

        // Validasi input tidak boleh kosong
        if (nama.isEmpty() || jenis.isEmpty() || hargaText.isEmpty() || stokText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Harap isi semua data!");
            return;
        }

        // Validasi gambar wajib dipilih
        if (gambar.equals("Belum ada gambar") || gambar.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Harap pilih gambar terlebih dahulu!");
            return;
        }

        // Validasi harga dan stok harus berupa angka
        int harga, stok;
        try {
            harga = Integer.parseInt(hargaText);
            stok = Integer.parseInt(stokText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Harga dan Stok harus berupa angka!");
            return;
        }

        // Validasi stok tidak boleh negatif
        if (stok < 0) {
            showAlert(Alert.AlertType.ERROR, "Stok tidak boleh negatif!");
            return;
        }

        // Set status otomatis berdasarkan stok
        String status = (stok == 0) ? "Sold Out" : "Tersedia";
        tfStatus.setText(status);

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql;
            PreparedStatement stmt;

            // Jika bukan mode edit, maka insert data baru
            if (editIdMenu == null) {
                sql = "INSERT INTO menu (nama_menu, jenis_menu, harga, stok, gambar, status) VALUES (?, ?, ?, ?, ?, ?)";
                stmt = conn.prepareStatement(sql);
            } else {
                // Jika mode edit, update data berdasarkan ID menu
                sql = "UPDATE menu SET nama_menu=?, jenis_menu=?, harga=?, stok=?, gambar=?, status=? WHERE id_menu=?";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(7, editIdMenu);
            }

            // Set parameter untuk SQL statement
            stmt.setString(1, nama);
            stmt.setString(2, jenis);
            stmt.setInt(3, harga);
            stmt.setInt(4, stok);
            stmt.setString(5, gambar);
            stmt.setString(6, status);

            // Eksekusi simpan/update
            stmt.executeUpdate();

            // Tampilkan notifikasi sukses
            if (editIdMenu == null) {
                showAlert(Alert.AlertType.INFORMATION, "Menu berhasil ditambahkan!");
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Menu berhasil diperbarui!");
            }

            // Reset form
            clearForm();
            editIdMenu = null;

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Gagal menyimpan ke database!");
        }
    }

    // Fungsi untuk tombol "Clear"/"Bersihkan"
    @FXML
    private void bersihkan() {
        clearForm();
        editIdMenu = null;
    }

    // Reset form ke kondisi kosong
    private void clearForm() {
        tfNamaMenu.clear();
        tfJenisMenu.clear();
        tfHarga.clear();
        tfStok.clear();
        tfStatus.clear();
        labelNamaGambar.setText("Belum ada gambar");
        gambarFile = null;
    }

    // Fungsi ini dipanggil jika menu yang akan diedit
    public void setEditData(int idMenu) {
        this.editIdMenu = idMenu;

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM menu WHERE id_menu = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, idMenu);
            ResultSet rs = stmt.executeQuery();

            // Isi form dari data database
            if (rs.next()) {
                tfNamaMenu.setText(rs.getString("nama_menu"));
                tfJenisMenu.setText(rs.getString("jenis_menu"));
                tfHarga.setText(String.valueOf(rs.getInt("harga")));
                tfStok.setText(String.valueOf(rs.getInt("stok")));
                tfStatus.setText(rs.getString("status"));
                labelNamaGambar.setText(rs.getString("gambar"));
                // gambarFile tidak perlu di-set kembali saat update, kecuali diubah manual
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Gagal memuat data menu.");
        }
    }

    // Fungsi untuk menampilkan alert
    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
