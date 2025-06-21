package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import model.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ManagerDashboardController {

    // Label untuk menampilkan pendapatan harian, mingguan, bulanan, dan tahunan
    @FXML
    private Label labelHarian, labelMingguan, labelBulanan, labelTahunan;

    // Tombol untuk navigasi ke halaman Menu & Stok, Manajemen Staf, dan Laporan Transaksi
    @FXML
    private Button btnMenuStok;
    @FXML
    private Button btnManajemenStaf;
    @FXML
    private Button btnLaporan;

    // Label logo yang juga berfungsi sebagai tombol untuk kembali ke halaman Login
    @FXML
    private Label logoLabel;

    // Event handler saat logo di-klik, untuk kembali ke halaman login
    @FXML
    private void handleLogoClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) logoLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 400, 600)); // Set ukuran window sama seperti login
            stage.setTitle("Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method initialize otomatis dipanggil saat controller dibuat
    @FXML
    public void initialize() {
        loadPendapatan(); // Memuat data pendapatan dari database

        // Set event untuk tombol-tombol navigasi
        btnMenuStok.setOnAction(e -> switchScene("MenuDanStok.fxml", "Menu & Stok"));
        btnManajemenStaf.setOnAction(e -> switchScene("ManajemenAkunStaf.fxml", "Manajemen Akun Staf"));
        btnLaporan.setOnAction(e -> switchScene("LaporanTransaksi.fxml", "Laporan Transaksi"));
    }

    // Method untuk memuat data pendapatan (harian, mingguan, bulanan, tahunan) dari database
    private void loadPendapatan() {
        try {
            Connection conn = DatabaseConnection.getConnection();

            // Query pendapatan harian (tanggal sama dengan hari ini) dan status bayar diterima
            String qHarian = "SELECT SUM(p.jumlah_pendapatan) " +
                    "FROM pendapatan p " +
                    "JOIN transaksi t ON p.id_transaksi = t.id_transaksi " +
                    "WHERE p.tanggal = CURDATE() AND t.status_bayar = 'Di Terima'";
            PreparedStatement sHarian = conn.prepareStatement(qHarian);
            ResultSet rHarian = sHarian.executeQuery();
            if (rHarian.next()) {
                labelHarian.setText("Harian: Rp " + rHarian.getInt(1));
            }

            // Query pendapatan mingguan (7 hari terakhir) dengan status bayar diterima
            String qMingguan = "SELECT SUM(p.jumlah_pendapatan) " +
                    "FROM pendapatan p " +
                    "JOIN transaksi t ON p.id_transaksi = t.id_transaksi " +
                    "WHERE p.tanggal >= CURDATE() - INTERVAL 7 DAY AND t.status_bayar = 'Di Terima'";
            PreparedStatement sMingguan = conn.prepareStatement(qMingguan);
            ResultSet rMingguan = sMingguan.executeQuery();
            if (rMingguan.next()) {
                labelMingguan.setText("Mingguan: Rp " + rMingguan.getInt(1));
            }

            // Query pendapatan bulanan (bulan dan tahun sama dengan sekarang) dan status bayar diterima
            String qBulanan = "SELECT SUM(p.jumlah_pendapatan) " +
                    "FROM pendapatan p " +
                    "JOIN transaksi t ON p.id_transaksi = t.id_transaksi " +
                    "WHERE MONTH(p.tanggal) = MONTH(CURDATE()) AND YEAR(p.tanggal) = YEAR(CURDATE()) " +
                    "AND t.status_bayar = 'Di Terima'";
            PreparedStatement sBulanan = conn.prepareStatement(qBulanan);
            ResultSet rBulanan = sBulanan.executeQuery();
            if (rBulanan.next()) {
                labelBulanan.setText("Bulanan: Rp " + rBulanan.getInt(1));
            }

            // Query pendapatan tahunan (tahun sama dengan sekarang) dan status bayar diterima
            String qTahunan = "SELECT SUM(p.jumlah_pendapatan) " +
                    "FROM pendapatan p " +
                    "JOIN transaksi t ON p.id_transaksi = t.id_transaksi " +
                    "WHERE YEAR(p.tanggal) = YEAR(CURDATE()) AND t.status_bayar = 'Di Terima'";
            PreparedStatement sTahunan = conn.prepareStatement(qTahunan);
            ResultSet rTahunan = sTahunan.executeQuery();
            if (rTahunan.next()) {
                labelTahunan.setText("Tahunan: Rp " + rTahunan.getInt(1));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method untuk pindah scene ke FXML lain dengan judul tertentu
    private void switchScene(String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/" + fxmlFile));
            Parent root = loader.load();

            // Ambil stage dari salah satu tombol (misal btnMenuStok)
            Stage stage = (Stage) btnMenuStok.getScene().getWindow();
            stage.setTitle(title);

            // Set scene baru dengan ukuran 400x600 (sama seperti login)
            stage.setScene(new Scene(root, 400, 600));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Gagal membuka halaman: " + e.getMessage());
        }
    }

    // Method untuk menampilkan popup alert dengan tipe dan pesan tertentu
    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Informasi");
        alert.setContentText(message);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
