package im.angry.openeuicc.ui

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import im.angry.openeuicc.common.R
import kotlin.random.Random

class EuiccInfoActivity : AppCompatActivity() {

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var infoList: RecyclerView

    data class Item(
        val title: String,
        val content: String,
        val copiedToastResId: Int? = null
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_euicc_info)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        swipeRefresh = findViewById(R.id.swipe_refresh)

        infoList = findViewById<RecyclerView>(R.id.recycler_view).also {
            it.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

            it.addItemDecoration(
                DividerItemDecoration(
                    this,
                    LinearLayoutManager.VERTICAL
                )
            )

            it.adapter = EuiccInfoAdapter()
        }

        swipeRefresh.setOnRefreshListener {
            refresh()
        }

        refresh()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun refresh() {
        swipeRefresh.isRefreshing = true

        val items = when (
            intent.getIntExtra("demo_euicc_info_type", 1)
        ) {
            2 -> buildDemoEuiccInfoItemsType2()
            3 -> buildDemoEuiccInfoItemsType3()
            else -> buildDemoEuiccInfoItemsType1()
        }

        (infoList.adapter as EuiccInfoAdapter).euiccInfoItems = items

        swipeRefresh.isRefreshing = false
    }

    /**
     * =========================================================
     * 随机 EID
     * =========================================================
     *
     * 前 8 位：
     * 第 1 位 3~9
     * 后 7 位 0~9
     *
     * 中间固定：
     * 20250000012500000
     *
     * 最后 7 位：
     * 0~9
     */
    private fun generateRandomEid(): String {
        val prefix = buildString {
            append(Random.nextInt(3, 10))

            repeat(7) {
                append(Random.nextInt(0, 10))
            }
        }

        val middle = "20250000012500000"

        val suffix = buildString {
            repeat(7) {
                append(Random.nextInt(0, 10))
            }
        }

        return prefix + middle + suffix
    }

    /**
     * =========================================================
     * 随机 SAS
     * =========================================================
     */
    private fun generateRandomSas(): String {
        return buildString {
            append("WD-BG-UP-")

            repeat(4) {
                append(Random.nextInt(0, 10))
            }
        }
    }

    /**
     * =========================================================
     * 随机 NVRAM
     * 0.01 ~ 499.99 KiB
     * =========================================================
     */
    private fun generateRandomNvram(): String {
        val value = Random.nextInt(1, 50_000) / 100.0
        return String.format("%.2f KiB", value)
    }

    /**
     * =========================================================
     * 随机 ATR
     * 44 个大写十六进制字符
     * =========================================================
     */
    private fun generateRandomAtr(): String {
        val hex = "0123456789ABCDEF"

        return buildString {
            repeat(44) {
                append(hex[Random.nextInt(hex.length)])
            }
        }
    }

    /**
     * =========================================================
     * 随机 16 位 CI
     * =========================================================
     */
    private fun generateRandomCi(): String {
        val hex = "0123456789ABCDEF"

        return buildString {
            repeat(16) {
                append(hex[Random.nextInt(hex.length)])
            }
        }
    }

    /**
     * =========================================================
     * 第一套
     * =========================================================
     */
    private fun buildDemoEuiccInfoItemsType1(): List<Item> {
        val randomEid = generateRandomEid()
        val randomSas = generateRandomSas()
        val randomNvram = generateRandomNvram()
        val randomAtr = generateRandomAtr()

        return listOf(
            Item(
                "Access Mode",
                "OpenMobile API (OMAPI)"
            ),

            Item(
                "Removable",
                "Yes"
            ),

            Item(
                "EID",
                randomEid,
                R.string.toast_eid_copied
            ),

            Item(
                "SGP.22 Version",
                "2.5.0"
            ),

            Item(
                "SAS Accreditation Number",
                randomSas
            ),

            Item(
                "Free NVRAM",
                "$randomNvram (for reference only)"
            ),

            Item(
                "Certificate Issuer (CI)",
                "GSMA Live CI"
            ),

            Item(
                "ATR",
                randomAtr,
                R.string.toast_atr_copied
            )
        )
    }

    /**
     * =========================================================
     * 第二套
     * =========================================================
     */
    private fun buildDemoEuiccInfoItemsType2(): List<Item> {
        val randomEid = generateRandomEid()
        val randomSas = generateRandomSas()

        val randomFreeNonVolatileMemory =
            Random.nextInt(200_000, 400_001)

        val randomFreeVolatileMemory =
            Random.nextInt(9_000, 11_001)

        // 两个 CI 必须使用同一个随机值
        val randomCi = generateRandomCi()

        return listOf(
            Item(
                "EID",
                randomEid,
                R.string.toast_eid_copied
            ),

            Item(
                "SAS Accreditation",
                randomSas
            ),

            Item(
                "Lowest Supported Version",
                "2.5.0"
            ),

            Item(
                "Free Non-volatile Memory",
                "${String.format("%,d", randomFreeNonVolatileMemory)} B"
            ),

            Item(
                "Free Volatile Memory",
                "${String.format("%,d", randomFreeVolatileMemory)} B"
            ),

            Item(
                "Default SM-DP+ Address",
                ""
            ),

            Item(
                "Root SM-DS Address",
                "testrootsmds.gsma.com"
            ),

            Item(
                "EUICC Sign CI",
                randomCi
            ),

            Item(
                "EUICC Verify CI",
                randomCi
            ),

            Item(
                "Profile Version",
                "2.2.0"
            ),

            Item(
                "Global Platform Version",
                "2.3.0"
            ),

            Item(
                "Firmware Version",
                "25.4.0"
            )
        )
    }

    /**
     * =========================================================
     * 第三套
     * =========================================================
     *
     * 随机项目全部使用第一套规则。
     */
    private fun buildDemoEuiccInfoItemsType3(): List<Item> {
        val randomEid = generateRandomEid()
        val randomSas = generateRandomSas()
        val randomNvram = generateRandomNvram()
        val randomAtr = generateRandomAtr()

        return listOf(
            Item(
                "Access Mode",
                "OpenMobile API (OMAPI)"
            ),

            Item(
                "Removable",
                "Yes"
            ),

            Item(
                "EID",
                randomEid,
                R.string.toast_eid_copied
            ),

            Item(
                "Manufacturer",
                "Beijing Watchdata(CN)"
            ),

            Item(
                "eUICC Profile version supported",
                "2.2.0"
            ),

            Item(
                "SGP.22 Version",
                "2.5.0"
            ),

            Item(
                "eUICC OS Version",
                "25.4.0"
            ),

            Item(
                "GlobalPlatform Version",
                "2.3.0"
            ),

            Item(
                "Protected Profile Version",
                "1.0.0"
            ),

            Item(
                "SAS Accreditation Number",
                randomSas
            ),

            Item(
                "Free NVRAM (eSIM profile storage)",
                "$randomNvram (for reference only)"
            ),

            Item(
                "Certificate Issuer (CI)",
                "GSMA Live CI"
            ),

            Item(
                "Answer To Reset (ATR)",
                randomAtr,
                R.string.toast_atr_copied
            )
        )
    }

    inner class EuiccInfoViewHolder(
        root: View
    ) : RecyclerView.ViewHolder(root) {

        private val title: TextView =
            root.findViewById(R.id.euicc_info_title)

        private val content: TextView =
            root.findViewById(R.id.euicc_info_content)

        private var copiedToastResId: Int? = null

        init {
            root.setOnClickListener {
                val toastResId = copiedToastResId ?: return@setOnClickListener

                val clipboard =
                    root.context.getSystemService(
                        ClipboardManager::class.java
                    )

                clipboard?.setPrimaryClip(
                    ClipData.newPlainText(
                        title.text.toString(),
                        content.text.toString()
                    )
                )

                Toast.makeText(
                    root.context,
                    toastResId,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        fun bind(item: Item) {
            copiedToastResId = item.copiedToastResId

            title.text = item.title
            content.text = item.content
        }
    }

    inner class EuiccInfoAdapter :
        RecyclerView.Adapter<EuiccInfoViewHolder>() {

        var euiccInfoItems: List<Item> = emptyList()
            @SuppressLint("NotifyDataSetChanged")
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): EuiccInfoViewHolder {

            val root = LayoutInflater
                .from(parent.context)
                .inflate(
                    R.layout.euicc_info_item,
                    parent,
                    false
                )

            return EuiccInfoViewHolder(root)
        }

        override fun getItemCount(): Int {
            return euiccInfoItems.size
        }

        override fun onBindViewHolder(
            holder: EuiccInfoViewHolder,
            position: Int
        ) {
            holder.bind(euiccInfoItems[position])
        }
    }
}
