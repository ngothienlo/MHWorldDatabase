package com.gatheringhallstudios.mhworlddatabase.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.gatheringhallstudios.mhworlddatabase.data.MHWDatabase
import com.gatheringhallstudios.mhworlddatabase.data.dao.ItemDao
import com.gatheringhallstudios.mhworlddatabase.getResult
import com.gatheringhallstudios.mhworlddatabase.initMHWDatabase
import org.junit.Assert
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ItemDaoTest {
    companion object {
        private lateinit var db: MHWDatabase
        private lateinit var dao: ItemDao

        @BeforeClass @JvmStatic
        fun initDatabase() {
            // this is read only, so its ok to use the actual database
            db = initMHWDatabase()
            dao = db.itemDao()
        }

        @AfterClass
        @JvmStatic
        fun closeDatabase() {
            db.close()
        }
    }

    @Test
    fun Can_Query_Item_List() {
        val items = dao.loadItems("en").getResult()
        Assert.assertFalse("expected results", items.isEmpty())
    }

    @Test
    fun Can_Query_Item_Locations() {
        val items = dao.loadItems("en").getResult()
        val testItem = items.find { it.name == "Herb" }
        Assert.assertNotNull("Expecting non-null test object", testItem)

        val itemId = testItem!!.id
        val itemLocations = dao.loadItemLocations("en", itemId).getResult()
        Assert.assertTrue("expecting location results", itemLocations.isNotEmpty())

    }
}