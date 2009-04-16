// THIS IS AN AUTO-GENERATED FILE. DO NOT MODIFY.    
// generated by GenCounter on Wed Apr 15 21:58:32 PDT 2009
package scalanlp.counters;
/*
 Copyright 2009 David Hall, Daniel Ramage
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at 
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License. 
*/
import scala.collection.mutable.Map;
import scala.collection.mutable.HashMap;

/**
 * Count objects of type T with type Long.
 * This trait is a wrapper around Scala's Map trait
 * and can work with any scala Map. 
 *
 * @author dlwh
 */
@serializable 
trait LongCounter[T] extends Map[T,Long] {

  private var pTotal: Long = 0;

  /**
   * Return the sum of all values in the map.
   */
  def total() = pTotal;

  final protected def updateTotal(delta : Long) {
    pTotal += delta;
  }

  override def clear() {
    pTotal = 0;
    super.clear();
  }


  abstract override def update(k : T, v : Long) = {
    updateTotal(v - this(k))
    super.update(k,v);
  }

  // this isn't necessary, except that the jcl MapWrapper overrides put to call Java's put directly.
  override def put(k : T, v : Long) :Option[Long] = { val old = get(k); update(k,v); old}

  abstract override def -=(key : T) = {

    updateTotal(-this(key))

    super.-=(key);
  }

  /**
   * Increments the count by the given parameter.
   */
    def incrementCount(t : T, v : Long) = {
     update(t,(this(t) + v).asInstanceOf[Long]);
   }


  override def ++=(kv: Iterable[(T,Long)]) = kv.foreach(+=);

  /**
   * Increments the count associated with T by Long.
   * Note that this is different from the default Map behavior.
  */
  override def +=(kv: (T,Long)) = incrementCount(kv._1,kv._2);

  override def default(k : T) : Long = defaultValue;

   def defaultValue: Long = 0;

  override def apply(k : T) : Long = super.apply(k);

  // TODO: clone doesn't seem to work. I think this is a JCL bug.
  override def clone(): LongCounter[T]  = super.clone().asInstanceOf[LongCounter[T]]

  /**
   * Return the T with the largest count
   */
   def argmax() : T = (elements reduceLeft ((p1:(T,Long),p2:(T,Long)) => if (p1._2 > p2._2) p1 else p2))._1

  /**
   * Return the T with the smallest count
   */
   def argmin() : T = (elements reduceLeft ((p1:(T,Long),p2:(T,Long)) => if (p1._2 < p2._2) p1 else p2))._1

  /**
   * Return the largest count
   */
   def max : Long = values reduceLeft ((p1:Long,p2:Long) => if (p1 > p2) p1 else p2)
  /**
   * Return the smallest count
   */
   def min : Long = values reduceLeft ((p1:Long,p2:Long) => if (p1 < p2) p1 else p2)

  // TODO: decide is this is the interface we want?
  /**
   * compares two objects by their counts
   */ 
   def comparator(a : T, b :T) = apply(a) compare apply(b);

  /**
   * Return a new DoubleCounter[T] with each Long divided by the total;
   */
   def normalized() : DoubleCounter[T] = {
    val normalized = DoubleCounter[T]();
    val total : Double = this.total
    if(total != 0.0)
      for (pair <- elements) {
        normalized(pair._1) = pair._2 / total;
      }
    normalized
  }

  /**
   * Return the sum of the squares of the values
   */
   def l2norm() : Double = {
    var norm = 0.0
    for (val v <- values) {
      norm += (v * v)
    }
    return Math.sqrt(norm)
  }

  /**
   * Return a List the top k elements, along with their counts
   */
   def topK(k : Int) = Counters.topK[(T,Long)](k,(x,y) => if(x._2 < y._2) -1 else if (x._2 == y._2) 0 else 1)(this);

  /**
   * Return \sum_(t) C1(t) * C2(t). 
   */
  def dot(that : LongCounter[T]) : Double = {
    var total = 0.0
    for (val (k,v) <- that.elements) {
      total += apply(k).asInstanceOf[Double] * v
    }
    return total
  }

  def +=(that : LongCounter[T]) {
    for(val (k,v) <- that.elements) {
      update(k,(this(k) + v).asInstanceOf[Long]);
    }
  }

  def -=(that : LongCounter[T]) {
    for(val (k,v) <- that.elements) {
      update(k,(this(k) - v).asInstanceOf[Long]);
    }
  }

   def *=(scale : Long) {
    transform { (k,v) => (v * scale).asInstanceOf[Long]}
  }

   def /=(scale : Long) {
    transform { (k,v) => (v / scale).asInstanceOf[Long]}
  }

   def unary_-() = {
      val rv : LongCounter[T] = LongCounter[T]();
      rv -= this;
      rv;
  }
}


object LongCounter {
  import it.unimi.dsi.fastutil.objects._
  import it.unimi.dsi.fastutil.ints._
  import it.unimi.dsi.fastutil.shorts._
  import it.unimi.dsi.fastutil.longs._
  import it.unimi.dsi.fastutil.floats._
  import it.unimi.dsi.fastutil.doubles._

  import scalanlp.counters.ints._
  import scalanlp.counters.shorts._
  import scalanlp.counters.longs._
  import scalanlp.counters.floats._
  import scalanlp.counters.doubles._


  import scala.collection.jcl.MapWrapper;
  @serializable
  @SerialVersionUID(2L)
  class FastMapCounter[T](override val defaultValue: Long) extends MapWrapper[T,Long] with LongCounter[T] {
    private val under = new Object2LongOpenHashMap[T];
    def underlying() = under.asInstanceOf[java.util.Map[T,Long]];
    override def apply(x : T) = under.getLong(x);
    override def update(x : T, v : Long) {
      val oldV = this(x);
      updateTotal(v-oldV);
      under.put(x,v);
    }
  }

  def apply[T]() = new FastMapCounter[T](0);

  def withDefaultValue[T](d: Long) = new FastMapCounter[T](d);
  
  private def runtimeClass[T](x : Any) = x.asInstanceOf[AnyRef].getClass

  private val INT = runtimeClass(3);
  private val LNG = runtimeClass(3l);
  private val FLT = runtimeClass(3.0f);
  private val SHR = runtimeClass(3.asInstanceOf[Short]);
  private val DBL = runtimeClass(3.0);

  def apply[T](implicit m : scala.reflect.Manifest[T]) : LongCounter[T] = fromClass(m.erasure.asInstanceOf[Class[T]]);
    
  def fromClass[T](c: Class[T]) : LongCounter[T] = c match {
    case INT => Int2LongCounter().asInstanceOf[LongCounter[T]];
    case DBL => Double2LongCounter().asInstanceOf[LongCounter[T]];
    case FLT => Float2LongCounter().asInstanceOf[LongCounter[T]];
    case SHR => Short2LongCounter().asInstanceOf[LongCounter[T]];
    case LNG => Long2LongCounter().asInstanceOf[LongCounter[T]];
    case _ => LongCounter().asInstanceOf[LongCounter[T]];
  }
}

