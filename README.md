# kotlin-functional-programming-actors-workshop
* https://www.manning.com/books/the-joy-of-kotlin
* https://github.com/pysaumont/fpinkotlin

# sharing mutable state
* general solution: remove state mutation
* single thread environment
    * `function(oldState) returns (newState, result)`
* multithreaded environment
    * immutable data structures don’t help
    * needs: mutable reference so that the new immutable data can replace the previous one
* example
    * in a multithreaded program, how can you increment the counter in a safe way, avoiding 
    concurrent access?
        * use locks or make operations atomic, or both
        * simple analogy
            * living on a desert island
            * if you’re the only inhabitant, there’s no need for locks on your doors
* in functional programming - sharing resources has to be done as an effect
    * every access to that resources treat as input/output (I/O)
* sharing a mutable should be abstracted
    * common technique: side effects as an implementation detail for a purely functional API
        * side effects are not observable to code
    * example: actor framework
    
# actor model
* in the actor model, a multithreaded application is divided into single-threaded components, called actors 
    * since each actor is single-threaded, it doesn’t need to share data using locks
    * actors communicate with other actors by way of effects
        * as if such communication were the I/O of messages
        * messages are sent asynchronously (no need to wait for an answer — there isn’t one)
    * actors process messages one at a time 
        * no concurrent access to their internal resources
* an actor system can be seen as a series of functional programs communicating with each other 
through effects
* actor model allows tasks to be parallelized by using a manager actor
    * breaks the task into subtasks
    * distributes them to worker actors
    * no worker actor is ever idle until the list of subtasks is empty
        * if worker actor returns a result - it’s given a new subtask
* for some tasks, the results of the subtasks may need to be reordered
    * the manager actor will probably send the results to a specific actor responsible for rearrangement
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

* The main use of actors isn’t for parallelization, but for the abstraction of sharing a
mutable state. 
    * In these examples, you used lists that were shared between tasks. 
    * Without actors, you’d have had to synchronize access to the workList and resultHeap to
    handle concurrency
    * Actors allow you to abstract synchronization and mutation in the framework.
    * actors provide a good way to make functional parts of your code work together,
      sharing mutable state in an abstracted manner
* An
  Actor is essentially a concurrent process that doesn’t constantly occupy a thread
  * it only occupies a thread when it receives a message
  * although
    multiple threads may be concurrently sending messages to an actor, the actor pro-
    cesses only one message at a time, queueing other messages for subsequent process-
    ing
* The main trickiness in an actor implementation has to do with the fact that multiple threads may be messag-
  ing the actor simultaneously. The implementation needs to ensure that messages are processed only one at a
  time, and also that all messages sent to the actor will be processed eventually rather than queued indefinitely.
    