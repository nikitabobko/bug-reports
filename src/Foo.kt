import kotlin.random.Random

fun foo(x: Int, low: Int, up: Int) {
	val x = Random(System.currentTimeMillis())
	(0..10).random(x)

	run {
		val x = Random(System.currentTimeMillis())
		val intRange = 0..10
//		intRange.
		(intRange).random(x)
	}
}
