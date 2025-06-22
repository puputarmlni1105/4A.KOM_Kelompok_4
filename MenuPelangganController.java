package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import model.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import java.util.HashMap;
import java.util.Map;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ResourceBundle;

public class MenuPelangganController implements Initializable {

    public Label logoLabel;
    @FXML
    private VBox menuContainer;
    @FXML
    private HBox cartSummaryBox;
    @FXML
    private ImageView cartIcon, pesanan;

    @FXML
    private void handlePesananClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/RiwayatTransaksi.fxml"));
            Parent root = loader.load();
            RiwayatTransaksiController controller = loader.getController();
            controller.setIdPelanggan(idPelanggan);
            Stage stage = (Stage) logoLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 400, 600));
            stage.setTitle("Riwayat Transaksi");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private Button btnFilterAll, btnFilterRamen, btnFilterMinuman;
    @FXML
    private Label itemCountLabel, totalPriceLabel;
    private int totalItems = 0;
    private int totalPrice = 0;
    private Map<Integer, Integer> cartQuantities = new HashMap<>();
    private int idPelanggan;


    public void setIdPelanggan(int idPelanggan) {
        this.idPelanggan = idPelanggan;
        System.out.println("ID Pelanggan diterima: " + idPelanggan);
    }

    private void updateCartSummary() {
        itemCountLabel.setText(totalItems + " item" + (totalItems != 1 ? "s" : ""));
        totalPriceLabel.setText("Rp " + formatRupiah(totalPrice));
    }

    private String formatRupiah(int amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat decimalFormat = new DecimalFormat("#,###", symbols);
        return decimalFormat.format(amount);
    }

    private String currentFilter = "Semua";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadMenu(currentFilter);
        highlightFilterButton(btnFilterAll);
        updateCartSummary(); // inisialisasi label jumlah item dan total harga
    }

    @FXML
    private void filterAll() {
        currentFilter = "Semua";
        loadMenu(currentFilter);
        highlightFilterButton(btnFilterAll);
    }

    @FXML
    private void filterRamen() {
        currentFilter = "Ramen";
        loadMenu(currentFilter);
        highlightFilterButton(btnFilterRamen);
    }

    @FXML
    private void filterMinuman() {
        currentFilter = "Minuman";
        loadMenu(currentFilter);
        highlightFilterButton(btnFilterMinuman);
    }

    private void highlightFilterButton(Button activeButton) {
        // Reset style semua tombol
        btnFilterAll.setStyle("-fx-background-color: transparent; -fx-text-fill: black; -fx-font-weight: normal;");
        btnFilterRamen.setStyle("-fx-background-color: transparent; -fx-text-fill: black; -fx-font-weight: normal;");
        btnFilterMinuman.setStyle("-fx-background-color: transparent; -fx-text-fill: black; -fx-font-weight: normal;");

        // Beri style khusus untuk tombol aktif
        activeButton.setStyle("-fx-background-color: #D72638; -fx-text-fill: white; -fx-font-weight: bold;");
    }


    @FXML
    private void handleLogoClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) logoLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 400, 600));
            stage.setTitle("Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadMenu(String currentFilter) {
        menuContainer.getChildren().clear();

        String query = "SELECT * FROM menu ";
        if ("Ramen".equals(currentFilter)) {
            query += "WHERE jenis_menu = 'Ramen' ";
        } else if ("Minuman".equals(currentFilter)) {
            query += "WHERE jenis_menu = 'Minuman' ";
        }
        query += "ORDER BY jenis_menu ASC, nama_menu ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("id_menu");
                String nama = rs.getString("nama_menu");
                String jenis = rs.getString("jenis_menu");
                int harga = rs.getInt("harga");
                int stok = rs.getInt("stok");
                String gambarPath = rs.getString("gambar");

                // Container baris per menu
                HBox menuRow = new HBox(10);
                menuRow.setPadding(new Insets(10));
                menuRow.setAlignment(Pos.CENTER_LEFT);
                menuRow.setStyle("-fx-background-color: #D3D3D3; -fx-background-radius: 10;");
                menuRow.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, new CornerRadii(10), BorderWidths.DEFAULT)));

                // 1) Gambar menu
                ImageView imageView = new ImageView();
                try {
                    Image img = new Image(getClass().getResource("/images/" + gambarPath).toExternalForm());
                    imageView.setImage(img);
                } catch (Exception e) {
                    imageView.setImage(new Image(getClass().getResource("/images/placeholder.png").toExternalForm()));
                }
                imageView.setFitWidth(60);
                imageView.setFitHeight(60);
                VBox imageBox = new VBox(imageView);
                imageBox.setPrefWidth(70);
                imageBox.setAlignment(Pos.CENTER);

                // 2) Info nama, harga, dan status
                Label namaLabel = new Label(nama);
                namaLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
                Label hargaLabel = new Label("Rp. " + formatRupiah(harga));
                hargaLabel.setStyle("-fx-text-fill: #888888;");
                String status = (stok > 0) ? "Tersedia" : "Sold Out";
                Label statusLabel = new Label(status);
                statusLabel.setStyle(status.equals("Tersedia")
                        ? "-fx-text-fill: green; -fx-font-style: italic;"
                        : "-fx-text-fill: red; -fx-font-style: italic;");
                VBox infoBox = new VBox(namaLabel, hargaLabel, statusLabel);
                infoBox.setSpacing(5);
                infoBox.setPrefWidth(180);

                // 3) Kontrol qty (-, label, +), awalnya sembunyi
                HBox qtyBox = new HBox(5);
                qtyBox.setAlignment(Pos.CENTER_LEFT);
                qtyBox.setVisible(false);
                qtyBox.setManaged(false);
                Button minusBtn = new Button("-");
                Label qtyLabel = new Label("1");
                Button plusBtn = new Button("+");
                minusBtn.setStyle("-fx-background-color: #e0e0e0; -fx-font-weight: bold;");
                plusBtn.setStyle("-fx-background-color: #e0e0e0; -fx-font-weight: bold;");
                qtyLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
                qtyBox.getChildren().addAll(minusBtn, qtyLabel, plusBtn);
                qtyBox.setPrefWidth(100);

                final int maxStok = stok;
                final int[] qty = {1};

                // 4) Tombol “Tambah” (jika stok == 0 maka disable)
                Button btnTambah = new Button("Tambah");
                btnTambah.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
                btnTambah.setPrefWidth(80);
                btnTambah.setDisable(stok == 0);
                btnTambah.setVisible(true);
                btnTambah.setManaged(true);

                // Restore qty jika sudah ada di cartQuantities
                if (cartQuantities.containsKey(id)) {
                    int savedQty = cartQuantities.get(id);
                    qty[0] = savedQty;
                    qtyLabel.setText(String.valueOf(savedQty));
                    qtyBox.setVisible(true);
                    qtyBox.setManaged(true);
                    btnTambah.setVisible(false);
                    btnTambah.setManaged(false);
                } else {
                    qtyBox.setVisible(false);
                    qtyBox.setManaged(false);
                    btnTambah.setVisible(true);
                    btnTambah.setManaged(true);
                }

                // Klik tombol “Tambah” → munculkan qtyBox dan sembunyikan tombol “Tambah”
                btnTambah.setOnAction(e -> {
                    if (maxStok <= 0) return;
                    qty[0] = 1;
                    qtyLabel.setText("1");
                    qtyBox.setVisible(true);
                    qtyBox.setManaged(true);
                    btnTambah.setVisible(false);
                    btnTambah.setManaged(false);

                    cartQuantities.put(id, qty[0]);

                    totalItems += 1;
                    totalPrice += harga;
                    updateCartSummary();

                });

                // Aksi minus di qtyBox
                minusBtn.setOnAction(e -> {
                    if (qty[0] > 1) {
                        qty[0]--;
                        totalItems--;
                        totalPrice -= harga;
                        qtyLabel.setText(String.valueOf(qty[0]));
                        cartQuantities.put(id, qty[0]);
                        updateCartSummary();
                    } else {
                        qtyBox.setVisible(false);
                        qtyBox.setManaged(false);
                        btnTambah.setVisible(true);
                        btnTambah.setManaged(true);

                        cartQuantities.remove(id);

                        totalItems--;
                        totalPrice -= harga;
                        updateCartSummary();
                    }
                });

                // Aksi plus di qtyBox (maksimum stok)
                plusBtn.setOnAction(e -> {
                    if (qty[0] < maxStok) {
                        qty[0]++;
                        totalItems++;
                        totalPrice += harga;
                        qtyLabel.setText(String.valueOf(qty[0]));
                        cartQuantities.put(id, qty[0]);
                        updateCartSummary();
                    }
                });

                // Susun HBox menuRow: [imageBox | infoBox | qtyBox | btnTambah]
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                menuRow.getChildren().addAll(imageBox, infoBox, spacer, qtyBox, btnTambah);
                menuContainer.getChildren().add(menuRow);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void handleCartClick(MouseEvent mouseEvent) {
        // Simpan data cart ke database
        try (Connection conn = DatabaseConnection.getConnection()) {
            for (Map.Entry<Integer, Integer> entry : cartQuantities.entrySet()) {
                int idMenu = entry.getKey();
                int qty = entry.getValue();

                String sql = "INSERT INTO keranjang (id_pelanggan, id_menu, qty) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE qty = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, idPelanggan);
                stmt.setInt(2, idMenu);
                stmt.setInt(3, qty);
                stmt.setInt(4, qty);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Hapus data keranjang lama
            String deleteSql = "DELETE FROM keranjang WHERE id_pelanggan = ?";
            PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
            deleteStmt.setInt(1, idPelanggan);
            deleteStmt.executeUpdate();

            // Insert data keranjang baru
            for (Map.Entry<Integer, Integer> entry : cartQuantities.entrySet()) {
                int idMenu = entry.getKey();
                int qty = entry.getValue();

                String sql = "INSERT INTO keranjang (id_pelanggan, id_menu, qty) VALUES (?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, idPelanggan);
                stmt.setInt(2, idMenu);
                stmt.setInt(3, qty);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        // Buka halaman Keranjang
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Keranjang.fxml"));
            Parent root = loader.load();

            // Kirim idPelanggan ke controller keranjang (optional)
            KeranjangController controller = loader.getController();
            controller.setIdPelanggan(idPelanggan);

            Stage stage = (Stage) cartSummaryBox.getScene().getWindow();
            stage.setScene(new Scene(root, 400, 600));
            stage.setTitle("Keranjang");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


