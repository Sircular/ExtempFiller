package com.chs.extemp;

import java.util.logging.Logger;

import com.chs.extemp.cli.CLI;
import com.chs.extemp.gui.ResearchGUI;

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
				new CLI(args[0]);
				return;
			} else {
				System.out.println("No questions list specified. Starting GUI interface...");
				new ResearchGUI();
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
