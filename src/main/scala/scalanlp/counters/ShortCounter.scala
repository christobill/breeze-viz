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
 * Count objects of type T with type Short.
 * This trait is a wrapper around Scala's Map trait
 * and can work with any scala Map. 
 *
 * @author dlwh
 */
@serializable 
trait ShortCounter[T] extends Map[T,Short] {

  private var pTotal: Int = 0;

  /**
   * Return the sum of all values in the map.
   */
  def total() = pTotal;

  final protected def updateTotal(delta : Int) {
    pTotal += delta;
  }

  override def clear() {
    pTotal = 0;
    super.clear();
  }


  abstract override def update(k : T, v : Short) = {
    updateTotal(v - this(k))
    super.update(k,v);
  }

  // this isn't necessary, except that the jcl MapWrapper overrides put to call Java's put directly.
  override def put(k : T, v : Short) :Option[Short] = { val old = get(k); update(k,v); old}

  abstract override def -=(key : T) = {

    updateTotal(-this(key))

    super.-=(key);
  }

  /**
   * Increments the count by the given parameter.
   */
    def incrementCount(t : T, v : Short) = {
     update(t,(this(t) + v).asInstanceOf[Short]);
   }


  override def ++=(kv: Iterable[(T,Short)]) = kv.foreach(+=);

  /**
   * Increments the count associated with T by Short.
   * Note that this is different from the default Map behavior.
  */
  override def +=(kv: (T,Short)) = incrementCount(kv._1,kv._2);

  override def default(k : T) : Short = defaultValue;

   def defaultValue: Short = 0;

  override def apply(k : T) : Short = super.apply(k);

  // TODO: clone doesn't seem to work. I think this is a JCL bug.
  override def clone(): ShortCounter[T]  = super.clone().asInstanceOf[ShortCounter[T]]

  /**
   * Return the T with the largest count
   */
   def argmax() : T = (elements reduceLeft ((p1:(T,Short),p2:(T,Short)) => if (p1._2 > p2._2) p1 else p2))._1

  /**
   * Return the T with the smallest count
   */
   def argmin() : T = (elements reduceLeft ((p1:(T,Short),p2:(T,Short)) => if (p1._2 < p2._2) p1 else p2))._1

  /**
   * Return the largest count
   */
   def max : Short = values reduceLeft ((p1:Short,p2:Short) => if (p1 > p2) p1 else p2)
  /**
   * Return the smallest count
   */
   def min : Short = values reduceLeft ((p1:Short,p2:Short) => if (p1 < p2) p1 else p2)

  // TODO: decide is this is the interface we want?
  /**
   * compares two objects by their counts
   */ 
   def comparator(a : T, b :T) = apply(a) compare apply(b);

  /**
   * Return a new DoubleCounter[T] with each Short divided by the total;
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
   def topK(k : Int) = Counters.topK[(T,Short)](k,(x,y) => if(x._2 < y._2) -1 else if (x._2 == y._2) 0 else 1)(this);

  /**
   * Return \sum_(t) C1(t) * C2(t). 
   */
  def dot(that : ShortCounter[T]) : Double = {
    var total = 0.0
    for (val (k,v) <- that.elements) {
      total += apply(k).asInstanceOf[Double] * v
    }
    return total
  }

  def +=(that : ShortCounter[T]) {
    for(val (k,v) <- that.elements) {
      update(k,(this(k) + v).asInstanceOf[Short]);
    }
  }

  def -=(that : ShortCounter[T]) {
    for(val (k,v) <- that.elements) {
      update(k,(this(k) - v).asInstanceOf[Short]);
    }
  }

   def *=(scale : Short) {
    transform { (k,v) => (v * scale).asInstanceOf[Short]}
  }

   def /=(scale : Short) {
    transform { (k,v) => (v / scale).asInstanceOf[Short]}
  }

   def unary_-() = {
      val rv : ShortCounter[T] = ShortCounter[T]();
      rv -= this;
      rv;
  }
}


object ShortCounter {
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
  class FastMapCounter[T](override val defaultValue: Short) extends MapWrapper[T,Short] with ShortCounter[T] {
    private val under = new Object2ShortOpenHashMap[T];
    def underlying() = under.asInstanceOf[java.util.Map[T,Short]];
    override def apply(x : T) = under.getShort(x);
    override def update(x : T, v : Short) {
      val oldV = this(x);
      updateTotal(v-oldV);
      under.put(x,v);
    }
  }

  def apply[T]() = new FastMapCounter[T](0);

  def withDefaultValue[T](d: Short) = new FastMapCounter[T](d);
  
  private def runtimeClass[T](x : Any) = x.asInstanceOf[AnyRef].getClass

  private val INT = runtimeClass(3);
  private val LNG = runtimeClass(3l);
  private val FLT = runtimeClass(3.0f);
  private val SHR = runtimeClass(3.asInstanceOf[Short]);
  private val DBL = runtimeClass(3.0);

  def apply[T](implicit m : scala.reflect.Manifest[T]) : ShortCounter[T] = fromClass(m.erasure.asInstanceOf[Class[T]]);
    
  def fromClass[T](c: Class[T]) : ShortCounter[T] = c match {
    case INT => Int2ShortCounter().asInstanceOf[ShortCounter[T]];
    case DBL => Double2ShortCounter().asInstanceOf[ShortCounter[T]];
    case FLT => Float2ShortCounter().asInstanceOf[ShortCounter[T]];
    case SHR => Short2ShortCounter().asInstanceOf[ShortCounter[T]];
    case LNG => Long2ShortCounter().asInstanceOf[ShortCounter[T]];
    case _ => ShortCounter().asInstanceOf[ShortCounter[T]];
  }
}

