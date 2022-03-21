package com.delycomps.myapplication.cache

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.delycomps.myapplication.model.Material
import com.delycomps.myapplication.model.Stock
import com.delycomps.myapplication.model.SurveyProduct

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

        val c = db.query(TABLE_STOCK, select, "$VISIT_ID = $visitID", null, null, null, null, null)

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

        values.put(VISIT_ID, visitId)
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
    //#SALE PROMOTER
    fun getSalePromoter(visitID: Int): List<SurveyProduct> {
        val db = readableDatabase
        val listStock = ArrayList<SurveyProduct>()

        val select = arrayOf(SALES_BRAND, SALES_QUANTITY, SALES_MEASURE_UNIT, SALES_MERCHANT, SALES_IMAGE_EVIDENCE, UUID)

        val c = db.query(TABLE_SALES, select, "$VISIT_ID = $visitID", null, null, null, null, null)

        if (c != null && c.count > 0) {
            c.moveToFirst()
            do {
                listStock.add(SurveyProduct(0, c.getString(0), c.getString(0), 0.00, c.getString(2), c.getInt(1), c.getString(3), c.getString(4), c.getString(5)))
            } while (c.moveToNext())
        }
        db?.close()
        c?.close()

        return listStock
    }
    fun addSalePromoter(product: SurveyProduct, visitID: Int) {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put(SALES_BRAND, product.brand)
        values.put(SALES_QUANTITY, product.quantity)
        values.put(SALES_MEASURE_UNIT, product.measureUnit)
        values.put(SALES_MERCHANT, product.merchant)
        values.put(SALES_IMAGE_EVIDENCE, product.imageEvidence)
        values.put(VISIT_ID, visitID)
        values.put(UUID, product.uuid)

        db.insert(TABLE_SALES, null, values)
        db.close()
    }
    fun deleteSalePromoter(uuid: String) {
        val db = readableDatabase
        db.delete(TABLE_SALES, "$UUID = ?", arrayOf(uuid))
        db.close()
    }
    fun updateSalePromoter(product: SurveyProduct) {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put(SALES_BRAND, product.brand)
        values.put(SALES_QUANTITY, product.quantity)
        values.put(SALES_MEASURE_UNIT, product.measureUnit)
        values.put(SALES_MERCHANT, product.merchant)
        values.put(SALES_IMAGE_EVIDENCE, product.imageEvidence)

        db.update(
            TABLE_SALES,
            values,
            "$UUID = ?",
            arrayOf(product.uuid)
        )
        db.close()
    }
    //#END SALE PROMOTER

    //#STOCK PROMOTER
    fun getStockPromoter(visitID: Int): List<Stock> {
        val db = readableDatabase
        val listStock = ArrayList<Stock>()
        val select = arrayOf(STOCK1_TYPE, STOCK1_BRAND, STOCK1_PRODUCT, UUID)

        val c = db.query(TABLE_STOCK1, select, "$VISIT_ID = $visitID", null, null, null, null, null)

        if (c != null && c.count > 0) {
            c.moveToFirst()
            do {
                listStock.add(Stock(c.getString(0), c.getString(1), c.getString(2), c.getString(3)))
            } while (c.moveToNext())
        }
        db?.close()
        c?.close()

        return listStock
    }
    fun addStockPromoter(stock: Stock, visitID: Int) {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put(STOCK1_PRODUCT, stock.product)
        values.put(STOCK1_BRAND, stock.brand)
        values.put(STOCK1_TYPE, stock.type)
        values.put(VISIT_ID, visitID)
        values.put(UUID, stock.uuid)

        db.insert(TABLE_STOCK1, null, values)
        db.close()
    }
    fun deleteStockPromoter(uuid: String) {
        val db = readableDatabase
        db.delete(TABLE_STOCK1, "$UUID = ?", arrayOf(uuid))
        db.close()
    }
    //#END STOCK PROMOTER

    companion object {
        private const val DATABASE_VERSION = 2

        private const val UUID = "uuidtmp"
        private const val VISIT_ID = "visitid"

        private const val DATABASE_NAME = "rintisa.db"
        private const val TABLE_STOCK = "merchant_stock"
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
                "   $VISIT_ID integer," +
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
                "   $UUID text," +
                "   $VISIT_ID integer" +
                ")")

        private const val SQL_CREATE_TABLE_SALES = ("" +
                "create table $TABLE_SALES (" +
                "  _id integer primary key autoincrement," +
                "   $SALES_BRAND text," +
                "   $SALES_QUANTITY integer," +
                "   $SALES_MEASURE_UNIT text," +
                "   $SALES_MERCHANT text," +
                "   $SALES_IMAGE_EVIDENCE text," +
                "   $UUID text," +
                "   $VISIT_ID integer" +
                ")")

    }
}