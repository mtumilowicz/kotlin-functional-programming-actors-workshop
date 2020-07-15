# kotlin-functional-programming-actors-workshop
* joy of kotlin

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
