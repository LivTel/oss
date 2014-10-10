package ngat.oss.impl.rmi.accessors;

import java.util.Iterator;
import java.util.List;

import ngat.icm.InstrumentCapabilitiesProvider;
import ngat.icm.InstrumentDescriptor;
import ngat.icm.InstrumentRegistry;
import ngat.oss.reference.Const;
import ngat.oss.transport.RMIConnectionPool;
import ngat.phase2.IPublishedSystemProperties;
import ngat.phase2.XPublishedSystemProperties;
import ngat.tcm.SciencePayload;
import ngat.tcm.Telescope;
import ngat.tcm.TelescopeSystem;

import org.apache.log4j.Logger;

public class StatusAccessor {

	static Logger logger = Logger.getLogger(StatusAccessor.class);

	/**
	 * use RMI to get a reference to the RCS based ngat.tcm.Telescope object.
	 * From this, derive the information required and return it wrapped up in a
	 * IPublishedSystemProperties object
	 * 
	 * @return IPublishedSystemProperties containg the properties required
	 */
	public IPublishedSystemProperties getPublishedSystemProperties() {

		logger.info("getPublishedSystemProperties() invoked");

		double rotatorBaseOffsetRads = -1;
		XPublishedSystemProperties psp = new XPublishedSystemProperties();

		try {
			Telescope telescope = (Telescope) RMIConnectionPool.getInstance()
					.getRemoteServiceObject(Const.OCC_TELESCOPE_PROPERTIES);
			TelescopeSystem telescopeSystem = telescope.getTelescopeSystem();
			SciencePayload sciencePayload = telescopeSystem.getSciencePayload();
			rotatorBaseOffsetRads = sciencePayload.getRotatorBaseOffset();

			// set the base rotator offset angle
			psp.setValue(IPublishedSystemProperties.ROTATOR_BASE_OFFSET,
					String.valueOf(rotatorBaseOffsetRads));

			// get a reference to the instrument registry
			InstrumentRegistry ireg = (InstrumentRegistry) RMIConnectionPool
					.getInstance().getRemoteServiceObject(
							Const.OCC_INSTRUMENT_REGISTRY);
			List instList = ireg.listInstruments();
			Iterator instListIter = instList.iterator();

			// iterate through the instruments and set teh instrument offset for
			// each
			while (instListIter.hasNext()) {
				InstrumentDescriptor instrumentDesc = (InstrumentDescriptor) instListIter
						.next();
				String instName = instrumentDesc.getInstrumentName();

				InstrumentCapabilitiesProvider instCapProv = ireg
						.getCapabilitiesProvider(instrumentDesc);
				double instRotOffset = instCapProv.getCapabilities()
						.getRotatorOffset();

				logger.info("Want to set rotator value for instrument "
						+ instName + " to " + instRotOffset
						+ " but need to know instrument name, so what is it?");

				// instrument names coming back from the RCS are always
				// capitalized. IO:O has a colon in it
				if (instName.equals("IO:O")) { // need to get the right
												// instrument name
					psp.setValue(
							IPublishedSystemProperties.IOO_INSTRUMENT_OFFSET,
							String.valueOf(instRotOffset));
				} else if (instName.equals("IO:I")) { // need to get the right
														// instrument name
					psp.setValue(
							IPublishedSystemProperties.IOI_INSTRUMENT_OFFSET,
							String.valueOf(instRotOffset));
				} else if (instName.equals("RISE")) { // need to get the right
														// instrument name
					psp.setValue(
							IPublishedSystemProperties.RISE_INSTRUMENT_OFFSET,
							String.valueOf(instRotOffset));
				} else if (instName.equals("RINGO3")) { // need to get the right
														// instrument name
					psp.setValue(
							IPublishedSystemProperties.RINGO3_INSTRUMENT_OFFSET,
							String.valueOf(instRotOffset));
				} else if (instName.equals("FRODO")) { // need to get the right
														// instrument name
					psp.setValue(
							IPublishedSystemProperties.FRODO_INSTRUMENT_OFFSET,
							String.valueOf(instRotOffset));
				} else if (instName.equals("SPRAT")) { // need to get the right
														// instrument name
					psp.setValue(
							IPublishedSystemProperties.SPRAT_INSTRUMENT_OFFSET,
							String.valueOf(instRotOffset));
				}
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return psp;
	}
}
