# kotlin-functional-programming-actors-workshop
* joy of kotlin
* https://bezkoder.com/kotlin-priority-queue/
* https://github.com/pysaumont/fpinkotlin

# Sharing mutable states with actors
* Passing the state as part of a function parameter and returning a new (immutable) state
  as part of the result (in the form of a pair containing both the result and the new state)
  is perfectly fine when dealing with a single thread
    * But as long as you need to share
      state mutation between threads, which is pretty much always the case in modern appli-
      cations, immutable data structures don’t help
    * to share this kind of data, one needs a
      mutable reference to it so that the new immutable data can replace the previous one
* This is the same as living on a desert island. If you’re the only inhabitant,
  there’s no need for locks on your doors. 
  * But in a multithreaded program, how can you
  increment the counter in a safe way, avoiding concurrent access?
  * The answer is gener-
    ally to use locks or to make operations atomic, or both
* In functional programming, sharing resources has to be done as an effect, which
  means, more or less, that each time you access a shared resource, you have to leave func-
  tional safety and treat this access as you did for input/output (I/O)
  * Sharing a mutable state can be abstracted in such a way that
    you can use it without bothering about the details. 
    * One way to achieve this is to use an
    actor framework.
# actor model
* In the actor model, a multithreaded application is divided into single-threaded com-
  ponents, called actors. 
  * If each actor is single-threaded, it doesn’t need to share data
  using locks or synchronization.
  * Actors communicate with other actors by way of effects, as if such communication
    were the I/O of messages
  * actors rely on a mechanism for serializing
    the messages they receive
    * serialization means handling one message after the
      other
  * Due to this mechanism, actors
    can process messages one at a time without having to bother about concurrent access
    to their internal resources
* an actor system can be seen as a series of func-
  tional programs communicating with each other through effects
  * Each actor can be
    single-threaded, so there’s no concurrent access to resources inside. 
    * Concurrency is abstracted inside the framework.
* Understanding asynchronous messaging
    * As part of the message processing, actors can send messages to other actors
    * Messages
      are sent
      asynchronously, which means an actor doesn’t need to wait for an answer—there
      isn’t one
    * As soon as a message is sent, the sender can continue its job, which mostly
      consists of processing one at a time a queue of messages it receives
* Handling parallelization
    * actor model allows tasks to be parallelized by using a manager actor that’s respon-
      sible for breaking the task into subtasks and distributing them to a number of worker
      actors
    * Each time a worker actor returns a result to the manager, it’s given a new sub-
      task
    * This model offers an advantage over other parallelization models in that no worker actor 
    is ever idle until the list of subtasks is empty
    * For some tasks, the results of the subtasks may need to be reordered when they’re received. 
        * In such a case, the manager actor will probably send the results to a specific actor 
        responsible for this job
* Handling actor state mutation
    * Actors
      can be stateless (immutable) or stateful, meaning they’re supposed to change
      their state according to the messages they receive
      * For example, a synchronizer actor
        can receive the results of computations that have to be reordered before being used
    * Imagine, for example, that you have a list of data that must go through heavy compu-
      tation in order to provide a list of results
      * In short, this is a mapping
      * It could be paral-
        lelized by breaking the list into several sublists and giving these sublists to worker actors
        for processing
      * But there’s no guarantee that the worker actors will finish their jobs in
        the same order that those jobs were given to them
    * One solution for resynchronizing the results is to number the tasks. 
        * When a worker sends back the result, it adds the corresponding task number so that 
        the receiver can put the results in a priority queue
        * Not only does this allow automatic sorting, but it
        also makes it possible to process the results as an asynchronous stream.
    * Each time the
      receiver receives a result, it compares the task number to the expected number. If
      there’s a match, it passes the result to the client and then looks into the priority queue to
      see if the first available result corresponds to the new expected task number. If there’s a
      match, the dequeuing process continues until there’s no longer a match. If the received
      result doesn’t match the expected result number, it’s added to the priority queue.
# actor framework implementation
* four components:
  * The Actor interface determines the behavior of an actor.
  * The AbstractActor class contains all the stuff that’s common to all actors. 
    * This class will be extended by business actors.
  * The ActorContext acts as a way to access actors. 
    * In your implementation, this component will be minimalist and will be used primarily to access 
    the actor state. 
    * This component isn’t necessary in such a small implementation, but most serious implementations 
    use such a component. 
    * This context allows, for example, searching for available actors.
  * The MessageProcessor interface will be the interface you’ll implement for any component that has 
  to handle a received message.
* Understanding the limitations
    * One other simplification is that each actor is mapped to a single thread. 
        * In a real actor system, actors are mapped to pools of threads, allowing thousands or even 
        millions of actors to run on a few dozen threads.
    * Another limitation of your implementation will be regarding remote actors. 
        * Most actor frameworks allow remote actors to be handled in a transparent way, meaning that
        you can use actors that are running on different machines without having to care about
        communication
        * This makes actor frameworks an ideal way to build scalable applications.
* Designing the actor framework interfaces
    * fun tell(message: T, sender: Result<Actor<T>>)
        * is used to send a message to this actor (meaning the actor holding the function)
        * This means that to send a message to an actor, you must have a reference to
          it. 
          * (This is different from real actor frameworks in which messages aren’t sent to actors
          but to actor references, proxies, or some other substitute. Without this enhancement,
          it wouldn’t be possible to send messages to remote actors.)
    ```
    interface ActorContext<T> {
        fun behavior(): MessageProcessor<T> // Allows access to the actor’s behavior
        fun become(behavior: MessageProcessor<T>) // Allows an actor to change its behavior by registering a new MessageProcessor
    }
  
    interface MessageProcessor<T> {
        fun process(message: T, sender: Result<Actor<T>>)
    }
    ```
    * become function allows an actor to change its behavior, meaning the way it processes messages
    * the behavior of an actor looks like an effect, taking as its argument a pair com-
      posed of the message to process and the sender
    * During the life of the application, the behavior of each actor is allowed to change.
      * Generally this change of behavior is caused by a modification to the state of the actor,
      replacing the original behavior with a new one
# AbstractActor implementation
* All the message management operations are common and
  are provided by the actor framework, so that you’ll only have to implement the busi-
  ness part
* abstract fun onReceive(message: T, sender: Result<Actor<T>>)
    * Holds the business processing, implemented by the user of the API
* The tell function is how an actor receives a message
    * It’s synchronized to ensure that messages are processed one at a time.
    * When a message is received, it’s
      processed by the current behavior
      returned by the actor context.
* Actors are useful when multiple threads are supposed to share some
  mutable state, as when a thread produces the result of a computation, and this result
  must be passed to another thread for further processing
# Running a computation in parallel
* To simulate a long-running computation, you’ll choose a
  list of random numbers between 0 and 30, and compute the corresponding Fibonacci
  value using a slow algorithm.
* The application is composed of three kinds of actors: a Manager , in charge of cre-
  ating a given number of worker actors and distributing the tasks to them; several
  instances of workers; and a client, which is implemented in the main program class
  as an anonymous actor
* As you can see, this actor is stateless. 
    * It computes the result and sends it back to the sender to which it has received a reference.
    * This might be a different actor than the caller
* 