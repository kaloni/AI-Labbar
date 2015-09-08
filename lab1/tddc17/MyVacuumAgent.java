package tddc17;


import aima.core.environment.liuvacuum.*;
import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.Percept;
import aima.core.agent.impl.*;

import java.util.Random;

class MyAgentState
{
	public int[][] world = new int[30][30];
	public int initialized = 0;
	public static final int UNKNOWN 	= 0;
	public static final int WALL 		= 1;
	public static final int CLEAR 	= 2;
	public static final int DIRT		= 3;
	public static final int HOME		= 4;
	public static final int ACTION_NONE 			= 0;
	public static final int ACTION_MOVE_FORWARD 	= 1;
	public static final int ACTION_TURN_RIGHT 	= 2;
	public static final int ACTION_TURN_LEFT 		= 3;
	public static final int ACTION_SUCK	 		= 4;

	public int agent_x_position = 1;
	public int agent_y_position = 1;
	public int agent_last_action = ACTION_NONE;

	public static final int NORTH = 0;
	public static final int EAST = 1;
	public static final int SOUTH = 2;
	public static final int WEST = 3;
	public int agent_direction = EAST;

    
    // added state variables
	boolean reachedFirstBump = false;
	boolean reachedCorner = false;
	public int turnAction = 0;
    public int turnCounter = 0;
    boolean cleanedMap = false;
    int mapX, mapY;

	MyAgentState()
	{
		for (int i=0; i < world.length; i++)
			for (int j=0; j < world[i].length ; j++)
				world[i][j] = UNKNOWN;
		world[1][1] = HOME;
		agent_last_action = ACTION_NONE;
	}
	// Based on the last action and the received percept updates the x & y agent position
	public void updatePosition(DynamicPercept p)
	{
		Boolean bump = (Boolean)p.getAttribute("bump");

		if (agent_last_action==ACTION_MOVE_FORWARD && !bump)
		{
			switch (agent_direction) {
			case MyAgentState.NORTH:
				agent_y_position--;
				break;
			case MyAgentState.EAST:
				agent_x_position++;
				break;
			case MyAgentState.SOUTH:
				agent_y_position++;
				break;
			case MyAgentState.WEST:
				agent_x_position--;
				break;
			}
		}

	}

	public void updateWorld(int x_position, int y_position, int info)
	{
		world[x_position][y_position] = info;
	}
    
    boolean rowClean(int row) {
        boolean clean = true;
        for(int i = 2; i < mapX; ++i) {
            clean = clean && (world[i][row] == MyAgentState.CLEAR);
        }
        return clean;
    }

	public void printWorldDebug()
	{
		for (int i=0; i < world.length; i++)
		{
			for (int j=0; j < world[i].length ; j++)
			{
				if (world[j][i]==UNKNOWN)
					System.out.print(" ? ");
				if (world[j][i]==WALL)
					System.out.print(" # ");
				if (world[j][i]==CLEAR)
					System.out.print(" . ");
				if (world[j][i]==DIRT)
					System.out.print(" D ");
				if (world[j][i]==HOME)
					System.out.print(" H ");
			}
			System.out.println("");
		}
	}
}

class MyAgentProgram implements AgentProgram {

	private int initnialRandomActions = 10;
	private Random random_generator = new Random();

	// Here you can define your variables!
	public int iterationCounter = 100000;
	public MyAgentState state = new MyAgentState();

	// moves the Agent to a random start position
	// uses percepts to update the Agent position - only the position, other percepts are ignored
	// returns a random action
	private Action moveToRandomStartPosition(DynamicPercept percept) {
		int action = random_generator.nextInt(6);
		initnialRandomActions--;
		state.updatePosition(percept);
		if(action==0) {
			state.agent_direction = ((state.agent_direction-1) % 4);
			if (state.agent_direction<0) 
				state.agent_direction +=4;
			state.agent_last_action = state.ACTION_TURN_LEFT;
			return LIUVacuumEnvironment.ACTION_TURN_LEFT;
		} else if (action==1) {
			state.agent_direction = ((state.agent_direction+1) % 4);
			state.agent_last_action = state.ACTION_TURN_RIGHT;
			return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
		} 
		state.agent_last_action=state.ACTION_MOVE_FORWARD;
		return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
	}

	@Override
	public Action execute(Percept percept) {

		// DO NOT REMOVE this if condition!!!

		if (initnialRandomActions>0) {
			return moveToRandomStartPosition((DynamicPercept) percept);

		} else if (initnialRandomActions==0) {
			// process percept for the last step of the initial random actions
			initnialRandomActions--;
			state.updatePosition((DynamicPercept) percept);
			System.out.println("Processing percepts after the last execution of moveToRandomStartPosition()");
			state.agent_last_action=state.ACTION_SUCK;

			return LIUVacuumEnvironment.ACTION_SUCK;
		}

		// This example agent program will update the internal agent state while only moving forward.
		// START HERE - code below should be modified!

		iterationCounter--;
        state.turnCounter--;

		if (iterationCounter==0) {
			System.out.println("out of iterations!");
			return NoOpAction.NO_OP;
		}

		DynamicPercept p = (DynamicPercept) percept;
		Boolean bump = (Boolean)p.getAttribute("bump");
		Boolean dirt = (Boolean)p.getAttribute("dirt");
		Boolean home = (Boolean)p.getAttribute("home");
		System.out.println("percept: " + p);

		// State update based on the percept value and the last action
		state.updatePosition((DynamicPercept)percept);

		System.out.println("x=" + state.agent_x_position);
		System.out.println("y=" + state.agent_y_position);
		System.out.println("dir=" + state.agent_direction);
        
        
		if (bump) {
			switch (state.agent_direction) {
			case MyAgentState.NORTH:
				state.updateWorld(state.agent_x_position,state.agent_y_position-1,state.WALL);
				break;
			case MyAgentState.EAST:
				state.updateWorld(state.agent_x_position+1,state.agent_y_position,state.WALL);
				break;
			case MyAgentState.SOUTH:
				state.updateWorld(state.agent_x_position,state.agent_y_position+1,state.WALL);
				break;
			case MyAgentState.WEST:
				state.updateWorld(state.agent_x_position-1,state.agent_y_position,state.WALL);
				break;
			}
		}
		if (dirt)
			state.updateWorld(state.agent_x_position,state.agent_y_position,state.DIRT);
		else
			state.updateWorld(state.agent_x_position,state.agent_y_position,state.CLEAR);

		state.printWorldDebug();


        // Look for EAST wall
		if( ! state.reachedFirstBump ) {

			System.out.println("Looking for east wall");
					// check if pacman is at right wall -> turn south
			if( bump ) {
				state.reachedFirstBump = true;
				state.agent_last_action = state.ACTION_TURN_RIGHT;
				state.agent_direction = ((state.agent_direction+1) % 4);
				System.out.println("Bumped east wall!");
				return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
			}

			// Direct Pacman to the right wall
			switch( state.agent_direction ) {

			case MyAgentState.NORTH:
				state.agent_direction = ((state.agent_direction+1) % 4);
				state.agent_last_action = state.ACTION_TURN_RIGHT;
				return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
			case MyAgentState.EAST:
				state.agent_last_action = state.ACTION_MOVE_FORWARD;
				return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
			case MyAgentState.SOUTH:
				state.agent_direction = (((state.agent_direction-1) % 4) + 4) % 4;
				state.agent_last_action = state.ACTION_TURN_LEFT;
				return LIUVacuumEnvironment.ACTION_TURN_LEFT;
			case MyAgentState.WEST:
				state.agent_direction = (((state.agent_direction-1) % 4) + 4) % 4;
				state.agent_last_action = state.ACTION_TURN_LEFT;
				return LIUVacuumEnvironment.ACTION_TURN_LEFT;
				/*
		    default:
		    	state.agent_last_action=state.ACTION_NONE;
		    	System.out.println("something went wrong in switch");
		    	return NoOpAction.NO_OP;
				 */
			}
		}
        // Look for SOUTH-EAST corner
		else if( ! state.reachedCorner ) {

			System.out.println("Looking for corner");
			if( bump ) {
				System.out.println("Bumped corner!");
				state.reachedCorner = true;
                state.mapX = state.agent_x_position;
                state.mapY = state.agent_y_position;
				state.agent_direction = ((state.agent_direction+1) % 4);
				state.agent_last_action = state.ACTION_TURN_RIGHT;
				return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
			}
		}

		// Next action selection based on the percept value
		if (dirt)
		{
			System.out.println("DIRT -> choosing SUCK action!");
			state.agent_last_action=state.ACTION_SUCK;
            state.turnCounter++;
			return LIUVacuumEnvironment.ACTION_SUCK;
		} 
		else
		{
			if (bump)
			{
                // Checked if map is cleaned
                if( home && state.reachedCorner ) {
                    if( state.rowClean(1) ) {
                        System.out.println("home ok");
                        return NoOpAction.NO_OP;
                    }
                }
                
                // If bumping into something, return action dependent on direction of the agent
                // Set the turnCounter variable = 2, which is decremented at each loop so agent knows when to make next turn
                // Set turnAction state variable so agent remembers what type of action to take at next turn
				System.out.println("bumped into something!!!");
                state.turnCounter = 2;
				if( state.agent_direction == MyAgentState.WEST ) {
					state.agent_direction = ((state.agent_direction+1) % 4);
					state.agent_last_action = state.ACTION_TURN_RIGHT;
					state.turnAction = MyAgentState.ACTION_TURN_RIGHT; //next move after forward move is right turn
					return LIUVacuumEnvironment.ACTION_TURN_RIGHT;

				}
				else if( state.agent_direction == MyAgentState.EAST ) {
					state.agent_direction = ((((state.agent_direction-1) % 4) + 4) % 4);
					state.agent_last_action = state.ACTION_TURN_LEFT;
                    state.turnAction = MyAgentState.ACTION_TURN_LEFT; //next move after forward move is left turn
					return LIUVacuumEnvironment.ACTION_TURN_LEFT;
				}
                else if( state.agent_direction == MyAgentState.NORTH ) {
                    state.agent_direction = ((((state.agent_direction-1) % 4) + 4) % 4);
                    state.agent_last_action = MyAgentState.NORTH;
                    state.turnCounter = -1;
                    return LIUVacuumEnvironment.ACTION_TURN_LEFT;
                }
				
			}
			else
			{
                // If its time to make the turn, check the turnAction state variable and act according to that
				if( state.turnCounter == 0 ) {
					switch( state.turnAction ) {
					case MyAgentState.ACTION_TURN_RIGHT:
						state.agent_direction = ((state.agent_direction+1) % 4);
						state.agent_last_action = state.ACTION_TURN_RIGHT;
						//state.timeToTurn = 0;
						return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
						
					case MyAgentState.ACTION_TURN_LEFT:
						state.agent_direction = ((((state.agent_direction-1) % 4) + 4) % 4);
						state.agent_last_action = state.ACTION_TURN_LEFT;
						//state.timeToTurn = 0;
						return LIUVacuumEnvironment.ACTION_TURN_LEFT;
						
                        default: System.out.println("something went wrong during turn");
					}

				}
                // No bumps, no turn actions, just keep on moving straight forward
				else {
					System.out.println("Movin foward as usual!");
					state.agent_last_action=state.ACTION_MOVE_FORWARD;
					return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
				}

            }
        }
        
        return NoOpAction.NO_OP;
    }

	public class MyVacuumAgent extends AbstractAgent {
		public MyVacuumAgent() {
			super(new MyAgentProgram());
        }
	}
}

