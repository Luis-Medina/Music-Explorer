/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package musicchecker;

import java.io.IOException;
import java.io.Reader;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

/**
 *
 * @author Luis
 */
public class HTML2Text extends HTMLEditorKit.ParserCallback {

        StringBuffer s;

        public HTML2Text() {
        }

        public void parse(Reader in) throws IOException {
            s = new StringBuffer();
            ParserDelegator delegator = new ParserDelegator();
            // the third parameter is TRUE to ignore charset directive
            delegator.parse(in, this, Boolean.TRUE);
        }
        

        @Override
        public void handleText(char[] text, int pos) {
            s.append("\n");
            s.append(text);
        }

        public String getText() {
            return s.toString();
        }
    }
