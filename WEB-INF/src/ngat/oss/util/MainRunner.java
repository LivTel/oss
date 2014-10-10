package ngat.oss.util;

import java.rmi.Naming;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ngat.oss.model.IPhase2Model;
import ngat.phase2.IGroup;
import ngat.phase2.IProposal;

public class MainRunner {

	private static final String CLEAR_NSO = "clearnso";
	private static final String SHOW_NSO = "shownso";

	public static void main(String[] args) {
		if (args.length > 0) {
			String firstArg = args[0];
			if (!firstArg.equals(CLEAR_NSO) && !firstArg.equals(SHOW_NSO)) {
				showUsage();
			}
			try {
				new MainRunner().clearNSO(firstArg.equalsIgnoreCase(CLEAR_NSO));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			showUsage();
		}
	}

	private static void showUsage() {
		System.out.println("USAGE: ");
		System.out.println(MainRunner.class.getName() + " <name_of_main_to_run>   e.g. shownso | clearnso");
		System.exit(0);
	}

	private void clearNSO(boolean deleteGroups) throws Exception {

		System.err.println("Lookup access and phase2 models");

		IPhase2Model phase2Model = (IPhase2Model) Naming
				.lookup("rmi://localhost/Phase2Model");

		System.err.println("Found phase2 model");

		// list the proposals in the NSO programme
		List proplist = phase2Model.listProposalsOfProgramme(36);

		System.err.println("Programme has " + proplist.size() + " proposals");

		ArrayList allGroups = new ArrayList();
		int totNumGrps = 0;
		Iterator iprop = proplist.iterator();

		// iterate through the proposals adding all the groups to allGroups list
		while (iprop.hasNext()) {
			// get the proposal
			IProposal proposal = (IProposal) iprop.next();

			System.err.println("Proposal: " + proposal.getName() + "["
					+ proposal.getID() + "]");

			long pid = proposal.getID();

			System.err.println("listGroups(" + pid + ")");
			List glist = phase2Model.listGroups(pid, true);
			System.err.println("... returns " + glist.size());
			allGroups.addAll(glist);
			totNumGrps += glist.size();

			System.err.println("FINISHED, TOTAL NUMBER OF GROUPS = "
					+ totNumGrps);
		}

		Iterator allGrpsIterator = allGroups.iterator();
		int gnum = 1;
		while (allGrpsIterator.hasNext()) {

			IGroup group = (IGroup) allGrpsIterator.next();
			System.err.println("(" + gnum + "/" + totNumGrps + "). group: "
					+ group.getName() + "[" + group.getID() + "]");

			long gid = group.getID();

			if (deleteGroups) {
				// delete the group and everything in it...
				System.err.println("... deleting group");
				phase2Model.deleteGroup(gid);
			}
			gnum++;
		}
	}

}
