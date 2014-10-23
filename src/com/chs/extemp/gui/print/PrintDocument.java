package com.chs.extemp.gui.print;

import javax.print.PrintService;
import javax.print.attribute.Attribute;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.JEditorPane;

public class PrintDocument {
	
	private String contents;
	private String footer;
	
	public PrintDocument(String contents, String footer) {
		this.contents = contents;
		this.footer = footer;
	}
	
	public boolean print(PrintService service, PrintRequestAttributeSet attributes) {
		JEditorPane editPane = new JEditorPane();
		editPane.setText(this.contents);
		//try {
			Attribute[] attrs = attributes.toArray();
			for (Attribute a : attrs) {
				System.out.println(a);
			}
			return false;
			//return editPane.print(new MessageFormat(""), new MessageFormat(this.footer), false, service, attributes, false);
		/*} catch (PrinterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}*/
	}

}
