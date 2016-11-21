package experiments.turtlebot.shell;

import burlap.behavior.singleagent.MDPSolver;
import burlap.shell.BurlapShell;
import burlap.shell.command.ShellCommand;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * @author James MacGlashan.
 */
public class ResetSolverCommand implements ShellCommand{

	protected MDPSolver agent;

	public ResetSolverCommand(MDPSolver agent) {
		this.agent = agent;
	}

	@Override
	public String commandName() {
		return "rsolve";
	}

	@Override
	public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {
		agent.resetSolver();
		return 0;
	}
}
