# ⚓ BattleshipFX — JavaFX Battleship Game

A modern **Battleship strategy game** built using **JavaFX**.
Players must locate and destroy the enemy fleet hidden on a **10×10 ocean grid** using logical hints and strategic firing.

This project demonstrates **JavaFX UI design, animations, game logic, and event-driven programming in Java**.

---

## 🎮 Gameplay Overview

The enemy fleet is hidden randomly on a **10×10 board**.

Your objective:

> **Destroy all enemy ships using the fewest shots possible.**

Click any tile on the board to fire a torpedo.

Each shot results in:

* **HIT** → A ship was hit
* **MISS** → No ship at that location
* **SUNK** → You destroyed the entire ship

When a shot misses, **row and column hints** help you locate the ships.

---

## 🚢 Fleet Composition

The game includes **5 ships** placed randomly each round.

| Ship       | Size |
| ---------- | ---- |
| Carrier    | 5    |
| Battleship | 4    |
| Cruiser    | 3    |
| Submarine  | 3    |
| Destroyer  | 2    |

Ships **cannot overlap or touch each other**, ensuring fair gameplay.

---

## 🧠 Hint System

After every **MISS**, hints are displayed inside the tile:

* **R:X** → Number of ship cells remaining in that **Row**
* **C:X** → Number of ship cells remaining in that **Column**

Example:

```
R:2
C:3
```

Meaning:

* 2 ship cells remain somewhere in that **row**
* 3 ship cells remain somewhere in that **column**

These clues help players **strategically find ships faster**.

---

## 📊 Game Statistics

The sidebar tracks:

* **Shots Fired**
* **Hits**
* **Ships Remaining**
* **Accuracy %**

Accuracy formula:

```
accuracy = (hits / attempts) × 100
```

---

## ✨ Features

* ⚡ Modern **JavaFX UI**
* 🎯 Interactive **10×10 game board**
* 🎨 Animated **hit and sunk effects**
* 📜 **Battle log** recording every move
* 📊 Real-time **statistics panel**
* 🧠 Strategic **row/column hint system**
* 🔁 **New Game** restart button
* 🏆 **Victory animation and popup**

---

## 🖥️ User Interface

The application consists of four main sections:

### Game Board

A 10×10 clickable grid where players fire torpedoes.

### Statistics Panel

Displays gameplay metrics like shots fired and accuracy.

### How To Play

Explains rules and game mechanics.

### Battle Log

Shows a chronological list of all shots fired.

---

## 🛠️ Technologies Used

* **Java**
* **JavaFX**
* **Object-Oriented Programming**
* **Event-Driven Programming**

JavaFX components used include:

* `Stage`
* `Scene`
* `GridPane`
* `StackPane`
* `VBox / HBox`
* `Rectangle`
* `Label`
* `TextArea`
* `Timeline`
* `FadeTransition`
* `ScaleTransition`

---

## 📂 Project Structure

```
BattleshipFX
│
└── BattleshipFX.java
    │
    ├── GameModel
    │   ├── Board logic
    │   ├── Ship placement
    │   └── Fire mechanics
    │
    └── GameUI
        ├── Board rendering
        ├── Sidebar (stats, hints, log)
        ├── Animations & effects
        └── Event handling
```

---

## ▶️ How to Run

### 1. Clone the repository

```bash
git clone https://github.com/your-username/battleshipfx.git
```

### 2. Open in your IDE

Recommended IDEs:

* IntelliJ IDEA
* VS Code
* Eclipse

### 3. Configure JavaFX

Add JavaFX SDK to VM options:

```
--module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml
```

### 4. Run the application

Run the main class:

```
BattleshipFX.java
```

---

## 🏆 Win Condition

The game ends when:

```
All 5 ships are sunk
```

After victory, the game displays:

* Total shots taken
* Hit accuracy
* Victory animation

---

## 📈 Learning Purpose

This project is useful for learning:

* JavaFX UI development
* Grid-based game logic
* Animations in JavaFX
* Object-oriented design
* Interactive desktop applications

---

## 👨‍💻 Author

**Manthan Suthar**

Computer Engineering Student
Government Engineering College, Modasa

---

## 📜 License

This project is open source and free to use for **learning and educational purposes**.
