package gq.core


fun <T1, T2, T3> crossLinkMaps(firstMap: Map<T1, T2?>, secondMap: Map<T2, T3?>) =
        firstMap.mapValues { if (it.value != null) secondMap[it.value as T2] else null }


fun <T1, T2, T3, T4> crossLinkMaps(firstMap: Map<T1, T2?>, secondMap: Map<T2, T3?>, thirdMap: Map<T3, T4?>) =
        crossLinkMaps(crossLinkMaps(firstMap, secondMap), thirdMap)

