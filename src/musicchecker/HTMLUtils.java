/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package musicchecker;

/**
 *
 * @author Luiso
 */
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.ArrayList;

import javax.swing.text.html.parser.ParserDelegator;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTML.Attribute;
import javax.swing.text.MutableAttributeSet;

public class HTMLUtils {
  private HTMLUtils() {}

  public static List<String> extractLinks(Reader reader) throws IOException {
    final ArrayList<String> list = new ArrayList<String>();

    ParserDelegator parserDelegator = new ParserDelegator();
    ParserCallback parserCallback = new ParserCallback() {
            @Override
      public void handleText(final char[] data, final int pos) { }
            @Override
      public void handleStartTag(Tag tag, MutableAttributeSet attribute, int pos) {
        if (tag == Tag.A) {
          String address = (String) attribute.getAttribute(Attribute.HREF);
          list.add(address);
        }
      }
            @Override
      public void handleEndTag(Tag t, final int pos) {  }
            @Override
      public void handleSimpleTag(Tag t, MutableAttributeSet a, final int pos) { }
            @Override
      public void handleComment(final char[] data, final int pos) { }
            @Override
      public void handleError(final java.lang.String errMsg, final int pos) { }
    };
    parserDelegator.parse(reader, parserCallback, false);
    return list;
  }

}

