/*
 * 04/29/2003 - 18:20:51
 *
 * MIRCLine.java
 * Copyright (C) 2000 veryfree
 * very.free@free.fr
 * http://jchatirc.free.fr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package musicchecker;

import org.apache.regexp.*;
import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MIRCLine permet de prendre en charge les attributs des messages envoy�e par le logiciel Mirc
 * @todo Use an MIRCLineeHelper singleton to parse a command to a MIRCLine
 * @author veryfree <very.free@free.fr>
 * @author $Author: veryfree $ (last edit)
 * @version $Revision: 1.7 $
 */
public class MIRCLine {
    private static Logger logger = Main.getLogger();

	/**
	* Tableau de couleurs hexadecimales correspondant chacune au couleurs utilis�e par Mirc
	**/
 public static final Color colors[] =
     {
        new Color(0x000000),
        new Color(0x000000),
        new Color(0x0000A0),
        new Color(0x008000),
        new Color(0xFF0000),
        new Color(0x800040),
        new Color(0x800080),
        new Color(0x808000),
        new Color(0xFFFF00),
        new Color(0x80FF00),
        new Color(0x008080),
        new Color(0x00FFFF),
        new Color(0x0000FF),
        new Color(0xFF0080),
        new Color(0x808080),
        new Color(0xC0C0C0)
      };


    public static final char BOLD = '\002';
    public static final char COLOR = '\003';
    public static final char BELL = '\007';
    public static final char RESET = '\017';
    public static final char ITALIC = '\024';
    public static final char REVERSE = '\026';
    public static final char UNDERLINE = '\037';
    public static final char INFO = '\001';


    private Color fcolor;
    private Color bgcolor;
    private String message = "";
    private static RE r = null;
    private StringBuffer sb;

    static {
        try {
            r = new RE("\002|\003[:digit:]{1,2}|\003|,[:digit:]{1,2}|\007|\017|\024|\026|\037|\001");
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not initialize regular expression for the mIRC colors  ", e);
        }
    }
	/**
	 * Contruit une nouvel objet MIRCLine a partir du message pass� en argument
	 **/
    public MIRCLine(String message) {

        this.message = message;
        this.fcolor = Color.black;
        this.bgcolor = Color.white;
        this.sb = new StringBuffer(message);

        int start = 0;
        int end;
        String match = "";

        while (r.match(sb.toString(), start)) {
            start = r.getParenStart(0);
            end = r.getParenEnd(0);
            match = r.getParen(0);

            switch (match.charAt(0)) {
                case COLOR:
                    this.fcolor = getColor(match.substring(1), Color.black);
                    sb.delete(start, end);
                    break;
                case ','://ca marche mais c pas tres beau et ca consome des ressources alors on vire
                    //	this.bgcolor=getColor(match.substring(1),Color.white);
                    sb.delete(start, end);
                    break;
                default:
                    sb.deleteCharAt(start);
            }
        }
        this.message = sb.toString();
    }

    private Color getColor(String c, Color defaultColor) {
        if (c.length() == 0)
            return defaultColor;
        try {
            int i = Integer.parseInt(c);
            if (i < colors.length)
                return colors[i];
            else
                return defaultColor;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to get Color from ["+c+"]", e);
            return defaultColor;
        }
    }
	/**
	* Enleve du message pass� en argument,tout les caracteres r�serv�s aux couleurs/bold/italique (\007;\017;\024;\026;\037 ...)
	* de facon a pouvoir l'afficher partout correctement.
	**/
    public static String filterMircAttributes(String text) {
        StringBuffer sb = new StringBuffer(text);
        int start = 0;
        int end;

        while (r.match(sb.toString(), start)) {
            start = r.getParenStart(0);
            end = r.getParenEnd(0);
            switch (r.getParen(0).charAt(0)) {
                case COLOR:
                case ',':
                    sb.delete(start, end);
                    break;
                default   :
                    sb.deleteCharAt(start);
            }
        }
        return sb.toString();
    }
	/**
	 * Retourne la couleur du texte sp�cifi�e par le message.
	 **/
    public Color getForegroundColor() {
        return fcolor;
    }
	/**
	 * Retourne la couleur de fond sp�cifi�e par le message.
	 **/
    public Color getBackgroundColor() {
        return bgcolor;
    }
	/**
	 * Retourne le message sans ses attributs. 
	 **/
    public String getMessage() {
        return message;
    }

}