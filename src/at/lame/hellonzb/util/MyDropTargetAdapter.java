/*******************************************************************************
 * HelloNzb -- The Binary Usenet Tool
 * Copyright (C) 2010-2011 Matthias F. Brandstetter
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package at.lame.hellonzb.util;

import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.util.*;
import java.io.*;
import javax.swing.JOptionPane;
import javax.xml.stream.XMLStreamException;
import at.lame.hellonzb.HelloNzb;
import at.lame.hellonzb.parser.NzbParser;
import java.util.logging.Level;
import java.util.logging.Logger;
import musicchecker.Main;


public class MyDropTargetAdapter extends DropTargetAdapter
{
	private HelloNzb mainApp;
	
	
	public MyDropTargetAdapter(HelloNzb mainApp)
	{
		this.mainApp = mainApp;
	}
	
	@SuppressWarnings("unchecked")
	public void drop(DropTargetDropEvent dtde)
	{
		if(dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
		{
			try
			{
				dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
				List<File> list = (List<File>) dtde.getTransferable().getTransferData(
						DataFlavor.javaFileListFlavor);
				for(File file : list)
				{
					// valid file type?
					if(!file.isFile())
						continue;
					if(!file.canRead())
						continue;
					
					try
					{
						mainApp.addNzbToQueue(new NzbParser(mainApp, file.getAbsolutePath()));
					}
					catch(XMLStreamException e)
					{
						String msg = Main.localer.getBundleText("PopupXMLParserError"); 
						String title = Main.localer.getBundleText("PopupErrorTitle");
						JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
						Main.getLogger().log(Level.SEVERE, null, e); 
					}
					catch(java.text.ParseException e)
					{
						String msg = e.getMessage();
						String title = Main.localer.getBundleText("PopupErrorTitle");
						JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
						Main.getLogger().log(Level.SEVERE, null, e); 
					}
				}
			}
			catch(UnsupportedFlavorException e)
			{
				Main.getLogger().log(Level.SEVERE, null, e); 
			}
			catch(IOException e)
			{
				Main.getLogger().log(Level.SEVERE, null, e); 
			}
		}
	}
}



































