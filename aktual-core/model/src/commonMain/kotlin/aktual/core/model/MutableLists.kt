package aktual.core.model

// Lets list builders add items as `+item`. Don't use with numbers, their own unaryPlus wins
context(list: MutableList<T>)
operator fun <T> T.unaryPlus(): Boolean = list.add(this)
