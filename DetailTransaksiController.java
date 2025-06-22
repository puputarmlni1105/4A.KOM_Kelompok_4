package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.DatabaseConnection;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.*;
import java.util.Optional;

public class DetailTransaksiController {

    @FXML
    private Label logoLabel;
    @FXML
    private Label greetingLabel;
    @FXML
    private Label noMejaLabel, menuLabel, porsiLabel, totalItemLabel;
    @FXML
    private Label hargaLabel, totalPembayaranLabel, noPesananLabel;
    @FXML
    private Label metodePembayaranLabel, waktuPesanLabel, waktuBayarLabel;

    @FXML
    private ImageView imageView;
    @FXML
    private Label statusTransaksiLabel;
    @FXML
    private VBox mainContainer; // tetap digunakan untuk snapshot struk
    @FXML
    private Button btnCetakStruk; // tombol cetak

    @FXML
    private VBox menuContainer;

    @FXML
    private HBox statusPesananBox;

    private int idTransaksi;
    private String namaUser;

    public void setIdTransaksi(int id) {
        this.idTransaksi = id;
        loadData();
    }

    public void loadData() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM transaksi WHERE id_transaksi = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idTransaksi);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                showError("Transaksi tidak ditemukan.");
                return;
            }

            greetingLabel.setText("Hore! Pesanan kamu sudah dibuat. Tunggu sampai staf kami konfirmasi pembayaran Anda.");

            noMejaLabel.setText(rs.getString("no_meja"));
            totalPembayaranLabel.setText("Rp " + rs.getInt("total_harga"));
            noPesananLabel.setText(String.valueOf(rs.getInt("id_transaksi")));
            metodePembayaranLabel.setText(rs.getString("metode_bayar"));
            waktuPesanLabel.setText(rs.getString("waktu_pesan"));
            statusTransaksiLabel.setText(rs.getString("status_bayar"));
            waktuBayarLabel.setText(rs.getString("waktu_bayar"));

            // Tampilkan gambar hore.png dari resources
            Image horeImage = new Image(getClass().getResourceAsStream("/images/icon/hore.png"));
            imageView.setImage(horeImage);


            // Query detail transaksi (menu yang dipesan)
            String detailQuery = "SELECT m.nama_menu, d.qty, d.total_per_item FROM detail_transaksi d " +
                    "JOIN menu m ON d.id_menu = m.id_menu WHERE d.id_transaksi = ?";
            PreparedStatement detailStmt = conn.prepareStatement(detailQuery);
            detailStmt.setInt(1, idTransaksi);
            ResultSet rsDetail = detailStmt.executeQuery();

            menuContainer.getChildren().clear(); // bersihkan dulu

            while (rsDetail.next()) {
                String namaMenu = rsDetail.getString("nama_menu");
                int qty = rsDetail.getInt("qty");
                int totalPerItem = rsDetail.getInt("total_per_item");

                HBox row = new HBox(20);
                row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                Label namaLabel = new Label(namaMenu);
                namaLabel.setPrefWidth(150);

                Label qtyLabel = new Label(String.valueOf(qty));
                qtyLabel.setPrefWidth(40);

                Label hargaLabel = new Label(formatRupiah(totalPerItem));
                hargaLabel.setPrefWidth(100);

                row.getChildren().addAll(namaLabel, qtyLabel, hargaLabel);
                menuContainer.getChildren().add(row);
            }



        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Terjadi kesalahan saat memuat data transaksi.");
        }
    }
    private String formatRupiah(int amount) {
        return String.format("Rp %,d", amount).replace(',', '.');
    }

    @FXML
    private void handleLogoClick(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MenuPelanggan.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) logoLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 400, 600));
            stage.setTitle("Menu Pelanggan");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Gagal membuka halaman Menu Pelanggan.");
        }
    }

    @FXML
    private void handleDownloadClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Simpan Struk Transaksi");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));
        File file = fileChooser.showSaveDialog(mainContainer.getScene().getWindow());

        if (file != null) {
            try {
                WritableImage snapshot = mainContainer.snapshot(null, null);
                BufferedImage bufferedImage = javafx.embed.swing.SwingFXUtils.fromFXImage(snapshot, null);
                ImageIO.write(bufferedImage, "png", file);
                showInfo("Struk transaksi berhasil disimpan.");
            } catch (Exception e) {
                e.printStackTrace();
                showError("Gagal menyimpan struk transaksi.");
            }
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
    }

    private void showError(String message) {
        showAlert(message);
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.showAndWait();
    }
}
