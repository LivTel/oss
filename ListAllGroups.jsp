<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="ngat.oss.model.*, 
    ngat.oss.reference.*, 
    ngat.oss.transport.*, 
    ngat.phase2.*, 
    java.util.*"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>LT Fixed-group allocations</title>
<body>
<% 	IPhase2Model model = (IPhase2Model)RMIConnectionPool.getInstance().getModel(Const.PHASE2_MODEL_SERVICE);
	List programmesList = model.listProgrammes();
	Iterator progIt = programmesList.iterator();
	while (progIt.hasNext()) {
		IProgram program = (IProgram)progIt.next();
		String programName = program.getName();
%>
<%= programName %></br>
<%
		long progId = program.getID();
		List proposalsList = model.listProposalsOfProgramme(progId);
		Iterator propIt = proposalsList.iterator();
		while (propIt.hasNext()) {
			IProposal proposal = (IProposal)propIt.next();
			String proposalName = proposal.getName();
%>
-- <%= proposalName %></br>
<%
			long proposalId = proposal.getID();
			List groupsList = model.listGroups(proposalId, true);
			Iterator groupsIt = groupsList.iterator();
			while (groupsIt.hasNext()) {
				IGroup group = (IGroup)groupsIt.next();
				String groupName = group.getName();
%>
-- -- <%= groupName %></br>
<%
			}
		}
	}
%>
</body>
</html>
