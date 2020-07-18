package common

import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService


sealed class ListK<out A> {

    abstract fun forEach(ef: (A) -> Unit)

    abstract fun isEmpty(): Boolean

    abstract fun init(): ListK<A>

    abstract val length: Int

    abstract fun headSafe(): Result<A>

    abstract fun tailSafe(): Result<ListK<A>>

    abstract fun <B> foldLeft(identity: B, zero: B,
                              f: (B) -> (A) -> B): Pair<B, ListK<A>>

    abstract fun <B> foldLeft(identity: B,
                              p: (B) -> Boolean,
                              f: (B) -> (A) -> B): B

    operator fun plus(a: @UnsafeVariance A): ListK<A> = cons(a)

    fun toArrayList(): java.util.ArrayList<@UnsafeVariance A> =
            foldLeft(ArrayList()) { list ->
                { a ->
                    list.add(a)
                    list
                }
            }

    fun zipWithPosition(): ListK<Pair<A, Int>> =
        zipWith(this, range(0, this.length)) { a -> { i: Int -> Pair(a, i) } }

    fun <B> parMap(es: ExecutorService, g: (A) -> B): Result<ListK<B>> =
            try {
                val result = this.map { x ->
                    es.submit<B> { g(x) }
                }.map<B> { fb ->
                            try {
                                fb.get()
                            } catch (e: InterruptedException) {
                                throw RuntimeException(e)
                            } catch (e: ExecutionException) {
                                throw RuntimeException(e)
                            }
                        }
                Result(result)
            } catch (e: Exception) {
                Result.failure(e)
            }

    fun <B> parFoldLeft(es: ExecutorService,
                        identity: B,
                        f: (B) -> (A) -> B,
                        m: (B) -> (B) -> B): Result<B> =
            try {
                val result: ListK<B> = divide(1024).map { list: ListK<A> ->
                    es.submit<B> { list.foldLeft(identity, f) }
                }.map<B> { fb ->
                            try {
                                fb.get()
                            } catch (e: InterruptedException) {
                                throw RuntimeException(e)
                            } catch (e: ExecutionException) {
                                throw RuntimeException(e)
                            }
                        }
                Result(result.foldLeft(identity, m))
            } catch (e: Exception) {
                Result.failure(e)
            }

    fun splitListAt(index: Int): ListK<ListK<A>> {
        tailrec fun splitListAt(acc: ListK<A>,
                                list: ListK<A>, i: Int): ListK<ListK<A>> =
                when (list) {
                    Nil -> ListK(list.reverse(), acc)
                    is Cons ->
                        if (i == 0)
                            ListK(list.reverse(), acc)
                        else
                            splitListAt(acc.cons(list.head), list.tail, i - 1)
                }
        return when {
            index < 0        -> splitListAt(0)
            index > length() -> splitListAt(length())
            else             -> splitListAt(Nil, this.reverse(), this.length() - index)
        }
    }

    fun divide(depth: Int): ListK<ListK<A>> {
        tailrec
        fun divide(list: ListK<ListK<A>>, depth: Int): ListK<ListK<A>> =
                when (list) {
                    Nil -> list // dead code
                    is Cons ->
                        if (list.head.length() < 2 || depth < 1)
                            list
                        else
                            divide(list.flatMap { x -> x.splitListAt(x.length() / 2) }, depth - 1)
                }
        return if (this.isEmpty())
            ListK(this)
        else
            divide(ListK(this), depth)
    }

    fun exists(p: (A) -> Boolean): Boolean =
            foldLeft(false, true) { x -> { y: A -> x || p(y) } }.first

    fun forAll(p: (A) -> Boolean): Boolean = !exists { !p(it) }

    fun <B> groupBy(f: (A) -> B): Map<B, ListK<A>> =
        reverse().foldLeft(mapOf()) { mt: Map<B, ListK<A>> ->
            { t ->
                f(t).let { mt + (it to (mt.getOrDefault(it, Nil)).cons(t)) }
            }
        }

    fun <B> groupByViaFoldRight(f: (A) -> B): Map<B, ListK<A>> =
        foldRight(mapOf()) { t ->
            {  mt: Map<B, ListK<A>> ->
                f(t).let { mt + (it to (mt.getOrDefault(it, Nil)).cons(t)) }
            }
        }

    fun splitAt(index: Int): Pair<ListK<A>, ListK<A>> {
        tailrec fun splitAt(acc: ListK<A>,
                            list: ListK<A>, i: Int): Pair<ListK<A>, ListK<A>> =
                when (list) {
                    Nil -> Pair(list.reverse(), acc)
                    is Cons ->
                        if (i == 0)
                            Pair(list.reverse(), acc)
                        else
                            splitAt(acc.cons(list.head), list.tail, i - 1)
                }
        return when {
            index < 0        -> splitAt(0)
            index > length() -> splitAt(length())
            else             -> splitAt(Nil, this.reverse(), this.length() - index)
        }
    }

//    data class Tuple<out A>(val first: Result<A>, val second: Int) {
//        override fun equals(other: Any?): Boolean {
//            return when (other) {
//                is Tuple<*> -> other.second == second
//                else -> false
//            }
//        }
//
//        override fun hashCode(): Int {
//            return first.hashCode() + second.hashCode()
//        }
//    }

    fun getAt(index: Int): Result<A> {
        val p: (Pair<Result<A>, Int>) -> Boolean = { it.second < 0 }
        return Pair<Result<A>, Int>(Result.failure("Index out of bound"), index).let { identity ->
            if (index < 0 || index >= length())
                identity
            else
                foldLeft(identity, p) { ta: Pair<Result<A>, Int> ->
                    { a: A ->
                        if (p(ta))
                            ta
                        else
                            Pair(Result(a), ta.second - 1)
                    }
                }

        }.first
    }


    fun <A1, A2> unzip(f: (A) -> Pair<A1, A2>): Pair<ListK<A1>, ListK<A2>> =
            this.coFoldRight(Pair(Nil, Nil)) { a ->
                { listPair: Pair<ListK<A1>, ListK<A2>> ->
                    f(a).let {
                        Pair(listPair.first.cons(it.first), listPair.second.cons(it.second))
                    }
                }
            }

    fun lastSafe(): Result<A> =
            foldLeft(Result()) {
                { y: A ->
                    Result(y)
                }
            }

    fun startsWith(sub: ListK<@UnsafeVariance A>): Boolean {
        tailrec fun startsWith(list: ListK<A>, sub: ListK<A>): Boolean =
                when (sub) {
                    Nil  -> true
                    is Cons -> when (list) {
                        Nil  -> false
                        is Cons ->
                            if (list.head == sub.head)
                                startsWith(list.tail, sub.tail)
                            else
                                false
                    }
                }
        return startsWith(this, sub)
    }

    fun hasSubList(sub: ListK<@UnsafeVariance A>): Boolean {
        tailrec
        fun <A> hasSubList(list: ListK<A>, sub: ListK<A>): Boolean =
                when (list) {
                    Nil -> sub.isEmpty()
                    is Cons ->
                        if (list.startsWith(sub))
                            true
                        else
                            hasSubList(list.tail, sub)
                }
        return hasSubList(this, sub)
    }

    fun setHead(a: @UnsafeVariance A): ListK<A> = when (this) {
        Nil -> throw IllegalStateException("setHead called on an empty list")
        is Cons -> Cons(a, this.tail)
    }

    fun cons(a: @UnsafeVariance A): ListK<A> = Cons(a, this)

    fun concat(list: ListK<@UnsafeVariance A>): ListK<A> = concat(this, list)

    fun concatViaFoldRight(list: ListK<@UnsafeVariance A>): ListK<A> = ListK.concatViaFoldRight(this, list)

    fun drop(n: Int): ListK<A> = drop(this, n)

    fun dropWhile(p: (A) -> Boolean): ListK<A> = dropWhile(this, p)

    fun reverse(): ListK<A> = foldLeft(Nil as ListK<A>) { acc -> { acc.cons(it) } }

    fun <B> foldRight(identity: B, f: (A) -> (B) -> B): B = foldRight(this, identity, f)

    fun <B> foldLeft(identity: B, f: (B) -> (A) -> B): B = foldLeft(identity, this, f)

    fun length(): Int = length

    fun <B> foldRightViaFoldLeft(identity: B, f: (A) -> (B) -> B): B =
            this.reverse().foldLeft(identity) { x -> { y -> f(y)(x) } }

    fun <B> coFoldRight(identity: B, f: (A) -> (B) -> B): B = coFoldRight(identity, this.reverse(), identity, f)

    fun <B> map(f: (A) -> B): ListK<B> = foldLeft(Nil) { acc: ListK<B> -> { h: A -> Cons(f(h), acc) } }.reverse()

    fun <B> flatMap(f: (A) -> ListK<B>): ListK<B> = flatten(map(f))

    fun filter(p: (A) -> Boolean): ListK<A> = flatMap { a -> if (p(a)) ListK(a) else Nil }

    internal object Nil: ListK<Nothing>() {

        override fun <B> foldLeft(identity: B, p: (B) -> Boolean, f: (B) -> (Nothing) -> B): B = identity

        override fun tailSafe(): Result<ListK<Nothing>> = Result.Empty

        override fun forEach(ef: (Nothing) -> Unit) {}

        override fun <B> foldLeft(identity: B, zero: B, f: (B) -> (Nothing) -> B):
                Pair<B, ListK<Nothing>> = Pair(identity, Nil)

        override fun headSafe(): Result<Nothing> = Result()

        override val length = 0

        override fun init(): ListK<Nothing> = throw IllegalStateException("init called on an empty list")

        override fun isEmpty() = true

        override fun toString(): String = "[NIL]"
    }

    internal class Cons<out A>(internal val head: A,
                               internal val tail: ListK<A>): ListK<A>() {

        override fun tailSafe(): Result<ListK<A>> = Result(tail)

        override fun forEach(ef: (A) -> Unit) {
            tailrec fun forEach(list: ListK<A>) {
                when (list) {
                    Nil -> {}
                    is Cons -> {
                        ef(list.head)
                        forEach(list.tail)
                    }
                }
            }
            forEach(this)
        }

        override fun <B> foldLeft(identity: B, p: (B) -> Boolean, f: (B) -> (A) -> B): B {
            fun foldLeft(acc: B, list: ListK<A>): B = when (list) {
                Nil -> acc
                is Cons ->
                    if (p(acc))
                        acc
                    else
                        foldLeft(f(acc)(list.head), list.tail)
            }
            return foldLeft(identity, this)
        }

        override fun <B> foldLeft(identity: B, zero: B, f: (B) -> (A) -> B): Pair<B, ListK<A>> {
            tailrec fun <B> foldLeft(acc: B, zero: B, list: ListK<A>, f: (B) -> (A) -> B): Pair<B, ListK<A>> = when (list) {
                Nil -> Pair(acc, list)
                is Cons ->
                    if (acc == zero)
                        Pair(acc, list)
                    else
                        foldLeft(f(acc)(list.head), zero, list.tail, f)
            }
            return foldLeft(identity, zero, this, f)
        }

        override fun headSafe(): Result<A> = Result(head)

        override val length = tail.length + 1

        override fun init(): ListK<A> = reverse().drop(1).reverse()

        override fun isEmpty() = false

        override fun toString(): String = "[${toString("", this)}NIL]"

        private tailrec fun toString(acc: String, list: ListK<A>): String = when (list) {
            Nil  -> acc
            is Cons -> toString("$acc${list.head}, ", list.tail)
        }
    }

    companion object {

        fun <A> cons(a: A, list: ListK<A>): ListK<A> = Cons(a, list)

        tailrec fun <A> drop(list: ListK<A>, n: Int): ListK<A> = when (list) {
            Nil -> list
            is Cons -> if (n <= 0) list else drop(list.tail, n - 1)
        }

        tailrec fun <A> dropWhile(list: ListK<A>, p: (A) -> Boolean): ListK<A> = when (list) {
            Nil -> list
            is Cons -> if (p(list.head)) dropWhile(list.tail, p) else list
        }

        fun <A> concat(list1: ListK<A>, list2: ListK<A>): ListK<A> = list1.reverse().foldLeft(list2) { x -> x::cons }

        fun <A> concatViaFoldRight(list1: ListK<A>, list2: ListK<A>): ListK<A> = foldRight(list1, list2) { x -> { y -> Cons(x, y) } }

        fun <A, B> foldRight(list: ListK<A>, identity: B, f: (A) -> (B) -> B): B =
                when (list) {
                    Nil -> identity
                    is Cons -> f(list.head)(foldRight(list.tail, identity, f))
                }

        tailrec fun <A, B> foldLeft(acc: B, list: ListK<A>, f: (B) -> (A) -> B): B =
                when (list) {
                    Nil -> acc
                    is Cons -> foldLeft(f(acc)(list.head), list.tail, f)
                }

        tailrec fun <A, B> coFoldRight(acc: B, list: ListK<A>, identity: B, f: (A) -> (B) -> B): B =
                when (list) {
                    Nil -> acc
                    is Cons -> coFoldRight(f(list.head)(acc), list.tail, identity, f)
                }


        operator fun <A> invoke(vararg az: A): ListK<A> =
                az.foldRight(Nil) { a: A, list: ListK<A> -> Cons(a, list) }

        fun fromSeparated(string: String, separator: String): ListK<String> = ListK(*string.split(separator).toTypedArray())
    }
}

fun <A> flatten(list: ListK<ListK<A>>): ListK<A> = list.coFoldRight(ListK.Nil) { x -> x::concat }

fun ListK<Int>.sum(): Int = foldRight(0) { x -> { y -> x + y } }

fun ListK<Double>.sum(): Double = foldRight(1.0) { x -> { y -> x + y } }

fun ListK<Int>.product(): Int = foldRight(1) { x -> { y -> x * y } }

fun ListK<Double>.product(): Double = foldRight(1.0) { x -> { y -> x * y } }

fun triple(list: ListK<Int>): ListK<Int> =
        ListK.foldRight(list, ListK()) { h -> { t: ListK<Int> -> t.cons(h * 3) } }

fun doubleToString(list: ListK<Double>): ListK<String> =
        ListK.foldRight(list, ListK())  { h -> { t: ListK<String> -> t.cons(h.toString()) } }

tailrec fun <A> lastSafe(list: ListK<A>): Result<A> = when (list) {
    ListK.Nil  -> Result()
    is ListK.Cons<A> -> when (list.tail) {
        ListK.Nil  -> Result(list.head)
        is ListK.Cons -> lastSafe(list.tail)
    }
}

fun <A> flattenResult(list: ListK<Result<A>>): ListK<A> =
        list.flatMap { ra -> ra.map { ListK(it) }.getOrElse(ListK()) }

fun <A> sequenceLeft(list: ListK<Result<A>>): Result<ListK<A>> =
        list.foldLeft(Result(
                ListK())) { x: Result<ListK<A>> ->
            { y -> map2(y, x) { a -> { b: ListK<A> -> b.cons(a) } } }
        }.map { it.reverse() }

fun <A> sequence2(list: ListK<Result<A>>): Result<ListK<A>> =
        list.filter{ !it.isEmpty() }.foldRight(Result(ListK())) { x ->
            { y: Result<ListK<A>> ->
                map2(x, y) { a -> { b: ListK<A> -> b.cons(a) } }
            }
        }

fun <A, B> traverse(list: ListK<A>, f: (A) -> Result<B>): Result<ListK<B>> =
        list.foldRight(Result(ListK())) { x ->
            { y: Result<ListK<B>> ->
                map2(f(x), y) { a -> { b: ListK<B> -> b.cons(a) } }
            }
        }

fun <A> sequence(list: ListK<Result<A>>): Result<ListK<A>> =
        traverse(list) { x: Result<A> -> x }

fun <A, B, C> zipWith(list1: ListK<A>,
                      list2: ListK<B>,
                      f: (A) -> (B) -> C): ListK<C> {
    tailrec
    fun zipWith(acc: ListK<C>,
                list1: ListK<A>,
                list2: ListK<B>): ListK<C> = when (list1) {
        ListK.Nil -> acc
        is ListK.Cons -> when (list2) {
            ListK.Nil -> acc
            is ListK.Cons ->
                zipWith(acc.cons(f(list1.head)(list2.head)),
                        list1.tail, list2.tail)
        }
    }
    return zipWith(ListK(), list1, list2).reverse()
}

fun <A, B, C> product(list1: ListK<A>,
                      list2: ListK<B>,
                      f: (A) -> (B) -> C): ListK<C> =
        list1.flatMap { a -> list2.map { b -> f(a)(b) } }

fun <A, B> unzip(list: ListK<Pair<A, B>>): Pair<ListK<A>, ListK<B>> = list.unzip { it }

fun <A, S> unfoldResult(z: S, getNext: (S) -> Result<Pair<A, S>>): Result<ListK<A>> {
    tailrec fun unfold(acc: ListK<A>, z: S): Result<ListK<A>> {
        val next = getNext(z)
        return when (next) {
            Result.Empty -> Result(acc)
            is Result.Failure -> Result.failure(next.exception)
            is Result.Success ->
                unfold(acc.cons(next.value.first), next.value.second)
        }
    }
    return unfold(ListK.Nil, z).map(ListK<A>::reverse)
}

fun <A, S> unfold(z: S, getNext: (S) -> Option<Pair<A, S>>): ListK<A> {
    tailrec fun unfold(acc: ListK<A>, z: S): ListK<A> {
        val next = getNext(z)
        return when (next) {
            Option.None -> acc
            is Option.Some ->
                unfold(acc.cons(next.value.first), next.value.second)
        }
    }
    return unfold(ListK.Nil, z).reverse()
}

fun range(start: Int, end: Int): ListK<Int> =
        unfold(start) { i ->
            if (i < end)
                Option(Pair(i, i + 1))
            else
                Option()
        }

