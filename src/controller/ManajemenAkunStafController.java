package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;
import model.DatabaseConnection;

import java.sql.*;

// Controller untuk mengelola tampilan dan aksi halaman manajemen akun staf
public class ManajemenAkunStafController {

    @FXML
    private Label logoLabel;  // Label logo yang bisa diklik untuk kembali ke dashboard

    @FXML
    private VBox stafContainer;  // Kontainer vertikal untuk menampilkan daftar staf secara dinamis

    // Handler saat logo diklik, akan kembali ke halaman dashboard manajer
    @FXML
    private void handleLogoClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ManagerDashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) logoLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 400, 600));
            stage.setTitle("Dashboard Manajer");
            stage.show();
        } catch (Exception e) {
            showAlert("Error", "Gagal kembali ke dashboard: " + e.getMessage(), AlertType.ERROR);
        }
    }

    // Handler tombol tambah staf, membuka form tambah staf kosong
    @FXML
    private void handleTambahStafClick() {
        bukaFormTambahStaf(null); // null menandakan form untuk tambah baru, bukan edit
    }

    // Metode initialize otomatis dipanggil saat controller dibuat, memuat data staf
    @FXML
    public void initialize() {
        loadData();  // Memanggil metode untuk menampilkan data staf dari database
    }

    // Metode utama untuk memuat data staf dan menampilkannya di VBox stafContainer
    private void loadData() {
        stafContainer.getChildren().clear(); // Bersihkan kontainer agar data tidak duplikat

        // Membuat header tabel (Nama, No HP, Role, Aksi)
        HBox header = new HBox(10);
        header.setPadding(new Insets(10));
        header.setAlignment(Pos.CENTER);
        header.setPrefWidth(350);

        String[] headers = {"Nama", "No HP", "Role", "Aksi"};
        for (String text : headers) {
            Label label = new Label(text);
            label.setPrefWidth(400); // Lebar label agar rata
            label.setAlignment(Pos.CENTER);
            label.setStyle("-fx-font-weight: bold;");
            header.getChildren().add(label);
        }
        stafContainer.getChildren().add(header);

        // Mengambil data staf dari tabel logkaryawan di database
        try (Connection conn = DatabaseConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM logkaryawan ORDER BY id ASC");

            // Looping setiap baris data staf untuk ditampilkan
            while (rs.next()) {
                int id = rs.getInt("id");
                String nama = rs.getString("nama");
                String no_hp = rs.getString("no_hp");
                String role = rs.getString("role");

                // Membuat satu baris data staf dalam HBox
                HBox row = new HBox(10);
                row.setPadding(new Insets(10));
                row.setAlignment(Pos.CENTER);
                // Styling latar belakang dan border agar rapi
                row.setStyle("-fx-background-color: #D3D3D3; -fx-background-radius: 10; -fx-border-color: #DDD; -fx-border-radius: 10;");
                row.setPrefWidth(350);

                // Label nama, no hp, dan role dengan style tebal dan rata tengah
                Label namaLabel = createLabel(nama);
                namaLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
                namaLabel.setPrefWidth(400);
                namaLabel.setAlignment(Pos.CENTER);

                Label hpLabel = createLabel(no_hp);
                hpLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
                hpLabel.setPrefWidth(400);
                hpLabel.setAlignment(Pos.CENTER);

                Label roleLabel = createLabel(role);
                roleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
                roleLabel.setPrefWidth(400);
                roleLabel.setAlignment(Pos.CENTER);

                // Tombol Edit dengan ikon pena
                ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/icon/edit.png")));
                editIcon.setFitHeight(25);
                editIcon.setFitWidth(25);
                Button btnEdit = new Button();
                btnEdit.setGraphic(editIcon);
                btnEdit.setStyle("-fx-background-color: transparent;"); // tombol transparan agar hanya ikon terlihat

                // Aksi tombol Edit, membuka form tambah staf dengan data diisi untuk edit
                btnEdit.setOnAction(e -> {
                    Karyawan k = new Karyawan(id, nama, no_hp, role);
                    bukaFormTambahStaf(k);
                });

                // Tombol Hapus dengan ikon tong sampah
                ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/icon/hapus.png")));
                deleteIcon.setFitHeight(23);
                deleteIcon.setFitWidth(23);
                Button btnDelete = new Button();
                btnDelete.setGraphic(deleteIcon);
                btnDelete.setStyle("-fx-background-color: transparent;");

                // Konfirmasi sebelum hapus data staf
                btnDelete.setOnAction(e -> {
                    Alert konfirmasi = new Alert(AlertType.CONFIRMATION, "Yakin ingin menghapus data staf ini?", ButtonType.YES, ButtonType.NO);
                    konfirmasi.setTitle("Konfirmasi Hapus");
                    konfirmasi.setHeaderText(null);
                    konfirmasi.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.YES) {
                            deleteKaryawan(id);  // Hapus data di DB
                            loadData();          // Reload data setelah hapus
                        }
                    });
                });

                // Box untuk tombol aksi edit dan hapus
                HBox aksiBox = new HBox(5, btnEdit, btnDelete);
                aksiBox.setAlignment(Pos.CENTER);
                aksiBox.setPrefWidth(400);

                // Tambahkan semua elemen ke dalam row HBox
                row.getChildren().addAll(namaLabel, hpLabel, roleLabel, aksiBox);

                // Tambahkan row staf ke kontainer staf
                stafContainer.getChildren().add(row);
            }
        } catch (Exception e) {
            showAlert("Error", "Gagal memuat data: " + e.getMessage(), AlertType.ERROR);
        }
    }

    // Membuat label dengan lebar tetap dan rata tengah untuk teks staf
    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setPrefWidth(80);
        label.setAlignment(Pos.CENTER);
        return label;
    }

    // Fungsi untuk menghapus data karyawan berdasarkan id dari database
    private void deleteKaryawan(int id) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM logkaryawan WHERE id = ?");
            stmt.setInt(1, id);
            stmt.executeUpdate();
            showAlert("Sukses", "Data staf berhasil dihapus.", AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Error", "Gagal menghapus data: " + e.getMessage(), AlertType.ERROR);
        }
    }

    // Membuka form tambah staf (atau edit staf) dengan mengirim data jika edit
    private void bukaFormTambahStaf(Karyawan karyawan) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/TambahStaf.fxml"));
            Parent root = loader.load();

            // Kirim data karyawan ke controller TambahStaf jika edit
            TambahStafController controller = loader.getController();
            if (karyawan != null) {
                controller.setEditData(karyawan.getId());
            }

            Stage stage = (Stage) logoLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 400, 600));
            stage.setTitle(karyawan == null ? "Tambah Staf" : "Edit Staf");
            stage.show();
        } catch (Exception e) {
            showAlert("Error", "Gagal membuka form staf: " + e.getMessage(), AlertType.ERROR);
        }
    }

    // Utility method untuk menampilkan alert pesan informasi, error, dll
    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Inner class yang merepresentasikan entitas Karyawan
    public static class Karyawan {
        private final int id;
        private final String nama;
        private final String no_hp;
        private final String role;

        public Karyawan(int id, String nama, String no_hp, String role) {
            this.id = id;
            this.nama = nama;
            this.no_hp = no_hp;
            this.role = role;
        }

        // Getter untuk properti Karyawan
        public int getId() {
            return id;
        }

        public String getNama() {
            return nama;
        }

        public String getNo_hp() {
            return no_hp;
        }

        public String getRole() {
            return role;
        }
    }
}
