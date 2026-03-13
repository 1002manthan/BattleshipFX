package com.example.battleshipfx;

import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.*;
import javafx.util.Duration;
import java.util.*;

public class BattleshipFX extends Application {

    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage stage) {
        GameModel model = new GameModel();
        GameUI ui = new GameUI(model, stage);
        //UI Window size
        Scene scene = new Scene(ui.buildRoot(), 1200, 900);
        scene.getStylesheets().add("data:text/css," + CSS);
        stage.setTitle("Battleship — JavaFX Edition");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(1000);
        stage.setMinHeight(1000);
        stage.show();
    }

    static final String CSS =
            ".root{-fx-background-color:#0a1428;-fx-font-family:'Segoe UI',Arial,sans-serif;}" +
                    ".header-title{-fx-font-size:28px;-fx-font-weight:bold;-fx-fill:#00c8e0;}" +
                    ".header-sub{-fx-font-size:12px;-fx-fill:#88bbd8;}" +
                    ".section-label{-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:#6ab0d4;-fx-padding:0 0 2 0;}" +
                    ".stat-title{-fx-font-size:11px;-fx-text-fill:#7aaccc;}" +
                    ".stat-value{-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:#ffc832;}" +
                    ".status-bar{-fx-background-color:#0d1e3c;-fx-border-color:#005090;-fx-border-width:1 0 0 0;}" +
                    ".status-label{-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#b8daf0;}" +
                    ".new-game-btn{-fx-background-color:#0090b0;-fx-text-fill:white;" +
                    "  -fx-font-weight:bold;-fx-font-size:13px;-fx-background-radius:8;-fx-cursor:hand;}" +
                    ".new-game-btn:hover{-fx-background-color:#00b8d8;}" +
                    ".card{-fx-background-color:#0d1e3c;-fx-background-radius:10;" +
                    "  -fx-border-color:#004880;-fx-border-radius:10;-fx-border-width:1;-fx-padding:10 14 10 14;}" +
                    ".how-title{-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:#00c8e0;}" +
                    ".how-text{-fx-font-size:11px;-fx-text-fill:#a8cce0;}";
}

// ═══════════════════════════════════════════════════════════
//  GAME MODEL
// ═══════════════════════════════════════════════════════════
class GameModel {
    static final int SIZE = 10;
    enum CellState { EMPTY, SHIP, HIT, MISS, SUNK }

    static class Ship {
        String name;
        int size, hits;
        List<int[]> cells = new ArrayList<>();
        Ship(String name, int size) { this.name = name; this.size = size; }
        boolean isSunk() { return hits >= size; }
    }

    CellState[][] board = new CellState[SIZE][SIZE];
    Ship[][]      shipAt = new Ship[SIZE][SIZE];
    List<Ship>    ships  = new ArrayList<>();
    int attempts, hitCount, totalShipCells;
    boolean gameOver;

    GameModel() { reset(); }

    void reset() {
        board  = new CellState[SIZE][SIZE];
        shipAt = new Ship[SIZE][SIZE];
        ships.clear();
        attempts = hitCount = totalShipCells = 0;
        gameOver = false;
        for (CellState[] row : board) Arrays.fill(row, CellState.EMPTY);
        placeFleet();
    }

    void placeFleet() {
        String[][] fleet = {
                {"Carrier","5"},{"Battleship","4"},
                {"Cruiser","3"},{"Submarine","3"},{"Destroyer","2"}
        };
        Random rng = new Random();
        for (String[] f : fleet) {
            Ship ship = new Ship(f[0], Integer.parseInt(f[1]));
            int tries = 0; boolean placed = false;
            while (!placed && tries++ < 2000) {
                boolean h = rng.nextBoolean();
                int r = rng.nextInt(SIZE - (h ? 0 : ship.size - 1));
                int c = rng.nextInt(SIZE - (h ? ship.size - 1 : 0));
                if (canPlace(r, c, ship.size, h)) {
                    for (int i = 0; i < ship.size; i++) {
                        int rr = h ? r : r + i, cc = h ? c + i : c;
                        board[rr][cc] = CellState.SHIP;
                        shipAt[rr][cc] = ship;
                        ship.cells.add(new int[]{rr, cc});
                        totalShipCells++;
                    }
                    ships.add(ship); placed = true;
                }
            }
        }
    }

    boolean canPlace(int r, int c, int sz, boolean h) {
        for (int i = 0; i < sz; i++) {
            int rr = h ? r : r + i, cc = h ? c + i : c;
            for (int dr = -1; dr <= 1; dr++)
                for (int dc = -1; dc <= 1; dc++) {
                    int nr = rr+dr, nc = cc+dc;
                    if (nr>=0 && nr<SIZE && nc>=0 && nc<SIZE
                            && board[nr][nc] == CellState.SHIP) return false;
                }
        }
        return true;
    }

    String fire(int r, int c) {
        CellState st = board[r][c];
        if (st==CellState.HIT || st==CellState.MISS || st==CellState.SUNK) return "ALREADY";
        attempts++;
        if (st == CellState.SHIP) {
            Ship ship = shipAt[r][c];
            ship.hits++; hitCount++;
            if (ship.isSunk()) {
                for (int[] cell : ship.cells) board[cell[0]][cell[1]] = CellState.SUNK;
                if (hitCount >= totalShipCells) gameOver = true;
                return "SUNK:" + ship.name + ":" + ship.size;
            }
            board[r][c] = CellState.HIT;
            return "HIT";
        }
        board[r][c] = CellState.MISS;
        return "MISS";
    }

    int rowHint(int r) { int n=0; for(int c=0;c<SIZE;c++) if(board[r][c]==CellState.SHIP) n++; return n; }
    int colHint(int c) { int n=0; for(int r=0;r<SIZE;r++) if(board[r][c]==CellState.SHIP) n++; return n; }
    int shipsRemaining() { int n=0; for(Ship s:ships) if(!s.isSunk()) n++; return n; }
    double accuracy() { return attempts==0 ? 0 : hitCount*100.0/attempts; }
}

// ═══════════════════════════════════════════════════════════
//  GAME UI
// ═══════════════════════════════════════════════════════════
class GameUI {

    // Sidebar is now 320px wide — plenty of room for all text
    static final int SIDEBAR_W = 320;
    static final double TEXT_W = SIDEBAR_W - 35;

    GameModel model;
    Stage stage;
    StackPane[][] cells = new StackPane[GameModel.SIZE][GameModel.SIZE];

    Label statusLabel;
    Label attemptsVal, hitsVal, shipsVal, accuracyVal;
    TextArea logArea;

    static final String C_WATER   = "#0d3d6e";
    static final String C_WATER_H = "#1560a8";
    static final String C_HIT     = "#c83010";
    static final String C_SUNK    = "#7a1010";
    static final String C_MISS    = "#1e4d7a";
    static final String C_GRID    = "#003060";

    GameUI(GameModel model, Stage stage) { this.model = model; this.stage = stage; }

    BorderPane buildRoot() {
        BorderPane root = new BorderPane();
        root.setTop(buildHeader());
        root.setCenter(buildBoard());
        root.setRight(buildSidebar());
        root.setBottom(buildStatusBar());
        return root;
    }

    // ── Header ──────────────────────────────────────────────
    HBox buildHeader() {
        Text title = new Text("BATTLESHIP");
        title.getStyleClass().add("header-title");
        Text sub = new Text("   |   Click a tile to fire — sink the entire fleet to win!");
        sub.getStyleClass().add("header-sub");
        TextFlow tf = new TextFlow(title, sub);

        Button btn = new Button("New Game");
        btn.getStyleClass().add("new-game-btn");
        btn.setPadding(new Insets(9, 20, 9, 20));
        btn.setOnAction(e -> newGame());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bar = new HBox(16, tf, spacer, btn);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(14, 18, 10, 18));
        bar.setStyle("-fx-background-color:#061228;-fx-border-color:#003860;-fx-border-width:0 0 1 0;");
        return bar;
    }

    // ── Board ────────────────────────────────────────────────
    VBox buildBoard() {
        GridPane grid = new GridPane();
        grid.setHgap(3); grid.setVgap(3);
        grid.setAlignment(Pos.CENTER);

        for (int c = 0; c < GameModel.SIZE; c++) {
            Label lbl = new Label(String.valueOf((char)('A' + c)));
            lbl.setFont(Font.font("SansSerif", FontWeight.BOLD, 12));
            lbl.setTextFill(Color.web("#60b0d8"));
            lbl.setMinWidth(52); lbl.setAlignment(Pos.CENTER);
            grid.add(lbl, c + 1, 0);
        }
        for (int r = 0; r < GameModel.SIZE; r++) {
            Label lbl = new Label(String.valueOf(r + 1));
            lbl.setFont(Font.font("SansSerif", FontWeight.BOLD, 12));
            lbl.setTextFill(Color.web("#60b0d8"));
            lbl.setMinWidth(24); lbl.setAlignment(Pos.CENTER_RIGHT);
            grid.add(lbl, 0, r + 1);
        }
        for (int r = 0; r < GameModel.SIZE; r++)
            for (int c = 0; c < GameModel.SIZE; c++) {
                StackPane cell = buildCell(r, c);
                cells[r][c] = cell;
                grid.add(cell, c + 1, r + 1);
            }

        VBox wrap = new VBox(grid);
        wrap.setAlignment(Pos.CENTER);
        wrap.setPadding(new Insets(14));
        wrap.setStyle("-fx-background-color:#08142a;-fx-background-radius:14;" +
                "-fx-border-color:#003870;-fx-border-radius:14;-fx-border-width:1.5;");
        VBox outer = new VBox(wrap);
        outer.setAlignment(Pos.CENTER);
        outer.setPadding(new Insets(10, 0, 10, 10));
        return outer;
    }

    StackPane buildCell(int r, int c) {
        Rectangle bg = new Rectangle(52, 52);
        bg.setArcWidth(9); bg.setArcHeight(9);
        bg.setFill(Paint.valueOf(C_WATER));
        bg.setStroke(Color.web(C_GRID)); bg.setStrokeWidth(1);

        Label icon = new Label();
        icon.setFont(Font.font("SansSerif", FontWeight.BOLD, 13));
        icon.setMouseTransparent(true);

        StackPane cell = new StackPane(bg, icon);
        cell.setMinSize(52, 52); cell.setMaxSize(52, 52);
        cell.setCursor(javafx.scene.Cursor.HAND);

        cell.setOnMouseEntered(e -> {
            if (model.board[r][c] == GameModel.CellState.EMPTY && !model.gameOver) {
                bg.setFill(Paint.valueOf(C_WATER_H));
                addGlow(cell, Color.web("#00d4ff"), 12);
            }
        });
        cell.setOnMouseExited(e -> {
            if (model.board[r][c] == GameModel.CellState.EMPTY) {
                bg.setFill(Paint.valueOf(C_WATER));
                cell.setEffect(null);
            }
        });
        cell.setOnMouseClicked(e -> handleFire(r, c));
        return cell;
    }

    // ── Fire Logic ───────────────────────────────────────────
    void handleFire(int r, int c) {
        if (model.gameOver) return;
        String result = model.fire(r, c);
        if (result.equals("ALREADY")) {
            flashStatus("Already fired here — pick another tile!", "#ffcc20");
            return;
        }
        refreshCell(r, c, result);
        updateStats();

        String col = String.valueOf((char)('A' + c));
        String row = String.valueOf(r + 1);

        if (result.equals("HIT")) {
            setStatus("[HIT]  Direct hit at " + col + row + "! Keep firing!", "#ff7a30");
            appendLog("[HIT]   " + col + row);
        } else if (result.startsWith("SUNK:")) {
            String[] parts = result.split(":");
            String name = parts[1]; int sz = Integer.parseInt(parts[2]);
            setStatus("[SUNK]  " + name + " (size " + sz + ") destroyed at " + col + row + "! Outstanding!", "#ff4040");
            appendLog("[SUNK]  " + name + " @ " + col + row);
            shakeSunk(name);
        } else {
            int rh = model.rowHint(r), ch = model.colHint(c);
            setStatus("[MISS]  " + col + row
                    + "   |   Row " + row + ": " + rh + " ship cell(s) left"
                    + "   |   Col " + col + ": " + ch + " ship cell(s) left", "#3090d0");
            appendLog("[MISS]  " + col + row + "  R:" + rh + " C:" + ch);
        }

        if (model.gameOver) showVictory();
    }

    void refreshCell(int r, int c, String result) {
        StackPane cell = cells[r][c];
        Rectangle bg = (Rectangle) cell.getChildren().get(0);
        Label icon   = (Label)     cell.getChildren().get(1);
        if (cell.getChildren().size() > 2) cell.getChildren().remove(2);
        cell.setEffect(null);

        GameModel.CellState st = model.board[r][c];

        if (st == GameModel.CellState.HIT) {
            bg.setFill(Paint.valueOf(C_HIT));
            icon.setText("HIT");
            icon.setTextFill(Color.web("#ffdd88"));
            addGlow(cell, Color.web("#ff6020"), 18);
            pulseEffect(cell);

        } else if (st == GameModel.CellState.SUNK) {
            for (GameModel.Ship s : model.ships)
                if (s.isSunk())
                    for (int[] pos : s.cells) refreshSunkCell(pos[0], pos[1]);

        } else {
            bg.setFill(Paint.valueOf(C_MISS));
            icon.setText("");
            int rh = model.rowHint(r), ch = model.colHint(c);

            VBox hint = new VBox(1);
            hint.setAlignment(Pos.CENTER);
            hint.setMouseTransparent(true);

            Label rLabel = new Label("R:" + rh);
            rLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 9));
            rLabel.setTextFill(Color.web("#80c8f0"));

            Label cLabel = new Label("C:" + ch);
            cLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 9));
            cLabel.setTextFill(Color.web("#80c8f0"));

            Circle dot = new Circle(3, Color.web("#4090c8"));
            hint.getChildren().addAll(rLabel, cLabel, dot);
            cell.getChildren().add(hint);
        }
    }

    void refreshSunkCell(int r, int c) {
        StackPane cell = cells[r][c];
        Rectangle bg = (Rectangle) cell.getChildren().get(0);
        Label icon   = (Label)     cell.getChildren().get(1);
        if (cell.getChildren().size() > 2) cell.getChildren().remove(2);
        bg.setFill(Paint.valueOf(C_SUNK));
        icon.setText("X");
        icon.setTextFill(Color.web("#ff8888"));
        icon.setFont(Font.font("SansSerif", FontWeight.BOLD, 18));
        addGlow(cell, Color.web("#ff2020"), 14);
    }

    // ── Sidebar ──────────────────────────────────────────────
    VBox buildSidebar() {
        VBox side = new VBox(12);
        side.setPadding(new Insets(14, 16, 14, 10));
        side.setPrefWidth(SIDEBAR_W);
        side.setMinWidth(SIDEBAR_W);
        side.setMaxWidth(SIDEBAR_W);
        side.setFillWidth(true);

        side.getChildren().add(sectionLabel("STATS"));
        side.getChildren().add(buildStatsCard());

        side.getChildren().add(sectionLabel("HOW TO PLAY"));
        side.getChildren().add(buildHowToPlayCard());

        side.getChildren().add(sectionLabel("LEGEND"));
        side.getChildren().add(buildLegendCard());

        side.getChildren().add(sectionLabel("BATTLE LOG"));
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefRowCount(6);
        logArea.setMaxWidth(Double.MAX_VALUE);
        logArea.setWrapText(true);
        logArea.setStyle(
                "-fx-control-inner-background:#08121e;" +
                        "-fx-text-fill:#40e080;" +
                        "-fx-font-family:'Courier New';" +
                        "-fx-font-size:11px;" +
                        "-fx-border-color:#004080;-fx-border-width:1;"
        );
        VBox.setVgrow(logArea, Priority.ALWAYS);
        side.getChildren().add(logArea);

        return side;
    }

    Label sectionLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("section-label");
        l.setMaxWidth(Double.MAX_VALUE);
        return l;
    }

    // ── Stats Card ───────────────────────────────────────────
    GridPane buildStatsCard() {
        attemptsVal = statValue("0");
        hitsVal     = statValue("0");
        shipsVal    = statValue(String.valueOf(model.shipsRemaining()));
        accuracyVal = statValue("0%");

        GridPane g = new GridPane();
        g.getStyleClass().add("card");
        g.setHgap(0); g.setVgap(8);
        g.setMaxWidth(Double.MAX_VALUE);

        ColumnConstraints col0 = new ColumnConstraints();
        col0.setPercentWidth(50); col0.setHalignment(HPos.LEFT);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50); col1.setHalignment(HPos.LEFT);
        g.getColumnConstraints().addAll(col0, col1);

        g.add(statBlock("Shots Fired", attemptsVal), 0, 0);
        g.add(statBlock("Hits",        hitsVal),     1, 0);
        g.add(statBlock("Ships Left",  shipsVal),    0, 1);
        g.add(statBlock("Accuracy",    accuracyVal), 1, 1);
        return g;
    }

    VBox statBlock(String title, Label valueLabel) {
        Label t = new Label(title);
        t.getStyleClass().add("stat-title");
        VBox box = new VBox(2, t, valueLabel);
        box.setAlignment(Pos.TOP_LEFT);
        box.setPadding(new Insets(0, 4, 0, 0));
        return box;
    }

    Label statValue(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("stat-value");
        return l;
    }

    // ── How To Play Card ─────────────────────────────────────
    VBox buildHowToPlayCard() {
        VBox box = new VBox(0);
        box.getStyleClass().add("card");
        box.setMaxWidth(Double.MAX_VALUE);

        String[][] steps = {
                {"OBJECTIVE",
                        "Sink all 5 enemy ships hidden on the 10x10 ocean grid."},
                {"HOW TO FIRE",
                        "Click any blue tile on the board to fire a torpedo at that location."},
                {"HITS & MISSES",
                        "Red tile (HIT) = you struck a ship! Dark red X = ship fully sunk! Blue tile = miss."},
                {"HINT SYSTEM",
                        "After a miss, R:2 means 2 ship cells remain in that Row. C:3 means 3 remain in that Column. Use these clues to find ships faster!"},
                {"THE FLEET",
                        "Carrier (5 cells), Battleship (4), Cruiser (3), Submarine (3), Destroyer (2). All placed randomly each game."},
                {"WIN CONDITION",
                        "Sink all 5 ships to win! Fewer shots = higher accuracy score."}
        };

        for (int i = 0; i < steps.length; i++) {
            Label title = new Label(steps[i][0]);
            title.getStyleClass().add("how-title");
            title.setMaxWidth(Double.MAX_VALUE);

            Label desc = new Label(steps[i][1]);
            desc.getStyleClass().add("how-text");
            desc.setWrapText(true);
            desc.setMaxWidth(TEXT_W);
            desc.setPrefWidth(TEXT_W);
            desc.setTextOverrun(javafx.scene.control.OverrunStyle.CLIP);

            VBox entry = new VBox(3, title, desc);
            entry.setPadding(new Insets(6, 0, 6, 0));
            box.getChildren().add(entry);

            if (i < steps.length - 1) {
                Separator sep = new Separator();
                sep.setStyle("-fx-background-color:#002050;-fx-border-color:#002050;");
                box.getChildren().add(sep);
            }
        }

        return box;
    }

    // ── Legend Card ──────────────────────────────────────────
    VBox buildLegendCard() {
        VBox box = new VBox(7);
        box.getStyleClass().add("card");
        box.setMaxWidth(Double.MAX_VALUE);
        box.getChildren().addAll(
                legendRow(C_WATER, "Water — untouched tile"),
                legendRow(C_HIT,   "HIT  — direct hit on ship"),
                legendRow(C_SUNK,  "X    — ship fully sunk"),
                legendRow(C_MISS,  "Miss — R/C hints shown")
        );
        return box;
    }

    HBox legendRow(String color, String text) {
        Rectangle swatch = new Rectangle(13, 13);
        swatch.setArcWidth(4); swatch.setArcHeight(4);
        swatch.setFill(Paint.valueOf(color));
        swatch.setStroke(Color.web(color).brighter()); swatch.setStrokeWidth(0.8);
        Label lbl = new Label(text);
        lbl.setFont(Font.font("SansSerif", 11));
        lbl.setTextFill(Color.web("#a0c8e0"));
        HBox row = new HBox(7, swatch, lbl);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    // ── Status Bar ───────────────────────────────────────────
    HBox buildStatusBar() {
        statusLabel = new Label("Click any tile to fire your first shot!");
        statusLabel.getStyleClass().add("status-label");
        statusLabel.setPadding(new Insets(10, 18, 10, 18));
        HBox bar = new HBox(statusLabel);
        bar.getStyleClass().add("status-bar");
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    // ── Effects ──────────────────────────────────────────────
    void addGlow(Node node, Color color, double radius) {
        DropShadow glow = new DropShadow(radius, color);
        glow.setSpread(0.3);
        node.setEffect(glow);
    }

    void pulseEffect(Node node) {
        ScaleTransition st = new ScaleTransition(Duration.millis(150), node);
        st.setFromX(1.0); st.setFromY(1.0);
        st.setToX(1.18);  st.setToY(1.18);
        st.setAutoReverse(true); st.setCycleCount(2);
        st.play();
    }

    void shakeSunk(String name) {
        for (GameModel.Ship s : model.ships) {
            if (s.name.equals(name)) {
                for (int[] pos : s.cells) {
                    StackPane cell = cells[pos[0]][pos[1]];
                    TranslateTransition tt = new TranslateTransition(Duration.millis(55), cell);
                    tt.setByX(5); tt.setAutoReverse(true); tt.setCycleCount(6);
                    tt.play();
                }
            }
        }
    }

    void flashStatus(String msg, String colorHex) {
        setStatus(msg, colorHex);
        FadeTransition ft = new FadeTransition(Duration.millis(700), statusLabel);
        ft.setFromValue(1.0); ft.setToValue(0.3);
        ft.setAutoReverse(true); ft.setCycleCount(2);
        ft.play();
    }

    void setStatus(String msg, String colorHex) {
        statusLabel.setText(msg);
        statusLabel.setTextFill(Color.web(colorHex));
    }

    void appendLog(String entry) { logArea.appendText(entry + "\n"); }

    void updateStats() {
        attemptsVal.setText(String.valueOf(model.attempts));
        hitsVal.setText(String.valueOf(model.hitCount));
        shipsVal.setText(String.valueOf(model.shipsRemaining()));
        accuracyVal.setText(String.format("%.0f%%", model.accuracy()));
    }

    void newGame() {
        model.reset();
        for (int r = 0; r < GameModel.SIZE; r++) {
            for (int c = 0; c < GameModel.SIZE; c++) {
                StackPane cell = cells[r][c];
                Rectangle bg = (Rectangle) cell.getChildren().get(0);
                Label icon   = (Label)     cell.getChildren().get(1);
                bg.setFill(Paint.valueOf(C_WATER));
                icon.setText(""); icon.setFont(Font.font("SansSerif", FontWeight.BOLD, 13));
                cell.setEffect(null);
                if (cell.getChildren().size() > 2) cell.getChildren().remove(2);
                int fr = r, fc = c;
                cell.setOnMouseEntered(e -> {
                    if (model.board[fr][fc] == GameModel.CellState.EMPTY && !model.gameOver) {
                        bg.setFill(Paint.valueOf(C_WATER_H));
                        addGlow(cell, Color.web("#00d4ff"), 12);
                    }
                });
                cell.setOnMouseExited(e -> {
                    if (model.board[fr][fc] == GameModel.CellState.EMPTY) {
                        bg.setFill(Paint.valueOf(C_WATER));
                        cell.setEffect(null);
                    }
                });
            }
        }
        logArea.clear();
        setStatus("New game started — click a tile to begin your attack!", "#00c8e0");
        updateStats();
    }

    void showVictory() {
        for (int r = 0; r < GameModel.SIZE; r++)
            for (int c = 0; c < GameModel.SIZE; c++)
                if (model.board[r][c] == GameModel.CellState.SUNK) {
                    StackPane cell = cells[r][c];
                    Timeline tl = new Timeline(
                            new KeyFrame(Duration.ZERO,        new KeyValue(cell.opacityProperty(), 1.0)),
                            new KeyFrame(Duration.millis(300), new KeyValue(cell.opacityProperty(), 0.4)),
                            new KeyFrame(Duration.millis(600), new KeyValue(cell.opacityProperty(), 1.0))
                    );
                    tl.setCycleCount(3); tl.play();
                }

        setStatus("VICTORY!  All ships destroyed in " + model.attempts
                + " shots!  |  Accuracy: " + String.format("%.0f%%", model.accuracy()), "#ffc832");
        appendLog("VICTORY — " + model.attempts + " shots, "
                + String.format("%.0f%%", model.accuracy()) + " accuracy");

        PauseTransition pause = new PauseTransition(Duration.millis(600));
        pause.setOnFinished(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Victory!");
            alert.setHeaderText("Fleet Destroyed!");
            alert.setContentText(
                    "You sunk all 5 ships in " + model.attempts + " shots!\n" +
                            "Hit accuracy: " + String.format("%.1f%%", model.accuracy()) + "\n\n" +
                            "Click 'New Game' to play again."
            );
            alert.initOwner(stage);
            alert.showAndWait();
        });
        pause.play();
    }
}