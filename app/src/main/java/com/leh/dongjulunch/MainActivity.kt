package com.leh.dongjulunch

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.jsoup.Jsoup
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    lateinit var preferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    private val lunchList: ArrayList<LunchData> = arrayListOf()
                                                //월 노랑   //화 핑  //수 초   //목 주  //금 하늘
    private val colorList = arrayListOf<String>("#FFB300","#9575CD","#009688","#FF7043","#78909C")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        preferences = applicationContext.getSharedPreferences("last_meal", Context.MODE_PRIVATE)
        editor = preferences.edit()
        mealParse()

        pager.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            getChildAt(0).overScrollMode = View.OVER_SCROLL_NEVER
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
        }

        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(MarginPageTransformer(40))
        compositePageTransformer.addTransformer(ViewPager2.PageTransformer { page, position ->
            val r = 1 - Math.abs(position)
            page.scaleY = 0.85f + r * 0.15f
        })

        pager.setPageTransformer(compositePageTransformer)
    }

    fun mealParse() {
        val parsing = GlobalScope.launch(Dispatchers.IO) {
            try {
                val request = Jsoup.connect(
                    "https://open.neis.go.kr/hub/mealServiceDietInfo?Type=json&pIndex=1&pSize=1" +
                            "&ATPT_OFCDC_SC_CODE=C10&SD_SCHUL_CODE=7201022&MLSV_FROM_YMD=${getMonday()}&MLSV_TO_YMD=${getFriday()}"
                ).ignoreContentType(true).ignoreHttpErrors(true).get()

                val jObject = JSONObject(request.body().text()).apply {
                    val meal_list = (this.getJSONArray("mealServiceDietInfo")[1] as JSONObject).getJSONArray("row")
                    for (meal in 0 until meal_list.length()) {
                        lunchList.add(LunchData("중식 ${(meal_list[meal] as JSONObject).getString("CAL_INFO")}",
                            (meal_list[meal] as JSONObject).getString("DDISH_NM"),
                            background = colorList[meal]))
                    }
                }

                runOnUiThread {
                    pager.adapter = MainAdapter(lunchList)
                    val cal = Calendar.getInstance();
                    val num = cal.get(Calendar.DAY_OF_WEEK);
                    pager.setCurrentItem(num, true)
                }
            } catch (e: Exception) {
//                runOnUiThread {
//                    kcal.text = preferences.getString("kcal", "급식")
//                }
            }
        }
    }

    fun getMonday(): String{

        val formatter = java.text.SimpleDateFormat("yyyyMMdd")

        val c = Calendar.getInstance()

        c.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY)

        return formatter.format(c.time)

    }

    fun getFriday(): String{

        val formatter = java.text.SimpleDateFormat("yyyyMMdd")

        val c = Calendar.getInstance()

        c.set(Calendar.DAY_OF_WEEK,Calendar.FRIDAY)

        return formatter.format(c.time)

    }
}