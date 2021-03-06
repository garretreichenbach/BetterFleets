package thederpgamer.betterfleets.utils;

import api.common.GameClient;
import api.common.GameServer;
import com.bulletphysics.linearmath.QuaternionUtil;
import com.bulletphysics.linearmath.Transform;
import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.ai.ShipAIEntity;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.BoundingBox;
import thederpgamer.betterfleets.manager.ConfigManager;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @version 1.0 - [09/12/2021]
 */
public class EntityUtils {

    public static float calculateFlankingBonus(SegmentController damaged, SegmentController damager) {
        float angle = EntityUtils.getRelativeFacingAngle(damaged, damager);
        float distance = EntityUtils.getDistance(damaged, damager);
        float modifier = ConfigManager.getSystemConfig().getConfigurableFloat("flanking-damage-bonus-multiplier", 1.15f);

        if(distance <= 500) modifier += 1.75f;
        else if(distance <= 1000) modifier += 1.5f;
        else if(distance <= 3000) modifier += 1.3f;
        else if(distance <= 10000) modifier += 1.15f;
        else return 0;

        if((angle <= -100 && angle >= -260) || (angle >= 100 && angle <= 260)) return modifier;
        else return 0;
    }

    public static float getRelativeFacingAngle(SegmentController from, SegmentController to) {
        Transform fromTransform = new Transform(from.getWorldTransform());
        Transform toTransform = new Transform(to.getWorldTransform());
        if(getDistance(from, to) < ConfigManager.getSystemConfig().getConfigurableFloat("flanking-damage-bonus-max-distance", 10000)) {
            Quat4f fromRotation = new Quat4f(fromTransform.getRotation(new Quat4f()));
            Quat4f toRotation = new Quat4f(toTransform.getRotation(new Quat4f()));
            toRotation.negate();
            fromRotation.add(toRotation);
            return (float) Math.toDegrees(QuaternionUtil.getAngle(fromRotation));
        }
        return 0;
    }

    public static void moveToPosition(Ship ship, Transform position) {
        ShipAIEntity aiEntity = ship.getAiConfiguration().getAiEntityState();
        Vector3f moveVector = new Vector3f();
        Vector3f posVector = new Vector3f(position.origin);
        Transform transform = new Transform(ship.getWorldTransform());
        moveVector.cross(transform.origin, posVector);
        ship.getNetworkObject().orientationDir.set(0, 0, 0, 0);
        ship.getNetworkObject().targetPosition.set(posVector);
        ship.getNetworkObject().moveDir.set(moveVector);
        aiEntity.moveTo(GameServer.getServerState().getController().getTimer(), moveVector, true);
    }

    /**
     * Returns the closest entity within the specified range the client is looking at.
     * <p>If the client is currently piloting an entity, will add the distance from the entity's center to it's front.</p>
     * @return The entity the client is looking at, if one can be found.
     */
    public static SimpleTransformableSendableObject<?> getLookingAtWithSizeOffset(boolean respectFilters, float maxDistance, boolean selectClosest) {
        float distanceToFront = 0.0f;
        if(GameClient.getCurrentControl() instanceof SegmentController) {
            SegmentController entity = (SegmentController) GameClient.getCurrentControl();
            BoundingBox boundingBox = entity.getBoundingBox();

            Vector3f forward = GlUtil.getForwardVector(new Vector3f(), entity.getWorldTransform());
            Vector3f entityMax = new Vector3f(boundingBox.max);
            Vector3f entityCenter = boundingBox.getCenter(new Vector3f());

            entity.getWorldTransform().transform(entityMax);
            entity.getWorldTransform().transform(entityCenter);

            float distance;
            if(entityMax.lengthSquared() > entityCenter.lengthSquared()) distance = distance(entityMax, entityCenter);
            else distance = distance(entityCenter, entityMax);

            forward.scale(distance);
            distanceToFront += forward.z;
        }
        return PlayerInteractionControlManager.getLookingAt(GameClient.getClientState(), respectFilters, maxDistance + distanceToFront, false, 0.0f, selectClosest);
    }

    private static float distance(Vector3f vectorA, Vector3f vectorB) {
        return (new Vector3f(Math.abs(vectorA.x - vectorB.x), Math.abs(vectorA.y - vectorB.y), Math.abs(vectorA.z - vectorB.z))).length();
    }

    public static float getDistance(SegmentController entityA, SegmentController entityB) {
        Transform entityATransform = new Transform(entityA.getWorldTransform());
        Transform entityBTransform = new Transform(entityB.getWorldTransform());
        return distance(entityATransform.origin, entityBTransform.origin);
    }
}
