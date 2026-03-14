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

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        GameModel model = new GameModel();
        GameUI ui = new GameUI(model, stage);
        Scene scene = new Scene(ui.buildRoot(), 1200, 900);
        scene.getStylesheets().add("data:text/css," + CSS);
        stage.setTitle("The Battleship War");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(1000);
        stage.setMinHeight(1000);
        stage.show();
    }

    // ═══════════════════════════════════════════════════════
    //  "THE BATTLESHIP WAR"
    //  Theme: aged parchment canvas (#d4b896) + deep sea
    //  teal cells (#1b4f6a) — exactly as the screenshot.
    //  Labels in dark brown, serif font throughout.
    // ═══════════════════════════════════════════════════════
    static final String PARCHMENT      = "#d6bc96";
    static final String PARCHMENT_DARK = "#c4a878";
    static final String PARCHMENT_MID  = "#cbb08a";
    static final String BROWN_DARK     = "#3a2408";
    static final String BROWN_MID      = "#6a4018";
    static final String GOLD           = "#8a5c10";
    static final String GOLD_BRIGHT    = "#b07820";
    static final String CRIMSON        = "#8a1c0c";

    static final String CSS =
            // Whole window is parchment
            ".root{-fx-background-color:" + PARCHMENT + ";" +
                    "  -fx-font-family:'Georgia','Book Antiqua','Times New Roman',serif;}" +

                    // Title — deep brown, large serif
                    ".header-title{-fx-font-size:28px;-fx-font-weight:bold;-fx-fill:" + BROWN_DARK + ";}" +

                    // Subtitle
                    ".header-sub{-fx-font-size:11px;-fx-fill:" + BROWN_MID + ";}" +

                    // Sidebar section labels
                    ".section-label{-fx-font-size:10px;-fx-font-weight:bold;" +
                    "  -fx-text-fill:" + GOLD + ";-fx-padding:0 0 2 0;" +
                    "  -fx-font-family:'Georgia',serif;" +
                    "  -fx-border-color:" + PARCHMENT_DARK + ";-fx-border-width:0 0 1 0;}" +

                    // Stat labels
                    ".stat-title{-fx-font-size:11px;-fx-text-fill:" + BROWN_MID + ";" +
                    "  -fx-font-family:'Georgia',serif;}" +
                    ".stat-value{-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:" + BROWN_DARK + ";" +
                    "  -fx-font-family:'Georgia',serif;}" +

                    // Status bar — slightly darker parchment strip
                    ".status-bar{-fx-background-color:" + PARCHMENT_DARK + ";" +
                    "  -fx-border-color:" + BROWN_MID + ";-fx-border-width:2 0 0 0;}" +
                    ".status-label{-fx-font-size:13px;-fx-font-weight:bold;" +
                    "  -fx-text-fill:" + BROWN_DARK + ";-fx-font-family:'Georgia',serif;}" +

                    // New Campaign button — deep teal wax seal style
                    ".new-game-btn{-fx-background-color:#1b4f6a;-fx-text-fill:" + PARCHMENT + ";" +
                    "  -fx-font-weight:bold;-fx-font-size:12px;-fx-background-radius:4;" +
                    "  -fx-cursor:hand;-fx-font-family:'Georgia',serif;" +
                    "  -fx-border-color:#2d7aaa;-fx-border-radius:4;-fx-border-width:1.5;}" +
                    ".new-game-btn:hover{-fx-background-color:#246080;-fx-border-color:#40a0cc;}" +

                    // Sidebar cards — slightly darker parchment
                    ".card{-fx-background-color:" + PARCHMENT_MID + ";" +
                    "  -fx-background-radius:4;" +
                    "  -fx-border-color:" + PARCHMENT_DARK + ";-fx-border-radius:4;" +
                    "  -fx-border-width:1.5;-fx-padding:10 14 10 14;}" +

                    // How-to-play text
                    ".how-title{-fx-font-size:10px;-fx-font-weight:bold;" +
                    "  -fx-text-fill:" + GOLD + ";-fx-font-family:'Georgia',serif;}" +
                    ".how-text{-fx-font-size:11px;-fx-text-fill:" + BROWN_DARK + ";" +
                    "  -fx-font-family:'Georgia',serif;}";
}

// ═══════════════════════════════════════════════════════════
//  GAME MODEL
// ═══════════════════════════════════════════════════════════
class GameModel {
    static final int SIZE = 10;

    enum CellState {EMPTY, SHIP, HIT, MISS, SUNK}

    static class Ship {
        String name;
        int size, hits;
        List<int[]> cells = new ArrayList<>();

        Ship(String name, int size) {
            this.name = name;
            this.size = size;
        }

        boolean isSunk() { return hits >= size; }
    }

    CellState[][] board = new CellState[SIZE][SIZE];
    Ship[][] shipAt     = new Ship[SIZE][SIZE];
    List<Ship> ships    = new ArrayList<>();
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
                {"Flagship",   "5"}, {"Man-of-War", "4"},
                {"Brigantine", "3"}, {"Corsair",    "3"}, {"Sloop", "2"}
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
                    ships.add(ship);
                    placed = true;
                }
            }
        }
    }

    boolean canPlace(int r, int c, int sz, boolean h) {
        for (int i = 0; i < sz; i++) {
            int rr = h ? r : r + i, cc = h ? c + i : c;
            for (int dr = -1; dr <= 1; dr++)
                for (int dc = -1; dc <= 1; dc++) {
                    int nr = rr + dr, nc = cc + dc;
                    if (nr >= 0 && nr < SIZE && nc >= 0 && nc < SIZE
                            && board[nr][nc] == CellState.SHIP) return false;
                }
        }
        return true;
    }

    String fire(int r, int c) {
        CellState st = board[r][c];
        if (st == CellState.HIT || st == CellState.MISS || st == CellState.SUNK) return "ALREADY";
        attempts++;
        if (st == CellState.SHIP) {
            Ship ship = shipAt[r][c];
            ship.hits++;
            hitCount++;
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

    int rowHint(int r) {
        int n = 0;
        for (int c = 0; c < SIZE; c++) if (board[r][c] == CellState.SHIP) n++;
        return n;
    }

    int colHint(int c) {
        int n = 0;
        for (int r = 0; r < SIZE; r++) if (board[r][c] == CellState.SHIP) n++;
        return n;
    }

    int shipsRemaining() {
        int n = 0;
        for (Ship s : ships) if (!s.isSunk()) n++;
        return n;
    }

    double accuracy() {
        return attempts == 0 ? 0 : hitCount * 100.0 / attempts;
    }
}

// ═══════════════════════════════════════════════════════════
//  GAME UI  —  The Battleship War — Parchment Edition
// ═══════════════════════════════════════════════════════════
class GameUI {

    static final int    SIDEBAR_W = 350;
    static final double TEXT_W    = SIDEBAR_W;

    // shorthand palette refs
    static final String PARCHMENT      = "#d6bc96";
    static final String PARCHMENT_DARK = "#c4a878";
    static final String PARCHMENT_MID  = "#cbb08a";
    static final String BROWN_DARK     = "#3a2408";
    static final String BROWN_MID      = "#6a4018";
    static final String GOLD           = "#8a5c10";
    static final String GOLD_BRIGHT    = "#b07820";
    static final String CRIMSON        = "#8a1c0c";

    // ── Cell colours — matching screenshot exactly ───────────
    // Normal sea tile — deep teal as in the screenshot
    static final String C_WATER   = "#1b4f6a";
    // Hover — slightly brighter teal
    static final String C_WATER_H = "#256690";
    // Hit — warm crimson / blood-red
    static final String C_HIT     = "#9e2010";
    // Sunk — very dark burgundy
    static final String C_SUNK    = "#5c0e0e";
    // Miss — muted slate-teal (darker than empty)
    static final String C_MISS    = "#163c50";
    // Grid gap colour = parchment (comes from background showing through)
    static final String C_HEADER  = "#3a2408";   // dark brown labels

    GameModel model;
    Stage stage;
    StackPane[][] cells = new StackPane[GameModel.SIZE][GameModel.SIZE];

    Label statusLabel;
    Label attemptsVal, hitsVal, shipsVal, accuracyVal;
    TextArea logArea;

    GameUI(GameModel model, Stage stage) {
        this.model = model;
        this.stage = stage;
    }

    BorderPane buildRoot() {
        BorderPane root = new BorderPane();
        root.setTop(buildHeader());
        root.setCenter(buildBoard());
        root.setRight(buildSidebar());
        root.setBottom(buildStatusBar());
        return root;
    }

    // ── Header ───────────────────────────────────────────────
    HBox buildHeader() {
        Text leftOrn = new Text("⚓  ");
        leftOrn.setFont(Font.font("Georgia", FontWeight.BOLD, 20));
        leftOrn.setFill(Color.web(BROWN_MID));

        Text title = new Text("THE  BATTLESHIP  WAR");
        title.getStyleClass().add("header-title");

        Text rightOrn = new Text("  ⚓");
        rightOrn.setFont(Font.font("Georgia", FontWeight.BOLD, 20));
        rightOrn.setFill(Color.web(BROWN_MID));

        Text sub = new Text("    —    Select a coordinate to open fire upon the enemy fleet");
        sub.getStyleClass().add("header-sub");

        TextFlow tf = new TextFlow(leftOrn, title, rightOrn, sub);

        Button btn = new Button("⚔  New Campaign");
        btn.getStyleClass().add("new-game-btn");
        btn.setPadding(new Insets(9, 20, 9, 20));
        btn.setOnAction(e -> newGame());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bar = new HBox(16, tf, spacer, btn);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(14, 18, 12, 18));
        // Header bar — medium parchment with bottom rule
        bar.setStyle(
                "-fx-background-color:" + PARCHMENT_DARK + ";" +
                        "-fx-border-color:" + BROWN_MID + ";-fx-border-width:0 0 2 0;"
        );
        return bar;
    }

    // ── Board ────────────────────────────────────────────────
    VBox buildBoard() {
        GridPane grid = new GridPane();
        // Gap IS the parchment background showing through — matches screenshot
        grid.setHgap(4);
        grid.setVgap(4);
        grid.setAlignment(Pos.CENTER);
        // Grid background = parchment so gaps look like the dividers in the screenshot
        grid.setStyle("-fx-background-color:" + PARCHMENT + ";");
        grid.setPadding(new Insets(0));

        // Column headers A–J  — bold dark-brown serif
        for (int c = 0; c < GameModel.SIZE; c++) {
            Label lbl = new Label(String.valueOf((char) ('A' + c)));
            lbl.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
            lbl.setTextFill(Color.web(C_HEADER));
            lbl.setMinWidth(54);
            lbl.setAlignment(Pos.CENTER);
            grid.add(lbl, c + 1, 0);
        }
        // Row headers 1–10
        for (int r = 0; r < GameModel.SIZE; r++) {
            Label lbl = new Label(String.valueOf(r + 1));
            lbl.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
            lbl.setTextFill(Color.web(C_HEADER));
            lbl.setMinWidth(28);
            lbl.setAlignment(Pos.CENTER_RIGHT);
            grid.add(lbl, 0, r + 1);
        }
        // Cells
        for (int r = 0; r < GameModel.SIZE; r++)
            for (int c = 0; c < GameModel.SIZE; c++) {
                StackPane cell = buildCell(r, c);
                cells[r][c] = cell;
                grid.add(cell, c + 1, r + 1);
            }

        // Outer wrapper — parchment padding so the border looks like screenshot
        VBox wrap = new VBox(grid);
        wrap.setAlignment(Pos.CENTER);
        wrap.setPadding(new Insets(14, 16, 16, 10));
        wrap.setStyle("-fx-background-color:" + PARCHMENT + ";");

        VBox outer = new VBox(wrap);
        outer.setAlignment(Pos.CENTER);
        outer.setPadding(new Insets(10, 0, 10, 10));
        outer.setStyle("-fx-background-color:" + PARCHMENT + ";");
        return outer;
    }

    StackPane buildCell(int r, int c) {
        Rectangle bg = new Rectangle(54, 54);
        bg.setArcWidth(4);
        bg.setArcHeight(4);
        bg.setFill(Paint.valueOf(C_WATER));
        // No stroke — parchment gap between cells IS the border (like screenshot)
        bg.setStroke(Color.TRANSPARENT);
        bg.setStrokeWidth(0);

        Label icon = new Label();
        icon.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        icon.setMouseTransparent(true);

        StackPane cell = new StackPane(bg, icon);
        cell.setMinSize(54, 54);
        cell.setMaxSize(54, 54);
        cell.setCursor(javafx.scene.Cursor.HAND);

        cell.setOnMouseEntered(e -> {
            if (model.board[r][c] == GameModel.CellState.EMPTY && !model.gameOver) {
                bg.setFill(Paint.valueOf(C_WATER_H));
                addGlow(cell, Color.web("#60b8e0"), 14);
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
            flashStatus("☠  These waters have already been charted — choose another coordinate.", GOLD_BRIGHT);
            return;
        }
        refreshCell(r, c, result);
        updateStats();

        String col = String.valueOf((char) ('A' + c));
        String row = String.valueOf(r + 1);

        if (result.equals("HIT")) {
            setStatus("⚔  Strike confirmed at " + col + row + "!  The vessel bleeds — press the attack!", CRIMSON);
            appendLog("[STRIKE]  " + col + row);
        } else if (result.startsWith("SUNK:")) {
            String[] parts = result.split(":");
            String name = parts[1]; int sz = Integer.parseInt(parts[2]);
            setStatus("☠  " + name + " (" + sz + " guns) sent to the depths at " + col + row + "!", "#b02010");
            appendLog("[SUNK]    " + name + "  ☠  " + col + row);
            shakeSunk(name);
        } else {
            int rh = model.rowHint(r), ch = model.colHint(c);
            setStatus("○  The cannonball finds only sea at " + col + row
                    + "     Row " + row + ": " + rh + " cell(s) remain"
                    + "   —   Col " + col + ": " + ch + " remain", BROWN_MID);
            appendLog("[MISS]    " + col + row + "  R:" + rh + "  C:" + ch);
        }
        if (model.gameOver) showVictory();
    }

    void refreshCell(int r, int c, String result) {
        StackPane cell = cells[r][c];
        Rectangle bg   = (Rectangle) cell.getChildren().get(0);
        Label icon     = (Label)     cell.getChildren().get(1);
        if (cell.getChildren().size() > 2) cell.getChildren().remove(2);
        cell.setEffect(null);

        GameModel.CellState st = model.board[r][c];

        if (st == GameModel.CellState.HIT) {
            bg.setFill(Paint.valueOf(C_HIT));
            icon.setText("⚔");
            icon.setFont(Font.font("Georgia", FontWeight.BOLD, 22));
            icon.setTextFill(Color.web("#f8e8b0"));
            addGlow(cell, Color.web("#e04010"), 20);
            pulseEffect(cell);

        } else if (st == GameModel.CellState.SUNK) {
            for (GameModel.Ship s : model.ships)
                if (s.isSunk())
                    for (int[] pos : s.cells) refreshSunkCell(pos[0], pos[1]);

        } else {
            // MISS — slightly darker teal + hint labels
            bg.setFill(Paint.valueOf(C_MISS));
            icon.setText("");
            int rh = model.rowHint(r), ch = model.colHint(c);

            VBox hint = new VBox(1);
            hint.setAlignment(Pos.CENTER);
            hint.setMouseTransparent(true);

            Label rLabel = new Label("R:" + rh);
            rLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 9));
            rLabel.setTextFill(Color.web("#a8d0e8"));

            Label cLabel = new Label("C:" + ch);
            cLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 9));
            cLabel.setTextFill(Color.web("#a8d0e8"));

            Label dot = new Label("◆");
            dot.setFont(Font.font("Georgia", 8));
            dot.setTextFill(Color.web("#5898b8"));

            hint.getChildren().addAll(rLabel, cLabel, dot);
            cell.getChildren().add(hint);
        }
    }

    void refreshSunkCell(int r, int c) {
        StackPane cell = cells[r][c];
        Rectangle bg   = (Rectangle) cell.getChildren().get(0);
        Label icon     = (Label)     cell.getChildren().get(1);
        if (cell.getChildren().size() > 2) cell.getChildren().remove(2);
        bg.setFill(Paint.valueOf(C_SUNK));
        icon.setText("☠");
        icon.setFont(Font.font("Georgia", FontWeight.BOLD, 22));
        icon.setTextFill(Color.web("#f08080"));
        addGlow(cell, Color.web("#c01010"), 16);
    }

    // ── Sidebar ──────────────────────────────────────────────
    VBox buildSidebar() {
        VBox side = new VBox(12);
        side.setPadding(new Insets(14, 16, 14, 12));
        side.setPrefWidth(SIDEBAR_W);
        side.setMinWidth(SIDEBAR_W);
        side.setMaxWidth(SIDEBAR_W);
        side.setFillWidth(true);
        // Sidebar = slightly darker parchment with left divider
        side.setStyle(
                "-fx-background-color:" + PARCHMENT_MID + ";" +
                        "-fx-border-color:" + PARCHMENT_DARK + ";-fx-border-width:0 0 0 2;"
        );

        side.getChildren().add(sectionLabel("⚓  BATTLE STATUS"));
        side.getChildren().add(buildStatsCard());

        side.getChildren().add(sectionLabel("⚔  TACTICAL ORDERS"));
        side.getChildren().add(buildHowToPlayCard());

        side.getChildren().add(sectionLabel("⚔  FIELD CODEX"));
        side.getChildren().add(buildLegendCard());

        side.getChildren().add(sectionLabel("⚔  COMBAT LOG"));
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefRowCount(6);
        logArea.setMaxWidth(Double.MAX_VALUE);
        logArea.setWrapText(true);
        logArea.setStyle(
                "-fx-control-inner-background:#e8d8b8;" +
                        "-fx-text-fill:" + BROWN_DARK + ";" +
                        "-fx-font-family:'Courier New';" +
                        "-fx-font-size:11px;" +
                        "-fx-border-color:" + PARCHMENT_DARK + ";-fx-border-width:1.5;"
        );
        VBox.setVgrow(logArea, Priority.ALWAYS);
        side.getChildren().add(logArea);

        return side;
    }

    Label sectionLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("section-label");
        l.setMaxWidth(Double.MAX_VALUE);
        l.setPadding(new Insets(4, 0, 4, 0));
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

        g.add(statBlock("Cannons Fired", attemptsVal),  0, 0);
        g.add(statBlock("Strikes",        hitsVal),      1, 0);
        g.add(statBlock("Vessels Afloat", shipsVal),     0, 1);
        g.add(statBlock("Accuracy",       accuracyVal),  1, 1);
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

    // ── Tactical Orders Card ─────────────────────────────────
    VBox buildHowToPlayCard() {
        VBox box = new VBox(0);
        box.getStyleClass().add("card");
        box.setMaxWidth(Double.MAX_VALUE);

        String[][] steps = {
                {"THE MISSION",
                        "Locate and destroy all 5 enemy vessels hidden on the 10x10 sea grid."},
                {"OPENING FIRE",
                        "Click any sea tile on the grid to fire a cannonball at that coordinate."},
                {"STRIKES & MISSES",
                        "⚔ Sword = direct strike!  ☠ Skull = vessel fully sunk!  Dark tile = miss."},
                {"INTELLIGENCE",
                        "After a miss, R:2 means 2 ship cells remain in that Row. C:3 in that Column. Use these clues to locate vessels faster."},
                {"THE FLEET",
                        "Flagship (5), Man-of-War (4), Brigantine (3), Corsair (3), Sloop (2). Positions randomised each campaign."},
                {"VICTORY",
                        "Sink all 5 vessels to win The Battleship War! Fewer shots earns greater honour."}
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
                sep.setStyle("-fx-background-color:" + PARCHMENT_DARK + ";-fx-border-color:" + PARCHMENT_DARK + ";");
                box.getChildren().add(sep);
            }
        }
        return box;
    }

    // ── Field Codex Card ─────────────────────────────────────
    VBox buildLegendCard() {
        VBox box = new VBox(7);
        box.getStyleClass().add("card");
        box.setMaxWidth(Double.MAX_VALUE);
        box.getChildren().addAll(
                legendRow(C_WATER,   "Uncharted sea — no contact"),
                legendRow(C_HIT,     "⚔  Strike confirmed on vessel"),
                legendRow(C_SUNK,    "☠  Vessel sent to the depths"),
                legendRow(C_MISS,    "○  Miss — R/C intel shown")
        );
        return box;
    }

    HBox legendRow(String color, String text) {
        Rectangle swatch = new Rectangle(14, 14);
        swatch.setArcWidth(4); swatch.setArcHeight(4);
        swatch.setFill(Paint.valueOf(color));
        swatch.setStroke(Color.web(PARCHMENT_DARK));
        swatch.setStrokeWidth(1);
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Georgia", 11));
        lbl.setTextFill(Color.web(BROWN_DARK));
        HBox row = new HBox(8, swatch, lbl);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    // ── Status Bar ───────────────────────────────────────────
    HBox buildStatusBar() {
        statusLabel = new Label(
                "⚔  Ready for engagement — select a coordinate to begin The Battleship War!"
        );
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
        glow.setSpread(0.35);
        node.setEffect(glow);
    }

    void pulseEffect(Node node) {
        ScaleTransition st = new ScaleTransition(Duration.millis(140), node);
        st.setFromX(1.0); st.setFromY(1.0);
        st.setToX(1.16);  st.setToY(1.16);
        st.setAutoReverse(true); st.setCycleCount(2);
        st.play();
    }

    void shakeSunk(String name) {
        for (GameModel.Ship s : model.ships)
            if (s.name.equals(name))
                for (int[] pos : s.cells) {
                    TranslateTransition tt =
                            new TranslateTransition(Duration.millis(55), cells[pos[0]][pos[1]]);
                    tt.setByX(5);
                    tt.setAutoReverse(true);
                    tt.setCycleCount(6);
                    tt.play();
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
                Rectangle bg   = (Rectangle) cell.getChildren().get(0);
                Label icon     = (Label)     cell.getChildren().get(1);
                bg.setFill(Paint.valueOf(C_WATER));
                bg.setStroke(Color.TRANSPARENT);
                icon.setText("");
                icon.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
                cell.setEffect(null);
                if (cell.getChildren().size() > 2) cell.getChildren().remove(2);
                int fr = r, fc = c;
                cell.setOnMouseEntered(e -> {
                    if (model.board[fr][fc] == GameModel.CellState.EMPTY && !model.gameOver) {
                        bg.setFill(Paint.valueOf(C_WATER_H));
                        addGlow(cell, Color.web("#60b8e0"), 14);
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
        setStatus(
                "⚔  A new campaign begins — The Battleship War is upon us. Select a coordinate!",
                BROWN_MID
        );
        updateStats();
    }

    // ── Victory ──────────────────────────────────────────────
    void showVictory() {
        for (int r = 0; r < GameModel.SIZE; r++)
            for (int c = 0; c < GameModel.SIZE; c++)
                if (model.board[r][c] == GameModel.CellState.SUNK) {
                    StackPane cell = cells[r][c];
                    Timeline tl = new Timeline(
                            new KeyFrame(Duration.ZERO,
                                    new KeyValue(cell.opacityProperty(), 1.0)),
                            new KeyFrame(Duration.millis(350),
                                    new KeyValue(cell.opacityProperty(), 0.35)),
                            new KeyFrame(Duration.millis(700),
                                    new KeyValue(cell.opacityProperty(), 1.0))
                    );
                    tl.setCycleCount(4);
                    tl.play();
                }

        setStatus(
                "⚓  VICTORY!  All vessels destroyed in " + model.attempts
                        + " shots!   Accuracy: " + String.format("%.0f%%", model.accuracy())
                        + "   —   The Battleship War has been won!",
                CRIMSON
        );
        appendLog("VICTORY ⚓  " + model.attempts + " shots  |  "
                + String.format("%.0f%%", model.accuracy()) + " accuracy");
    }
}