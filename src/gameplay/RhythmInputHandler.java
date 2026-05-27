package gameplay;

import settings.GameplaySettings;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.function.IntConsumer;

public class RhythmInputHandler {
    private final GameplaySettings options;
    private int[] boundLaneKeyCodes = new int[0];

    public RhythmInputHandler(GameplaySettings options) {
        this.options = options;
    }

    public void install(
            JComponent component,
            IntConsumer onLaneKeyPressed,
            IntConsumer onLaneKeyReleased,
            Runnable onEscapePressed
    ) {
        removeLaneKeyBindings(component);

        int[] laneKeyCodes = options.laneKeyCodes();
        for (int laneKeyCode : laneKeyCodes) {
            bindPress(component, laneKeyCode, onLaneKeyPressed);
            bindRelease(component, laneKeyCode, onLaneKeyReleased);
        }
        boundLaneKeyCodes = laneKeyCodes;

        bindPress(component, KeyEvent.VK_ESCAPE, ignored -> onEscapePressed.run());
    }

    private void removeLaneKeyBindings(JComponent component) {
        for (int laneKeyCode : boundLaneKeyCodes) {
            component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .remove(KeyStroke.getKeyStroke(laneKeyCode, 0, false));
            component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .remove(KeyStroke.getKeyStroke(laneKeyCode, 0, true));
            component.getActionMap().remove("pressed-" + laneKeyCode);
            component.getActionMap().remove("released-" + laneKeyCode);
        }
    }

    private void bindPress(JComponent component, int keyCode, IntConsumer onPressed) {
        String actionKey = "pressed-" + keyCode;
        component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(keyCode, 0, false), actionKey);
        component.getActionMap().put(actionKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onPressed.accept(keyCode);
            }
        });
    }

    private void bindRelease(JComponent component, int keyCode, IntConsumer onReleased) {
        String actionKey = "released-" + keyCode;
        component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(keyCode, 0, true), actionKey);
        component.getActionMap().put(actionKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onReleased.accept(keyCode);
            }
        });
    }
}
