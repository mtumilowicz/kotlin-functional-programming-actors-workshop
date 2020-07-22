# kotlin-functional-programming-actors-workshop
* https://www.manning.com/books/the-joy-of-kotlin
* https://github.com/pysaumont/fpinkotlin
* [Actor Model Explained](https://www.youtube.com/watch?v=ELwEdb_pD0k)
* [Hewitt, Meijer and Szyperski: The Actor Model (everything you wanted to know...)](https://www.youtube.com/watch?v=7erJ1DV_Tlo)

# preface
* goals of this workshop
    * introduction to actor model
* workshop are in `workshop` package, answers: `answers`

# introduction
* sharing mutable state
    * general solution: remove state mutation
    * single thread environment
        * `function(oldState) returns (newState, result)`
    * multithreaded environment
        * use locks or make operations atomic, or both
            * analogy vs single thread: living on a desert island
                * if you’re the only inhabitant, there’s no need for locks on your doors
        * immutable data structures don’t help
        * needs: mutable reference so that the new immutable data can replace the previous one
    * should be abstracted
        * in functional programming - sharing resources has to be done as an effect
            * every access to that resources treat as input/output (I/O)
        * common technique: side effects as an implementation detail for a purely functional API
            * side effects are not observable to code
        * example: actor framework

# actor
* actor - fundamental unit of computation
* actor has to embody 3 essentials elements of computations
    * processing - get something done
    * storage - remember things
    * communication
* one ant is no ant - one actor is no actor
* actors come in systems
* actor can address himself - way of implement recursion
    * example: factorial
* fundamental properties
    * everything is an actor
    * misconception: every actor has a mailbox, and mailbox is an actor - mailbox needs a mailbox?
        * resolve it with axioms
* when an actor receives a message, all he can do is:
    * create more actors
    * send messages to other actors
    * decide what he gonna do with the next message it receives
        * example: account balance - 5$, deposit 1$, now - account balance is 6$
        * what is a difference from creating a new actor: we expect that the old actor has up-to-date balance
* each actor has an address, we can send messages to
    * many-to-many
    * one address for a bunch of actors (ex. replicating behind the scenes)
    * one actor for many addresses
    * proxy/forwarding actor (forwards messages to other actors)
    * all you can do with an address is send it a message
* actors can receive messages in any order
    * packets in TCP can come in any order (sequence number to reconstruct in order)
    * post in any country - you could get letters in any order
* there are no channels
    * message will be delivered at most once
        * it could take a long time, like message in the bottle that floats over the see
    * no intermediaries
    * messages go directly
    * you could create an actor that acts like a channel
* processes one message at a time
    * no concurrent access to their internal resources
* messages are sent asynchronously (no need to wait for an answer — there isn’t one)
* an actor system can be seen as a series of functional programs communicating with each other 
through effects

# actor model
* actor model allows tasks to be parallelized by using a manager actor
    * breaks the task into subtasks
    * distributes them to worker actors
    * no worker actor is ever idle until the list of subtasks is empty
        * if worker actor returns a result - it’s given a new subtask
* for some tasks, the results of the subtasks may need to be reordered
    * the manager actor will probably send the results to a specific actor responsible 
    for rearrangement
* Handling actor state mutation
    * Actors can be stateless (immutable) or stateful
        * their state can change according to the messages they receive
        * example
            * synchronizer actor reorders results of computations
    * for example, a list of data that must go through heavy computation in order 
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
* behaviour of each actor is allowed to change
    * is caused by a modification to the state of the actor, replacing the original behaviour with a new one

# actor framework implementation
* four components:
    * `Actor`
        * determines the behaviour of an actor
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
            * an ideal way to build scalable applications
* main use of actors isn’t for parallelization, but for the abstraction of sharing a mutable state 
    * without actors, you’d have had to synchronize access to resources to handle concurrency
* Actor is essentially a concurrent process that doesn’t constantly occupy a thread
    * it only occupies a thread when it receives a message
    * although multiple threads may be concurrently sending messages to an actor, the actor pro-
    cesses only one message at a time, queueing other messages for subsequent processing
* main trickiness in an actor implementation has to do with the fact that multiple threads may be 
messaging the actor simultaneously
    * implementation needs to ensure that messages are processed only one at a time
        * all messages sent to the actor must be processed rather than queued indefinitely