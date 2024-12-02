package com.javalabs.ahmadqaher_comp228lab5;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

import java.sql.*;
import java.time.LocalDate;

public class GamePlayerController {
    @FXML
    private TextField gameTitleTextField;
    @FXML
    private TextField numberOfPlayersTextField;
    @FXML
    private VBox playerInputContainer;
    @FXML
    private TextField searchFirstNameTextField;
    @FXML
    private TextField searchLastNameTextField;
    @FXML
    private TextField searchGameTitleTextField;
    @FXML
    private TableView<PlayerGameInfo> playerGamesTableView;
    @FXML
    private TableColumn<PlayerGameInfo, String> firstNameColumn;
    @FXML
    private TableColumn<PlayerGameInfo, String> lastNameColumn;
    @FXML
    private TableColumn<PlayerGameInfo, String> gameTitleColumn;
    @FXML
    private TableColumn<PlayerGameInfo, Date> playingDateColumn;
    @FXML
    private TableColumn<PlayerGameInfo, Integer> scoreColumn;

    // Game Players Table
    @FXML
    private TableView<GamePlayerInfo> gamePlayersTableView;
    @FXML
    private TableColumn<GamePlayerInfo, String> gameSearchTitleColumn;
    @FXML
    private TableColumn<GamePlayerInfo, String> gamePlayerFirstNameColumn;
    @FXML
    private TableColumn<GamePlayerInfo, String> gamePlayerLastNameColumn;
    @FXML
    private TableColumn<GamePlayerInfo, Date> gamePlayerPlayingDateColumn;
    @FXML
    private TableColumn<GamePlayerInfo, Integer> gamePlayerScoreColumn;

    private static final String DB_URL = "jdbc:oracle:thin:@199.212.26.208:1521:SQLD";
    private static final String DB_USER = "COMP228_F24_soh_21";
    private static final String DB_PASSWORD = "password1";

    private void registerDriver() {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException e) {
            showAlert("Error", "Oracle JDBC Driver not found: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        registerDriver();
        setupTableColumns();

        // Test connection on startup
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            System.out.println("Database connected successfully!");
        } catch (SQLException e) {
            showAlert("Connection Error", "Could not connect to database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupTableColumns() {
        // Player Games Table
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        gameTitleColumn.setCellValueFactory(new PropertyValueFactory<>("gameTitle"));
        playingDateColumn.setCellValueFactory(new PropertyValueFactory<>("playingDate"));
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));

        // Game Players Table
        gameSearchTitleColumn.setCellValueFactory(new PropertyValueFactory<>("gameTitle"));
        gamePlayerFirstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        gamePlayerLastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        gamePlayerPlayingDateColumn.setCellValueFactory(new PropertyValueFactory<>("playingDate"));
        gamePlayerScoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));
    }

    @FXML
    public void createGame() {
        playerInputContainer.getChildren().clear();

        if (gameTitleTextField.getText().isEmpty()) {
            showAlert("Error", "Please enter a game title");
            return;
        }

        int numPlayers;
        try {
            numPlayers = Integer.parseInt(numberOfPlayersTextField.getText());
            if (numPlayers <= 0) {
                showAlert("Error", "Please enter a positive number of players");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid number of players");
            return;
        }

        createPlayerInputFields(numPlayers);
    }

    private void createPlayerInputFields(int numPlayers) {
        for (int i = 0; i < numPlayers; i++) {
            GridPane playerGrid = new GridPane();
            playerGrid.setHgap(10);
            playerGrid.setVgap(10);
            playerGrid.setPadding(new Insets(10));

            Label firstNameLabel = new Label("Player " + (i + 1) + " First Name:");
            TextField firstNameField = new TextField();
            Label lastNameLabel = new Label("Player " + (i + 1) + " Last Name:");
            TextField lastNameField = new TextField();

            playerGrid.add(firstNameLabel, 0, 0);
            playerGrid.add(firstNameField, 1, 0);
            playerGrid.add(lastNameLabel, 0, 1);
            playerGrid.add(lastNameField, 1, 1);

            Button addButton = new Button("Add Player " + (i + 1));
            playerGrid.add(addButton, 1, 2);

            addButton.setOnAction(e -> addPlayerToGame(firstNameField.getText(), lastNameField.getText()));

            playerInputContainer.getChildren().add(playerGrid);
        }
    }

    private void addPlayerToGame(String firstName, String lastName) {
        if (firstName.isEmpty() || lastName.isEmpty()) {
            showAlert("Error", "Please enter both first name and last name");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            conn.setAutoCommit(false);
            try {
                // Insert game
                String gameTitle = gameTitleTextField.getText();
                int gameId;

                String insertGameSql = "INSERT INTO Ahmad_Qaher_game (game_title) VALUES (?)";
                try (PreparedStatement stmt = conn.prepareStatement(insertGameSql, new String[]{"game_id"})) {
                    stmt.setString(1, gameTitle);
                    stmt.executeUpdate();
                    ResultSet rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        gameId = rs.getInt(1);
                    } else {
                        throw new SQLException("Failed to get game ID");
                    }
                }

                // Insert player
                int playerId;
                String insertPlayerSql = "INSERT INTO Ahmad_Qaher_player (first_name, last_name) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(insertPlayerSql, new String[]{"player_id"})) {
                    stmt.setString(1, firstName);
                    stmt.setString(2, lastName);
                    stmt.executeUpdate();
                    ResultSet rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        playerId = rs.getInt(1);
                    } else {
                        throw new SQLException("Failed to get player ID");
                    }
                }

                // Create player-game relationship
                String insertRelationSql = "INSERT INTO Ahmad_Qaher_player_and_game (game_id, player_id, playing_date, score) VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(insertRelationSql)) {
                    stmt.setInt(1, gameId);
                    stmt.setInt(2, playerId);
                    stmt.setDate(3, Date.valueOf(LocalDate.now()));
                    stmt.setInt(4, 0);
                    stmt.executeUpdate();
                }

                conn.commit();
                showAlert("Success", "Player added to game successfully!");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to add player to game: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void searchPlayerGames() {
        String firstName = searchFirstNameTextField.getText().trim();
        String lastName = searchLastNameTextField.getText().trim();

        if (firstName.isEmpty() && lastName.isEmpty()) {
            showAlert("Error", "Please enter at least one search term");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT p.first_name, p.last_name, g.game_title, pg.playing_date, pg.score " +
                    "FROM Ahmad_Qaher_player p " +
                    "JOIN Ahmad_Qaher_player_and_game pg ON p.player_id = pg.player_id " +
                    "JOIN Ahmad_Qaher_game g ON pg.game_id = g.game_id " +
                    "WHERE LOWER(p.first_name) LIKE LOWER(?) OR LOWER(p.last_name) LIKE LOWER(?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, "%" + firstName + "%");
                stmt.setString(2, "%" + lastName + "%");

                ResultSet rs = stmt.executeQuery();
                ObservableList<PlayerGameInfo> playerGames = FXCollections.observableArrayList();

                while (rs.next()) {
                    playerGames.add(new PlayerGameInfo(
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("game_title"),
                            rs.getDate("playing_date"),
                            rs.getInt("score")
                    ));
                }

                playerGamesTableView.setItems(playerGames);
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to search player games: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void searchGamePlayers() {
        String gameTitle = searchGameTitleTextField.getText().trim();

        if (gameTitle.isEmpty()) {
            showAlert("Error", "Please enter a game title to search");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT g.game_title, p.first_name, p.last_name, pg.playing_date, pg.score " +
                    "FROM Ahmad_Qaher_game g " +
                    "JOIN Ahmad_Qaher_player_and_game pg ON g.game_id = pg.game_id " +
                    "JOIN Ahmad_Qaher_player p ON pg.player_id = p.player_id " +
                    "WHERE LOWER(g.game_title) LIKE LOWER(?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, "%" + gameTitle + "%");

                ResultSet rs = stmt.executeQuery();
                ObservableList<GamePlayerInfo> gamePlayers = FXCollections.observableArrayList();

                while (rs.next()) {
                    gamePlayers.add(new GamePlayerInfo(
                            rs.getString("game_title"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getDate("playing_date"),
                            rs.getInt("score")
                    ));
                }

                gamePlayersTableView.setItems(gamePlayers);
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to search game players: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}