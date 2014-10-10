<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="ngat.oss.model.*, 
    ngat.tcm.*,
    ngat.icm.*,
    ngat.oss.reference.*, 
    ngat.oss.transport.*, 
    ngat.phase2.*, 
    java.util.*"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Show service properties</title>
<body>
<%
	Telescope telescope = (Telescope)RMIConnectionPool.getInstance().getRemoteServiceObject(Const.OCC_TELESCOPE_PROPERTIES);
	TelescopeSystem telescopeSystem = telescope.getTelescopeSystem();
	SciencePayload sciencePayload = telescopeSystem.getSciencePayload();
	double rotatorBaseOffsetRads = sciencePayload.getRotatorBaseOffset();
%>
Rotator base-offset (radians) =  <%= rotatorBaseOffsetRads %></br>

<%
	InstrumentRegistry ireg = (InstrumentRegistry)RMIConnectionPool.getInstance().getRemoteServiceObject(Const.OCC_INSTRUMENT_REGISTRY);
	List instList = ireg.listInstruments();
	Iterator instListIter = instList.iterator();
	while (instListIter.hasNext()) {
		InstrumentDescriptor instrumentDesc = (InstrumentDescriptor) instListIter.next();
		String instName = instrumentDesc.getInstrumentName();
		
		InstrumentCapabilitiesProvider instCapProv = ireg.getCapabilitiesProvider(instrumentDesc);
		double instRotOffset = instCapProv.getCapabilities().getRotatorOffset();
%>
<%= instName %> rotator offset = <%= instRotOffset %></br>
<%		
	}		
%>

</body>
</html>
