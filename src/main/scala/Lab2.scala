object Lab2 extends jsy.util.JsyApplication {
  import jsy.lab2.Parser
  import jsy.lab2.ast._
  
  /*
   * CSCI 3155: Lab 2
   */

  /*
   * Replace the 'throw new UnsupportedOperationException' expression with
   * your code in each function.
   * 
   * Do not make other modifications to this template, such as
   * - adding "extends App" or "extends Application" to your Lab object,
   * - adding a "main" method, and
   * - leaving any failing asserts.
   * 
   * Your lab will not be graded if it does not compile.
   * 
   * This template compiles without error. Before you submit comment out any
   * code that does not compile or causes a failing assert.  Simply put in a
   * 'throws new UnsupportedOperationException' as needed to get something
   * that compiles without error.
   */
  
  /* We represent a variable environment is as a map from a string of the
   * variable name to the value to which it is bound.
   * 
   * You may use the following provided helper functions to manipulate
   * environments, which are just thin wrappers around the Map type
   * in the Scala standard library.  You can use the Scala standard
   * library directly, but these are the only interfaces that you
   * need.
   */
  
  type Env = Map[String, Expr]
  val emp: Env = Map()
  def get(env: Env, x: String): Expr = env(x)
  def extend(env: Env, x: String, v: Expr): Env = {
    require(isValue(v))
    env + (x -> v)
  }
  
  /* Some useful Scala methods for working with Scala values include:
   * - Double.NaN
   * - s.toDouble (for s: String)
   * - n.isNaN (for n: Double)
   * - n.isWhole (for n: Double)
   * - s (for n: Double)
   * - s format n (for s: String [a format string like for printf], n: Double)
   */

  def toNumber(v: Expr): Double = {
    require(isValue(v))
    (v: @unchecked) match {
      case N(n) => n
      case B(b) => if (b) 1 else 0
      case S(s) => try { s.toDouble } catch { case e: NumberFormatException => Double.NaN }
      case Var(x) => x.toDouble
      case _ => Double.NaN
    }
  }
  
  def toBoolean(v: Expr): Boolean = {
    require(isValue(v))
    (v: @unchecked) match {
      case B(b) => b
      case N(n) => if (n == 0 ||  n.isNaN()) false else true
      case S(s) => if (s == null) false else true
      case Undefined => false
      case _ => throw new UnsupportedOperationException
    }
  }
  
  def toStr(v: Expr): String = {
    println(v)
    require(isValue(v))
    (v: @unchecked) match {
      case S(s) => s
      case N(n) => if (n / n.toInt == 1) n.toInt.toString() else n.toString
      case B(b) => b.toString()
      case Undefined => "undefined"
      case _ => throw new UnsupportedOperationException
    }
  }
  
  def eval(env: Env, e: Expr): Expr = {
    /* Some helper functions for convenience. */
    def eToVal(e: Expr): Expr = eval(env, e)

    e match {
      /* Base Cases */
      case N(_) | B(_) | S(_) | Undefined => e
      /* Inductive Cases */
      case Var(x) => eToVal(get(env, x))
      case ConstDecl(x, e1, e2) => val e1Val = eToVal(e1); eval(extend(env, x, e1Val), e2)
      case If(e1, e2, e3) => if (toBoolean(e1)) eToVal(e2) else eToVal(e3)
      case Print(e1) => println(pretty(eToVal(e1))); Undefined
      case Unary(uop, e1) => uop match {
        case Neg => N(-toNumber(eToVal(e1)))
        case Not => B(!toBoolean(eToVal(e1)))
        case _ => throw new UnsupportedOperationException
      }
      case Binary(bop, e1, e2) => bop match {
        case Plus =>
          if (e1.isInstanceOf[S] || e2.isInstanceOf[S])
            S(toStr(eToVal(e1)) + toStr(eToVal(e2)))
          else
            N(toNumber(eToVal(e1)) + toNumber(eToVal(e2)))
        case Minus => N(toNumber(eToVal(e1)) - toNumber(eToVal(e2)))
        case Times => N(toNumber(eToVal(e1)) * toNumber(eToVal(e2)))
        case Div => N(toNumber(eToVal(e1)) / toNumber(eToVal(e2)))
        case Eq => B(eToVal(e1) == eToVal(e2))
        case Ne => B(!(eToVal(e1) == eToVal(e2)))
        case Lt =>
          if (e1.isInstanceOf[S] && e2.isInstanceOf[S])
            B(toStr(eToVal(e1)).compareTo(toStr(eToVal(e2))) < 0)
          else
            B(toNumber(eToVal(e1)) < toNumber(eToVal(e2)))
        case Le => B(toNumber(eToVal(e1)) <= toNumber(eToVal(e2)))
        case Gt => B(toNumber(eToVal(e1)) > toNumber(eToVal(e2)))
        case Ge => B(toNumber(eToVal(e1)) >= toNumber(eToVal(e2)))
        case And =>
          val e1Val = eToVal(e1);
          val e2Val = eToVal(e2);
          if (!toBoolean(e1Val)) e1Val
          else e2Val
        case Or =>
          val e1Val = eToVal(e1);
          val e2Val = eToVal(e2);
          if (toBoolean(e1Val)) e1Val
          else e2Val
        case Seq => eToVal(e1); eToVal(e2)
        case _ => throw new UnsupportedOperationException
      }
      case _ => throw new UnsupportedOperationException
    }
  }
    
  // Interface to run your interpreter starting from an empty environment.
  def eval(e: Expr): Expr = eval(emp, e)

  // Interface to run your interpreter from a string.  This is convenient
  // for unit testing.
  def eval(s: String): Expr = eval(Parser.parse(s))

 /* Interface to run your interpreter from the command-line.  You can ignore what's below. */ 
 def processFile(file: java.io.File) {
    if (debug) { println("Parsing ...") }
    
    val expr = Parser.parseFile(file)
    
    if (debug) {
      println("\nExpression AST:\n  " + expr)
      println("------------------------------------------------------------")
    }
    
    if (debug) { println("Evaluating ...") }
    
    val v = eval(expr)
    
    println(pretty(v))
  }

}