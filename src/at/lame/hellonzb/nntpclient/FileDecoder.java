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
package at.lame.hellonzb.nntpclient;

import at.lame.hellonzb.*;
import at.lame.hellonzb.helloyenc.*;
import at.lame.hellonzb.parser.*;
import at.lame.hellonzb.util.*;

import java.io.*;
import java.util.*;
import javax.swing.*;

import com.sun.mail.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import musicchecker.Main;


/**
 * This class is called by the class NntpFileDownload as a thread's
 * runnable object when a file has been finished downloading and
 * should now be decoded.
 * 
 * @author Matthias F. Brandstetter
 */
public class FileDecoder implements Runnable, HelloYencRunnable
{
	/** write out raw, encoded data to tmp. file */
	private static boolean writeEncData = false;

	/** The main HelloNzb application object */
	private final HelloNzb mainApp;		

	/** download file to decode */
	private DownloadFile dlFile;
	
	/** the data to decode */
	private Vector<byte[]> articleData;
	
	/** data encoding */
	private String encoding;
		
	/** The download directory on local disk */
	private File dlDir;
	
	
	public FileDecoder(HelloNzb mainApp, File dlDir, DownloadFile dlFile, 
			Vector<byte[]> data, String encoding)
	{
		this.mainApp = mainApp;
		this.dlDir = dlDir;
		this.dlFile = dlFile;
		this.articleData = data;
		this.encoding = encoding;
		this.dlDir = dlDir;
	}
	
	public void run()
	{
		Vector<byte []> outVector = new Vector<byte []>();
		HelloYenc yencDecoder = null;
		UUDecoderStream uuDecoder = null;
		FileOutputStream fileOutStream = null;
		File resultFile = null;
		File resultDir = null;
		String filename = "";
		int uuLength = 0;


		File tmpfile = null;
		FileOutputStream tmpoutstream = null;
		if(writeEncData)
		{
                        Main.getLogger().log(Level.INFO, "Creating Debug-File S:/tmp/debug.yenc");
			tmpfile = new File("S:/tmp/debug.yenc");
			
			if(tmpfile.exists())
				tmpfile.delete();
			try
			{
				tmpfile.createNewFile();
				tmpoutstream = new FileOutputStream(tmpfile); 
			}
			catch(IOException e)
			{
				Main.getLogger().log(Level.SEVERE, null, e);
			}
		}
				
		// prepare suitable decoder
		if(encoding.equals("yenc"))
			yencDecoder = new HelloYenc();
		else if(encoding.equals("uu"))
			uuDecoder = null;
		else
		{
                        Main.getLogger().log(Level.SEVERE, "Could not find a suitable decoder!");
			return;
		}
		
		try
		{
			int i = 0;
			while(articleData.size() > 0)
			{
				// check for corrupt download
				if(articleData.get(0) == null || articleData.get(0).length == 0)
				{
                                        Main.getLogger().log(Level.SEVERE, "FileDecoder: Corrupt data found");
					articleData.remove(0);
					continue;
				}
					
				// set data input stream of the yenc decoder object
				if(encoding.equals("yenc"))
				{
					yencDecoder.setInputData(articleData.get(0));
					yencDecoder.setPartNum(i + 1);
					yencDecoder.setRunnable(this);
				}
				
				// set data for the UUDecoder object
				else if(encoding.equals("uu"))
				{
					byte [] src = articleData.get(0);
									
					uuLength = src.length;
					
					ByteArrayInputStream inStream = new ByteArrayInputStream(src); 
					uuDecoder = new UUDecoderStream(inStream);
				}
					
				// do we have the first (yenc) part loaded (then get filename)
				if(i == 0)
				{
					// get filename
					if(encoding.equals("yenc"))
						filename = yencDecoder.getFileName();
					else if(encoding.equals("uu"))
						filename = uuDecoder.getName();
					
					if(filename.equals(""))
						filename = dlFile.getFilename();
					
					int pos = 0;
					if((pos = filename.indexOf("name=")) != -1)
						filename = filename.substring(pos + 5, filename.length());
						
					if(filename.charAt(0) == '[')
						filename = filename.substring(1);
					if(filename.charAt(filename.length() - 1) == ']')
						filename = filename.substring(0, filename.length() - 1);
						
					try
					{
						resultFile = new File(dlDir.getAbsolutePath().trim() + File.separator + filename);
						resultDir = new File(dlDir.getAbsolutePath().trim());
						
						// create directory
						resultDir.mkdirs();
						
						if(resultFile.exists())
							resultFile.delete();
						
						resultFile.createNewFile();
					}
					catch(IOException ex) // invalid filename in yenc header, so use name from article subject
					{
						filename = dlFile.getFilename(); 
						resultFile = new File(dlDir.getAbsolutePath().trim() + File.separator + filename);
						resultDir = new File(dlDir.getAbsolutePath().trim());
						
						// create directory
						resultDir.mkdirs();
							
						if(resultFile.exists())
							resultFile.delete();
						
						resultFile.createNewFile();
					}
							
					fileOutStream = new FileOutputStream(resultFile);
				}
				
				// tmp. debugging: write raw, encoded data to extra file
				if(writeEncData)
				{
					byte [] btmp = articleData.get(0);
					tmpoutstream.write(btmp);
					tmpoutstream.flush();
				}
					
				// now decode the current article data block
				if(articleData.get(0).length > 0)
				{
					if(encoding.equals("yenc"))
						outVector.add(yencDecoder.decode());
					
					else if(encoding.equals("uu"))
					{
						byte [] bytes = new byte[uuLength];
						int b;
						
						b = uuDecoder.read();
						int outBufCounter = 0;
						for(; b != -1 && outBufCounter < uuLength; outBufCounter++)
						{
							bytes[outBufCounter] = (byte) b;
							b = uuDecoder.read();
						}

						byte [] newOutBuf = new byte[outBufCounter];
						System.arraycopy(bytes, 0, newOutBuf, 0, outBufCounter);
						outVector.add(newOutBuf);
					}
				}
					
				// update progress bar in main window
				final int j = i + 1;
		        SwingUtilities.invokeLater(new Runnable() 
		        { 
		        	public void run()
		        	{
		        		mainApp.updateDownloadQueue(dlFile.getFilename(), j);
		        	} 
		        } );
					
				// remove processed element
		        articleData.remove(0);
		        i++;
			}
			
			// notify main application that writing of file started
	        SwingUtilities.invokeLater(new Runnable() 
	        { 
	        	public void run()
	        	{
	        		mainApp.fileWritingStarted(dlFile.getFilename());
	        		mainApp.updateDownloadQueue(dlFile.getFilename(), 0);
	        	} 
	        } );
			
			// write data to output file
			i = 0;
			while(outVector.size() > 0)
			{
				byte [] buffer = outVector.get(0);
				fileOutStream.write(buffer);
				fileOutStream.flush();
				
				// update progress bar in main window
				final int j = i + 1;
		        SwingUtilities.invokeLater(new Runnable() 
		        { 
		        	public void run()
		        	{
		        		mainApp.updateDownloadQueue(dlFile.getFilename(), j);
		        	} 
		        } );

		        outVector.remove(0);
		        i++;
			}
			
			// close output file
			fileOutStream.close();
			
			// debug file
			if(writeEncData)
				tmpoutstream.close();
		}
		catch(Exception e)
		{
			Main.getLogger().log(Level.SEVERE, null, e);
		}

		// update main application window that file decoding is finished now
        SwingUtilities.invokeLater(new Runnable() 
        { 
        	public void run()
        	{
				// notify main application that decoding of this file has finished
				mainApp.fileDecodingFinished(dlFile.getFilename());
			} 
		} ); 
	}

	public void crc32Error()
	{
		// TODO Auto-generated method stub
	}
}






























