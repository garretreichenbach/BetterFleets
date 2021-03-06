package org.schema.game.common.data.fleet;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.PlayerButtonTilesInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.PlayerSectorInput;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.common.language.Lng;
import org.schema.schine.common.language.Translatable;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonColor;
import org.schema.schine.input.InputState;
import org.schema.schine.network.client.ClientState;
import thederpgamer.betterfleets.utils.FleetUtils;
import java.util.Arrays;

/**
 * Modified version of FleetCommandTypes.
 *
 * @author Schema, TheDerpGamer
 * @since 07/06/2021
 */
public enum FleetCommandTypes {
    IDLE(new Translatable() {
        @Override
        public String getName(Enum en) {
            return Lng.str("Idle");
        }
    },
            new Translatable() {
                @Override
                public String getName(Enum en) {
                    return Lng.str("Fleet will idle. It will not attack enemies.");
                }
            },
            new FleetCommandDialog() {
                @Override
                public void clientSend(final Fleet fleet) {
                    fleet.sendFleetCommand(IDLE);
                }
            },
            Transition.RESTART),
    MOVE_FLEET(new Translatable() {
        @Override
        public String getName(Enum en) {
            return Lng.str("Move Fleet");
        }
    },
            new Translatable() {
                @Override
                public String getName(Enum en) {
                    return Lng.str("Moves fleet to a specific location.");
                }
            },
            new FleetCommandDialog() {
                @Override
                public void clientSend(final Fleet fleet) {
                    PlayerSectorInput p = new PlayerSectorInput((GameClientState) fleet.getState(),
                            Lng.str("Enter destination sector!"),
                            Lng.str("Enter destination sector! (e.g. 10, 20, 111 [or 10 20 111][or 10.20.100])"), "") {

                        @Override
                        public void handleEnteredEmpty() {
                            deactivate();
                        }

                        @Override
                        public void handleEntered(Vector3i p) {
                            System.err.println("[CLIENT][FLEET] Sending fleet move command: " + p + "; on fleet " + fleet);
                            fleet.sendFleetCommand(MOVE_FLEET, p);
                        }

                        @Override
                        public Object getSelectCoordinateButtonText() {
                            return Lng.str("USE");
                        }

                    };
                    p.activate();
                }
            },
            Transition.MOVE_TO_SECTOR,
            Vector3i.class),
    PATROL_FLEET(new Translatable() {
        @Override
        public String getName(Enum en) {
            return Lng.str("Patrols Fleet");
        }
    },
            new Translatable() {
                @Override
                public String getName(Enum en) {
                    return Lng.str("Patrols Fleet between current sector and target sector.");
                }
            },
            null,
            Transition.FLEET_PATROL,
            Vector3i.class),
    TRADE_FLEET(new Translatable() {
        @Override
        public String getName(Enum en) {
            return Lng.str("Trading Fleet");
        }
    },
            new Translatable() {
                @Override
                public String getName(Enum en) {
                    return Lng.str("Use fleet to trade between sectors.");
                }
            },
            null,
            Transition.FLEET_TRADE,
            Vector3i.class),
    FLEET_ATTACK(new Translatable() {
        @Override
        public String getName(Enum en) {
            return Lng.str("Attack Sector");
        }
    },
            new Translatable() {
                @Override
                public String getName(Enum en) {
                    return Lng.str("Tries to move to target sector and attack enemy ships. For the time being, only loaded sectors can be attacked. The Fleet will wait outside the target sector otherwise.");
                }
            },
            new FleetCommandDialog() {
                @Override
                public void clientSend(final Fleet fleet) {
                    PlayerSectorInput p = new PlayerSectorInput((GameClientState) fleet.getState(),
                            Lng.str("Enter destination sector!"),
                            Lng.str("Enter destination sector! (e.g. 10, 20, 111 [or 10 20 111][or 10.20.100])"), "") {

                        @Override
                        public void handleEnteredEmpty() {
                            deactivate();
                        }

                        @Override
                        public void handleEntered(Vector3i p) {
                            System.err.println("[CLIENT][FLEET] Sending fleet ATTACK command: " + p + "; on fleet " + fleet);
                            fleet.sendFleetCommand(FLEET_ATTACK, p);
                        }

                        @Override
                        public Object getSelectCoordinateButtonText() {
                            return Lng.str("USE");
                        }
                    };
                    p.activate();

                }
            },
            Transition.FLEET_ATTACK,
            Vector3i.class),
    FLEET_DEFEND(new Translatable() {
        @Override
        public String getName(Enum en) {
            return Lng.str("Defend Sector");
        }
    },
            new Translatable() {
                @Override
                public String getName(Enum en) {
                    return Lng.str("Moves fleet to a sector. The fleet will attack any enemy in proximity. A ship will stop chasing if it is more than 2 sectors away from the target sector.");
                }
            },
            new FleetCommandDialog() {
                @Override
                public void clientSend(final Fleet fleet) {
                    PlayerSectorInput p = new PlayerSectorInput((GameClientState) fleet.getState(),
                            Lng.str("Enter destination sector!"),
                            Lng.str("Enter destination sector! (e.g. 10, 20, 111 [or 10 20 111][or 10.20.100])"), "") {

                        @Override
                        public void handleEnteredEmpty() {
                            deactivate();
                        }

                        @Override
                        public void handleEntered(Vector3i p) {
                            System.err.println("[CLIENT][FLEET] Sending fleet DEFEND command: " + p + "; on fleet " + fleet);
                            fleet.sendFleetCommand(FLEET_DEFEND, p);
                        }

                        @Override
                        public Object getSelectCoordinateButtonText() {
                            return Lng.str("USE");
                        }
                    };
                    p.activate();

                }
            },
            Transition.FLEET_DEFEND,
            Vector3i.class),
    //INSERTED CODE
    ARTILLERY(new Translatable() {
        @Override
        public String getName(Enum en) {
            return "Artillery Fire Mode";
        }
    }, new Translatable() {
        @Override
        public String getName(Enum en) {
            return "Fleet will engage targets from as far away as possible while still being within the max range of it's guns.";
        }
    }, new FleetCommandDialog() {
        @Override
        public void clientSend(final Fleet fleet) {
            fleet.sendFleetCommand(ARTILLERY);
        }
    }, Transition.FLEET_ARTILLERY),
    INTERCEPT(new Translatable() {
        @Override
        public String getName(Enum en) {
            return "Intercept Mode";
        }
    }, new Translatable() {
        @Override
        public String getName(Enum en) {
            return "Fleet will engage enemy targets at close range, attempting to intercept attacks on allied ships by drawing away the attention of nearby enemies.";
        }
    }, new FleetCommandDialog() {
        @Override
        public void clientSend(final Fleet fleet) {
            fleet.sendFleetCommand(INTERCEPT);
        }
    }, Transition.FLEET_INTERCEPT),
    SUPPORT(new Translatable() {
        @Override
        public String getName(Enum en) {
            return "Support Mode";
        }
    }, new Translatable() {
        @Override
        public String getName(Enum en) {
            return "Fleet will use support beams on nearby friendly targets while attempting to stay away from enemies.";
        }
    }, new FleetCommandDialog() {
        @Override
        public void clientSend(final Fleet fleet) {
            if(FleetUtils.hasRepairBeams(fleet)) fleet.sendFleetCommand(SUPPORT);
            else fleet.sendFleetCommand(IDLE);
        }
    }, Transition.FLEET_SUPPORT),
    //
    SENTRY_FORMATION(new Translatable() {
        @Override
        public String getName(Enum en) {
            return Lng.str("Formation Sentry Mode");
        }
    },
            new Translatable() {
                @Override
                public String getName(Enum en) {
                    return Lng.str("WARNING: FORMATION IS EXPERIMENTAL AND CAN CAUSE GLITCHES. Fleet will keep formation. If an enemy is nearby, they will break formation and attack.");
                }
            },
            new FleetCommandDialog() {
                @Override
                public void clientSend(final Fleet fleet) {
                    fleet.sendFleetCommand(SENTRY_FORMATION);

                }
            },
            Transition.FLEET_SENTRY_FORMATION),
    SENTRY(new Translatable() {
        @Override
        public String getName(Enum en) {
            return Lng.str("Sentry Mode");
        }
    },
            new Translatable() {
                @Override
                public String getName(Enum en) {
                    return Lng.str("Fleet will not keep formation. Any nearby enemy will be attacked.");
                }
            },
            new FleetCommandDialog() {
                @Override
                public void clientSend(final Fleet fleet) {
                    fleet.sendFleetCommand(SENTRY);
                }
            },
            Transition.FLEET_SENTRY),
    FLEET_IDLE_FORMATION(new Translatable() {
        @Override
        public String getName(Enum en) {
            return Lng.str("Idle in Formation");
        }
    },
            new Translatable() {
                @Override
                public String getName(Enum en) {
                    return Lng.str("WARNING: FORMATION IS EXPERIMENTAL AND CAN CAUSE GLITCHES. Fleet will keep formation and not attack nearby enemies.");
                }
            },
            new FleetCommandDialog() {
                @Override
                public void clientSend(final Fleet fleet) {
                    fleet.sendFleetCommand(FLEET_IDLE_FORMATION);

                }
            },
            Transition.FLEET_IDLE_FORMATION),
    CALL_TO_CARRIER(new Translatable() {
        @Override
        public String getName(Enum en) {
            return Lng.str("Carrier Recall");
        }
    },
            new Translatable() {
                @Override
                public String getName(Enum en) {
                    return Lng.str("Fleet ships will return to the pickup area of the flagship they last used to dock.");
                }
            },
            new FleetCommandDialog() {
                @Override
                public void clientSend(final Fleet fleet) {
                    fleet.sendFleetCommand(CALL_TO_CARRIER);
                }
            },
            Transition.FLEET_RECALL_CARRIER),
    MINE_IN_SECTOR(new Translatable() {
        @Override
        public String getName(Enum en) {
            return Lng.str("Mine this Sector");
        }
    },
            new Translatable() {
                @Override
                public String getName(Enum en) {
                    return Lng.str("All fleet ships except the flag ship will try to mine astroids in this sector.");
                }
            },
            new FleetCommandDialog() {
                @Override
                public void clientSend(final Fleet fleet) {
                    fleet.sendFleetCommand(MINE_IN_SECTOR);
                }
            },
            Transition.FLEET_MINE),
    //INSERTED CODE
    ACTIVATE_TURRETS(new Translatable() {
        @Override
        public String getName(Enum en) {
            return "Activate All Turrets";
        }
    }, new Translatable() {
        @Override
        public String getName(Enum en) {
            return "Fleet will activate docked turrets.";
        }
    }, new FleetCommandDialog() {
        @Override
        public void clientSend(final Fleet fleet) {
            fleet.sendFleetCommand(ACTIVATE_TURRETS);
        }
    }, Transition.FLEET_ACTIVATING_TURRETS),
    DEACTIVATE_TURRETS(new Translatable() {
        @Override
        public String getName(Enum en) {
            return "Deactivate All Turrets";
        }
    }, new Translatable() {
        @Override
        public String getName(Enum en) {
            return "Fleet will deactivate docked turrets.";
        }
    }, new FleetCommandDialog() {
        @Override
        public void clientSend(final Fleet fleet) {
            fleet.sendFleetCommand(DEACTIVATE_TURRETS);
        }
    }, Transition.FLEET_DEACTIVATING_TURRETS),
    //
    CLOAK(new Translatable() {
        @Override
        public String getName(Enum en) {
            return Lng.str("Enable Fleet Cloak");
        }
    },
            new Translatable() {
                @Override
                public String getName(Enum en) {
                    return Lng.str("All fleet ships cloak if they can.");
                }
            },
            new FleetCommandDialog() {
                @Override
                public void clientSend(final Fleet fleet) {
                    fleet.sendFleetCommand(CLOAK);
                }
            },
            Transition.FLEET_CLOAK),
    UNCLOAK(new Translatable() {
        @Override
        public String getName(Enum en) {
            return Lng.str("Disable Fleet Cloak");
        }
    },
            new Translatable() {
                @Override
                public String getName(Enum en) {
                    return Lng.str("All fleet ships uncloak if they can.");
                }
            },
            new FleetCommandDialog() {
                @Override
                public void clientSend(final Fleet fleet) {
                    fleet.sendFleetCommand(UNCLOAK);
                }
            },
            Transition.FLEET_UNCLOAK),
    JAM(new Translatable() {
        @Override
        public String getName(Enum en) {
            return Lng.str("Enable Fleet Jamming");
        }
    },
            new Translatable() {
                @Override
                public String getName(Enum en) {
                    return Lng.str("All fleet ships radar jam if they can.");
                }
            },
            new FleetCommandDialog() {
                @Override
                public void clientSend(final Fleet fleet) {
                    fleet.sendFleetCommand(JAM);
                }
            },
            Transition.FLEET_JAM),
    UNJAM(new Translatable() {
        @Override
        public String getName(Enum en) {
            return Lng.str("Disable Fleet Jamming");
        }
    },
            new Translatable() {
                @Override
                public String getName(Enum en) {
                    return Lng.str("All fleet ships stop radar jamming if they can.");
                }
            },
            new FleetCommandDialog() {
                @Override
                public void clientSend(final Fleet fleet) {
                    fleet.sendFleetCommand(UNJAM);
                }
            },
            Transition.FLEET_UNJAM);
    public interface FleetCommandDialog{

        public void clientSend(final Fleet fleet);
    }

    private final Translatable name;
    private final Translatable description;
    public final Class<?>[] args;
    public final FleetCommandDialog c;
    public final Transition transition;

    private FleetCommandTypes(Translatable name, Translatable description, FleetCommandDialog c, Transition transition, Class<?>... args) {
        this.name = name;
        this.args = args;
        this.description = description;
        this.c = c;
        this.transition = transition;
    }



    public String getName(){
        return name.getName(this);
    }

    public String getDescription(){
        return description.getName(this);
    }

    public boolean isAvailableOnClient(){
        return c != null;
    }
    public void addTile(final PlayerButtonTilesInput a,
                        final Fleet fleet) {

        if(!isAvailableOnClient()){
            return;
        }

        final GUIActivationCallback ac = new GUIActivationCallback() {

            @Override
            public boolean isVisible(InputState state) {
                return true;
            }

            @Override
            public boolean isActive(InputState state) {
                return fleet.isCommandUsable(FleetCommandTypes.this);
            }
        };

        a.addTile(getName(), getDescription(), HButtonColor.BLUE, new GUICallback() {
            @Override
            public boolean isOccluded() {
                return !(a.isActive() && (ac == null || ac.isActive((ClientState)fleet.getState())));
            }

            @Override
            public void callback(GUIElement callingGuiElement, MouseEvent event) {
                if(event.pressedLeftMouse()){
                    FleetCommandTypes.this.c.clientSend(fleet);
                    a.deactivate();
                }
            }
        }, ac);
    }

    public void checkMatches(Object[] to) {
        if (args.length != to.length) {
            throw new IllegalArgumentException("Invalid argument count: Provided: " + Arrays.toString(to) + ", but needs: " + Arrays.toString(args));
        }
        for (int i = 0; i < args.length; i++) {
            if (!to[i].getClass().equals(args[i])) {
                System.err.println("Not Equal: " + to[i] + " and " + args[i]);
                throw new IllegalArgumentException("Invalid argument on index " + i + ": Provided: " + Arrays.toString(to) + "; cannot take " + to[i] + ":" + to[i].getClass() + ", it has to be type: " + args[i].getClass());
            }
        }
    }


}