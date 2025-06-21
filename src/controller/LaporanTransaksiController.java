// Import semua kebutuhan untuk JavaFX dan SQL
package controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
import javafx.util.Callback;
import model.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;

public class LaporanTransaksiController {

    // Deklarasi elemen-elemen dari FXML (form laporan)
    @FXML private Label logoLabel;
    @FXML private DatePicker startDatePicker, endDatePicker;
    @FXML private TableView<ObservableList<String>> transaksiTable;
    @FXML private TableColumn<ObservableList<String>, String> colTanggal, colPesanan, colTotal, colMeja;
    @FXML private TableColumn<ObservableList<String>, Void> colDetail;
    @FXML private TableColumn<ObservableList<String>, Void> colAksi;

    private Connection conn;

    @FXML
    public void initialize() {
        // Method ini otomatis dijalankan saat form dimuat
        try {
            conn = DatabaseConnection.getConnection(); // koneksi ke database
            startDatePicker.setValue(LocalDate.now()); // tanggal awal default: hari ini
            endDatePicker.setValue(LocalDate.now());   // tanggal akhir default: hari ini
            loadColumns();  // atur struktur kolom tabel
            loadData();     // muat data transaksi dari database
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Mengatur isi kolom berdasarkan data dari ObservableList
    private void loadColumns() {
        colTanggal.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().get(0)));
        colPesanan.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().get(1))); // id transaksi
        colTotal.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().get(3)));
        colMeja.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().get(4)));

        // Kolom detail berisi daftar menu dalam transaksi
        colDetail.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }
                ObservableList<String> row = getTableView().getItems().get(getIndex());
                String idTransaksi = row.get(1);
                setGraphic(getDetailNode(idTransaksi)); // tampilkan daftar menu untuk transaksi tsb
            }
        });

        // Kolom aksi berisi tombol "hapus" dan "print"
        colAksi.setCellFactory(getAksiButtonFactory());
    }

    // Method untuk memuat data semua transaksi
    private void loadData() {
        transaksiTable.getItems().clear();
        String sql = "SELECT id_transaksi, total_harga, no_meja, waktu_bayar FROM transaksi " +
                "WHERE status_bayar = 'Di Terima' ORDER BY waktu_bayar DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                addRowToTable(rs); // tambahkan setiap baris ke tabel
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method untuk menambahkan satu baris data transaksi ke dalam tabel
    private void addRowToTable(ResultSet rs) throws SQLException {
        String idTransaksi = rs.getString("id_transaksi");
        String tanggal = rs.getString("waktu_bayar");
        String total = rs.getString("total_harga");
        String meja = rs.getString("no_meja");

        ObservableList<String> row = FXCollections.observableArrayList();
        row.add(tanggal);      // index 0
        row.add(idTransaksi);  // index 1
        row.add("");           // index 2 (kosong, dummy)
        row.add(total);        // index 3
        row.add(meja);         // index 4

        transaksiTable.getItems().add(row);
    }

    // Saat tombol "Cari" ditekan, tampilkan transaksi antara dua tanggal
    @FXML
    private void handleSearch() {
        transaksiTable.getItems().clear();
        String sql = "SELECT id_transaksi, total_harga, no_meja, waktu_bayar FROM transaksi " +
                "WHERE status_bayar = 'Di Terima' AND waktu_bayar BETWEEN ? AND ? ORDER BY waktu_bayar DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(startDatePicker.getValue()));
            stmt.setDate(2, Date.valueOf(endDatePicker.getValue()));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                addRowToTable(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Membuat komponen VBox berisi detail menu dari suatu transaksi
    private VBox getDetailNode(String idTransaksi) {
        VBox container = new VBox(5);
        container.setPadding(new Insets(5));

        String sql = "SELECT dt.id_detail, m.nama_menu, m.harga, dt.qty, (m.harga * dt.qty) as total " +
                "FROM detail_transaksi dt JOIN menu m ON dt.id_menu = m.id_menu " +
                "WHERE dt.id_transaksi = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, idTransaksi);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);

                String namaMenu = rs.getString("nama_menu");
                double harga = rs.getDouble("harga");
                int qty = rs.getInt("qty");
                double total = rs.getDouble("total");
                int idDetail = rs.getInt("id_detail");

                // Tampilkan nama menu dan harga per qty
                Label label = new Label(namaMenu + " | Rp" + harga + " x " + qty + " = Rp" + total);

                // Tombol hapus menu dari detail transaksi
                Button hapusBtn = new Button("🗑");
                hapusBtn.setStyle("-fx-background-color: red; -fx-text-fill: white;");
                hapusBtn.setOnAction(e -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Yakin ingin menghapus item ini?", ButtonType.YES, ButtonType.NO);
                    confirm.setHeaderText(null);
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.YES) {
                            hapusItemDetail(idDetail);
                            loadData(); // refresh tabel
                        }
                    });
                });

                row.getChildren().addAll(label, hapusBtn);
                container.getChildren().add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return container;
    }

    // Buat kolom aksi (hapus transaksi dan unduh/cetak)
    private Callback<TableColumn<ObservableList<String>, Void>, TableCell<ObservableList<String>, Void>> getAksiButtonFactory() {
        return col -> new TableCell<>() {
            private final Button hapusBtn = new Button();
            private final Button unduhBtn = new Button();

            {
                // Icon tombol hapus
                ImageView imgHapus = new ImageView(new Image(getClass().getResourceAsStream("/images/icon/hapus.png")));
                imgHapus.setFitWidth(20);
                imgHapus.setFitHeight(20);
                hapusBtn.setGraphic(imgHapus);
                hapusBtn.setStyle("-fx-background-color: transparent;");

                // Icon tombol print
                ImageView imgUnduh = new ImageView(new Image(getClass().getResourceAsStream("/images/icon/print.png")));
                imgUnduh.setFitWidth(20);
                imgUnduh.setFitHeight(20);
                unduhBtn.setGraphic(imgUnduh);
                unduhBtn.setStyle("-fx-background-color: transparent;");

                // Aksi tombol hapus transaksi
                hapusBtn.setOnAction(e -> {
                    ObservableList<String> data = getTableView().getItems().get(getIndex());
                    String idTransaksi = data.get(1);
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Yakin ingin menghapus transaksi ini?", ButtonType.YES, ButtonType.NO);
                    confirm.setHeaderText(null);
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.YES) {
                            deleteTransaksi(idTransaksi);
                        }
                    });
                });

                // Aksi tombol cetak transaksi
                unduhBtn.setOnAction(e -> {
                    ObservableList<String> data = getTableView().getItems().get(getIndex());
                    String idTransaksi = data.get(1);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Cetak Transaksi");
                    alert.setHeaderText("Fitur Cetak");
                    alert.setContentText("Mencetak transaksi ID: " + idTransaksi);
                    alert.showAndWait();

                    // Catatan: fitur export PDF bisa ditambahkan di sini
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(5, unduhBtn, hapusBtn);
                    setGraphic(box);
                }
            }
        };
    }

    // Menghapus satu item menu dalam transaksi
    private void hapusItemDetail(int idDetail) {
        try {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM detail_transaksi WHERE id_detail = ?");
            stmt.setInt(1, idDetail);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Menghapus seluruh transaksi (termasuk detailnya)
    private void deleteTransaksi(String idTransaksi) {
        try {
            PreparedStatement deleteDetail = conn.prepareStatement("DELETE FROM detail_transaksi WHERE id_transaksi = ?");
            deleteDetail.setString(1, idTransaksi);
            deleteDetail.executeUpdate();

            PreparedStatement deleteTrans = conn.prepareStatement("DELETE FROM transaksi WHERE id_transaksi = ?");
            deleteTrans.setString(1, idTransaksi);
            deleteTrans.executeUpdate();

            loadData(); // refresh tabel
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Tombol logo untuk kembali ke dashboard manajer
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
            e.printStackTrace();
        }
    }

    // Placeholder untuk fitur export ke PDF
    public void handleExportPDF(ActionEvent actionEvent) {
        // TODO: Implementasi export PDF jika dibutuhkan
    }
}
