package com.chs.extemp;

import java.util.logging.Logger;

import com.chs.extemp.cli.CLI;
import com.chs.extemp.gui.ResearchGUI;

/**
 * ExtempFiller program
 *
 * @author Logan Lembke
 */
public class Main {

	public static void main(final String[] args) {
		initLogger();
		try {
			System.out.println("CHS Extemporaneous Researcher");
			if (args.length > 0)
				new CLI(args[0]);
			else {
				System.out.println("No questions list specified. Starting GUI interface...");
				new ResearchGUI();
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private static void initLogger() {
		final Logger logger = ExtempLogger.getLogger();
		logger.setUseParentHandlers(false);
	}
}
