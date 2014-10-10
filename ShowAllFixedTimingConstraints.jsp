<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="ngat.oss.model.*,
    ngat.oss.impl.mysql.reference.*, 
    ngat.oss.reference.*, 
    ngat.oss.transport.*, 
    ngat.phase2.*, 
    java.util.*,
    java.text.*,
    ngat.sms.bds.TestChargeAccountingModel"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>LT Fixed-group allocations</title>
<body>
Fixed Group times:<br><br>
<% 	
	SimpleDateFormat defaultDateFormat = new SimpleDateFormat("d MMM yyyy HH:mm:ss z");
	defaultDateFormat.setLenient(false);
	defaultDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	
	IPhase2Model model = (IPhase2Model)RMIConnectionPool.getInstance().getModel(Const.PHASE2_MODEL_SERVICE);
	long now = System.currentTimeMillis();

	List groupsWithFixedTimingConstraints = model.listGroupsWithTimingConstraintOfType(GroupTypes.FIXED_GROUP);
	Iterator ftci = groupsWithFixedTimingConstraints.iterator();
	while (ftci.hasNext()) {
		IGroup group = (IGroup)ftci.next();
		ISequenceComponent sequenceComponent = model.getObservationSequenceOfGroup(group.getID());
		XFixedTimingConstraint fixedTimingConstraint = (XFixedTimingConstraint)group.getTimingConstraint();
		
		TestChargeAccountingModel tcam = new TestChargeAccountingModel();

		long estimatedDuration = (long)tcam.calculateCost(sequenceComponent);
		long startTime = fixedTimingConstraint.getStartTime();
		long estimatedEndTime = startTime + estimatedDuration;
					
		String startTimeString = defaultDateFormat.format(new Date(startTime));
		String estimatedEndTimeString = defaultDateFormat.format(new Date(estimatedEndTime));
%>
<%= startTimeString %> <--> <%= estimatedEndTimeString %><br>
<%		
	}
%>
</body>
</html>
