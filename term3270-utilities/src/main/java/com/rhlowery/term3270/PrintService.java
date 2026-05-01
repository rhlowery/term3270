package com.rhlowery.term3270;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.openide.util.lookup.ServiceProvider;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Service implementation for generating PDF output from the terminal screen.
 */
@ServiceProvider(service = IPrintProvider.class)
public class PrintService implements IPrintProvider {

  @Override
  public void print(ScreenBuffer buffer, File file) throws Exception {
    Document document = new Document();
    PdfWriter.getInstance(document, new FileOutputStream(file));
    document.open();

    // Use a monospaced font to maintain terminal alignment
    Font font = FontFactory.getFont(FontFactory.COURIER, 10);
    
    String text = buffer.toPlainText();
    for (String line : text.split("\n")) {
      Paragraph p = new Paragraph(line, font);
      document.add(p);
    }

    document.close();
  }

  @Override
  public boolean supports(String format) {
    return "PDF".equalsIgnoreCase(format);
  }
}
