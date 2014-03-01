package com.chs.extemp;

import java.util.logging.Logger;

import com.chs.extemp.gui.ExtempFillerGUI;

/**
 * ExtempFiller program
 * @author Logan Lembke
 */
public class Main {
	
	private static Logger logger;

	public static void main(final String[] args) {
		initLogger();
		try {
			System.out.println("CHS Extemporaneous Researcher");
			if(args.length > 0) {
				String filename = args[0];
				System.out.println("Starting CLI using questions-list \"" + filename + ".\"");
				
				return;
			} else {
				System.out.println("No questions list specified. Starting GUI interface...");
				ExtempFillerGUI gui = new ExtempFillerGUI();
				return;
			}

		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}
	
	// Used to prevent nasty console output
	private static void initLogger() {
		logger = ExtempLogger.getLogger();
		logger.setUseParentHandlers(false);
	}
}
