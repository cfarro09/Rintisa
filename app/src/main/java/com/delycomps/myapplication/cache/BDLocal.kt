package com.delycomps.myapplication.cache

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.delycomps.myapplication.model.Material

class BDLocal(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_TABLE_STOCK)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("Not yet implemented")
    }

    fun getMaterialStock(visitid: Int): List<Material> {
        val db = readableDatabase
        val listMaterial = ArrayList<Material>()
        val select = arrayOf(STOCK_MATERIAL, STOCK_BRAND, STOCK_QUANTITY, STOCK_UUID)

        val c = db.query(TABLE_STOCK, select, "$STOCK_VISIT_ID = $visitid", null, null, null, null, null)

        if (c != null && c.count > 0) {
            c.moveToFirst()
            do {
                listMaterial.add(Material(c.getString(0), c.getString(1), c.getInt(2), c.getString(3)))
            } while (c.moveToNext())
        }
        db?.close()
        c?.close()

        return listMaterial
    }

    fun addMaterialStock(material: Material, visitid: Int) {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put(STOCK_VISIT_ID, visitid)
        values.put(STOCK_MATERIAL, material.material)
        values.put(STOCK_BRAND, material.brand)
        values.put(STOCK_QUANTITY, material.quantity)
        values.put(STOCK_UUID, material.uuid)

        db.insert(TABLE_STOCK, null, values)
        db.close()
    }

    fun updateMaterialsFromVisit(material: Material) {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put(STOCK_MATERIAL, material.material)
        values.put(STOCK_BRAND, material.brand)
        values.put(STOCK_QUANTITY, material.quantity)

        val a  = db.update(
            TABLE_STOCK,
            values,
            "$STOCK_UUID = ?",
            arrayOf(material.uuid)
        )
        db.close()
    }

    fun deleteMaterialsFromVisit(visitId: Int, uuid: String) {
        val db = readableDatabase
        db.delete(TABLE_STOCK, "$STOCK_UUID = ?", arrayOf(uuid))
        db.close()
    }

    companion object {

        private const val DATABASE_VERSION = 2
        private const val DATABASE_NAME = "rintisa.db"
        private const val TABLE_STOCK = "merchant_stock"
        private const val STOCK_VISIT_ID = "visitid"
        private const val STOCK_UUID = "uuidtmp"
        private const val STOCK_MATERIAL = "material"
        private const val STOCK_BRAND = "brand"
        private const val STOCK_QUANTITY = "quantity"
        private const val TABLE_PRICES = "merchant_prices"

        private const val SQL_CREATE_TABLE_STOCK = ("" +
                "create table $TABLE_STOCK (" +
                "  _id integer primary key autoincrement," +
                "   $STOCK_VISIT_ID integer," +
                "   $STOCK_MATERIAL text," +
                "   $STOCK_BRAND text," +
                "   $STOCK_QUANTITY integer," +
                "   $STOCK_UUID text" +
                ")")
    }
}