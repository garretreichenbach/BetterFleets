package thederpgamer.betterfleets.controller.tacticalmap;

import api.common.GameClient;
import api.common.GameCommon;
import api.network.packets.PacketUtil;
import api.utils.draw.ModWorldDrawer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.effects.Indication;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.player.faction.FactionRelation;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.graphicsengine.camera.Camera;
import org.schema.schine.graphicsengine.core.*;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIAncor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea;
import org.schema.schine.input.InputState;
import thederpgamer.betterfleets.gui.element.GUIRightClickButtonPane;
import thederpgamer.betterfleets.gui.element.sprite.TacticalMapEntityIndicator;
import thederpgamer.betterfleets.network.client.ClientRequestNearbyEntitiesPacket;

import javax.annotation.Nullable;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * World drawer for tactical map GUI.
 *
 * @author TheDerpGamer
 * @since 07/12/2021
 */
public class TacticalMapGUIDrawer extends ModWorldDrawer implements Drawable {

    public TacticalMapControlManager controlManager;
    public TacticalMapCamera camera;
    public boolean toggleDraw;

    public final int sectorSize;
    public final float maxDrawDistance;
    public final Vector3f labelOffset;

    private boolean initialized;
    private boolean firstTime = true;

    public final ConcurrentHashMap<Integer, TacticalMapEntityIndicator> drawMap;
    public final ArrayList<SegmentController> selectedEntities = new ArrayList<>();

    private GUIRightClickButtonPane buttonPane;

    public boolean drawMovementPaths = false;
    public boolean drawTargetingPaths = true;

    public TacticalMapGUIDrawer() {
        toggleDraw = false;
        initialized = false;
        sectorSize = (int) ServerConfig.SECTOR_SIZE.getCurrentState();
        maxDrawDistance = sectorSize * 4.0f;
        labelOffset = new Vector3f(0.0f, -20.0f, 0.0f);
        drawMap = new ConcurrentHashMap<>();
    }

    public void clearSelected() {
        ArrayList<SegmentController> temp = new ArrayList<>(selectedEntities);
        for(SegmentController i : temp) drawMap.get(i.getId()).onUnSelect();
    }

    public void toggleDraw() {
        if(!initialized) onInit();
        if(!(GameClient.getClientState().isInAnyStructureBuildMode() || GameClient.getClientState().isInFlightMode()) || GameClient.getClientState().getWorldDrawer().getGameMapDrawer().isMapActive()) {
            toggleDraw = false;
        } else toggleDraw = !toggleDraw;

        if(toggleDraw) {
            Controller.setCamera(camera);
            controlManager.onSwitch(true);
            if(firstTime) {
                camera.reset();
                firstTime = false;
            }
        } else {
            Controller.setCamera(getDefaultCamera());
            controlManager.onSwitch(false);
        }
    }

    public Camera getDefaultCamera() {
        if(GameClient.getClientState().isInAnyStructureBuildMode()) return GameClient.getClientState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getSegmentBuildController().getShipBuildCamera();
        else if(GameClient.getClientState().isInFlightMode()) return GameClient.getClientState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getShipExternalFlightController().shipCamera;
        else return Controller.getCamera();
    }

    @Override
    public void onInit() {
        controlManager = new TacticalMapControlManager(this);
        camera = new TacticalMapCamera();
        camera.reset();
        camera.alwaysAllowWheelZoom = false;
        recreateButtonPane(null);
        initialized = true;
    }

    @Override
    public void draw() {
        if(!initialized) onInit();
        if(toggleDraw && Controller.getCamera() instanceof TacticalMapCamera) {
            drawGrid(-sectorSize, sectorSize);
            GlUtil.glEnable(GL11.GL_BLEND);
            GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            //drawGrid(-sectorSize, sectorSize);
            drawMovementPaths = Keyboard.isKeyDown(Keyboard.KEY_LMENU);
            drawIndicators();
            GlUtil.glDisable(GL11.GL_BLEND);

            if(buttonPane.active) {
                GUIElement.enableOrthogonal();
                buttonPane.draw();
                GUIElement.disableOrthogonal();
            }
        } else cleanUp();
    }

    @Override
    public void update(Timer timer) {
        if(!toggleDraw || !(Controller.getCamera() instanceof TacticalMapCamera)) return;
        controlManager.update(timer);
        SegmentController currentEntity = getCurrentEntity();
        if(currentEntity != null) PacketUtil.sendPacketToServer(new ClientRequestNearbyEntitiesPacket(currentEntity));
    }


    @Override
    public void cleanUp() {

    }

    @Override
    public boolean isInvisible() {
        return false;
    }

    public void recreateButtonPane(@Nullable TacticalMapEntityIndicator indicator) {
        GUIAncor buttonPaneAnchor = new GUIAncor(GameClient.getClientState(), 150.0f, 300.0f);
        (buttonPane = new GUIRightClickButtonPane(GameClient.getClientState(), 1, 1, buttonPaneAnchor)).onInit();
        buttonPaneAnchor.attach(buttonPane);
        buttonPane.moveToMouse();

        if(indicator != null) {
            FactionRelation.RType relation = Objects.requireNonNull(GameCommon.getGameState()).getFactionManager().getRelation(Objects.requireNonNull(getCurrentEntity()).getFactionId(), indicator.getEntity().getFactionId());
            int currentFactionId = getCurrentEntity().getFactionId();
            int selectedFactionId = indicator.getEntity().getFactionId();
            int index = 0;

            buttonPane.addRow();
            buttonPane.addButton(0, index, "MOVE TO", GUIHorizontalArea.HButtonColor.PINK, new GUICallback() {
                @Override
                public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                    if(mouseEvent.pressedLeftMouse()) {
                        //Todo: Move to
                        buttonPane.active = false;
                        clearSelected();
                    }
                }

                @Override
                public boolean isOccluded() {
                    return false;
                }
            }, new GUIActivationCallback() {
                @Override
                public boolean isVisible(InputState inputState) {
                    return true;
                }

                @Override
                public boolean isActive(InputState inputState) {
                    return true;
                }
            });
            index ++;

            if(relation.equals(FactionRelation.RType.FRIEND)) {
                buttonPane.addRow();
                buttonPane.addButton(0, index, "DEFEND", GUIHorizontalArea.HButtonColor.GREEN, new GUICallback() {
                    @Override
                    public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                        if(mouseEvent.pressedLeftMouse()) {
                            //Todo: Defend target
                            buttonPane.active = false;
                            clearSelected();
                        }
                    }

                    @Override
                    public boolean isOccluded() {
                        return false;
                    }
                }, new GUIActivationCallback() {
                    @Override
                    public boolean isVisible(InputState inputState) {
                        return true;
                    }

                    @Override
                    public boolean isActive(InputState inputState) {
                        return true;
                    }
                });
                index ++;

                buttonPane.addRow();
                buttonPane.addButton(0, index, "REPAIR", GUIHorizontalArea.HButtonColor.GREEN, new GUICallback() {
                    @Override
                    public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                        if(mouseEvent.pressedLeftMouse()) {
                            //Todo: Repair target
                            buttonPane.active = false;
                            clearSelected();
                        }
                    }

                    @Override
                    public boolean isOccluded() {
                        return false;
                    }
                }, new GUIActivationCallback() {
                    @Override
                    public boolean isVisible(InputState inputState) {
                        return true;
                    }

                    @Override
                    public boolean isActive(InputState inputState) {
                        return true;
                    }
                });
                index ++;
            } else if(relation.equals(FactionRelation.RType.NEUTRAL)) {

            } else if(relation.equals(FactionRelation.RType.ENEMY)) {
                buttonPane.addRow();
                buttonPane.addButton(0, index, "ATTACK", GUIHorizontalArea.HButtonColor.RED, new GUICallback() {
                    @Override
                    public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                        if(mouseEvent.pressedLeftMouse()) {
                            //Todo: Attack target
                            buttonPane.active = false;
                            clearSelected();
                        }
                    }

                    @Override
                    public boolean isOccluded() {
                        return false;
                    }
                }, new GUIActivationCallback() {
                    @Override
                    public boolean isVisible(InputState inputState) {
                        return true;
                    }

                    @Override
                    public boolean isActive(InputState inputState) {
                        return true;
                    }
                });
                index ++;
            }

            buttonPane.addRow();
            buttonPane.addButton(0, index, "SCAN", GUIHorizontalArea.HButtonColor.ORANGE, new GUICallback() {
                @Override
                public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                    if(mouseEvent.pressedLeftMouse()) {
                        //Todo: Scan target
                        buttonPane.active = false;
                        clearSelected();
                    }
                }

                @Override
                public boolean isOccluded() {
                    return false;
                }
            }, new GUIActivationCallback() {
                @Override
                public boolean isVisible(InputState inputState) {
                    return true;
                }

                @Override
                public boolean isActive(InputState inputState) {
                    return true;
                }
            });
            index ++;
        } else buttonPane.cleanUp();
    }

    private void drawGrid(float start, float spacing) {
        GlUtil.glMatrixMode(GL11.GL_PROJECTION);
        GlUtil.glPushMatrix();

        float aspect = (float) GLFrame.getWidth() / (float) GLFrame.getHeight();
        GlUtil.gluPerspective(Controller.projectionMatrix, (Float) EngineSettings.G_FOV.getCurrentState(), aspect, 10, 25000, true);
        GlUtil.glMatrixMode(GL11.GL_MODELVIEW);
        Vector3i selectedPos = new Vector3i();

        selectedPos.x = ByteUtil.modU16(selectedPos.x);
        selectedPos.y = ByteUtil.modU16(selectedPos.y);
        selectedPos.z = ByteUtil.modU16(selectedPos.z);

        GlUtil.glBegin(GL11.GL_LINES);
        float size = spacing * 3;
        float end = (start + (1f / 3f) * size);
        float lineAlpha;
        float lineAlphaB;
        for(float i = 0; i < 3; i ++) {
            lineAlphaB = 1;
            lineAlpha = 1;

            if(i == 0) {
                lineAlpha = 0;
                lineAlphaB = 0.6f;
            } else if(i == 2) {
                lineAlpha = 0.6f;
                lineAlphaB = 0;
            }

            GlUtil.glColor4fForced(1, 1, 1, lineAlpha);
            GL11.glVertex3f(selectedPos.x * spacing, selectedPos.y * spacing, start);
            GlUtil.glColor4fForced(1, 1, 1, lineAlphaB);
            GL11.glVertex3f(selectedPos.x * spacing, selectedPos.y * spacing, end);

            GlUtil.glColor4fForced(1, 1, 1, lineAlpha);
            GL11.glVertex3f(start, selectedPos.y * spacing, selectedPos.z * spacing);
            GlUtil.glColor4fForced(1, 1, 1, lineAlphaB);
            GL11.glVertex3f(end, selectedPos.y * spacing, selectedPos.z * spacing);

            GlUtil.glColor4fForced(1, 1, 1, lineAlpha);
            GL11.glVertex3f(selectedPos.x * spacing, start, selectedPos.z * spacing);
            GlUtil.glColor4fForced(1, 1, 1, lineAlphaB);
            GL11.glVertex3f(selectedPos.x * spacing, end, selectedPos.z * spacing);

            GlUtil.glColor4fForced(1, 1, 1, lineAlpha);
            GL11.glVertex3f(selectedPos.x * spacing, (selectedPos.y + 1) * spacing, start);
            GlUtil.glColor4fForced(1, 1, 1, lineAlphaB);
            GL11.glVertex3f(selectedPos.x * spacing, (selectedPos.y + 1) * spacing, end);

            GlUtil.glColor4fForced(1, 1, 1, lineAlpha);
            GL11.glVertex3f(start, (selectedPos.y) * spacing, (selectedPos.z + 1) * spacing);
            GlUtil.glColor4fForced(1, 1, 1, lineAlphaB);
            GL11.glVertex3f(end, (selectedPos.y) * spacing, (selectedPos.z + 1) * spacing);

            GlUtil.glColor4fForced(1, 1, 1, lineAlpha);
            GL11.glVertex3f((selectedPos.x) * spacing, start, (selectedPos.z + 1) * spacing);
            GlUtil.glColor4fForced(1, 1, 1, lineAlphaB);
            GL11.glVertex3f((selectedPos.x) * spacing, end, (selectedPos.z + 1) * spacing);

            GlUtil.glColor4fForced(1, 1, 1, lineAlpha);
            GL11.glVertex3f((selectedPos.x + 1) * spacing, (selectedPos.y) * spacing, start);
            GlUtil.glColor4fForced(1, 1, 1, lineAlphaB);
            GL11.glVertex3f((selectedPos.x + 1) * spacing, (selectedPos.y) * spacing, end);

            GlUtil.glColor4fForced(1, 1, 1, lineAlpha);
            GL11.glVertex3f(start, (selectedPos.y + 1) * spacing, (selectedPos.z) * spacing);
            GlUtil.glColor4fForced(1, 1, 1, lineAlphaB);
            GL11.glVertex3f(end, (selectedPos.y + 1) * spacing, (selectedPos.z) * spacing);

            GlUtil.glColor4fForced(1, 1, 1, lineAlpha);
            GL11.glVertex3f((selectedPos.x + 1) * spacing, start, (selectedPos.z) * spacing);
            GlUtil.glColor4fForced(1, 1, 1, lineAlphaB);
            GL11.glVertex3f((selectedPos.x + 1) * spacing, end, (selectedPos.z) * spacing);

            GlUtil.glColor4fForced(1, 1, 1, lineAlpha);
            GL11.glVertex3f((selectedPos.x + 1) * spacing, (selectedPos.y + 1) * spacing, start);
            GlUtil.glColor4fForced(1, 1, 1, lineAlphaB);
            GL11.glVertex3f((selectedPos.x + 1) * spacing, (selectedPos.y + 1) * spacing, end);

            GlUtil.glColor4fForced(1, 1, 1, lineAlpha);
            GL11.glVertex3f(start, (selectedPos.y + 1) * spacing, (selectedPos.z + 1) * spacing);
            GlUtil.glColor4fForced(1, 1, 1, lineAlphaB);
            GL11.glVertex3f(end, (selectedPos.y + 1) * spacing, (selectedPos.z + 1) * spacing);

            GlUtil.glColor4fForced(1, 1, 1, lineAlpha);
            GL11.glVertex3f((selectedPos.x + 1) * spacing, start, (selectedPos.z + 1) * spacing);
            GlUtil.glColor4fForced(1, 1, 1, lineAlphaB);
            GL11.glVertex3f((selectedPos.x + 1) * spacing, end, (selectedPos.z + 1) * spacing);

            end += (1f / 3f) * size;
            start += (1f / 3f) * size;
        }
        GlUtil.glEnd();

        GlUtil.glMatrixMode(GL11.GL_PROJECTION);
        GlUtil.glPopMatrix();
        GlUtil.glMatrixMode(GL11.GL_MODELVIEW);
    }

    private void drawIndicators() {
        for(Map.Entry<Integer, TacticalMapEntityIndicator> entry : drawMap.entrySet()) {
            TacticalMapEntityIndicator indicator = entry.getValue();
            if(indicator.getDistance() < maxDrawDistance && indicator.getEntity() != null) {
                Indication indication = indicator.getIndication(indicator.getSystem());
                indicator.drawSprite(indication.getCurrentTransform(), getCurrentEntity());
                indicator.drawLabel(indication.getCurrentTransform(), getCurrentEntity());
                if(drawMovementPaths && indicator.selected) indicator.drawMovementPath(camera, 1.0f);
                if(drawTargetingPaths && indicator.selected) indicator.drawTargetingPath(camera, 1.0f);
            } else drawMap.remove(entry.getKey());
        }
    }

    private SegmentController getCurrentEntity() {
        if(GameClient.getCurrentControl() != null && GameClient.getCurrentControl() instanceof SegmentController) {
            return (SegmentController) GameClient.getCurrentControl();
        } else return null;
    }
}