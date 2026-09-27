```kotlin
package im.angry.openeuicc.ui

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import im.angry.openeuicc.common.R
import im.angry.openeuicc.core.EuiccChannel
import im.angry.openeuicc.core.EuiccChannelManager
import im.angry.openeuicc.util.*
import kotlinx.coroutines.launch
import kotlin.random.Random


class EuiccInfoActivity : BaseEuiccAccessActivity(), OpenEuiccContextMarker {

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var infoList: RecyclerView

    private var logicalSlotId: Int = -1
    private var seId: EuiccChannel.SecureElementId =
        EuiccChannel.SecureElementId.DEFAULT

    data class Item(
        val title: String,
        val content: String?,
        val copiedToastResId: Int? = null,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_euicc_info)
        setSupportActionBar(requireViewById(R.id.toolbar))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        swipeRefresh = requireViewById(R.id.swipe_refresh)

        infoList = requireViewById<RecyclerView>(R.id.recycler_view).also {
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

        logicalSlotId = intent.getIntExtra("logicalSlotId", 0)

        seId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(
                "seId",
                EuiccChannel.SecureElementId::class.java
            )
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("seId")
        } ?: EuiccChannel.SecureElementId.DEFAULT

        setChannelTitle(
            if (logicalSlotId == EuiccChannelManager.USB_CHANNEL_ID) {
                this@EuiccInfoActivity.getString(R.string.channel_name_format_usb)
            } else {
                appContainer.customizableTextProvider
                    .formatNonUsbChannelName(logicalSlotId)
            }
        )

        swipeRefresh.setOnRefreshListener {
            refresh()
        }

        setupRootViewSystemBarInsets(
            window.decorView.rootView,
            arrayOf(
                this::activityToolbarInsetHandler,
                mainViewPaddingInsetHandler(infoList)
            )
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }

    private fun setChannelTitle(title: CharSequence) {
        super.setTitle(
            this@EuiccInfoActivity.getString(
                R.string.euicc_info_activity_title,
                title
            )
        )
    }

    override fun onInit() {
        refresh()
    }

    /**
     * Demo 模式：
     *
     * 不访问真实 eUICC。
     * 不调用 euiccChannelManager。
     * 不读取 APDU。
     * 不读取 EuiccInfo2。
     *
     * 根据 demo_euicc_info_type 显示三套不同的模拟数据。
     */
    private fun refresh() {
        swipeRefresh.isRefreshing = true

        lifecycleScope.launch {
            val items = when (
                intent.getIntExtra("demo_euicc_info_type", 1)
            ) {
                2 -> buildDemoEuiccInfoItemsType2()
                3 -> buildDemoEuiccInfoItemsType3()
                else -> buildDemoEuiccInfoItemsType1()
            }

            (infoList.adapter!! as EuiccInfoAdapter)
                .euiccInfoItems = items

            swipeRefresh.isRefreshing = false
        }
    }

    /**
     * =========================================================
     * 公共随机数据
     * =========================================================
     */

    /**
     * EID：
     *
     * 前 8 位随机：
     * 第 1 位：3~9
     * 第 2~8 位：0~9
     *
     * 中间 17 位固定：
     * 20250000012500000
     *
     * 最后 7 位随机：
     * 0~9
     */
    private fun generateRandomEid(): String {

        val randomEidPrefix = buildString {
            append(Random.nextInt(3, 10))

            repeat(7) {
                append(Random.nextInt(0, 10))
            }
        }

        val fixedEidMiddle = "20250000012500000"

        val randomEidSuffix = buildString {
            repeat(7) {
                append(Random.nextInt(0, 10))
            }
        }

        return randomEidPrefix +
                fixedEidMiddle +
                randomEidSuffix
    }

    /**
     * SAS：
     *
     * WD-BG-UP- + 4 位随机数字
     */
    private fun generateRandomSas(): String =
        buildString {
            append("WD-BG-UP-")

            repeat(4) {
                append(Random.nextInt(0, 10))
            }
        }

    /**
     * NVRAM：
     *
     * 0.01 ~ 499.99 KiB
     */
    private fun generateRandomNvram(): String =
        String.format(
            "%.2f KiB",
            Random.nextDouble(0.01, 500.0)
        )

    /**
     * ATR：
     *
     * 44 位随机大写十六进制字符
     */
    private fun generateRandomAtr(): String {

        val hexCharacters = "0123456789ABCDEF"

        return buildString {
            repeat(44) {
                append(
                    hexCharacters[
                        Random.nextInt(hexCharacters.length)
                    ]
                )
            }
        }
    }

    /**
     * 16 位随机十六进制 CI
     */
    private fun generateRandomCi(): String {

        val hexCharacters = "0123456789ABCDEF"

        return buildString {
            repeat(16) {
                append(
                    hexCharacters[
                        Random.nextInt(hexCharacters.length)
                    ]
                )
            }
        }
    }

    /**
     * =========================================================
     * 第一套
     * =========================================================
     */
    private fun buildDemoEuiccInfoItemsType1(): List<Item> {
    val yesText = getString(R.string.euicc_info_yes)
    val eidTitle = getString(R.string.euicc_info_eid)
    val sgp22Title = getString(R.string.euicc_info_sgp22_version)
    val sasTitle = getString(R.string.euicc_info_sas_accreditation_number)
    val nvramTitle = getString(R.string.euicc_info_free_nvram)
    val nvramHint = getString(R.string.euicc_info_free_nvram_hint)
    val ciTitle = getString(R.string.euicc_info_ci_type)
    val ciValue = getString(R.string.euicc_info_ci_gsma_live)
    val atrTitle = getString(R.string.euicc_info_atr)

    val randomEid = generateRandomEid()
    val randomSas = generateRandomSas()
    val randomNvram = generateRandomNvram()
    val randomAtr = generateRandomAtr()

    return buildList {
        add(Item("Access Mode", "OpenMobile API (OMAPI)"))
        add(Item("Removable", yesText))
        add(Item(eidTitle, randomEid, R.string.toast_eid_copied))
        add(Item(sgp22Title, "2.5.0"))
        add(Item(sasTitle, randomSas))
        add(Item(nvramTitle, "$randomNvram $nvramHint"))
        add(Item(ciTitle, ciValue))
        add(Item(atrTitle, randomAtr, R.string.toast_atr_copied))
    }
}

    /**
     * =========================================================
     * 第二套
     * =========================================================
     */
    private fun buildDemoEuiccInfoItemsType2() = buildList {

        val randomEid = generateRandomEid()

        val randomSas = generateRandomSas()

        /**
         * 200,000 ~ 400,000 B
         */
        val randomFreeNonVolatileMemory =
            Random.nextInt(
                200_000,
                400_001
            )

        /**
         * 约 10,000 B
         *
         * 范围：9,000 ~ 11,000 B
         */
        val randomFreeVolatileMemory =
            Random.nextInt(
                9_000,
                11_001
            )

        /**
         * 两个 EUICC CI：
         *
         * 这里只随机一次。
         * Sign CI 和 Verify CI 必须完全相同。
         */
        val randomCi = generateRandomCi()

        add(
            Item(
                "EID",
                randomEid,
                copiedToastResId = R.string.toast_eid_copied
            )
        )

        add(
            Item(
                "SAS Accreditation",
                randomSas
            )
        )

        add(
            Item(
                "Lowest Supported Version",
                "2.5.0"
            )
        )

        add(
            Item(
                "Free Non-volatile Memory",
                "${
                    String.format(
                        "%,d",
                        randomFreeNonVolatileMemory
                    )
                } B"
            )
        )

        add(
            Item(
                "Free Volatile Memory",
                "${
                    String.format(
                        "%,d",
                        randomFreeVolatileMemory
                    )
                } B"
            )
        )

        /**
         * 注意：
         * 这里故意使用空字符串。
         * 不添加空格、不添加未知值。
         */
        add(
            Item(
                "Default SM-DP+ Address",
                ""
            )
        )

        add(
            Item(
                "Root SM-DS Address",
                "testrootsmds.gsma.com"
            )
        )

        add(
            Item(
                "EUICC Sign CI",
                randomCi
            )
        )

        add(
            Item(
                "EUICC Verify CI",
                randomCi
            )
        )

        add(
            Item(
                "Profile Version",
                "2.2.0"
            )
        )

        add(
            Item(
                "Global Platform Version",
                "2.3.0"
            )
        )

        add(
            Item(
                "Firmware Version",
                "25.4.0"
            )
        )
    }

    /**
     * =========================================================
     * 第三套
     *
     * 第三套的随机项目全部使用第一套规则。
     * =========================================================
     */
    private fun buildDemoEuiccInfoItemsType3(): List<Item> {
    val yesText = "Yes"
    val nvramHint = "(for reference only)"
    val ciValue = "GSMA Live CI"

    val randomEid = generateRandomEid()
    val randomSas = generateRandomSas()
    val randomNvram = generateRandomNvram()
    val randomAtr = generateRandomAtr()

    return buildList {
        add(Item("Access Mode", "OpenMobile API (OMAPI)"))
        add(Item("Removable", yesText))
        add(Item("EID", randomEid, R.string.toast_eid_copied))
        add(Item("Manufacturer", "Beijing Watchdata(CN)"))
        add(Item("eUICC Profile version supported", "2.2.0"))
        add(Item("SGP.22 Version", "2.5.0"))
        add(Item("eUICC OS Version", "25.4.0"))
        add(Item("GlobalPlatform Version", "2.3.0"))
        add(Item("Protected Profile Version", "1.0.0"))
        add(Item("SAS Accreditation Number", randomSas))
        add(
            Item(
                "Free NVRAM (eSIM profile storage)",
                "$randomNvram $nvramHint"
            )
        )
        add(Item("Certificate Issuer (CI)", ciValue))
        add(Item("Answer To Reset (ATR)", randomAtr, R.string.toast_atr_copied))
    }
}

    /**
     * =========================================================
     * RecyclerView
     * =========================================================
     */

    inner class EuiccInfoViewHolder(root: View) : ViewHolder(root) {

    private val title: TextView =
        root.requireViewById(R.id.euicc_info_title)

    private val content: TextView =
        root.requireViewById(R.id.euicc_info_content)

    private var copiedToastResId: Int? = null

    init {
        root.setOnClickListener {

            if (copiedToastResId != null) {

                val label = title.text.toString()

                val clipboard =
                    root.context.getSystemService(
                        ClipboardManager::class.java
                    )

                clipboard?.setPrimaryClip(
                    ClipData.newPlainText(
                        label,
                        content.text
                    )
                )

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                    Toast.makeText(
                        root.context,
                        copiedToastResId!!,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun bind(item: Item) {

        copiedToastResId = item.copiedToastResId

        title.text = item.title

        content.text = item.content ?: "Unknown"
    }
}

    inner class EuiccInfoAdapter :
        RecyclerView.Adapter<EuiccInfoViewHolder>() {

        var euiccInfoItems: List<Item> = listOf()

            @SuppressLint("NotifyDataSetChanged")
            set(newVal) {
                field = newVal
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

        override fun getItemCount(): Int =
            euiccInfoItems.size

        override fun onBindViewHolder(
            holder: EuiccInfoViewHolder,
            position: Int
        ) {
            holder.bind(
                euiccInfoItems[position]
            )
        }
    }
}
