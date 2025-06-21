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
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class MenuStokController {

    @FXML
    private Label logoLabel; // Label logo yang bisa diklik untuk kembali ke dashboard manajer

    @FXML
    private VBox menuContainer; // Container VBox untuk menampilkan daftar menu secara vertikal

    // Event handler untuk klik logo, untuk kembali ke halaman Dashboard Manajer
    @FXML
    private void handleLogoClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ManagerDashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) logoLabel.getScene().getWindow(); // Ambil stage saat ini
            stage.setScene(new Scene(root, 400, 600)); // Ganti scene ke dashboard
            stage.setTitle("Dashboard Manajer");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace(); // Cetak error jika gagal pindah halaman
        }
    }

    // Event handler tombol tambah menu, membuka form TambahMenu.fxml untuk menambah data menu baru
    @FXML
    private void handleTambahMenuClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/TambahMenu.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) logoLabel.getScene().getWindow(); // Ambil stage saat ini
            stage.setScene(new Scene(root, 400, 600)); // Ganti scene ke form tambah menu
            stage.setTitle("Tambah Menu");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace(); // Cetak error jika gagal membuka form tambah menu
        }
    }

    // Fungsi utilitas untuk memformat angka menjadi format mata uang rupiah (contoh: 10.000)
    private String formatRupiah(int amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.'); // separator ribuan pake titik
        symbols.setDecimalSeparator(','); // separator desimal pake koma (meskipun di sini tidak dipakai)
        DecimalFormat decimalFormat = new DecimalFormat("#,###", symbols);
        return decimalFormat.format(amount);
    }

    // Method initialize otomatis dipanggil saat controller di-load, memuat data menu ke tampilan
    @FXML
    private void initialize() {
        loadMenuData();
    }

    // Memuat data menu dari database dan menampilkannya ke dalam VBox menuContainer
    private void loadMenuData() {
        menuContainer.getChildren().clear(); // Bersihkan dulu container agar tidak dobel data

        // Membuat header kolom untuk daftar menu
        HBox header = new HBox(10);
        header.setPadding(new Insets(10));
        header.setAlignment(Pos.CENTER);
        header.setPrefWidth(350);

        // Label header kolom
        Label gambarTitle = new Label("Gambar");
        Label namaTitle = new Label("Nama");
        Label stokTitle = new Label("Stok");
        Label aksiTitle = new Label("Aksi");

        // Set lebar dan gaya tiap label header supaya rata tengah dan tebal
        for (Label label : new Label[]{gambarTitle, namaTitle, stokTitle, aksiTitle}) {
            label.setPrefWidth(400);
            label.setAlignment(Pos.CENTER);
            label.setStyle("-fx-font-weight: bold;");
        }

        header.getChildren().addAll(gambarTitle, namaTitle, stokTitle, aksiTitle);
        menuContainer.getChildren().add(header);

        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();

            // Query ambil semua data menu, diurutkan berdasarkan jenis menu dan nama menu
            ResultSet rs = stmt.executeQuery("SELECT * FROM menu ORDER BY jenis_menu ASC, nama_menu ASC");

            while (rs.next()) {
                // Ambil data tiap baris menu dari hasil query
                int idMenu = rs.getInt("id_menu");
                String nama = rs.getString("nama_menu");
                int harga = rs.getInt("harga");
                int stok = rs.getInt("stok");
                String gambarPath = rs.getString("gambar");

                // Membuat HBox baris menu
                HBox menuRow = new HBox(10);
                menuRow.setPadding(new Insets(10));
                menuRow.setAlignment(Pos.CENTER);
                menuRow.setStyle("-fx-background-color: #D3D3D3; -fx-background-radius: 10; -fx-border-color: #DDD; -fx-border-radius: 10;");
                menuRow.setPrefWidth(350);

                // Membuat ImageView untuk gambar menu
                ImageView imageView = new ImageView();
                try {
                    // Coba load gambar menu sesuai path yang ada
                    Image img = new Image(getClass().getResource("/images/" + gambarPath).toExternalForm());
                    imageView.setImage(img);
                } catch (Exception e) {
                    // Jika gagal (gambar tidak ada), gunakan placeholder default
                    imageView.setImage(new Image(getClass().getResource("/images/placeholder.png").toExternalForm()));
                }
                imageView.setFitWidth(60);
                imageView.setFitHeight(60);
                VBox imageBox = new VBox(imageView);
                imageBox.setPrefWidth(400);
                imageBox.setAlignment(Pos.CENTER);

                // VBox untuk nama dan harga menu
                VBox infoBox = new VBox(5);
                Label namaLabel = new Label(nama);
                namaLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
                Label hargaLabel = new Label("Rp. " + formatRupiah(harga)); // Format harga ke rupiah
                infoBox.getChildren().addAll(namaLabel, hargaLabel);
                infoBox.setPrefWidth(400);
                infoBox.setAlignment(Pos.CENTER);

                // HBox stok dan tombol tambah stok
                HBox stokBox = new HBox(5);
                stokBox.setAlignment(Pos.CENTER);
                Label stokLabel = new Label(String.valueOf(stok)); // Tampilkan jumlah stok
                ImageView tambahIcon = new ImageView(new Image(getClass().getResource("/images/icon/tambah.png").toExternalForm()));
                tambahIcon.setFitWidth(20);
                tambahIcon.setFitHeight(20);

                // Saat klik icon tambah stok, panggil fungsi tambahStok untuk id menu ini
                tambahIcon.setOnMouseClicked(e -> tambahStok(idMenu));
                stokBox.getChildren().addAll(stokLabel, tambahIcon);
                stokBox.setPrefWidth(400);

                // HBox aksi: tombol edit dan hapus menu
                HBox aksiBox = new HBox(5);
                aksiBox.setAlignment(Pos.CENTER);
                ImageView editIcon = new ImageView(new Image(getClass().getResource("/images/icon/edit.png").toExternalForm()));
                editIcon.setFitWidth(25);
                editIcon.setFitHeight(25);

                // Klik icon edit akan membuka form edit menu
                editIcon.setOnMouseClicked(e -> editMenu(idMenu));

                ImageView hapusIcon = new ImageView(new Image(getClass().getResource("/images/icon/hapus.png").toExternalForm()));
                hapusIcon.setFitWidth(23);
                hapusIcon.setFitHeight(23);

                // Klik icon hapus akan menghapus menu setelah konfirmasi
                hapusIcon.setOnMouseClicked(e -> hapusMenu(idMenu));

                aksiBox.getChildren().addAll(editIcon, hapusIcon);
                aksiBox.setPrefWidth(400);

                // Tambahkan semua komponen ke baris menu
                menuRow.getChildren().addAll(imageBox, infoBox, stokBox, aksiBox);
                menuContainer.getChildren().add(menuRow);
            }

            // Tutup resource database
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace(); // Tampilkan error jika gagal load data menu
        }
    }

    // Fungsi untuk menambah stok menu sebanyak 1
    private void tambahStok(int idMenu) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement("UPDATE menu SET stok = stok + 1 WHERE id_menu = ?");
            stmt.setInt(1, idMenu);
            stmt.executeUpdate();
            stmt.close();
            conn.close();

            loadMenuData(); // Refresh data tampilan setelah update stok
        } catch (Exception e) {
            e.printStackTrace(); // Cetak error jika gagal update stok
        }
    }

    // Fungsi hapus menu berdasarkan id, dengan konfirmasi terlebih dahulu
    private void hapusMenu(int idMenu) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Hapus");
        alert.setHeaderText("Yakin ingin menghapus menu ini?");
        alert.setContentText("Data menu akan dihapus secara permanen.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement stmt = conn.prepareStatement("DELETE FROM menu WHERE id_menu = ?");
                    stmt.setInt(1, idMenu);
                    stmt.executeUpdate();
                    stmt.close();
                    conn.close();

                    loadMenuData(); // Refresh data setelah hapus
                } catch (Exception e) {
                    e.printStackTrace(); // Cetak error jika gagal hapus data
                }
            }
        });
    }

    // Fungsi untuk membuka form edit menu dengan mengirimkan id menu yang dipilih
    private void editMenu(int idMenu) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/TambahMenu.fxml"));
            Parent root = loader.load();

            // Ambil controller dari form TambahMenu untuk mengisi data edit
            TambahMenuController controller = loader.getController();
            controller.setEditData(idMenu); // Kirim id menu ke form tambah/edit

            Stage stage = (Stage) logoLabel.getScene().getWindow(); // Ambil stage sekarang
            stage.setScene(new Scene(root, 400, 600)); // Tampilkan form edit menu
            stage.setTitle("Edit Menu");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace(); // Cetak error jika gagal membuka form edit menu
        }
    }
}
