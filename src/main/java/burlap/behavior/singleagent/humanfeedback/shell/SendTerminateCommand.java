package burlap.behavior.singleagent.humanfeedback.shell;

import burlap.behavior.singleagent.humanfeedback.environment.HumanFeedbackEnvironment;
import burlap.shell.BurlapShell;
import burlap.shell.EnvironmentShell;
import burlap.shell.command.ShellCommand;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * @author James MacGlashan.
 */
public class SendTerminateCommand implements ShellCommand {

	@Override
	public String commandName() {
		return "sendTerm";
	}

	@Override
	public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {
		HumanFeedbackEnvironment env = (HumanFeedbackEnvironment)((EnvironmentShell)shell).getEnv();
		env.setTerminalSignal(true);
		return 0;
	}
}
