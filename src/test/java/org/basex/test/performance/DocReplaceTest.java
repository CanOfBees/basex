package org.basex.test.performance;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.test.*;
import org.junit.*;

/**
 * This class replaces document nodes in a database.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class DocReplaceTest extends SandboxTest {
  /** Number of runs per client. */
  private static final int NQUERIES = 10000;

  /**
   * Runs the test.
   * @throws Exception exception
   */
  @Test
  public void run() throws Exception {
    CONTEXT.prop.set(Prop.TEXTINDEX, false);
    CONTEXT.prop.set(Prop.ATTRINDEX, false);
    CONTEXT.prop.set(Prop.AUTOFLUSH, false);

    // create test database
    new CreateDB(NAME).execute(CONTEXT);

    // replace nodes
    for(int i = 0; i < NQUERIES; i++) {
      new Add(i + IO.XMLSUFFIX, "<a/>").execute(CONTEXT);
    }

    // replace nodes with same content
    for(int i = 0; i < NQUERIES; i++) {
      new Replace(i + IO.XMLSUFFIX, "<a/>").execute(CONTEXT);
    }

    // Drop database
    new DropDB(NAME).execute(CONTEXT);
  }
}
