package com.delycomps.myapplication.cache

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Point
import com.delycomps.myapplication.model.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class BDLocal(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_TABLE_STOCK)
        db.execSQL(SQL_CREATE_TABLE_STOCK1)
        db.execSQL(SQL_CREATE_TABLE_SALES)
        db.execSQL(SQL_CREATE_TABLE_PRICE)
        db.execSQL(SQL_CREATE_TABLE_AVAILABILITY)
        db.execSQL(SQL_CREATE_TABLE_PDV)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        p0?.execSQL(SQL_CREATE_TABLE_PDV)
    }



    fun getPointSale(): List<PointSale> {
        val db = readableDatabase
        val listPDV = ArrayList<PointSale>()
        val select = arrayOf(
            PDV_VISITID, PDV_CUSTOMERID, PDV_CLIENTCODE, PDV_CLIENT, PDV_MARKET,
            PDV_STALLNUMBER, PDV_VISITFREQUENCY, PDV_VISITDAY, PDV_LASTVISIT, PDV_TRAFFICLIGHTS,
            PDV_SHOWSURVEY, PDV_SHOWAVAILABILITY, PDV_MANAGEMENT, PDV_IMAGEBEFORE, PDV_IMAGEAFTER,
            UUID, PDV_IMAGEBEFORELOCAL, PDV_IMAGEAFTERLOCAL, PDV_STATUSLOCAL, PDV_DATEFINISHLOCAL,
            PDV_STATUSMANAGEMENT, PDV_MOTIVEMANAGEMENT, PDV_OBSERVATION)
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val c = db.query(TABLE_PDV, select, "$PDV_DATE = ?", arrayOf(date), null, null, null, null)

        if (c != null && c.count > 0) {
            c.moveToFirst()
            do {
                val ps = PointSale(c.getInt(0), c.getInt(1), c.getString(2) ?: "", c.getString(3) ?: "", c.getString(4) ?: "", c.getString(5) ?: "", c.getString(6) ?: "", c.getString(7) ?: "", c.getString(8) ?: "", c.getString(9) ?: "", c.getInt(10) == 1, c.getInt(11) == 1, c.getString(12) ?: "", c.getString(13) ?: "", c.getString(14) ?: "", c.getString(15) ?: "")
                ps.imageBeforeLocal = c.getString(16) ?: ""
                ps.imageAfterLocal = c.getString(17) ?: ""
                ps.wasSaveOnBD = (c.getString(18) ?: "") != "NOENVIADO"
                ps.dateFinish = c.getString(19) ?: ""
                ps.statusManagement = c.getString(20) ?: ""
                ps.motiveManagement = c.getString(21) ?: ""
                ps.observation = c.getString(22) ?: ""
                listPDV.add(ps)
            } while (c.moveToNext())
        }
        db?.close()
        c?.close()

        return listPDV
    }

    fun getPointSaleOne(visitID: Int): List<PointSale> {
        val db = readableDatabase
        val listPDV = ArrayList<PointSale>()
        val select = arrayOf(
            PDV_VISITID, PDV_CUSTOMERID, PDV_CLIENTCODE, PDV_CLIENT, PDV_MARKET,
            PDV_STALLNUMBER, PDV_VISITFREQUENCY, PDV_VISITDAY, PDV_LASTVISIT, PDV_TRAFFICLIGHTS,
            PDV_SHOWSURVEY, PDV_SHOWAVAILABILITY, PDV_MANAGEMENT, PDV_IMAGEBEFORE, PDV_IMAGEAFTER,
            UUID, PDV_IMAGEBEFORELOCAL, PDV_IMAGEAFTERLOCAL, PDV_STATUSLOCAL, PDV_DATEFINISHLOCAL,
            PDV_STATUSMANAGEMENT, PDV_MOTIVEMANAGEMENT, PDV_OBSERVATION)

        val c = db.query(TABLE_PDV, select, "$PDV_VISITID = ?", arrayOf(visitID.toString()), null, null, null, null)

        if (c != null && c.count > 0) {
            c.moveToFirst()
            do {
                val ps = PointSale(c.getInt(0), c.getInt(1), c.getString(2) ?: "", c.getString(3) ?: "", c.getString(4) ?: "", c.getString(5) ?: "", c.getString(6) ?: "", c.getString(7) ?: "", c.getString(8) ?: "", c.getString(9) ?: "", c.getInt(10) == 1, c.getInt(11) == 1, c.getString(12) ?: "", c.getString(13) ?: "", c.getString(14) ?: "", c.getString(15) ?: "")
                ps.imageBeforeLocal = c.getString(16) ?: ""
                ps.imageAfterLocal = c.getString(17) ?: ""
                ps.wasSaveOnBD = (c.getString(18) ?: "") != "NOENVIADO"
                ps.dateFinish = c.getString(19) ?: ""
                ps.statusManagement = c.getString(20) ?: ""
                ps.motiveManagement = c.getString(21) ?: ""
                ps.observation = c.getString(22) ?: ""
                listPDV.add(ps)
            } while (c.moveToNext())
        }
        db?.close()
        c?.close()

        return listPDV
    }

    fun updatePointSaleManagement(visitID: Int, status: String, motive: String, observation: String) {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put(PDV_STATUSMANAGEMENT, status)
        values.put(PDV_MOTIVEMANAGEMENT, motive)
        values.put(PDV_OBSERVATION, observation)

        db.update(
            TABLE_PDV,
            values,
            "$VISIT_ID = ?",
            arrayOf(visitID.toString())
        )
        db.close()
    }

    fun updatePointSaleLocal(visitID: Int, management: String? = null, statusLocal: String? = null, imageAfterLocal: String? = null, imageBeforeLocal: String? = null, finishDateLocal: String? = null) {
        val db = this.writableDatabase
        val values = ContentValues()

        if (management != null)
            values.put(PDV_MANAGEMENT, management)
        if (statusLocal != null)
            values.put(PDV_STATUSLOCAL, statusLocal)
        if (imageAfterLocal != null)
            values.put(PDV_IMAGEAFTERLOCAL, imageAfterLocal)
        if (imageBeforeLocal != null)
            values.put(PDV_IMAGEBEFORELOCAL, imageBeforeLocal)
        if (finishDateLocal != null)
            values.put(PDV_DATEFINISHLOCAL, finishDateLocal)

        db.update(
            TABLE_PDV,
            values,
            "$VISIT_ID = ?",
            arrayOf(visitID.toString())
        )
        db.close()
    }

    fun deleteAllPointSale () {
        val dbd = this.writableDatabase
        dbd.delete(TABLE_PDV, null, null)
        dbd.close()
    }

    fun savePointSales(list: List<PointSale>): List<PointSale> {
        val listPoint = getPointSale()
        val dbd = this.writableDatabase
        dbd.delete(TABLE_PDV, null, null)
        dbd.close()
        for ((i, item) in list.withIndex()) {
            val db = this.writableDatabase
            val values = ContentValues()
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

            values.put(PDV_VISITID, item.visitId)
            values.put(PDV_CUSTOMERID, item.customerId)
            values.put(PDV_CLIENTCODE, item.clientCode)
            values.put(PDV_CLIENT, item.client)
            values.put(PDV_MARKET, item.market)
            values.put(PDV_STALLNUMBER, item.stallNumber)
            values.put(PDV_VISITFREQUENCY, item.visitFrequency)
            values.put(PDV_VISITDAY, item.visitDay)
            values.put(PDV_LASTVISIT, item.lastVisit)
            values.put(PDV_TRAFFICLIGHTS, item.trafficLights)
            values.put(PDV_SHOWSURVEY, if (item.showSurvey) 1 else 0)

            values.put(PDV_IMAGEBEFORE, item.imageBefore)
            values.put(PDV_IMAGEAFTER, item.imageAfter)
            values.put(PDV_DATE, date.toString())
            values.put(UUID, item.uuid)

            val pointSaved = listPoint.find { it.visitId ==item.visitId }
            if (pointSaved != null) {

                list[i].wasSaveOnBD = pointSaved.wasSaveOnBD
                list[i].management = pointSaved.management
                list[i].imageAfterLocal = pointSaved.imageAfterLocal
                list[i].imageBeforeLocal = pointSaved.imageBeforeLocal
                list[i].dateFinish = pointSaved.dateFinish

                values.put(PDV_STATUSLOCAL, if (pointSaved.wasSaveOnBD) "ENVIADO" else "NOENVIADO")
                values.put(PDV_MANAGEMENT, pointSaved.management)
                values.put(PDV_IMAGEAFTERLOCAL, pointSaved.imageAfterLocal)
                values.put(PDV_IMAGEBEFORELOCAL, pointSaved.imageBeforeLocal)
                values.put(PDV_DATEFINISHLOCAL, pointSaved.dateFinish)
            } else {

                list[i].wasSaveOnBD = item.management == "VISITADO"
                values.put(PDV_MANAGEMENT, item.management)
                values.put(PDV_STATUSLOCAL, if(item.management == "VISITADO") "ENVIADO" else "NOENVIADO")
            }

            db.insert(TABLE_PDV, null, values)
            dbd.close()
        }
        return list
    }




    fun getProductsAvailability(visitID: Int): List<Availability> {
        val db = readableDatabase
        val listAvailability = ArrayList<Availability>()
        val select = arrayOf(AVAILABILITY_PRODUCT_ID, AVAILABILITY_DESCRIPTION, AVAILABILITY_BRAND, AVAILABILITY_COMPETENCE, UUID )

        val c = db.query(TABLE_AVAILABILITY, select, "$VISIT_ID = $visitID", null, null, null, null, null)

        if (c != null && c.count > 0) {
            c.moveToFirst()
            do {
                listAvailability.add(Availability(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), true, c.getString(4)))
            } while (c.moveToNext())
        }
        db?.close()
        c?.close()

        return listAvailability
    }

    fun addProductsAvailability(availability: Availability, visitId: Int) {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put(VISIT_ID, visitId)
        values.put(AVAILABILITY_PRODUCT_ID, availability.productid)
        values.put(AVAILABILITY_BRAND, availability.brand)
        values.put(AVAILABILITY_DESCRIPTION, availability.description)
        values.put(AVAILABILITY_COMPETENCE, availability.competence)
        values.put(UUID, availability.uuid)

        db.insert(TABLE_AVAILABILITY, null, values)
        db.close()
    }

    fun deleteProductAvailability(uuid: String) {
        val db = readableDatabase
        db.delete(TABLE_AVAILABILITY, "$UUID = ?", arrayOf(uuid))
        db.close()
    }

    fun deleteProductAvailabilityFromVisit(visitID: Int) {
        val db = readableDatabase
        db.delete(TABLE_AVAILABILITY, "$VISIT_ID = ?", arrayOf(visitID.toString()))
        db.close()
    }


    fun getMerchantPrices(visitID: Int): List<SurveyProduct> {
        val db = readableDatabase
        val listSurveyProduct = ArrayList<SurveyProduct>()
        val select = arrayOf(PRICES_PRODUCT_ID, PRICES_DESCRIPTION, PRICES_BRAND, PRICES_PRICE, PRICES_MEASURE_UNIT, UUID )

        val c = db.query(TABLE_PRICES, select, "$VISIT_ID = $visitID", null, null, null, null, null)

        if (c != null && c.count > 0) {
            c.moveToFirst()
            do {
                listSurveyProduct.add(SurveyProduct(c.getInt(0), c.getString(1), c.getString(2), c.getDouble(3), c.getString(4), 0.0, "", "", c.getString(5)))
            } while (c.moveToNext())
        }
        db?.close()
        c?.close()

        return listSurveyProduct
    }

    fun addMerchantPrice(surveyProduct: SurveyProduct, visitId: Int) {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put(VISIT_ID, visitId)
        values.put(PRICES_PRODUCT_ID, surveyProduct.productId)
        values.put(PRICES_BRAND, surveyProduct.brand)
        values.put(PRICES_DESCRIPTION, surveyProduct.description)
        values.put(PRICES_PRICE, surveyProduct.price)
        values.put(PRICES_MEASURE_UNIT, surveyProduct.measureUnit)
        values.put(UUID, surveyProduct.uuid)

        db.insert(TABLE_PRICES, null, values)
        db.close()
    }

    fun updateMerchantPrice(surveyProduct: SurveyProduct) {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put(PRICES_PRODUCT_ID, surveyProduct.productId)
        values.put(PRICES_BRAND, surveyProduct.brand)
        values.put(PRICES_DESCRIPTION, surveyProduct.description)
        values.put(PRICES_PRICE, surveyProduct.price)
        values.put(PRICES_MEASURE_UNIT, surveyProduct.measureUnit)

        db.update(
            TABLE_PRICES,
            values,
            "$UUID = ?",
            arrayOf(surveyProduct.uuid)
        )
        db.close()
    }

    fun deleteMerchantPrices(uuid: String) {
        val db = readableDatabase
        db.delete(TABLE_PRICES, "$UUID = ?", arrayOf(uuid))
        db.close()
    }

    fun deleteMerchantPricesFromVisit(visitID: Int) {
        val db = readableDatabase
        db.delete(TABLE_PRICES, "$VISIT_ID = ?", arrayOf(visitID.toString()))
        db.close()
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

    fun deleteMaterial(uuid: String) {
        val db = readableDatabase
        db.delete(TABLE_STOCK, "$UUID = ?", arrayOf(uuid))
        db.close()
    }

    fun deleteMaterialFromVisit(visitID: Int) {
        val db = readableDatabase
        db.delete(TABLE_STOCK, "$visitID = ?", arrayOf(visitID.toString()))
        db.close()
    }

    //#SALE PROMOTER
    fun getSalePromoter(visitID: Int): List<SurveyProduct> {
        val db = readableDatabase
        val listStock = ArrayList<SurveyProduct>()

        val select = arrayOf(SALES_BRAND, SALES_QUANTITY, SALES_MEASURE_UNIT, SALES_MERCHANT, SALES_IMAGE_EVIDENCE, UUID, SALES_IMAGE_EVIDENCE_LOCAL)

        val c = db.query(TABLE_SALES, select, "$VISIT_ID = $visitID", null, null, null, null, null)

        if (c != null && c.count > 0) {
            c.moveToFirst()
            do {
                val sv = SurveyProduct(0, c.getString(0), c.getString(0), 0.00, c.getString(2), c.getDouble(1), c.getString(3), c.getString(4), c.getString(5))
                sv.imageEvidenceLocal = c.getString(6) ?: ""
                listStock.add(sv)
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
        values.put(SALES_IMAGE_EVIDENCE_LOCAL, product.imageEvidenceLocal)
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

    fun deleteSalePromoterFromVisit(visitID: Int) {
        val db = readableDatabase
        db.delete(TABLE_SALES, "$VISIT_ID = ?", arrayOf(visitID.toString()))
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
        values.put(SALES_IMAGE_EVIDENCE_LOCAL, product.imageEvidenceLocal)

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

    fun deleteStockPromoterFromVisit(visitID: Int) {
        val db = readableDatabase
        db.delete(TABLE_STOCK1, "$VISIT_ID = ?", arrayOf(visitID.toString()))
        db.close()
    }
    //#END STOCK PROMOTER

    companion object {
        private const val DATABASE_VERSION = 3

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
        private const val SALES_IMAGE_EVIDENCE_LOCAL = "image_evidence_local"

        private const val TABLE_PRICES = "merchant_prices"
        private const val PRICES_PRODUCT_ID = "productid"
        private const val PRICES_BRAND = "brand"
        private const val PRICES_DESCRIPTION = "description"
        private const val PRICES_PRICE = "price"
        private const val PRICES_MEASURE_UNIT = "measure_unit"

        private const val TABLE_AVAILABILITY = "merchant_availability"
        private const val AVAILABILITY_PRODUCT_ID = "productid"
        private const val AVAILABILITY_DESCRIPTION = "description"
        private const val AVAILABILITY_BRAND = "brand"
        private const val AVAILABILITY_COMPETENCE = "competence"
        private const val AVAILABILITY_FLAG = "flag"

        private const val TABLE_PDV = "pdv"
        private const val PDV_VISITID = "visitid"
        private const val PDV_CUSTOMERID = "customerid"
        private const val PDV_CLIENTCODE = "clientcode"
        private const val PDV_CLIENT = "client"
        private const val PDV_MARKET = "market"
        private const val PDV_STALLNUMBER = "stallnumber"
        private const val PDV_VISITFREQUENCY = "visitfrequency"
        private const val PDV_VISITDAY = "visitday"
        private const val PDV_LASTVISIT = "lastvisit"
        private const val PDV_TRAFFICLIGHTS = "trafficlights"
        private const val PDV_SHOWSURVEY = "showsurvey"
        private const val PDV_SHOWAVAILABILITY = "showavailability"
        private const val PDV_MANAGEMENT = "management"
        private const val PDV_IMAGEBEFORE = "imagebefore"
        private const val PDV_IMAGEAFTER = "imageafter"

        private const val PDV_IMAGEBEFORELOCAL = "imagebeforelocal"
        private const val PDV_IMAGEAFTERLOCAL = "imageafterlocal"
        private const val PDV_STATUSLOCAL = "statuslocal"
        private const val PDV_DATEFINISHLOCAL = "datefinishlocal"

        private const val PDV_STATUSMANAGEMENT = "statusmanagement"
        private const val PDV_MOTIVEMANAGEMENT = "motivemanagement"
        private const val PDV_OBSERVATION = "observation"

        private const val PDV_DATE = "visitdate"

        private const val SQL_CREATE_TABLE_PDV = ("" +
                "create table $TABLE_PDV (" +
                "  _id integer primary key autoincrement," +
                " $PDV_VISITID integer, " +
                " $PDV_CUSTOMERID integer, " +
                " $PDV_CLIENTCODE text, " +
                " $PDV_CLIENT text, " +
                " $PDV_MARKET text, " +
                " $PDV_STALLNUMBER text, " +
                " $PDV_VISITFREQUENCY text, " +
                " $PDV_VISITDAY text, " +
                " $PDV_LASTVISIT text, " +
                " $PDV_TRAFFICLIGHTS text, " +
                " $PDV_SHOWSURVEY integer, " +
                " $PDV_SHOWAVAILABILITY integer, " +
                " $PDV_MANAGEMENT text, " +
                " $PDV_IMAGEBEFORE text, " +
                " $PDV_IMAGEAFTER text, " +
                " $PDV_DATE text, " +
                " $PDV_IMAGEBEFORELOCAL text, " +
                " $PDV_IMAGEAFTERLOCAL text, " +
                " $PDV_STATUSLOCAL text, " +
                " $PDV_DATEFINISHLOCAL text, " +
                " $PDV_STATUSMANAGEMENT text, " +
                " $PDV_MOTIVEMANAGEMENT text, " +
                " $PDV_OBSERVATION text, " +
                " $UUID text" +
                ")")

        private const val SQL_CREATE_TABLE_PRICE = ("" +
                "create table $TABLE_PRICES (" +
                "  _id integer primary key autoincrement," +
                "   $PRICES_PRODUCT_ID integer," +
                "   $PRICES_BRAND text," +
                "   $PRICES_DESCRIPTION text," +
                "   $PRICES_PRICE double," +
                "   $PRICES_MEASURE_UNIT text," +
                "   $UUID text," +
                "   $VISIT_ID integer" +
                ")")

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
                "   $SALES_QUANTITY double," +
                "   $SALES_MEASURE_UNIT text," +
                "   $SALES_MERCHANT text," +
                "   $SALES_IMAGE_EVIDENCE text," +
                "   $UUID text," +
                "   $SALES_IMAGE_EVIDENCE_LOCAL text," +
                "   $VISIT_ID integer" +
                ")")

        private const val SQL_CREATE_TABLE_AVAILABILITY = ("" +
                "create table $TABLE_AVAILABILITY (" +
                "  _id integer primary key autoincrement," +
                "   $AVAILABILITY_PRODUCT_ID integer," +
                "   $AVAILABILITY_DESCRIPTION text," +
                "   $AVAILABILITY_BRAND text," +
                "   $AVAILABILITY_COMPETENCE text," +
                "   $UUID text," +
                "   $VISIT_ID integer" +
                ")")

    }
}