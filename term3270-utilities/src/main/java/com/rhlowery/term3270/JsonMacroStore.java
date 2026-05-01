package com.rhlowery.term3270;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openide.util.lookup.ServiceProvider;
import java.io.File;
import java.util.List;

/**
 * JSON implementation of macro storage using Jackson.
 */
@ServiceProvider(service = IMacroStore.class)
public class JsonMacroStore implements IMacroStore {

  private static final ObjectMapper mapper = new ObjectMapper();

  @Override
  public void save(List<MacroAction> actions, File file) throws Exception {
    mapper.writeValue(file, actions);
  }

  @Override
  public List<MacroAction> load(File file) throws Exception {
    return mapper.readValue(file, 
        mapper.getTypeFactory().constructCollectionType(List.class, MacroAction.class));
  }

  @Override
  public boolean supports(String format) {
    return "JSON".equalsIgnoreCase(format);
  }
}
