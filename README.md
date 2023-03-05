[![Build Status](https://travis-ci.com/mtumilowicz/kotlin-functional-programming-actors-workshop.svg?branch=master)](https://travis-ci.com/mtumilowicz/kotlin-functional-programming-actors-workshop)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

# kotlin-functional-programming-actors-workshop
* references
    * https://www.manning.com/books/the-joy-of-kotlin
    * https://github.com/pysaumont/fpinkotlin
    * [Actor Model Explained](https://www.youtube.com/watch?v=ELwEdb_pD0k)
    * [Hewitt, Meijer and Szyperski: The Actor Model (everything you wanted to know...)](https://www.youtube.com/watch?v=7erJ1DV_Tlo)
    * https://www.manning.com/books/scala-in-action
    * https://github.com/evacchi/min-java-actors/blob/main/src/main/java/io/github/evacchi/TypedActor.java
    * https://gist.github.com/viktorklang/2362563
    * [Write You An Actor System For Great Good! with JBang, JDK 19, records, pattern matching and virtual](https://www.youtube.com/watch?v=TOL3zpn1vvQ)

## preface
* goals of this workshop
    * introduction to actor model
* workshop plan
    1. implement simple actor system
    2. use it to create ping-pong game 

## introduction
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
* main use of actors isn’t for parallelization, but for the abstraction of sharing 
a mutable state 
    * without actors - need to synchronize access to resources to handle concurrency
    
## actor
* is essentially a concurrent process that doesn’t constantly occupy a thread
    * occupies only when it receives a message
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
        * supervision context
            * actor needs to supervise the actors it creates
            * decides what should happen when components fail in the system
                * can decide to restart an actor or take the actor out of service
    * send messages to other actors
    * decide what it gonna do with the next message it receives
        * example: account balance - 5$, deposit 1$, now - account balance is 6$
        * what is a difference from creating a new actor: we expect that the old actor has up-to-date balance
* each actor has an address, we can send messages to
    * many-to-many relationship
        * one address for a bunch of actors (ex. replicating behind the scenes)
        * one actor for many addresses
    * all you can do with an address is send it a message
* actors can receive messages in any order
    * analogy
        * packets in TCP can come in any order (sequence number to reconstruct in order)
        * postbox - you could get letters in any order
* there are no channels
    * message will be delivered at most once
        * it could take a long time, like message in the bottle that floats over the see
    * no intermediaries
        * but you could create an actor that acts like a channel
        * proxy/forwarding actor (forwards messages to other actors)
    * messages go directly
    * messages are sent asynchronously (no need to wait for an answer — there isn’t one)
* actor processes one message at a time
    * queueing other messages for subsequent processing
    * no concurrent access to actor's internal resources
* an actor system can be seen as a series of functional programs communicating with each other 
through effects

## actor model
* actor model allows tasks to be parallelized by using a manager actor
    * breaks the task into subtasks
    * distributes them to worker actors
    * no worker actor is ever idle until the list of subtasks is empty
        * if worker actor returns a result - it’s given a new subtask
* for some tasks, the results of the subtasks may need to be reordered
    * the manager actor will probably send the results to a specific actor responsible 
    for rearrangement
    
## actor state mutation
* actors can be stateless (immutable) or stateful
    * behaviour of each actor is allowed to change
        * is caused by a modification to the state of the actor, replacing the original behaviour 
        with a new one

## actor framework implementation
* components
    * `Behaviour<T> : (T) -> Behaviour<T>`
        * how actor behaves when it gets message `T`
        * example
            ```
            fun behaviour(msg: MsgType): Behaviour<MsgType> {
               return {
                   // do something
                   Behaviour { msg -> newBehaviour } // change behaviour
               }
            }
            ```
        * why behaviour cannot be `(T) -> Unit`?
            * if you change type to unit and remove mutation of `behaviour` in ActorSystem, engine will create
            new object for every invocation
                ```
                Behaviour { msg ->
                    Player1(self) // here we are creating new object
                        .behaviour(msg)
                }
                ```
            * so we have to keep it like this `(T) -> Behaviour<T>` and return new behaviour from behaviour methods
    * `fun interface ActorRef<T> { fun tell(msg: T) }`
        * represents actor that you sent a message
    * `ActorSystem(val executor: ExecutorService)`
        * `fun <T> spawn(initial: (ActorRef<T>) -> Behaviour<T>): ActorRef<T>`
            * creates actors and schedules them on executor
    * `ActorRef` implementation
        ```
        val mailbox = ConcurrentLinkedQueue<T>()
        var behaviour = initial(this)
        override fun tell(msg: T) {
            mailbox.offer(msg)
            process()
        }
        ```
        * `process` is CAS scheduler
            * if mailbox is not empty and we are not processing anything => schedule and set flag

