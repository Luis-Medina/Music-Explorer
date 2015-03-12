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
package at.lame.hellonzb.nntpclient.nioengine;

public enum ChannelStatus
{
	INIT,
	CONNECTED,
	W_AUTH_USER,
	R_AUTH_USER,
	W_AUTH_PASS,
	R_AUTH_PASS,
	START_FETCH,
	GROUP_SENT,
	IDLE,
	READY,
	START_RECEIVE,
	RECEIVING_DATA,
	TO_QUIT,
	FINISHED,
	SERVER_ERROR
	
	
	
}



































