package thederpgamer.betterfleets.gui.element;

import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.input.InputState;
import org.schema.schine.input.Mouse;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @since 06/28/2021
 */
public class GUIMouseUpdatedButtonPane extends GUIHorizontalButtonTablePane {

    public GUIMouseUpdatedButtonPane(InputState inputState, int columns, int rows, GUIElement parent) {
        super(inputState, columns, rows, parent);
    }

    @Override
    public void draw() {
        super.draw();
        try {
            setPos(Mouse.getX() + 15.0f, Mouse.getY() - 15.0f, 0.0f);
        } catch(Exception ignored) {
            cleanUp();
        }
    }
}