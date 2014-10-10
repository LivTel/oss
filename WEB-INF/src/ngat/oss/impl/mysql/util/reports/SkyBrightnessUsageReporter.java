package ngat.oss.impl.mysql.util.reports;


import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import ngat.oss.impl.mysql.ConnectionPool;
import ngat.oss.impl.mysql.accessors.AccessPermissionAccessor;
import ngat.oss.impl.mysql.accessors.GroupAccessor;
import ngat.oss.impl.mysql.accessors.ProgrammeAccessor;
import ngat.oss.impl.mysql.accessors.ProposalAccessor;
import ngat.oss.impl.mysql.accessors.UserAccessor;
import ngat.phase2.IGroup;
import ngat.phase2.IObservingConstraint;
import ngat.phase2.IProgram;
import ngat.phase2.IProposal;
import ngat.phase2.IUser;
import ngat.phase2.XSkyBrightnessConstraint;

public class SkyBrightnessUsageReporter {

	private GroupAccessor  groupAccessor;
	private ProposalAccessor proposalAccessor;
	private ProgrammeAccessor programmeAccessor;
	private AccessPermissionAccessor accessPermissionAccessor;
	private UserAccessor userAccessor;
	private Connection connection;
	
	public SkyBrightnessUsageReporter() throws SQLException {
		
		connection = ConnectionPool.getInstance().getConnection();
		
		groupAccessor = new GroupAccessor();
		proposalAccessor = new ProposalAccessor();
		programmeAccessor = new ProgrammeAccessor();
		accessPermissionAccessor = new AccessPermissionAccessor();
		userAccessor = new UserAccessor();
	}
	
	public void showReport() throws Exception {
		List programmesList = listProgrammes();
		Iterator progIterator = programmesList.iterator();
		while (progIterator.hasNext()) {
			
			IProgram programme = (IProgram) progIterator.next();
			List proposalsList = listProposalsOfProgramme(programme.getID());
			Iterator proposalsIterator = proposalsList.iterator();
			while (proposalsIterator.hasNext()) {
				
				IProposal proposal = (IProposal) proposalsIterator.next();
				List groupsList = listActiveUnexpiredGroupsOfProposal(proposal.getID());
				Iterator groupsListIterator = groupsList.iterator();
				while (groupsListIterator.hasNext()) {
					
					IGroup group = (IGroup) groupsListIterator.next();
					List obsConstraintsList = group.listObservingConstraints();
					Iterator obsConstraintsIterator = obsConstraintsList.iterator();
					boolean groupHasSkyBrightnessConstraint = false;
					
					//System.err.println("DESCRIBE: obs constraints of " +group.getName() + " number of constraints=" +obsConstraintsList.size() );
					
					while (obsConstraintsIterator.hasNext()) {
						
						IObservingConstraint observingConstraint = (IObservingConstraint) obsConstraintsIterator.next();
						
						//System.err.println("... observingConstraint=" + observingConstraint.getClass().getName());
						
						if (observingConstraint instanceof XSkyBrightnessConstraint) {
							groupHasSkyBrightnessConstraint = true;
							XSkyBrightnessConstraint skyBrightnessConstraint = (XSkyBrightnessConstraint) observingConstraint;
							if (skyBrightnessConstraint.getSkyBrightnessCategory() == IObservingConstraint.DARK) {
								showDarkSkyBConstraintMessage(group, proposal, programme);
							}
						}
					}
					if (!groupHasSkyBrightnessConstraint) {
						showNoSkyBConstraintMessage(group, proposal, programme);
					}
				}
			}
		}
	}
	
	public void showDarkSkyBConstraintMessage(IGroup group, IProposal proposal, IProgram prog) throws RemoteException {
		String m = "";
		
		m += "RESULT: PI: " +getProposalPI(proposal).getName();
		m += " Proposal: " + proposal.getName() + "(" + proposal.getID() + ")";
		m += " Programme: " + prog.getName() + "(" + prog.getID() + ")";
		m += " Group: " + group.getName() + "(" + group.getID() + ")";
		m += " [NIGHT TIME only sky brightness constraint specified]";
		System.out.println(m);
	}
	
	public void showNoSkyBConstraintMessage(IGroup group, IProposal proposal, IProgram prog) throws RemoteException {
		String m = "";
		
		m += "RESULT: PI: " +getProposalPI(proposal).getName();
		m += " Proposal: " + proposal.getName() + "(" + proposal.getID() + ")";
		m += " Programme: " + prog.getName() + "(" + prog.getID() + ")";
		m += " Group: " + group.getName() + "(" + group.getID() + ")";
		m += " [No sky brightness constraint specified]";
		
		System.out.println(m);
	}
	
	
	public IUser getProposalPI(IProposal proposal) throws RemoteException {
		//long proposalPi = accessPermissionAccessor.getProposalPI(connection, proposalId);
		try {
			long uid = accessPermissionAccessor.getProposalPI(connection, proposal.getID());
			return userAccessor.getUser(connection, uid);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} 
	}
	
	public List listProgrammes() throws RemoteException {
		try {
			return programmeAccessor.listProgrammes(connection);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} 
	}
	
	public List listProposalsOfProgramme(long progID) throws RemoteException {
		try {
			return proposalAccessor.listProposalsOfProgramme(connection, progID);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		}
	}
	
	public List listActiveUnexpiredGroupsOfProposal(long propID) throws RemoteException {
		try {
			return groupAccessor.listGroups(connection, propID, true);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} 
	}
	
	public static void main(String[] args) {
		
		try {
			new SkyBrightnessUsageReporter().showReport();
		} catch (Exception e) {
			e.printStackTrace();
		}
				
	}
	
}
