package burlap.behavior.singleagent.humanfeedback.shell;

import burlap.behavior.singleagent.humanfeedback.FeedbackReceiver;
import burlap.behavior.singleagent.humanfeedback.environment.HumanFeedbackEnvironment;
import burlap.shell.BurlapShell;
import burlap.shell.EnvironmentShell;
import burlap.shell.command.ShellCommand;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * @author James MacGlashan.
 */
public class SendFeedback implements ShellCommand {

	protected List<FeedbackReceiver> auxReceivers = new ArrayList<>();

    public SendFeedback() {
    }

    public SendFeedback(FeedbackReceiver...receivers) {
        this.auxReceivers.addAll(Arrays.asList(receivers));
    }

    @Override
	public String commandName() {
		return "sendFeedback";
	}

	@Override
	public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {
		HumanFeedbackEnvironment env = (HumanFeedbackEnvironment)((EnvironmentShell)shell).getEnv();
		double f = Double.parseDouble(argString);
		env.receiveHumanFeedback(f);
        for(FeedbackReceiver r : auxReceivers){
            r.receiveHumanFeedback(f);
        }
		return 0;
	}
}
