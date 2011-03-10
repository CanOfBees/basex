package org.basex.query;

import static org.basex.core.Text.*;

import org.basex.core.BaseXException;
import org.basex.data.SerializerException;
import org.basex.io.IO;
import org.basex.query.item.Empty;
import org.basex.query.item.Value;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;
import org.basex.util.InputParser;
import org.basex.util.StringList;
import org.basex.util.TokenBuilder;

/**
 * This class indicates exceptions during query parsing or evaluation.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class QueryException extends Exception {
  /** Error reference. */
  private Err err;
  /** Alternative error code. */
  private String code;
  /** Error value. */
  private Value value = Empty.SEQ;
  /** File reference. */
  private IO file;
  /** Code completions. */
  private StringList complete;
  /** Error line and column. */
  private int[] lineCol;
  /** Marked error column. */
  private int markedCol;

  /**
   * Default constructor.
   * @param ii input info
   * @param er error reference
   * @param ext error extension
   */
  public QueryException(final InputInfo ii, final Err er, final Object... ext) {
    this(ii, null, null, er.desc, ext);
    err = er;
  }

  /**
   * Default constructor.
   * @param ii input info
   * @param ex serializer exception
   */
  public QueryException(final InputInfo ii, final SerializerException ex) {
    this(ii, (String) null, null, ex.getLocalizedMessage());
    err = ex.err();
  }

  /**
   * Default constructor.
   * @param ii input info
   * @param ex serializer exception
   */
  public QueryException(final InputInfo ii, final QueryException ex) {
    this(ii, ex.code, ex.value, ex.getLocalizedMessage());
  }

  /**
   * Constructor, specifying the error code and message as string.
   * @param ii input info
   * @param errc error code
   * @param val error value
   * @param msg error message
   * @param ext error extension
   */
  public QueryException(final InputInfo ii, final String errc, final Value val,
      final String msg, final Object... ext) {

    super(BaseXException.message(msg, ext));
    code = errc;
    value = val;
    if(ii == null) return;

    file = ii.file;
    lineCol = ii.lineCol();
  }

  /**
   * Returns the error column.
   * @return error column
   */
  public int col() {
    return lineCol == null ? 0 : lineCol[1];
  }

  /**
   * Returns the marked error column.
   * @return marked error column
   */
  public int markedCol() {
    return markedCol;
  }

  /**
   * Returns the error line.
   * @return error line
   */
  public int line() {
    return lineCol == null ? 0 : lineCol[0];
  }

  /**
   * Returns suggestions for code completions.
   * @return suggestions
   */
  public StringList complete() {
    return complete == null ? new StringList() : complete;
  }

  /**
   * Sets suggestions for code completions.
   * @param qp query parser
   * @param comp completions
   */
  public void complete(final InputParser qp, final StringList comp) {
    complete = comp;
    pos(qp);
  }

  /**
   * Finds line and column for the specified query parser.
   * @param parser parser
   */
  void pos(final InputParser parser) {
    markedCol = parser.qm;
    // check if information has already been added
    if(lineCol != null) return;

    file = parser.file;
    lineCol = InputInfo.lineCol(parser.query, Math.min(parser.qm, parser.ql));
  }

  /**
   * Returns the error code.
   * @return error code
   */
  public String code() {
    return code == null ? err.toString() : code;
  }

  /**
   * Returns the error code.
   * @return error code
   */
  public Err err() {
    return err;
  }

  /**
   * Returns the error value.
   * @return error value
   */
  public Value value() {
    return value;
  }

  @Override
  public String getLocalizedMessage() {
    return super.getMessage();
  }

  @Override
  public String getMessage() {
    final TokenBuilder tb = new TokenBuilder();
    if(lineCol != null) {
      tb.add(STOPPED + ' ').addExt(LINEINFO, lineCol[0]);
      if(lineCol[1] != 0) tb.add(QueryTokens.SEP).addExt(COLINFO, lineCol[1]);
      if(file != null) tb.add(' ').addExt(FILEINFO, file);
      tb.add(": \n");
    }
    final String c = code();
    if(!c.isEmpty()) tb.add("[" + c + "] ");
    return tb.add(getLocalizedMessage()).toString();
  }
}
