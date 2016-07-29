package gq.core.data

import org.junit.Test

import org.junit.Assert.*

class GQGseInfoTest {

    @Test
    fun testReadFromFileOk() {
        val info = GQGseInfoCollection {
            readGseInfoFromFile(Thread.currentThread().contextClassLoader.getResource("collection/titles.txt").path)
        }
        assertEquals(info["GSE11111"]?.title, "asdf a")
        assertEquals(info["GSE11111"]?.id, 11111)
        assertEquals(info["GSE11111"], info[11111])
        assertEquals(info.size(), 14)
    }

    @Test(expected = RuntimeException::class)
    fun testReadFromFileFail() {
        GQGseInfoCollection {
            readGseInfoFromFile(Thread.currentThread().contextClassLoader.getResource("collection/titles_fail.txt").path)
        }
    }
}