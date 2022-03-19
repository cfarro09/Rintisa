package com.delycomps.myapplication.cache

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.delycomps.myapplication.model.Material

class BDLocal(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_TABLE_STOCK)
        db.execSQL(SQL_CREATE_TABLE_STOCK1)
        db.execSQL(SQL_CREATE_TABLE_SALES)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("Not yet implemented")
    }

    fun getMaterialStock(visitID: Int): List<Material> {
        val db = readableDatabase
        val listMaterial = ArrayList<Material>()
        val select = arrayOf(STOCK_MATERIAL, STOCK_BRAND, STOCK_QUANTITY, UUID)

        val c = db.query(TABLE_STOCK, select, "$STOCK_VISIT_ID = $visitID", null, null, null, null, null)

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

    fun addMaterialStock(material: Material, visitId: Int) {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put(STOCK_VISIT_ID, visitId)
        values.put(STOCK_MATERIAL, material.material)
        values.put(STOCK_BRAND, material.brand)
        values.put(STOCK_QUANTITY, material.quantity)
        values.put(UUID, material.uuid)

        db.insert(TABLE_STOCK, null, values)
        db.close()
    }

    fun updateMaterialsFromVisit(material: Material) {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put(STOCK_MATERIAL, material.material)
        values.put(STOCK_BRAND, material.brand)
        values.put(STOCK_QUANTITY, material.quantity)

        db.update(
            TABLE_STOCK,
            values,
            "$UUID = ?",
            arrayOf(material.uuid)
        )
        db.close()
    }

    fun deleteMaterialsFromVisit(uuid: String) {
        val db = readableDatabase
        db.delete(TABLE_STOCK, "$UUID = ?", arrayOf(uuid))
        db.close()
    }

    companion object {
        private const val DATABASE_VERSION = 2

        private const val UUID = "uuidtmp"

        private const val DATABASE_NAME = "rintisa.db"
        private const val TABLE_STOCK = "merchant_stock"
        private const val STOCK_VISIT_ID = "visitid"
        private const val STOCK_MATERIAL = "material"
        private const val STOCK_BRAND = "brand"
        private const val STOCK_QUANTITY = "quantity"

        private const val TABLE_STOCK1 = "promoter_stock"
        private const val STOCK1_PRODUCT = "product"
        private const val STOCK1_BRAND = "brand"
        private const val STOCK1_TYPE = "type"

        private const val TABLE_SALES = "promoter_sales"
        private const val SALES_BRAND = "brand"
        private const val SALES_QUANTITY = "quantity"
        private const val SALES_MEASURE_UNIT = "measure_unit"
        private const val SALES_MERCHANT = "merchant"
        private const val SALES_IMAGE_EVIDENCE = "image_evidence"

//        private const val TABLE_PRICES = "merchant_prices"

        private const val SQL_CREATE_TABLE_STOCK = ("" +
                "create table $TABLE_STOCK (" +
                "  _id integer primary key autoincrement," +
                "   $STOCK_VISIT_ID integer," +
                "   $STOCK_MATERIAL text," +
                "   $STOCK_BRAND text," +
                "   $STOCK_QUANTITY integer," +
                "   $UUID text" +
                ")")

        private const val SQL_CREATE_TABLE_STOCK1 = ("" +
                "create table $TABLE_STOCK1 (" +
                "  _id integer primary key autoincrement," +
                "   $STOCK1_PRODUCT text," +
                "   $STOCK1_BRAND text," +
                "   $STOCK1_TYPE text," +
                "   $UUID text" +
                ")")

        private const val SQL_CREATE_TABLE_SALES = ("" +
                "create table $TABLE_SALES (" +
                "  _id integer primary key autoincrement," +
                "   $SALES_BRAND text," +
                "   $SALES_QUANTITY integer," +
                "   $SALES_MEASURE_UNIT text," +
                "   $SALES_MERCHANT text," +
                "   $SALES_IMAGE_EVIDENCE text," +
                "   $UUID text" +
                ")")

    }
}