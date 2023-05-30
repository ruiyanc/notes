## Effective Java

#### 第一章 -- 引言

* 目标是更加有效地使用Java及其基本类库java.lang.java.util和java.io，以及子包java.util.concurrent和java.util.function等。
* 本书不是针对初学者，而是适用于任何具有实际Java工作经验的程序员

#### 第二章 -- 创建和销毁对象  

1.  用静态工厂方法代替构造器  

   * ```java
     public static Boolean valueOf(boolean b){
         return b ? Boolean.TRUE : Boolean.FALSE;
     }
     ```

   * 优点
     
     1. 它们有名称
     
     2. 不必在每次调用它们的时候都创建一个新对象
     
     3. 它们可以返回原返回类型的任何子类型的对象
     
     4. 所返回的对象的类可以随着每次调用而发生变化，这取决于静态工厂方法的参数值
     
     5. 方法返回的对象所属的类，在编写包含该静态工厂方法的类时可以不存在

   *  缺点

     1. 类如果不含公有的或者受保护的构造器，就不能被子类化
     
     2. 程序员很难发现它们

2. 遇到多个构造器参数时要考虑使用构建器

   1. 重叠构造器模式

      * 第一个构造器只有必要的参数，第二个有一个可选参数，第三个有两个，以此类推，最后一个构造器包含所有可选参数

      * ```java
        public class NutritionFacts {
           private final int servingSize; //(mL)         required
           private final int servings; //(per container) required
           private final int calories; //(per serving)   optional
           private final int fat; //(g/serving)          optional
           private final int sodium; //(mg/serving)      optional
           public NutritionFacts(int servingSize, int servings) {
               this(servingSize, servings, 0);
           }
           public NutritionFacts(int servingSize, int servings, int calories) {
               this(servingSize, servings, calories, 0);
           }
           public NutritionFacts(int servingSize, int servings, int calories, int fat) {
               this(servingSize, servings, calories, fat, 0);
           }
           public NutritionFacts(int servingSize, int servings, int calories, int fat, int sodium) {
               this.servingSize = servingSize;
               this.servings = servings;
               this.calories = calories;
               this.fat = fat;
               this.sodium = sodium;
           }
        }
        ```

      * 创建实例的时候利用参数列表最短的构造器，但列表中包含了要设置的所有参数
        
        * `NutritionFacts cocaCola = new NutritionFacts(240,8,100,0,35);`

      * 重叠构造器模式可行，但当有许多参数的时候，客户端代码会很难编写，并且仍然较难以阅读

   2. JavaBeans模式

      * 先调用一个无参构造器来创建对象，然后再调用setter方法来设置每个必要的参数，以及可选参数

      * ```java
        public class NutritionFacts {
           private int servingSize = -1;   //required;no default value
           private int servings    = -1;   //required;no default value
           private int calories    = 0;
           private int fat         = 0;
           private int sodium      = 0;
           public NutritionFacts() {}
           public void setServingSize(int val) { this.servingSize = val;}
           public void setServings(int val)    { this.servings = val;}
           public void setCalories(int val)    { this.calories = val;}
           public void setFat(int val)         { this.fat = val;}
           public void setSodium(int val)      { this.sodium = val;}
        ```

      * 创建实例很容易，产生的代码阅读起来轻松

        * `NutritionFacts cocaCola = new NutritionFacts();`
        * `cocaCola.setServingSize(240);`
        * `cocaCola.setServings(8);`...省略后续set方法

      * 在构造过程中JavaBeans可能处于不一致的状态

      * JavaBeans模式使得把类做成不可变的可能性不复存在

   3. 建造者模式

      * **既能保证像重叠构造器那样安全性，也能保证像JavaBeans模式那么好的可读性。**

      * 不直接生成想要的对象，而是让客户端利用所有必要的参数调用构造器，得到一个builder对象，然后客户端在builder对象上调用类似setter方法，来设置每个相关的可选参数。最后，客户端调用无参的build方法来生成通常是不可变的对象

      * ```java
        public class NutritionFacts {
           private final int servingSize; 
           private final int servings; 
           private final int calories; 
           private final int fat;
           private final int sodium; 
           public static class Builder { 
             private final int servingSize;
             private final int servings;
             private int calories    = 0;
             private int fat         = 0;
             private int sodium      = 0; 
        	 public Builder(int servingSize, int servings){
                 this.servingSize = servingSize;
                 this.servings    = servings;
             }
             public Buidler calories(int val)
             {    calories = val; return this;}
             public Buidler fat(int val)
             {     fat = val;     return this;}
             public Buidler sodium(int val)
             {     sodium = val;  return this;}
             public NutritionFacts build(){
                 return new NutritionFacts(this);
             }
           }
           private NutritionFacts(Builder builder){
               servingSize = builder.servingSize;
               servings    = builer.servings;
               calories    = builder.calories;
               fat         = builer.fat;
               sodium      = builer.sodium;
           }
        }
        ```

      * NutritionFacts是不可变的，所有默认参数值都单独放在一个地方。builder的设值方法返回builder本身，以便把调用链接起来，得到一个流式的API。Builder模式模拟了具名的可选参数。

        * `NutritionFacts cocaCola = new NutritionFacts.Builder(240,8).calories(100).sodium(35).build();`

      * Builder模式也适用于类层次结构

3. 用私有构造器或枚举类型强化Singleton类型

   1. Singleton是指仅仅被实例化一次的类。它通常用来代表一个无状态的对象，或者那些本质上唯一的系统组件。

   2. 实现Singleton的两种常见方法。这两方法都要保持构造器为私有的，并导出公有的静态成员，以便允许客户端能够访问该类的唯一实例。

      1. 公有静态成员是个final域

         * ```java
           public class Elvis {
               public static final Elvis INSTANCE = new Elvis();
               private Elvis(){...}
               public void leaveTheBuilding(){...}
           }
           ```

         * 私有构造器仅被调用一次，用来实例化公有的静态final域Elvis.INSTANCE。由于缺少公有的或者受保护的构造器，所以保证了Elvis的全局唯一性：一旦Elvis类被实例化，将只会存在一个Elvis实例。

      2. 公有成员是个静态工厂方法

         * ```java
           public class Elvis {
               private static final Elvis INSTANCE = new Elvis();
               private Elvis(){...}
               public static Elvis getInstance(){ return INSTANCE;}
               public void leaveTheBuilding(){...}
           }
           ```

         * 对于静态方法Elvis.getInstance的所有调用，都会返回同一个对象引用，所以永远不会创建其它的Elvis实例

      3. 声明一个包含单个元素的枚举类型**(最佳)**

         * ```java
           public enum Elvis {
               INSTANCE;
               public void leaveTheBuilding(){...}
           }
           ```

         * 功能上与公有域方法相似，但更简洁，无偿地提供了序列化机制，绝对防止多次实例化，即使是在面对复杂的序列化或反射攻击的时候。如果Singleton必须扩展一个超类，而不是扩展Enum的时候，则不宜使用这个方法。

4. 通过私有构造器强化不可实例化的能力

   1. 企图通过将类做成抽象类来强制该类不可被实例化是行不通的。

   2. 由于只有当类不包含显式的构造器时，编译器才会生成缺省的构造器，因此只要让这个类包含一个私有构造器，它就不能被实例化。

   3. ```java
      public class UtilityClass {
          //Suppress default constructor for noninstantiability
          private UtilityClass(){
              throw new AssertionError();
          }
      }
      ```

   4. 它使得一个类不能被子类化。所有的构造器都必须显式或隐式地调用超类构造器，在这种情形下，子类就没有可访问的超类构造器可调用了。

5. 优化考虑依赖注入来引用资源

   1. 静态工具类和Singleton类不适用于需要引用底层资源的类

   2. 当创建一个新的实例时，就将该资源传到构造器中。也就是依赖注入(@Autowired)

   3. ```java
      public class SpellChecker {
          private final Levicon dictionary;
          public SpellChecker(Lexicon dictionary){
              this.dictionary = Objects.requireNonNull(dictionary);
          }
      }
      ```

   4. 不要用Singleton和静态工具类来实现依赖一个或多个底层资源的类，且该资源的行为会影响到该类的行为；也不要直接用这个类来创建这些资源。而应该将资源或工厂传给构造器来创建类。这种被称作依赖注入，它极大地提升了类的灵活性、可重用性和可测试性。

6. 避免创建不必要的对象

   1. 一般来说最好能重用单个对象，而不是在每次需要的时候就创建一个相同功能的新对象。重用方式既快速又流行。

      * `String s = new String("bikini");` //Don't do this ! 

   2. 有些对象创建的成本比其他对象要高得多，建议缓存下来重用

      * ```java
        //确定一个字符串是否为一个有效的罗马数字
        static boolean isRomanNumereal(String s){
            return s.matches("^(?=.)M*(C[MD]|D?C{0,3})" + "(X[CL]|L?X{0,3})(I[XV]|V?I{0,3})&");
        }
        ```

      * 依赖于String.matches()方法。**虽然String.matches方法易于查看一个字符串是否与正则表达式相匹配，但并不适合在注重性能的情形中重复使用。**问题在于在内部为正则表达式创建了一个Pattern实例，却只用一次就进行垃圾回收了，创建Pattern实例成本很高，因为需要将正则表达式编译成一个有限状态机。

      * ```java
        //优化后的性能提升
        public class RomanNumerals{
            private static final Pattern ROMAN = Pattern.compile(
            	"^(?=.)M*(C[MD]|D?C{0,3})" + "(X[CL]|L?X{0,3})(I[XV]|V?I{0,3})&");
            
            static boolean isRomanNumeral(String s){
                return ROMAN.matcher(s).matches();
            }
        }
        ```

   3. 创建多余对象的方法，称作自动装箱，它允许程序员将基本类型和包装类型混用，按需自动装箱和拆箱。**自动装箱使得基本类型和包装类型之间的差别变得模糊起来，但并没有完全消除。**他们在语义上还有着微妙的差别，在性能上也有着比较明显的差别。

      * ```java
        //由于Long自动装箱和拆箱，执行效率出奇地慢。把Long->long运行时间8136ms->737ms
        private static long sum(){
            Long sum = 0L;
            for(long i = 0; i < Integer.MAX_VALUE; i++){
                sum += i;
            }
            return sum
        }
        ```

      * **要优先使用基本类型而不是包装类型，要当心无意识地自动装箱**

7. 清除过期的对象引用

   1. 内存泄漏 --> **对象过期引用**

      * ```java
        public class Stack{
            private Object[] elements;
            private int size = 0;
            private static final int DEFAULT_INITIAL_CAPACITY = 16;
            public Stack(){
                elements = new Object[DEFAULT_INITIAL_CAPACITY];
            }
            public void push(Object e){
                ensureCapacity();
                elements[size++] = e;
            }
            public Object pop(){
                if (size == 0)
                    throw new EmptyStackException();
                return elements[--size];
            }
            //确保至少有一个以上元素的空间，每次数组需要增长时，容量大约增加一倍
            private void ensureCapacity(){
                if(elements.length == size)
                    elements = Arrays.copyOf(elements, 2 * size + 1);
            }
        }
        ```

      * 一个栈先增长然后再收缩，那么从栈中弹出来的对象将不会被当作垃圾回收，即使使用栈的程序不再引用这些对象，它们也不会被回收。因为栈内部维护着对这些对象的过期引用。过期引用是指永远也不会再被解除的引用。本例中凡是在elements数组的“活动部分(elements中下标小于size的那些元素)”之外的任何引用都是过期的。

      * ```java
        //一旦对象引用已经过期，只需清空这些引用即可
        public Object pop(){
            if(size == 0)
                throw new EmptyStackException();
            Object result = elements[--size];
            elements[size] = null;//清除过期引用
            return result;
        }
        ```

      * 清空对象引用应该是一种例外，而不是一中规范行为。消除过期引用最好的方法是让包含该引用的变量结束其生命周期。

      * 一般来说，只要类是自己管理内存，程序员就应该警惕内存泄漏问题。

   2. 内存泄漏 --> **缓存**

      * 一旦把对象引用放到缓存中，很容易被遗忘掉，从而使得它不再有用之后很长一段时间内仍然留在缓存中。只要在缓存之外存在对某个项的键的引用，该项就有意义，那么就用WeakHashMap代表缓存；当缓存中的项过期之后，就会自动被删除。
      * 缓存项的生命周期是否有意义不确定，随着时间推移，其中的项会变得越来越没有价值。在这种情况下，缓存应该时不时地清除掉没用的项，可以由一个后台线程（ScheduledThreadPoolExecutor）来完成，或者在给缓存添加新条目的时候顺便进行清理（利用LinkedHashMap的removeEldestEntry方法）。对于更复杂的缓存，必须直接使用java.lang.ref

   3. 内存泄漏 --> **监听器和其他回调**

      * 如果实现一个API，客户端在API中注册回调，却没有显式地取消注册，会不断堆积起来。确保回调立即被当作垃圾回收的最佳方法是只保存它们的弱引用。例如保存成WeakHashMap中的键

   4. 借助于Heap培训工具（Heap Profiler）来发现内存泄漏问题

8. 避免使用终结方法和清除方法

   1. 终结方法和清除方法的弊端

      1. 终结方法（finalizer）通常是不可预测的，也是很危险的，一般情况下是不必要的。清除方法（cleaner）没有终结方法那么危险，但仍然不可预测、运行缓慢，也不必要。
      2. 终结方法和清除方法的缺点在于不能保证会被及时执行。注重时间（time-critical）的任务不应该由它们来完成。例如用它们来关闭已经打开的文件，就是严重的错误，因为打开文件的描述符是一种很有限的资源。如果系统无法及时运行终结方法或清除方法就会导致大量的文件仍然保留在打开状态。
         * 如果类对象中封装的资源确实需要终止，**让类实现AutoCloseable**，并要求在每个实例不再需要的时候调用close方法，利用try-with-resource确保终止。
      3. 及时地执行终结方法和清除方法正是垃圾回收算法的主要功能。**永远不应该依赖终结方法或清除方法来更新重要的持久状态。**例如，依赖它们来释放共享资源（比如数据库）上的永久锁，这很容易让整个分布式系统垮掉。System.gc和System.runFinalization确实增加了它们被执行的机会，但并不保证一定会被执行。
      4. 使用终结方法和清除方法有一个非常严重的性能损失。
      5. 终结方法有一个严重的安全问题，为终结方法攻击打开了类的大门。

   2. 终结方法和清除方法的合理应用

      1. 当资源的所有者忘记调用它的close方法时，它们可充当“安全网”，迟点释放总比不释放要好。

      2. 与本地对等体（native peer）有关。本地对等体是一个本地对象（native object），普通对象通过本地方法（native method）委托给一个本地对象。因为本地对等体不是普通对象，垃圾回收器不会回收它，如果本地对等体没有关键资源，并且性能也可以接受的话，那么用清楚方法或终结方法很合适。

      3. ```java
         //事例：房间在收回之前必须进行清除
         // 使用清除方法作为安全网的可自动关闭的类
         public class Room implements AutoCloseable {
             private static final Cleaner cleaner = Cleaner.create();
             // 需要清理的资源。 Must not refer to Room!
             private static class State implements Runnable {
                 int numJunkPiles; // 这个房间里有多少堆垃圾
                 State(int numJunkPiles) {
                     this.numJunkPiles = numJunkPiles;
                 }
                 // 由close方法或清理器调用
                 @Override public void run() {
                     System.out.println("Cleaning room");
                     numJunkPiles = 0;
                 }
             }
             // 房间的状态
             private final State state;
             // 我们的清洗。在符合gc条件时打扫房间
             private final Cleaner.Cleanable cleanable;
             public Room(int numJunkPiles) {
                 state = new State(numJunkPiles);
                 cleanable = cleaner.register(this, state);
             }
             @Override public void close() {
                 cleanable.clean();
             }
         }
         ```

      4. 除非作为安全网，或者为了终止非关键的本地资源，否则不要使用清除方法。

9. try-with-resources优先于try-finally

   1. Java 库包含许多必须通过调用 close 方法手动关闭的资源。包括 InputStream，OutputStream 和 java.sql.Connection。关闭资源经常被忽视，会带来可预见的可怕性能后果。

   2. 在jdk7之前try-finally 语句是保证资源正确关闭的最佳方式，但添加多个资源时，会导致第二个异常完全覆盖掉第一个异常，从而无法捕获到第一个异常记录。

   3. ```java
      // 当与多个资源一起使用时，try-finally很难看
      static void copy(String src, String dst) throws IOException {
          InputStream in = new FileInputStream(src);
          try {
              OutputStream out = new FileOutputStream(dst);
              try {
                  byte[] buf = new byte[BUFFER_SIZE];
                  int n;
                  while ((n = in.read(buf)) >= 0)
                  out.write(buf, 0, n);
              } finally {
                  out.close();
              }
          } finally {
              in.close();
          }
      }
      ```

   4. 当 Java 7 引入了 try-with-resources 语句时，所有这些问题都得到了一并解决。要使用此构造，资源必须实现 AutoCloseable 接口，该接口由单个返回类型为 void 的 close 方法组成。Java 库和第三方库中的许多类和接口现在实现或继承 AutoCloseable。

   5. ```java
      // 在多种资源上尝试使用try-with-resources
      static void copy(String src, String dst) throws IOException {
          try (InputStream in = new FileInputStream(src);
              OutputStream out = new FileOutputStream(dst)) {
              byte[] buf = new byte[BUFFER_SIZE];
              int n;
              while ((n = in.read(buf)) >= 0)
              out.write(buf, 0, n);
          }
      }
      ```

   6. **在处理必须关闭的资源时，始终要优先考虑用try-with-resources，而不是用try-finally。代码更简洁、清晰，产生的异常也更有价值。**

#### 第三章 -- 对于所有对象都通用的方法

10. 覆盖equals时请遵守通用约定
    1. 类的每个实例本质上都是唯一的
    1. 类没有必要提供“逻辑相等”的测试功能
    1. 超类已经覆盖了equals，超类的行为对于这个类也是合适的
    1. 类是私有的，或者是包级私有的，可以确定它的equals方法永远不会被调用