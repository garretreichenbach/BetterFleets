package thederpgamer.betterfleets.entity.fleet.commands.support;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.missions.machines.states.FleetState;
import org.schema.schine.ai.stateMachines.FSMException;
import java.util.List;

/**
 * Fleet support command state.
 *
 * @author TheDerpGamer
 * @since 07/06/2021
 */
public class Support extends FleetState {

    private List<Ship> loaded = new ObjectArrayList<>();

    public Support(Fleet fleet) {
        super(fleet);
    }

    @Override
    public FleetStateType getType() {
        return FleetStateType.SUPPORT;
    }

    @Override
    public boolean onExit() {
        return false;
    }

    @Override
    public boolean onUpdate() throws FSMException {
        return false;
    }

    @Override
    public boolean onEnterFleetState() {
        try {
            restartAllLoaded();
        } catch(FSMException exception) {
            exception.printStackTrace();
        }
        return super.onEnterFleetState();
    }
}