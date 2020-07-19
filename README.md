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
    * Actors can be stateless (immutable) or stateful
        * their state can change according to the messages they receive
        * example
            * synchronizer actor reorders results of computations
    * Imagine, for example, that you have a list of data that must go through heavy computation in order 
    to provide a list of results
        * mapping phase: It could be parallelized by breaking the list into several sublists and giving these 
        sublists to worker actors for processing
            * no guarantee that the worker actors will finish their jobs in the same order that those 
            jobs were given to them
        * solution - number the tasks 
            * when a worker sends back the result, it adds the corresponding task number
            * maybe priority queue
                * automatic sorting
                * makes possible to process the results as an asynchronous stream
                    * receiver receives a result, it compares the task number to the expected number
                        * if match - passes the result to the client and then looks into the priority queue to
                        see if the first available result corresponds to the new expected task number
                            * if there’s a match, the dequeuing process continues until there’s no longer 
                            a match
                        * if the received result doesn’t match the expected result number, it’s added to 
                        the priority queue

# actor framework implementation
* four components:
    * `Actor`
        * determines the behavior of an actor
    * `AbstractActor`
         * contains all the stuff that’s common to all actors 
        * will be extended by business actors
    * `ActorContext`
        * acts as a way to access actors
        * in our case - will be minimalist and used primarily to access the actor state
        * allows for searching for available actors
    * `MessageProcessor`
        * implement for any component that handles a received message
* our implementation
    * each actor is mapped to a single thread 
        * in a real actor system, actors are mapped to pools of threads
            * millions of actors run on a few dozen threads
    * no support for remote actors 
        * in a real actor system you can use actors that are running on different machines without 
        having to care about communication
            * messages aren’t sent to actors but to actor references, proxies, or some other substitute
            * an ideal way to build scalable applications
            
* behavior of an actor looks like an effect
    * arguments: message to process and the sender
* behavior of each actor is allowed to change
    * is caused by a modification to the state of the actor, replacing the original behavior with a new one
    
# AbstractActor implementation
* All the message management operations are common and
  are provided by the actor framework, so that you’ll only have to implement the business part
* actors are useful when multiple threads are supposed to share some mutable state
    * when a thread produces the result of a computation, and this result must be passed to 
    another thread for further processing
    
# Running a computation in parallel
* application is composed of three kinds of actors
    * Manager - creates a given number of worker actors and distributing the tasks to them
    * several instances of workers
    * client
* main use of actors isn’t for parallelization, but for the abstraction of sharing a mutable state 
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
    