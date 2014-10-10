package ngat.oss.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import ngat.oss.impl.mysql.accessors.BeamSteeringConfigAccessor;
import ngat.phase2.IOpticalSlideConfig;
import ngat.phase2.ITipTiltAbsoluteOffset;
import ngat.phase2.XBeamSteeringConfig;
import ngat.phase2.XDetectorConfig;
import ngat.phase2.XFilterDef;
import ngat.phase2.XFilterSpec;
import ngat.phase2.XFocusControl;
import ngat.phase2.XOpticalSlideConfig;
import ngat.phase2.XPhase2Identity;
import ngat.phase2.XTipTiltAbsoluteOffset;
import ngat.phase2.XTipTiltImagerInstrumentConfig;
import ngat.phase2.XWindow;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;

public class JiBXTester {
	
	private static final String OUTPUT_FILENAME = "jibxTestClassFile.xml";

	/**
	 * Loads a file and converts it into a String object.
	 * @param aFile The file to be convereted into a long String
	 * @return A String containing the full contents of the file.
	 */
	private static String getFileContents(File aFile) {
		//...checks on aFile are elided
		StringBuffer contents = new StringBuffer();

		//declared here to make visible to finally clause
		BufferedReader input = null;
		try {
			//use buffering, reading one line at a time
			//FileReader always assumes default encoding is OK!
			input = new BufferedReader( new FileReader(aFile) );
			String line = null; //not declared within while loop
			/*
			 * readLine  :
			 *  returns the content of a line MINUS the newline.
			 *  returns null only for the END of the stream.
			 *  returns an empty String if two newlines appear in a row.
			 */
			while (( line = input.readLine()) != null){
				contents.append(line);
				contents.append(System.getProperty("line.separator"));
			}
		}
		catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}
		catch (IOException ex){
			ex.printStackTrace();
		}
		finally {
			try {
				if (input!= null) {
					//flush and close both "input" and its underlying FileReader
					input.close();
				}
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return contents.toString();
	}
	
	/**\
	 * Takes an object and writes in JiBX format to a file,
	 * it then prints out the contents of that file to the console, 
	 * it then loads that object back out of the file again, and returns the 
	 * new loaded object.
	 * @param object
	 * @return
	 * @throws Exception
	 */
	private static Object jibxMarshal(Object object) throws Exception {
		System.out.println("testObject to marshal= " + object);
		System.out.println("Creating BindingFactory instance for " + object.getClass().getName());
		
		//marshal the object to a file ****************************************************************************************************
		IBindingFactory factory = BindingDirectory.getFactory(object.getClass());
		
		System.out.println("... factory created");
		System.out.println("Marshal the object to a file");
		System.out.println("... creating marshalling context");
		
		IMarshallingContext mContext = factory.createMarshallingContext();
		
		System.out.println("... marshalling context created");
		System.out.println("... outputing to file: " + OUTPUT_FILENAME);
		
		mContext.marshalDocument(object, "UTF-8", null, new FileOutputStream(OUTPUT_FILENAME));
		System.out.println("Complete");
		
		//print out the contents of the file
		System.out.println("\nFile: " + OUTPUT_FILENAME + " reads:");
		System.out.println(getFileContents(new File(OUTPUT_FILENAME)));
		
		//unmarshal the object from the file **********************************************************************************************
		System.out.println("Unmarshal the file to an object");
		System.out.println("... creating unmarshalling context");
		
		IUnmarshallingContext uContext = factory.createUnmarshallingContext();
		
		System.out.println("... unmarshalling context created");
		System.out.println("... loading file: " + OUTPUT_FILENAME);
		
		Object loadedObject = uContext.unmarshalDocument(new FileInputStream(OUTPUT_FILENAME), null);
		
		System.out.println("... file loaded");
		System.out.println("\n\nObject found has type: " + loadedObject.getClass().getName());
		System.out.println("\n" + loadedObject);
		
		if (loadedObject instanceof XPhase2Identity) {
			System.out.println("... lock=" + ((XPhase2Identity)loadedObject).getLock());
			XPhase2Identity phase2Identity = (XPhase2Identity)loadedObject;
			System.out.println("... XPhase2Identity stuff");
			System.out.println("... ... id=" + phase2Identity.getID());
			System.out.println("... ... name=" + phase2Identity.getName());
		}
		return loadedObject;
	}
	/**
	 * Given an object, print to the system.out the jibx representation of that object
	 * @param object jibx defined object to be displayed
	 * @throws Exception pears
	 */
	public static void showJibxOfObject(Object object) throws Exception {
		IBindingFactory factory = BindingDirectory.getFactory(object.getClass());

		IMarshallingContext mContext = factory.createMarshallingContext();
		
		mContext.marshalDocument(object, "UTF-8", null, System.out);
		//mContext.marshalDocument(object, "UTF-8", null, new FileOutputStream(OUTPUT_FILENAME));
	}
	
	public static void main (String[] a) {
		//create an instance of the class to marshal
		//System.err.println("START");
		/*
		XFocusControl focusControl = new XFocusControl();
		focusControl.setInstrumentName("IO:O");
		*/
		XTipTiltAbsoluteOffset tipTiltAbsoluteOffset = 
			new XTipTiltAbsoluteOffset(
					3.14, 
					159.2654, 
					"IO:O", 
					ITipTiltAbsoluteOffset.OFFSET_TYPE_SKY, 
					ITipTiltAbsoluteOffset.TIPTILT_BOTTOM);
		
		/*
		XBeamSteeringConfig beamSteeringConfig = new XBeamSteeringConfig();
			XOpticalSlideConfig opticalSlideConfig1 = new XOpticalSlideConfig();
			opticalSlideConfig1.setSlide(IOpticalSlideConfig.SLIDE_BOTTOM);
			opticalSlideConfig1.setPosition(IOpticalSlideConfig.POSITION_DI_BR);
			XOpticalSlideConfig opticalSlideConfig2 = new XOpticalSlideConfig();
			opticalSlideConfig2.setSlide(IOpticalSlideConfig.SLIDE_BOTTOM);
			opticalSlideConfig2.setPosition(IOpticalSlideConfig.POSITION_DI_BR);
		beamSteeringConfig.setSlideConfig1(opticalSlideConfig1);
		beamSteeringConfig.setSlideConfig2(opticalSlideConfig2);
		*/
		/*
		Object marshalledObject = null;
		try {
			marshalledObject = jibxMarshal(tipTiltAbsoluteOffset);
			compareProperties(tipTiltAbsoluteOffset, marshalledObject);
			
			//System.out.println("\n\n");
			//marshalledObject = jibxMarshal(group);
			//compareProperties(group, marshalledObject);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.err.println("END");
		*/
		try {
			showJibxOfObject(tipTiltAbsoluteOffset);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static boolean compareProperties(Object before, Object after) {
		System.out.println("... running compareProperties()");
		
		String bS = before.toString();
		String aS = after.toString();
		
		System.out.println(aS);
		System.out.println(bS);
		boolean equals = aS.equals(bS);
		System.out.println("... ... compareProperties returns '" + equals + "'");
		return equals;
	}
}
