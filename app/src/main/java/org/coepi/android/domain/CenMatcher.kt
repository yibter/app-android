package org.coepi.android.domain

import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.coepi.android.cen.Cen
import org.coepi.android.cen.CenKey
import org.coepi.android.domain.CenLogicImpl.Companion.cenLifetimeInSeconds
import org.coepi.android.extensions.toHex

interface CenMatcher {
    fun match(cens: List<Cen>, keys: List<CenKey>, maxDate: UnixTime): List<CenKey>
}

class CenMatcherImpl(
    private val cenLogic: CenLogic
) : CenMatcher {

    override fun match(cens: List<Cen>, keys: List<CenKey>, maxDate: UnixTime): List<CenKey> =
        if (cens.isEmpty()) {
            emptyList()
        } else {
            runBlocking {
                matchSuspended(cens, keys, maxDate)
            }
        }

    private suspend fun matchSuspended(cens: List<Cen>, keys: List<CenKey>,
                                       maxDate: UnixTime): List<CenKey> =
        coroutineScope {
            val censSet: Set<String> = cens.map { it.toHex() }.toHashSet()
            keys.distinct().map { key ->
                async(Default) {
                    if (match(censSet, key, maxDate)) {
                        key
                    } else {
                        null
                    }
                }
            }.awaitAll().filterNotNull()
        }

    private fun match(censSet: Set<String>, key: CenKey, maxDate: UnixTime): Boolean {
        val secondsInAWeek: Long = 604800
        val possibleCensCount = (secondsInAWeek / cenLifetimeInSeconds).toInt()
        for (i in 0..possibleCensCount) {
            val ts = maxDate.value - cenLifetimeInSeconds * i
            val cen = cenLogic.generateCen(key, ts)
            if (censSet.contains(cen.bytes.toHex())) {
                return true
            }
        }
        return false
    }
}
