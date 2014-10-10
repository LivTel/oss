package ngat.oss.impl.mysql.model;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ngat.oss.exception.Phase2Exception;
import ngat.oss.impl.mysql.ConnectionPool;
import ngat.oss.impl.mysql.accessors.DetectorConfigAccessor;
import ngat.oss.impl.mysql.accessors.GroupAccessor;
import ngat.oss.impl.mysql.accessors.InstrumentConfigAccessor;
import ngat.oss.impl.mysql.accessors.ProgrammeAccessor;
import ngat.oss.impl.mysql.accessors.ProposalAccessor;
import ngat.oss.impl.mysql.accessors.RevisionAccessor;
import ngat.oss.impl.mysql.accessors.SequenceComponentAccessor;
import ngat.oss.impl.mysql.accessors.TagAccessor;
import ngat.oss.impl.mysql.accessors.TargetAccessor;
import ngat.oss.impl.mysql.accessors.TestAccessor;
import ngat.oss.impl.mysql.accessors.TimingConstraintAccessor;
import ngat.oss.listeners.Phase2ModelUpdateListener;
import ngat.oss.model.IPhase2Model;
import ngat.oss.monitor.Phase2Monitor;
import ngat.oss.transport.RemotelyPingable;
import ngat.phase2.IGroup;
import ngat.phase2.IInstrumentConfig;
import ngat.phase2.IProgram;
import ngat.phase2.IProposal;
import ngat.phase2.IRevision;
import ngat.phase2.ISequenceComponent;
import ngat.phase2.ITag;
import ngat.phase2.ITarget;
import ngat.phase2.XGroup;
import ngat.phase2.XTarget;

import org.apache.log4j.Logger;

public class Phase2Model extends UnicastRemoteObject implements IPhase2Model,
		Phase2Monitor, RemotelyPingable {

	static Logger logger = Logger.getLogger(Phase2Model.class);

	// phase2 accessors
	TagAccessor tagAccessor;
	ProgrammeAccessor programmeAccessor;
	GroupAccessor groupAccessor;
	ProposalAccessor proposalAccessor;
	TargetAccessor targetAccessor;
	TimingConstraintAccessor timingConstraintAccessor;
	InstrumentConfigAccessor instrumentConfigAccessor;
	RevisionAccessor revisionAccessor;
	SequenceComponentAccessor sequenceComponentAccessor;
	DetectorConfigAccessor detectorConfigAccessor;
	TestAccessor testAccessor;

	// update listeners
	ArrayList updateListeners;

	public Phase2Model(int rmiPort) throws RemoteException {
		super(rmiPort);
		tagAccessor = new TagAccessor();
		programmeAccessor = new ProgrammeAccessor();
		groupAccessor = new GroupAccessor();
		proposalAccessor = new ProposalAccessor();
		targetAccessor = new TargetAccessor();
		timingConstraintAccessor = new TimingConstraintAccessor();
		instrumentConfigAccessor = new InstrumentConfigAccessor();
		revisionAccessor = new RevisionAccessor();
		sequenceComponentAccessor = new SequenceComponentAccessor();
		testAccessor = new TestAccessor();

		updateListeners = new ArrayList();
	}

	public void ping() throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			testAccessor.ping(connection);
		} catch (Exception e) {
			throw new Phase2Exception(e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	// **************************** ADDS
	// *********************************************************//

	public long addGroup(long propID, IGroup group) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			long gid = groupAccessor.addGroup(connection, propID, group);
			connection.commit();
			XGroup xgroup = (XGroup) group;
			xgroup.setID(gid);
			notifyListenersGroupAdded(propID, xgroup);
			return gid;
		} catch (Exception e) {
			e.printStackTrace();
			try {
				logger.info("performing connection.rollback();");
				connection.rollback();
				logger.info("connection.rollback() completed successfully.");
			} catch (SQLException e1) {
				logger.error("rollback failed!");
				e1.printStackTrace();
			}
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public long addTarget(long progID, ITarget target) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			long tid = targetAccessor.addTarget(connection, progID, target);
			connection.commit();
			XTarget xTarget = (XTarget) target;
			xTarget.setID(tid);
			notifyListenersTargetAdded(progID, xTarget);
			return tid;
		} catch (Exception e) {
			e.printStackTrace();
			try {
				logger.info("performing connection.rollback();");
				connection.rollback();
				logger.info("connection.rollback() completed successfully.");
			} catch (SQLException e1) {
				logger.error("rollback failed!");
				e1.printStackTrace();
			}
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public long addInstrumentConfig(long progID, IInstrumentConfig config)
			throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			long icid = instrumentConfigAccessor.addInstrumentConfig(
					connection, progID, config);
			connection.commit();
			return icid;
		} catch (Exception e) {
			e.printStackTrace();
			try {
				logger.info("performing connection.rollback();");
				connection.rollback();
				logger.info("connection.rollback() completed successfully.");
			} catch (SQLException e1) {
				logger.error("rollback failed!");
				e1.printStackTrace();
			}
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public long addObservationSequence(long groupID,
			ISequenceComponent sequenceComponent) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			long icid = sequenceComponentAccessor.addObservationSequence(
					connection, groupID, sequenceComponent);
			sequenceComponentAccessor.setObservationSequenceIDOfGroup(
					connection, groupID, icid);
			connection.commit();
			notifyListenersGroupObsSequenceAdded(groupID, sequenceComponent);
			return icid;
		} catch (Exception e) {
			e.printStackTrace();
			try {
				logger.info("performing connection.rollback();");
				connection.rollback();
				logger.info("connection.rollback() completed successfully.");
			} catch (SQLException e1) {
				logger.error("rollback failed!");
				e1.printStackTrace();
			}
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public void addRevision(long propID, IRevision revision)
			throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			long rid = revisionAccessor.addRevision(connection, propID,
					revision);
			connection.commit();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				logger.info("performing connection.rollback();");
				connection.rollback();
				logger.info("connection.rollback() completed successfully.");
			} catch (SQLException e1) {
				logger.error("rollback failed!");
				e1.printStackTrace();
			}
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public long addProgramme(IProgram programme) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			long pid = programmeAccessor.addProgramme(connection, programme);
			connection.commit();
			return pid;
		} catch (Exception e) {
			e.printStackTrace();
			try {
				logger.info("performing connection.rollback();");
				connection.rollback();
				logger.info("connection.rollback() completed successfully.");
			} catch (SQLException e1) {
				logger.error("rollback failed!");
				e1.printStackTrace();
			}
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public long addProposal(long tagId, long progId, IProposal proposal)
			throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			long pid = proposalAccessor.addProposal(connection, tagId, progId,
					proposal);
			connection.commit();
			return pid;
		} catch (Exception e) {
			e.printStackTrace();
			try {
				logger.info("performing connection.rollback();");
				connection.rollback();
				logger.info("connection.rollback() completed successfully.");
			} catch (SQLException e1) {
				logger.error("rollback failed!");
				e1.printStackTrace();
			}
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public long addTag(ITag tag) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			long tagId = tagAccessor.addTag(connection, tag);
			connection.commit();
			return tagId;
		} catch (Exception e) {
			e.printStackTrace();
			try {
				logger.info("performing connection.rollback();");
				connection.rollback();
				logger.info("connection.rollback() completed successfully.");
			} catch (SQLException e1) {
				logger.error("rollback failed!");
				e1.printStackTrace();
			}
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	// **************************** CHANGES
	// ********************************************************//
	public void changeTagOfProposal(long proposalId, long tagId)
			throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			proposalAccessor.changeTagOfProposal(connection, proposalId, tagId);
			connection.commit();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				logger.info("performing connection.rollback();");
				connection.rollback();
				logger.info("connection.rollback() completed successfully.");
			} catch (SQLException e1) {
				logger.error("rollback failed!");
				e1.printStackTrace();
			}
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public void changeProgrammeOfProposal(long proposalId, long progId)
			throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			proposalAccessor.changeProgrammeOfProposal(connection, proposalId,
					progId);
			connection.commit();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				logger.info("performing connection.rollback();");
				connection.rollback();
				logger.info("connection.rollback() completed successfully.");
			} catch (SQLException e1) {
				logger.error("rollback failed!");
				e1.printStackTrace();
			}
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	// **************************** DELETES
	// *********************************************************//

	public void deleteGroup(long groupID) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			groupAccessor.deleteGroup(connection, groupID);
			connection.commit();
			notifyListenersGroupDeleted(groupID);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				logger.info("performing connection.rollback();");
				connection.rollback();
				logger.info("connection.rollback() completed successfully.");
			} catch (SQLException e1) {
				logger.error("rollback failed!");
				e1.printStackTrace();
			}
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public void deleteObservationSequenceOfGroup(long groupID)
			throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			sequenceComponentAccessor.deleteObservationSequenceOfGroup(
					connection, groupID);
			connection.commit();
			notifyListenersGroupObsSequenceDeleted(groupID);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				logger.info("performing connection.rollback();");
				connection.rollback();
				logger.info("connection.rollback() completed successfully.");
			} catch (SQLException e1) {
				logger.error("rollback failed!");
				e1.printStackTrace();
			}
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public void deleteProposal(long propID) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			proposalAccessor.deleteProposal(connection, propID);
			connection.commit();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				logger.info("performing connection.rollback();");
				connection.rollback();
				logger.info("connection.rollback() completed successfully.");
			} catch (SQLException e1) {
				logger.error("rollback failed!");
				e1.printStackTrace();
			}
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public void deleteTarget(long targetID) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			targetAccessor.deleteTarget(connection, targetID);
			connection.commit();
			notifyListenersTargetDeleted(targetID);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				logger.info("performing connection.rollback();");
				connection.rollback();
				logger.info("connection.rollback() completed successfully.");
			} catch (SQLException e1) {
				logger.error("rollback failed!");
				e1.printStackTrace();
			}
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public void deleteProgramme(long programID) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			programmeAccessor.deleteProgramme(connection, programID);
			connection.commit();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				logger.info("performing connection.rollback();");
				connection.rollback();
				logger.info("connection.rollback() completed successfully.");
			} catch (SQLException e1) {
				logger.error("rollback failed!");
				e1.printStackTrace();
			}
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public void deleteTag(long tagID) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			tagAccessor.deleteTag(connection, tagID);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				logger.info("performing connection.rollback();");
				connection.rollback();
				logger.info("connection.rollback() completed successfully.");
			} catch (SQLException e1) {
				logger.error("rollback failed!");
				e1.printStackTrace();
			}
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public void deleteInstrumentConfig(long configID) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			instrumentConfigAccessor.deleteInstrumentConfig(connection,
					configID);
			connection.commit();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				logger.info("performing connection.rollback();");
				connection.rollback();
				logger.info("connection.rollback() completed successfully.");
			} catch (SQLException e1) {
				logger.error("rollback failed!");
				e1.printStackTrace();
			}
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	// **************************** GETS
	// *********************************************************//

	public int getNumberOfGroups(long proposalID, boolean active)
			throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return groupAccessor.getNumberOfGroups(connection, proposalID,
					active);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public IGroup getGroup(long groupID) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return groupAccessor.getGroup(connection, groupID);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public IInstrumentConfig getInstrumentConfig(long cid)
			throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return instrumentConfigAccessor
					.getInstrumentConfig(connection, cid);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public IProposal getProposal(long propID) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return proposalAccessor.getProposal(connection, propID);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public IProposal getProposalOfGroup(long groupID) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return proposalAccessor.getProposalOfGroup(connection, groupID);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public ISequenceComponent getObservationSequenceOfGroup(long groupID)
			throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return sequenceComponentAccessor.getObservationSequenceOfGroup(
					connection, groupID);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public ISequenceComponent getObservationSequence(long seqID)
			throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return sequenceComponentAccessor.getObservationSequenceComponent(
					connection, seqID);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public IProgram getProgramme(long programID) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return programmeAccessor.getProgramme(connection, programID);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public IProgram getProgrammeOfProposal(long propID) throws RemoteException {

		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			long progId = proposalAccessor.getProgrammeIdOfProposal(connection,
					propID);
			return programmeAccessor.getProgramme(connection, progId);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public IProgram getProgrammeOfGroup(long groupID) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			IProposal proposal = proposalAccessor.getProposalOfGroup(
					connection, groupID);
			IProgram programme = programmeAccessor.getProgrammeOfProposal(
					connection, proposal.getID());
			return programme;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public ITag getTag(long tagID) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return tagAccessor.getTag(connection, tagID);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public ITag getTagOfProposal(long propId) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			long tagId = proposalAccessor
					.getTagIdOfProposal(connection, propId);
			return tagAccessor.getTag(connection, tagId);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public ITarget getTarget(long tid) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return targetAccessor.getTarget(connection, tid);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	// **************************** FINDS
	// *********************************************************//

	public ITag findTag(String name) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return tagAccessor.findTag(connection, name);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public ITarget findTarget(long programId, String targetName)
			throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return targetAccessor.findTarget(connection, programId, targetName);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public IProgram findProgram(String name) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return programmeAccessor.findProgramme(connection, name);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public long findIdOfGroupInProposal(String name, long proposalId)
			throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return groupAccessor.findIdOfGroupInProposal(connection, name,
					proposalId);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public IProposal findProposal(String name) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return proposalAccessor.findProposal(connection, name);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public boolean groupHasObservationSequence(long groupID)
			throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return sequenceComponentAccessor.groupHasObservationSequence(
					connection, groupID);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public boolean proposalExists(String proposalName, long progId)
			throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return proposalAccessor.proposalExists(connection, proposalName,
					progId);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public boolean groupExists(String groupName, long proposalId)
			throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return groupAccessor.groupExists(connection, groupName, proposalId);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public long findProposalIdOfGroup(long gid) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return groupAccessor.findProposalIdOfGroup(connection, gid);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	// **************************** LISTS
	// *********************************************************//

	public List listActiveUnexpiredGroups(long propID) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return groupAccessor.listGroups(connection, propID, false, false);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public List listActiveFixedGroups() throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return groupAccessor.listActiveFixedGroups(connection);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public List listGroups(long propID, boolean includeInactiveGroups)
			throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return groupAccessor.listGroups(connection, propID,
					includeInactiveGroups);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public List listGroupsUsingTarget(ITarget target) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return groupAccessor.listGroupsUsingTarget(connection, target);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	// not implemented in client yet
	public List listGroupsWithTimingConstraintOfType(int timingConstraintType)
			throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return groupAccessor.listGroupsWithTimingConstraintOfType(
					connection, timingConstraintType);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public List listGroupsUsingInstrumentConfig(
			IInstrumentConfig instrumentConfig) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return groupAccessor.listGroupsUsingInstrumentConfig(connection,
					instrumentConfig);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public List listRevisions(long propID) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return revisionAccessor.listRevisions(connection, propID);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public List listInstrumentConfigs(long progID) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return instrumentConfigAccessor.listInstrumentConfigs(connection,
					progID);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public List listTargets(long progID) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return targetAccessor.listTargets(connection, progID);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public List listTimingConstraints() throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return timingConstraintAccessor.listTimingConstraints(connection);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public List listTimingConstraintsOfType(int type) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return timingConstraintAccessor.listTimingConstraints(connection,
					type);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public List listTimingConstraintsOfTypeEndingAfter(int type, long cutOffTime)
			throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return timingConstraintAccessor.listTimingConstraints(connection,
					type, cutOffTime);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public List listLinkages(long proposalID) throws RemoteException {
		try {
			throw new Exception("not implemented");

		} catch (Exception e) {
			e.printStackTrace();
			throw new Phase2Exception(e);
		}
	}

	public List listProposalsOfProgramme(long progID) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return proposalAccessor
					.listProposalsOfProgramme(connection, progID);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public List listProposalsOfTag(long tagID) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return proposalAccessor.listProposalsOfTag(connection, tagID);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public List listProposalNames(boolean limitToProposalsWithoutPIs)
			throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return proposalAccessor.listProposalNames(connection,
					limitToProposalsWithoutPIs);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public List listProgrammes() throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return programmeAccessor.listProgrammes(connection);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public List listProgrammesOfUser(long uid) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return programmeAccessor.listProgrammesOfUser(connection, uid);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public List listTags() throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return tagAccessor.listTags(connection);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	// **************************** UPDATES
	// *********************************************************//

	public void updateDetectorConfig(IInstrumentConfig config, long keyId)
			throws Exception {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			detectorConfigAccessor.updateDetectorConfig(connection, config,
					keyId);
			connection.commit();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				logger.info("performing connection.rollback();");
				connection.rollback();
				logger.info("connection.rollback() completed successfully.");
			} catch (SQLException e1) {
				logger.error("rollback failed!");
				e1.printStackTrace();
			}
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public void updateGroup(IGroup group, long keyID) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			groupAccessor.updateGroup(connection, group, keyID);
			connection.commit();
			notifyListenersGroupUpdated(group);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				logger.info("performing connection.rollback();");
				connection.rollback();
				logger.info("connection.rollback() completed successfully.");
			} catch (SQLException e1) {
				logger.error("rollback failed!");
				e1.printStackTrace();
			}
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public void updateGroupUrgency(long groupId, boolean isUrgent, long keyID)
			throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			groupAccessor.updateGroupUrgency(connection, groupId, isUrgent,
					keyID);
			connection.commit();
			IGroup group = getGroup(groupId);
			notifyListenersGroupUpdated(group);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				logger.info("performing connection.rollback();");
				connection.rollback();
				logger.info("connection.rollback() completed successfully.");
			} catch (SQLException e1) {
				logger.error("rollback failed!");
				e1.printStackTrace();
			}
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public void updateObservationSequenceOfGroup(long groupID,
			ISequenceComponent sequence, long keyID) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			sequenceComponentAccessor.updateObservationSequenceOfGroup(
					connection, groupID, sequence, keyID);
			connection.commit();
			notifyListenersGroupObsSequenceUpdated(groupID, sequence);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				logger.info("performing connection.rollback();");
				connection.rollback();
				logger.info("connection.rollback() completed successfully.");
			} catch (SQLException e1) {
				logger.error("rollback failed!");
				e1.printStackTrace();
			}
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public void updateTarget(ITarget target, long keyID) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			targetAccessor.updateTarget(connection, target, keyID);
			connection.commit();
			notifyListenersTargetUpdated(target);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				logger.info("performing connection.rollback();");
				connection.rollback();
				logger.info("connection.rollback() completed successfully.");
			} catch (SQLException e1) {
				logger.error("rollback failed!");
				e1.printStackTrace();
			}
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public void updateInstrumentConfig(IInstrumentConfig instConfig, long keyID)
			throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			instrumentConfigAccessor.updateInstrumentConfig(connection,
					instConfig, keyID);
			connection.commit();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				logger.info("performing connection.rollback();");
				connection.rollback();
				logger.info("connection.rollback() completed successfully.");
			} catch (SQLException e1) {
				logger.error("rollback failed!");
				e1.printStackTrace();
			}
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public void updateProgramme(IProgram programme, long keyID)
			throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			programmeAccessor.updateProgramme(connection, programme, keyID);
			connection.commit();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				logger.info("performing connection.rollback();");
				connection.rollback();
				logger.info("connection.rollback() completed successfully.");
			} catch (SQLException e1) {
				logger.error("rollback failed!");
				e1.printStackTrace();
			}
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public void updateProposal(IProposal prop, long keyId)
			throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			proposalAccessor.updateProposal(connection, prop, keyId);
			long pid = prop.getID();
			connection.commit();
			notifyListenersProposalUpdated(prop);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				logger.info("performing connection.rollback();");
				connection.rollback();
				logger.info("connection.rollback() completed successfully.");
			} catch (SQLException e1) {
				logger.error("rollback failed!");
				e1.printStackTrace();
			}
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public void updateTag(ITag tag, long keyID) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			tagAccessor.updateTag(connection, tag, keyID);
			connection.commit();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	// **************************** LISTENERS
	// *********************************************************//
	public void addPhase2UpdateListener(Phase2ModelUpdateListener listener)
			throws RemoteException {
		logger.info(".addPhase2UpdateListener(" + listener + ")");
		if (!updateListeners.contains(listener)) {
			updateListeners.add(listener);
			logger.info("listener added:" + listener);
		}
	}

	public void removePhase2UpdateListener(Phase2ModelUpdateListener listener)
			throws RemoteException {
		logger.info(".removePhase2UpdateListener(" + listener + ")");
		if (updateListeners.contains(listener)) {
			updateListeners.remove(listener);
			logger.info("listener removed:" + listener);
		}
	}

	// **************************** PRIVATE METHODS
	// *********************************************************//

	/*
	 * private void notifyListenersProposalUpdated(long pid) {
	 * logger.info(".notifyListenersProposalUpdated(" + pid + ")"); Iterator i =
	 * phase2UpdateListeners.iterator(); while (i.hasNext()) {
	 * Phase2ModelUpdateListener phase2ModelUpdateListener =
	 * (Phase2ModelUpdateListener)i.next(); try {
	 * phase2ModelUpdateListener.proposalUpdated(pid); } catch (Exception e) {
	 * e.printStackTrace(); logger.error(e); i.remove();
	 * logger.info("removed unresponsive listener:" +phase2ModelUpdateListener
	 * ); } } }
	 * 
	 * private void notifyListenersProposalAdded(long pid) {
	 * logger.info(".notifyListenersProposalAdded(" + pid + ")"); Iterator i =
	 * phase2UpdateListeners.iterator(); while (i.hasNext()) {
	 * Phase2ModelUpdateListener phase2ModelUpdateListener =
	 * (Phase2ModelUpdateListener)i.next(); try {
	 * phase2ModelUpdateListener.proposalAdded(pid); } catch (Exception e) {
	 * e.printStackTrace(); logger.error(e); i.remove();
	 * logger.info("removed unresponsive listener:" +phase2ModelUpdateListener
	 * ); } } }
	 * 
	 * private void notifyListenersProposalDeleted(long pid) {
	 * logger.info(".notifyListenersProposalDeleted(" + pid + ")"); Iterator i =
	 * phase2UpdateListeners.iterator(); while (i.hasNext()) {
	 * Phase2ModelUpdateListener phase2ModelUpdateListener =
	 * (Phase2ModelUpdateListener)i.next(); try {
	 * phase2ModelUpdateListener.proposalDeleted(pid); } catch (Exception e) {
	 * e.printStackTrace(); logger.error(e); i.remove();
	 * logger.info("removed unresponsive listener:" +phase2ModelUpdateListener
	 * ); } } }
	 */

	private void notifyListenersGroupAdded(long pid, IGroup group) {
		logger.info(".notifyListenersGroupAdded(" + pid + "," + group + ")");
		logger.info(".... phase2UpdateListeners=");

		Iterator j = updateListeners.iterator();
		while (j.hasNext()) {
			logger.info(".... ... " + (Phase2ModelUpdateListener) j.next());
		}
		logger.info(".... END");

		Iterator i = updateListeners.iterator();
		while (i.hasNext()) {
			Phase2ModelUpdateListener phase2ModelUpdateListener = (Phase2ModelUpdateListener) i
					.next();
			try {
				logger.info("calling groupAdded(" + pid + "," + group
						+ ") on Phase2ModelUpdateListener: "
						+ phase2ModelUpdateListener);
				phase2ModelUpdateListener.groupAdded(pid, group);
				logger.info("... call completed");
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e);
				i.remove();
				logger.info("removed unresponsive listener:"
						+ phase2ModelUpdateListener);
			}
		}
	}

	private void notifyListenersGroupUpdated(IGroup group) {
		logger.info(".notifyListenersGroupUpdated(" + group + ")");
		Iterator i = updateListeners.iterator();
		while (i.hasNext()) {
			Phase2ModelUpdateListener phase2ModelUpdateListener = (Phase2ModelUpdateListener) i
					.next();
			try {
				phase2ModelUpdateListener.groupUpdated(group);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e);
				i.remove();
				logger.info("removed unresponsive listener:"
						+ phase2ModelUpdateListener);
			}
		}
	}

	private void notifyListenersProposalUpdated(IProposal proposal) {
		logger.info(".notifyListenersProposalUpdated(" + proposal + ")");
		Iterator i = updateListeners.iterator();
		while (i.hasNext()) {
			Phase2ModelUpdateListener phase2ModelUpdateListener = (Phase2ModelUpdateListener) i
					.next();
			try {
				phase2ModelUpdateListener.proposalUpdated(proposal);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e);
				i.remove();
				logger.info("removed unresponsive listener:"
						+ phase2ModelUpdateListener);
			}
		}
	}

	private void notifyListenersGroupDeleted(long gid) {
		logger.info(".notifyListenersGroupDeleted(" + gid + ")");
		Iterator i = updateListeners.iterator();
		while (i.hasNext()) {
			Phase2ModelUpdateListener phase2ModelUpdateListener = (Phase2ModelUpdateListener) i
					.next();
			try {
				phase2ModelUpdateListener.groupDeleted(gid);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e);
				i.remove();
				logger.info("removed unresponsive listener:"
						+ phase2ModelUpdateListener);
			}
		}
	}

	private void notifyListenersGroupObsSequenceAdded(long gid,
			ISequenceComponent sequence) {
		logger.info(".notifyListenersGroupObsSequenceAdded(" + gid + ","
				+ sequence + ")");
		Iterator i = updateListeners.iterator();
		while (i.hasNext()) {
			Phase2ModelUpdateListener phase2ModelUpdateListener = (Phase2ModelUpdateListener) i
					.next();
			try {
				phase2ModelUpdateListener.groupObsSequenceAdded(gid, sequence);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e);
				i.remove();
				logger.info("removed unresponsive listener:"
						+ phase2ModelUpdateListener);
			}
		}
	}

	private void notifyListenersGroupObsSequenceDeleted(long gid) {
		logger.info(".notifyListenersGroupObsSequenceDeleted(" + gid + ")");
		Iterator i = updateListeners.iterator();
		while (i.hasNext()) {
			Phase2ModelUpdateListener phase2ModelUpdateListener = (Phase2ModelUpdateListener) i
					.next();
			try {
				phase2ModelUpdateListener.groupObsSequenceDeleted(gid);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e);
				i.remove();
				logger.info("removed unresponsive listener:"
						+ phase2ModelUpdateListener);
			}
		}
	}

	private void notifyListenersGroupObsSequenceUpdated(long gid,
			ISequenceComponent sequence) {
		logger.info(".notifyListenersGroupObsSequenceUpdated(" + gid + ","
				+ sequence + ")");
		Iterator i = updateListeners.iterator();
		while (i.hasNext()) {
			Phase2ModelUpdateListener phase2ModelUpdateListener = (Phase2ModelUpdateListener) i
					.next();
			try {
				phase2ModelUpdateListener
						.groupObsSequenceUpdated(gid, sequence);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e);
				i.remove();
				logger.info("removed unresponsive listener:"
						+ phase2ModelUpdateListener);
			}
		}
	}

	private void notifyListenersTargetAdded(long progID, ITarget target) {
		logger.info(".notifyListenersTargetAdded(" + progID + "," + target
				+ ")");
		Iterator i = updateListeners.iterator();
		while (i.hasNext()) {
			Phase2ModelUpdateListener phase2ModelUpdateListener = (Phase2ModelUpdateListener) i
					.next();
			try {
				phase2ModelUpdateListener.targetAdded(progID, target);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e);
				i.remove();
				logger.info("removed unresponsive listener:"
						+ phase2ModelUpdateListener);
			}
		}
	}

	private void notifyListenersTargetDeleted(long targetID) {
		logger.info(".notifyListenersTargetDeleted(" + targetID + ")");
		Iterator i = updateListeners.iterator();
		while (i.hasNext()) {
			Phase2ModelUpdateListener phase2ModelUpdateListener = (Phase2ModelUpdateListener) i
					.next();
			try {
				phase2ModelUpdateListener.targetDeleted(targetID);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e);
				i.remove();
				logger.info("removed unresponsive listener:"
						+ phase2ModelUpdateListener);
			}
		}
	}

	private void notifyListenersTargetUpdated(ITarget target) {
		logger.info(".notifyListenersTargetUpdated(" + target + ")");
		Iterator i = updateListeners.iterator();
		while (i.hasNext()) {
			Phase2ModelUpdateListener phase2ModelUpdateListener = (Phase2ModelUpdateListener) i
					.next();
			try {
				phase2ModelUpdateListener.targetUpdated(target);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e);
				i.remove();
				logger.info("removed unresponsive listener:"
						+ phase2ModelUpdateListener);
			}
		}
	}

}
