package gq.core.data

import java.io.File

fun populateModulesFromGmt(path: String, species: Species, dest: MutableList<GQModule>) {
    File(path).forEachLine {
        if (it.isNotEmpty()) {
            try {
                val (fullName, commaSepEntrezIds) = it.split("\t")
                dest.add(GQModule.buildByFullName(
                        fullName,
                        species,
                        commaSepEntrezIds.split(',').map { it.toLong() }.toLongArray()))
            } catch(e: Exception) {
                throw RuntimeException("Fail to parse GMT line: $it", e)
            }
        }
    }
}

fun readModulesFromFiles(speciesToPath: Iterable<Pair<Species, String>>): List<GQModule> {
    val modules = mutableListOf<GQModule>()
    speciesToPath.forEach { populateModulesFromGmt(it.second, it.first, modules) }
    return modules
}

fun readModulesFromFiles(vararg speciesToPath: Pair<Species, String>) = readModulesFromFiles(speciesToPath.asList())

class GQModuleCollection(modules: Iterable<GQModule>) {

    constructor(modulesInit: () -> Iterable<GQModule>) : this(modulesInit())

    val fullNameToGQModule = modules.associateBy { it.fullName() }

    val speciesToModules = fullNameToGQModule.values
            .groupBy { it.species }

    val seriesToModules = fullNameToGQModule.values
            .groupBy { it.seriesName() }
            .mapValues { it -> it.value
                    .sortedBy { it.clusterId }
                    .toTypedArray() }

    val speciesToGseGpl = seriesToModules.keys.groupBy { seriesToModules[it]!!.first().species }

    override fun toString(): String =
            "GQCollection(${fullNameToGQModule.size} modules,${speciesToModules.size} species,${seriesToModules.size} series)"
}