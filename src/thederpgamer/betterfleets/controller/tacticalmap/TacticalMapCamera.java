package thederpgamer.betterfleets.controller.tacticalmap;

import api.common.GameClient;
import com.bulletphysics.linearmath.Transform;
import org.schema.game.common.controller.SegmentController;
import org.schema.schine.graphicsengine.camera.Camera;
import org.schema.schine.graphicsengine.camera.viewer.PositionableViewer;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.Mouse;
import thederpgamer.betterfleets.BetterFleets;
import thederpgamer.betterfleets.utils.Inputs;

import javax.vecmath.Vector3f;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @since 07/12/2021
 */
public class TacticalMapCamera extends Camera {

    public Transform transform;

    public TacticalMapCamera() {
        super(GameClient.getClientState(), new PositionableViewer());
    }

    @Override
    public void reset() {
        super.reset();
        if(GameClient.getCurrentControl() != null && GameClient.getCurrentControl() instanceof SegmentController) {
            Transform defaultTransform = new Transform();
            if(transform == null) transform = (((SegmentController) GameClient.getCurrentControl()).getWorldTransform());
            defaultTransform.set(transform);
            defaultTransform.origin.add(new Vector3f(-BetterFleets.getInstance().tacticalMapDrawer.sectorSize, BetterFleets.getInstance().tacticalMapDrawer.sectorSize, -BetterFleets.getInstance().tacticalMapDrawer.sectorSize));
            setLookAlgorithm(new TacticalCameraLook(this, transform));

            Transform temp = new Transform(transform);
            temp.basis.set(lookAt(false).basis);
            temp.basis.invert();
            getWorldTransform().set(temp);

            Vector3f backwards = new Vector3f(getForward());
            backwards.scale(((SegmentController) GameClient.getCurrentControl()).getBoundingBox().maxHalfSize() + 30);
            backwards.negate();
            getWorldTransform().origin.add(backwards);
        }
    }

    @Override
    public void update(Timer timer, boolean server) {
        alwaysAllowWheelZoom = true;
        if(GameClient.getCurrentControl() != null && GameClient.getCurrentControl() instanceof SegmentController) {
            if(transform == null) transform = (((SegmentController) GameClient.getCurrentControl()).getWorldTransform());
            if(Mouse.isGrabbed() && Mouse.isButtonDown(Inputs.MouseButtons.RIGHT_MOUSE.id)) {
                getLookAlgorithm().mouseRotate(server, (float)this.mouseState.dx / 1000.0F, (float)this.mouseState.dy / 1000.0F, 0.0F, this.getMouseSensibilityX(), this.getMouseSensibilityY(), 0.0F);
            }
        }
    }
}
