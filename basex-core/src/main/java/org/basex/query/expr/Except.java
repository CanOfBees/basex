package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Except expression.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class Except extends Set {
  /**
   * Constructor.
   * @param info input info
   * @param exprs expressions
   */
  public Except(final InputInfo info, final Expr[] exprs) {
    super(info, exprs);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    super.optimize(cc);

    final ExprList list = new ExprList(exprs.length);
    for(final Expr expr : exprs) {
      if(expr == Empty.SEQ) {
        // remove empty operands (return empty sequence if first value is empty)
        if(list.isEmpty()) return cc.emptySeq(this);
        cc.info(OPTREMOVE_X_X, expr, description());
      } else {
        list.add(expr);
      }
    }
    // ensure that results are always sorted
    if(list.size() == 1 && iterable) return list.get(0);
    // replace expressions with optimized list
    exprs = list.finish();
    return this;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final Except ex = new Except(info, copyAll(cc, vm, exprs));
    ex.iterable = iterable;
    return copyType(ex);
  }

  @Override
  protected ANodeBuilder eval(final Iter[] iters, final QueryContext qc) throws QueryException {
    final ANodeBuilder list = new ANodeBuilder();
    for(Item item; (item = qc.next(iters[0])) != null;) list.add(toNode(item));
    list.check();

    final int el = exprs.length;
    for(int e = 1; e < el && !list.isEmpty(); e++) {
      final Iter iter = iters[e];
      for(Item item; (item = qc.next(iter)) != null;) list.delete(toNode(item));
    }
    return list;
  }

  @Override
  protected NodeIter iter(final Iter[] iters, final QueryContext qc) {
    return new SetIter(qc, iters) {
      @Override
      public ANode next() throws QueryException {
        if(nodes == null) {
          final int il = iter.length;
          nodes = new ANode[il];
          for(int i = 0; i < il; i++) next(i);
        }

        final int il = nodes.length;
        for(int i = 1; i < il; i++) {
          if(nodes[0] == null) return null;
          if(nodes[i] == null) continue;
          final int d = nodes[0].diff(nodes[i]);

          if(d < 0 && i + 1 == il) break;
          if(d == 0) {
            next(0);
            i = 0;
          }
          if(d > 0) next(i--);
        }
        final ANode temp = nodes[0];
        next(0);
        return temp;
      }
    };
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Except && super.equals(obj);
  }
}
