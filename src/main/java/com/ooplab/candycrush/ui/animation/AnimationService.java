package com.ooplab.candycrush.ui.animation;

import com.ooplab.candycrush.domain.*;
import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.*;
import java.util.logging.Logger;

public final class AnimationService {
    private static final Logger LOG = Logger.getLogger(AnimationService.class.getName());
    private final GridPane boardGrid;
    private final Pane overlayPane;
    private final CandyNodeManager nodeManager;
    private boolean animating;

    public AnimationService(GridPane boardGrid, Pane overlayPane, CandyNodeManager nodeManager) {
        this.boardGrid = boardGrid;
        this.overlayPane = overlayPane;
        this.nodeManager = nodeManager;
        this.animating = false;
    }

    public boolean isAnimating() {
        return animating;
    }

    public void playEvents(List<BoardEvent> events, Board preSnapshot, Board postSnapshot, Runnable onComplete) {
        if (events.isEmpty()) {
            if (onComplete != null) onComplete.run();
            return;
        }

        animating = true;
        List<Animation> sequence = new ArrayList<>();

        Board currentBefore = preSnapshot.copy();

        for (BoardEvent event : events) {
            Animation anim = buildAnimation(event, currentBefore, postSnapshot);
            if (anim != null) {
                sequence.add(anim);
                PauseTransition pause = new PauseTransition(Duration.millis(AnimationConfig.CASCADE_DELAY_MS));
                sequence.add(pause);
                applyEventToSnapshot(currentBefore, event);
            }
        }

        PauseTransition finalPause = new PauseTransition(Duration.millis(100));
        finalPause.setOnFinished(e -> {
            animating = false;
            if (onComplete != null) onComplete.run();
        });
        sequence.add(finalPause);

        SequentialTransition full = new SequentialTransition();
        full.getChildren().addAll(sequence);
        full.play();
    }

    private Animation buildAnimation(BoardEvent event, Board currentBefore, Board postSnapshot) {
        return switch (event.type()) {
            case SWAP -> null;
            case CLEAR -> buildClearAnimation(event);
            case SPECIAL_SPAWN -> buildSpecialSpawnAnimation(event);
            case GRAVITY -> buildGravityAnimation(currentBefore, postSnapshot);
            case SCORE -> buildScorePopupAnimation(event);
            default -> null;
        };
    }

    private void applyEventToSnapshot(Board board, BoardEvent event) {
        if (event.type() == BoardEventType.CLEAR) {
            Collection<?> positions = (Collection<?>) event.payload().get("positions");
            if (positions != null) {
                for (Object obj : positions) {
                    if (obj instanceof Position pos) {
                        board.setCandy(pos, null);
                    }
                }
            }
        }
    }

    /* ─── Swap ─── */

    public void playSwap(Position from, Position to, Runnable onComplete) {
        StackPane nodeFrom = nodeManager.getNode(from);
        StackPane nodeTo = nodeManager.getNode(to);
        if (nodeFrom == null || nodeTo == null) {
            if (onComplete != null) onComplete.run();
            return;
        }

        double translateX = (to.col() - from.col()) * AnimationConfig.CELL_STEP_PX;
        double translateY = (to.row() - from.row()) * AnimationConfig.CELL_STEP_PX;

        TranslateTransition t1 = new TranslateTransition(Duration.millis(AnimationConfig.SWAP_DURATION_MS), nodeFrom);
        t1.setToX(translateX);
        t1.setToY(translateY);
        t1.setInterpolator(Interpolator.EASE_BOTH);

        TranslateTransition t2 = new TranslateTransition(Duration.millis(AnimationConfig.SWAP_DURATION_MS), nodeTo);
        t2.setToX(-translateX);
        t2.setToY(-translateY);
        t2.setInterpolator(Interpolator.EASE_BOTH);

        ParallelTransition parallel = new ParallelTransition(t1, t2);
        parallel.setOnFinished(e -> {
            nodeFrom.setTranslateX(0);
            nodeFrom.setTranslateY(0);
            nodeTo.setTranslateX(0);
            nodeTo.setTranslateY(0);
            nodeManager.swapPositions(from, to);
            if (onComplete != null) onComplete.run();
        });
        parallel.play();
    }

    public void playReverseSwap(Position from, Position to, Runnable onComplete) {
        StackPane nodeFrom = nodeManager.getNode(from);
        StackPane nodeTo = nodeManager.getNode(to);
        if (nodeFrom == null || nodeTo == null) {
            if (onComplete != null) onComplete.run();
            return;
        }

        double translateX = (to.col() - from.col()) * AnimationConfig.CELL_STEP_PX;
        double translateY = (to.row() - from.row()) * AnimationConfig.CELL_STEP_PX;

        TranslateTransition t1 = new TranslateTransition(Duration.millis(AnimationConfig.SWAP_DURATION_MS), nodeFrom);
        t1.setToX(-translateX);
        t1.setToY(-translateY);
        t1.setInterpolator(Interpolator.EASE_BOTH);

        TranslateTransition t2 = new TranslateTransition(Duration.millis(AnimationConfig.SWAP_DURATION_MS), nodeTo);
        t2.setToX(translateX);
        t2.setToY(translateY);
        t2.setInterpolator(Interpolator.EASE_BOTH);

        ParallelTransition parallel = new ParallelTransition(t1, t2);
        parallel.setOnFinished(e -> {
            nodeFrom.setTranslateX(0);
            nodeFrom.setTranslateY(0);
            nodeTo.setTranslateX(0);
            nodeTo.setTranslateY(0);
            if (onComplete != null) onComplete.run();
        });
        parallel.play();
    }

    /* ─── Clear ─── */

    private Animation buildClearAnimation(BoardEvent event) {
        Collection<?> positions = (Collection<?>) event.payload().get("positions");
        Integer chain = (Integer) event.payload().get("chain");
        if (positions == null || positions.isEmpty()) return null;
        Set<Position> posSet = new HashSet<>();
        for (Object p : positions) {
            if (p instanceof Position pos) posSet.add(pos);
        }

        List<Animation> parallelAnims = new ArrayList<>();

        for (Position pos : posSet) {
            StackPane node = nodeManager.getNode(pos);
            if (node == null) continue;

            ScaleTransition scale = new ScaleTransition(Duration.millis(AnimationConfig.CLEAR_DURATION_MS), node);
            scale.setToX(0.0);
            scale.setToY(0.0);

            FadeTransition fade = new FadeTransition(Duration.millis(AnimationConfig.CLEAR_DURATION_MS), node);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);

            parallelAnims.add(new ParallelTransition(scale, fade));
        }

        ParallelTransition clearAll = new ParallelTransition(parallelAnims.toArray(new Animation[0]));
        clearAll.setOnFinished(e -> {
            for (Position pos : posSet) {
                StackPane node = nodeManager.getNode(pos);
                if (node != null) {
                    boardGrid.getChildren().remove(node);
                    nodeManager.unregister(pos);
                }
            }
            if (chain != null && chain >= 4) {
                playBoardShake();
            }
            if (chain != null && chain >= 2) {
                playChainPopup(chain);
            }
        });
        return clearAll;
    }

    /* ─── Gravity ─── */

    private Animation buildGravityAnimation(Board before, Board after) {
        GravityMoveCalculator calculator = new GravityMoveCalculator();
        Map<Position, Position> moves = calculator.calculate(before, after);
        if (moves.isEmpty()) return null;

        List<Animation> fallAnims = new ArrayList<>();

        for (Map.Entry<Position, Position> entry : moves.entrySet()) {
            Position oldPos = entry.getKey();
            Position newPos = entry.getValue();
            StackPane node = nodeManager.getNode(oldPos);
            if (node == null) continue;

            int rowDiff = newPos.row() - oldPos.row();
            double pixelOffset = rowDiff * AnimationConfig.CELL_STEP_PX;

            LOG.info(() -> String.format("Gravity: (%d,%d) -> (%d,%d) rows=%d px=%.0f duration=%.0fms",
                    oldPos.row(), oldPos.col(), newPos.row(), newPos.col(), rowDiff, pixelOffset, gravityDuration(rowDiff)));

            node.setTranslateY(pixelOffset);
            double duration = gravityDuration(rowDiff);
            TranslateTransition fall = new TranslateTransition(Duration.millis(duration), node);
            fall.setToY(0);
            fall.setInterpolator(new BounceInterpolator());

            final Position oldP = oldPos;
            final Position newP = newPos;
            fall.setOnFinished(e -> {
                LOG.info(() -> String.format("Gravity done: (%d,%d) -> (%d,%d)", oldP.row(), oldP.col(), newP.row(), newP.col()));
                nodeManager.updatePosition(oldP, newP, node);
            });

            fallAnims.add(fall);
        }

        return fallAnims.isEmpty() ? null : new ParallelTransition(fallAnims.toArray(new Animation[0]));
    }

    /* ─── Refill ─── */

    public void playRefill(Map<Position, Candy> newCandies, Runnable onComplete) {
        if (newCandies.isEmpty()) {
            if (onComplete != null) onComplete.run();
            return;
        }

        LOG.info(() -> "Refill: " + newCandies.size() + " new candies");
        List<Animation> dropAnims = new ArrayList<>();

        for (Map.Entry<Position, Candy> entry : newCandies.entrySet()) {
            Position pos = entry.getKey();
            Candy candy = entry.getValue();
            StackPane cell = createCandyCell(candy);
            double startY = -(pos.row() + 1) * AnimationConfig.CELL_STEP_PX;
            cell.setTranslateY(startY);
            cell.setOpacity(0);

            double duration = gravityDuration(pos.row() + 1);
            LOG.info(() -> String.format("  Refill (%d,%d): startY=%.0f duration=%.0fms",
                    pos.row(), pos.col(), startY, duration));
            TranslateTransition drop = new TranslateTransition(Duration.millis(duration), cell);
            drop.setToY(0);
            drop.setInterpolator(new BounceInterpolator());

            FadeTransition fadeIn = new FadeTransition(Duration.millis(Math.min(150, duration / 2)), cell);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            dropAnims.add(new ParallelTransition(drop, fadeIn));

            boardGrid.add(cell, pos.col(), pos.row());
            nodeManager.register(pos, cell);
        }

        ParallelTransition allDrops = new ParallelTransition(dropAnims.toArray(new Animation[0]));
        allDrops.setOnFinished(e -> {
            for (Node cell : boardGrid.getChildren()) {
                cell.setTranslateY(0);
                cell.setOpacity(1);
            }
            if (onComplete != null) onComplete.run();
        });
        allDrops.play();
    }

    /* ─── Special Spawn ─── */

    private Animation buildSpecialSpawnAnimation(BoardEvent event) {
        Position pos = (Position) event.payload().get("position");
        if (pos == null) return null;

        StackPane node = nodeManager.getNode(pos);
        if (node == null) return null;

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(AnimationConfig.SPECIAL_SPAWN_DURATION_MS / 2), node);
        scaleUp.setToX(1.3);
        scaleUp.setToY(1.3);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(AnimationConfig.SPECIAL_SPAWN_DURATION_MS / 2), node);
        scaleDown.setFromX(1.3);
        scaleDown.setFromY(1.3);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);

        return new SequentialTransition(scaleUp, scaleDown);
    }

    /* ─── Score Popup ─── */

    private Animation buildScorePopupAnimation(BoardEvent event) {
        Integer delta = (Integer) event.payload().get("delta");
        if (delta == null || delta == 0) return null;

        Label popup = new Label("+" + delta);
        popup.getStyleClass().add("score-popup");
        popup.setTranslateX(boardGrid.getWidth() / 2 - 30);
        popup.setTranslateY(boardGrid.getHeight() / 2);
        popup.setOpacity(1.0);
        overlayPane.getChildren().add(popup);

        TranslateTransition rise = new TranslateTransition(Duration.millis(AnimationConfig.SCORE_POPUP_DURATION_MS), popup);
        rise.setByY(-60);
        rise.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition fade = new FadeTransition(Duration.millis(AnimationConfig.SCORE_POPUP_DURATION_MS), popup);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);

        ParallelTransition parallel = new ParallelTransition(rise, fade);
        parallel.setOnFinished(e -> overlayPane.getChildren().remove(popup));
        return parallel;
    }

    /* ─── Chain Popup ─── */

    private void playChainPopup(int chain) {
        Label popup = new Label("x" + chain);
        popup.getStyleClass().add("chain-popup");
        popup.setTranslateX(boardGrid.getWidth() / 2 - 20);
        popup.setTranslateY(boardGrid.getHeight() / 2 + 30);
        popup.setOpacity(1.0);
        overlayPane.getChildren().add(popup);

        TranslateTransition rise = new TranslateTransition(Duration.millis(800), popup);
        rise.setByY(-40);
        rise.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition fade = new FadeTransition(Duration.millis(800), popup);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);

        ParallelTransition parallel = new ParallelTransition(rise, fade);
        parallel.setOnFinished(e -> overlayPane.getChildren().remove(popup));
        parallel.play();
    }

    /* ─── Board Shake ─── */

    private void playBoardShake() {
        Timeline shake = new Timeline();
        double shakeAmount = 5.0;
        int cycles = 6;
        for (int i = 0; i < cycles; i++) {
            double t = (i * AnimationConfig.BOARD_SHAKE_DURATION_MS) / (double) cycles;
            KeyValue kv = new KeyValue(boardGrid.translateXProperty(),
                    (i % 2 == 0 ? shakeAmount : -shakeAmount) * (1.0 - (double) i / cycles));
            KeyFrame kf = new KeyFrame(Duration.millis(t), kv);
            shake.getKeyFrames().add(kf);
        }
        shake.setOnFinished(e -> boardGrid.setTranslateX(0));
        shake.play();
    }

    /* ─── Helpers ─── */

    private double gravityDuration(int rowDiff) {
        double base = 120.0;
        double perRow = 80.0;
        return Math.min(base + rowDiff * perRow, 600.0);
    }

    private StackPane createCandyCell(Candy candy) {
        StackPane cell = new StackPane();
        cell.getStyleClass().add("board-cell");

        Region base = new Region();
        base.setMinSize(40, 40);
        base.setPrefSize(40, 40);
        base.setMaxSize(40, 40);
        base.setBackground(new javafx.scene.layout.Background(
                new javafx.scene.layout.BackgroundFill(candyColor(candy),
                        new javafx.scene.layout.CornerRadii(14), null)));
        cell.getChildren().add(base);

        if (candy.specialType() == SpecialType.STRIPED_ROW) {
            Region stripe = new Region();
            stripe.setMinSize(36, 3);
            stripe.setBackground(new javafx.scene.layout.Background(
                    new javafx.scene.layout.BackgroundFill(Color.web("rgba(255,255,255,0.65)"),
                            new javafx.scene.layout.CornerRadii(2), null)));
            cell.getChildren().add(stripe);
        } else if (candy.specialType() == SpecialType.STRIPED_COLUMN) {
            Region stripe = new Region();
            stripe.setMinSize(3, 36);
            stripe.setBackground(new javafx.scene.layout.Background(
                    new javafx.scene.layout.BackgroundFill(Color.web("rgba(255,255,255,0.65)"),
                            new javafx.scene.layout.CornerRadii(2), null)));
            cell.getChildren().add(stripe);
        } else if (candy.specialType() == SpecialType.WRAPPED) {
            Region border = new Region();
            border.setMinSize(40, 40);
            border.setBorder(new Border(
                    new BorderStroke(Color.web("#ffd43b"),
                            BorderStrokeStyle.SOLID,
                            new CornerRadii(14),
                            new BorderWidths(3))));
            cell.getChildren().add(border);
        }

        return cell;
    }

    private Color candyColor(Candy candy) {
        if (candy.specialType() == SpecialType.COLOR_BOMB) {
            return Color.web("#2d3436");
        }
        if (candy.color() == null) return Color.web("#636e72");
        return switch (candy.color()) {
            case RED -> Color.web("#ff6b6b");
            case GREEN -> Color.web("#51cf66");
            case BLUE -> Color.web("#4dabf7");
            case YELLOW -> Color.web("#ffd43b");
            case ORANGE -> Color.web("#ffa94d");
            case PURPLE -> Color.web("#b197fc");
        };
    }
}
