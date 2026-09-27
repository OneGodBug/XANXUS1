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
        
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_euicc_info)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportActionBar?.title = localized(
        "eSIM Info",
        "eSIM 详情",
        "eSIM 詳情",
        "eSIM 情報"
)

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

    /*
     * 根据当前 App 语言返回对应文字。
     *
     * 支持：
     * English
     * 简体中文
     * 繁體中文
     * 日本語
     */
    private fun localized(
        english: String,
        simplifiedChinese: String,
        traditionalChinese: String,
        japanese: String
    ): String {
        val locale = resources.configuration.locales[0]

        return when (locale.language.lowercase()) {
            "ja" -> japanese

            "zh" -> {
                when (locale.country.uppercase()) {
                    "TW", "HK", "MO" -> traditionalChinese
                    else -> simplifiedChinese
                }
            }

            else -> english
        }
    }

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

    private fun generateRandomSas(): String {
        return buildString {
            append("WD-BG-UP-")

            repeat(4) {
                append(Random.nextInt(0, 10))
            }
        }
    }

    private fun generateRandomNvram(): String {
        val value = Random.nextInt(1, 50_000) / 100.0

        return String.format(
            "%.2f KiB",
            value
        )
    }

    private fun generateRandomAtr(): String {
        val hex = "0123456789ABCDEF"

        return buildString {
            repeat(44) {
                append(hex[Random.nextInt(hex.length)])
            }
        }
    }

    private fun generateRandomCi(): String {
        val hex = "0123456789ABCDEF"

        return buildString {
            repeat(16) {
                append(hex[Random.nextInt(hex.length)])
            }
        }
    }

    /*
     * ============================
     * Set 1
     * ============================
     */
    private fun buildDemoEuiccInfoItemsType1(): List<Item> {

        val randomEid = generateRandomEid()
        val randomSas = generateRandomSas()
        val randomNvram = generateRandomNvram()
        val randomAtr = generateRandomAtr()

        val accessModeTitle = localized(
            "Access Mode",
            "访问模式",
            "存取模式",
            "アクセスモード"
        )

        val removableTitle = localized(
            "Removable",
            "可移除",
            "可移除",
            "取り外し可能"
        )

        val eidTitle = localized(
            "EID",
            "EID",
            "EID",
            "EID"
        )

        val sgp22Title = localized(
            "SGP.22 Version",
            "SGP.22 版本",
            "SGP.22 版本",
            "SGP.22 バージョン"
        )

        val sasTitle = localized(
            "SAS Accreditation Number",
            "SAS 认证编号",
            "SAS 認證編號",
            "SAS 認定番号"
        )

        val nvramTitle = localized(
            "Free NVRAM (eSIM profile storage)",
            "可用 NVRAM（eSIM 配置文件存储空间）",
            "可用 NVRAM（eSIM 設定檔儲存空間）",
            "空き NVRAM（eSIM プロファイル保存領域）"
        )

        val nvramHint = localized(
            "(for reference only)",
            "（仅供参考）",
            "（僅供參考）",
            "（参考値）"
        )

        val ciTitle = localized(
            "Certificate Issuer (CI)",
            "证书颁发者（CI）",
            "憑證發行者（CI）",
            "証明書発行者（CI）"
        )

        val ciValue = "GSMA Live CI"

        val atrTitle = localized(
            "Answer To Reset (ATR)",
            "复位应答（ATR）",
            "重置應答（ATR）",
            "Answer To Reset (ATR)"
        )

        return listOf(
            Item(
                accessModeTitle,
                "OpenMobile API (OMAPI)"
            ),

            Item(
                removableTitle,
                localized(
                    "Yes",
                    "是",
                    "是",
                    "はい"
                )
            ),

            Item(
                eidTitle,
                randomEid,
                R.string.toast_eid_copied
            ),

            Item(
                sgp22Title,
                "2.5.0"
            ),

            Item(
                sasTitle,
                randomSas
            ),

            Item(
                nvramTitle,
                "$randomNvram $nvramHint"
            ),

            Item(
                ciTitle,
                ciValue
            ),

            Item(
                atrTitle,
                randomAtr,
                R.string.toast_atr_copied
            )
        )
    }

    /*
     * ============================
     * Set 2
     * ============================
     */
    private fun buildDemoEuiccInfoItemsType2(): List<Item> {

        val randomEid = generateRandomEid()
        val randomSas = generateRandomSas()

        val randomFreeNonVolatileMemory =
            Random.nextInt(200_000, 400_001)

        val randomFreeVolatileMemory =
            Random.nextInt(9_000, 11_001)

        /*
         * 同一次刷新中：
         * EUICC Sign CI
         * EUICC Verify CI
         *
         * 使用完全相同的随机值。
         */
        val randomCi = generateRandomCi()

        val eidTitle = localized(
            "EID",
            "EID",
            "EID",
            "EID"
        )

        val sasTitle = localized(
            "SAS Accreditation Number",
            "SAS 认证编号",
            "SAS 認證編號",
            "SAS 認定番号"
        )

        val lowestVersionTitle = localized(
            "Lowest Supported Version",
            "最低支持版本",
            "最低支援版本",
            "最低対応バージョン"
        )

        val freeNonVolatileTitle = localized(
            "Free Non-volatile Memory",
            "可用非易失性内存",
            "可用非揮發性記憶體",
            "空き不揮発性メモリ"
        )

        val freeVolatileTitle = localized(
            "Free Volatile Memory",
            "可用易失性内存",
            "可用揮發性記憶體",
            "空き揮発性メモリ"
        )

        val defaultSmdpTitle = localized(
            "Default SM-DP+ Address",
            "默认 SM-DP+ 地址",
            "預設 SM-DP+ 位址",
            "デフォルト SM-DP+ アドレス"
        )

        val rootSmdsTitle = localized(
            "Root SM-DS Address",
            "根 SM-DS 地址",
            "根 SM-DS 位址",
            "ルート SM-DS アドレス"
        )

        val signCiTitle = localized(
            "EUICC Sign CI",
            "eUICC 签名 CI",
            "eUICC 簽章 CI",
            "eUICC 署名 CI"
        )

        val verifyCiTitle = localized(
            "EUICC Verify CI",
            "eUICC 验证 CI",
            "eUICC 驗證 CI",
            "eUICC 検証 CI"
        )

        val profileVersionTitle = localized(
            "Profile Version",
            "配置文件版本",
            "設定檔版本",
            "プロファイルバージョン"
        )

        val globalPlatformTitle = localized(
            "Global Platform Version",
            "Global Platform 版本",
            "Global Platform 版本",
            "Global Platform バージョン"
        )

        val firmwareTitle = localized(
            "Firmware Version",
            "固件版本",
            "韌體版本",
            "ファームウェアバージョン"
        )

        return listOf(

            Item(
                eidTitle,
                randomEid,
                R.string.toast_eid_copied
            ),

            Item(
                sasTitle,
                randomSas
            ),

            Item(
                lowestVersionTitle,
                "2.5.0"
            ),

            Item(
                freeNonVolatileTitle,
                "${String.format("%,d", randomFreeNonVolatileMemory)} B"
            ),

            Item(
                freeVolatileTitle,
                "${String.format("%,d", randomFreeVolatileMemory)} B"
            ),

            /*
             * 必须是真正的空字符串。
             */
            Item(
                defaultSmdpTitle,
                ""
            ),

            Item(
                rootSmdsTitle,
                "testrootsmds.gsma.com"
            ),

            Item(
                signCiTitle,
                randomCi
            ),

            Item(
                verifyCiTitle,
                randomCi
            ),

            Item(
                profileVersionTitle,
                "2.2.0"
            ),

            Item(
                globalPlatformTitle,
                "2.3.0"
            ),

            Item(
                firmwareTitle,
                "25.4.0"
            )
        )
    }

    /*
     * ============================
     * Set 3
     * ============================
     */
    private fun buildDemoEuiccInfoItemsType3(): List<Item> {

        val randomEid = generateRandomEid()
        val randomSas = generateRandomSas()
        val randomNvram = generateRandomNvram()
        val randomAtr = generateRandomAtr()

        val accessModeTitle = localized(
            "Access Mode",
            "访问模式",
            "存取模式",
            "アクセスモード"
        )

        val removableTitle = localized(
            "Removable",
            "可移除",
            "可移除",
            "取り外し可能"
        )

        val eidTitle = localized(
            "EID",
            "EID",
            "EID",
            "EID"
        )

        val manufacturerTitle = localized(
            "Manufacturer",
            "制造商",
            "製造商",
            "製造元"
        )

        val profileVersionSupportedTitle = localized(
            "eUICC Profile Version Supported",
            "支持的 eUICC 配置文件版本",
            "支援的 eUICC 設定檔版本",
            "対応 eUICC プロファイルバージョン"
        )

        val sgp22Title = localized(
            "SGP.22 Version",
            "SGP.22 版本",
            "SGP.22 版本",
            "SGP.22 バージョン"
        )

        val osVersionTitle = localized(
            "eUICC OS Version",
            "eUICC OS 版本",
            "eUICC OS 版本",
            "eUICC OS バージョン"
        )

        val globalPlatformTitle = localized(
            "GlobalPlatform Version",
            "GlobalPlatform 版本",
            "GlobalPlatform 版本",
            "GlobalPlatform バージョン"
        )

        val protectedProfileTitle = localized(
            "Protected Profile Version",
            "受保护配置文件版本",
            "受保護設定檔版本",
            "保護プロファイルバージョン"
        )

        val sasTitle = localized(
            "SAS Accreditation Number",
            "SAS 认证编号",
            "SAS 認證編號",
            "SAS 認定番号"
        )

        val nvramTitle = localized(
            "Free NVRAM (eSIM profile storage)",
            "可用 NVRAM（eSIM 配置文件存储空间）",
            "可用 NVRAM（eSIM 設定檔儲存空間）",
            "空き NVRAM（eSIM プロファイル保存領域）"
        )

        val nvramHint = localized(
            "(for reference only)",
            "（仅供参考）",
            "（僅供參考）",
            "（参考値）"
        )

        val ciTitle = localized(
            "Certificate Issuer (CI)",
            "证书颁发者（CI）",
            "憑證發行者（CI）",
            "証明書発行者（CI）"
        )

        val atrTitle = localized(
            "Answer To Reset (ATR)",
            "复位应答（ATR）",
            "重置應答（ATR）",
            "Answer To Reset (ATR)"
        )

        return listOf(

            Item(
                accessModeTitle,
                "OpenMobile API (OMAPI)"
            ),

            Item(
                removableTitle,
                localized(
                    "Yes",
                    "是",
                    "是",
                    "はい"
                )
            ),

            Item(
                eidTitle,
                randomEid,
                R.string.toast_eid_copied
            ),

            Item(
                manufacturerTitle,
                "Beijing Watchdata(CN)"
            ),

            Item(
                profileVersionSupportedTitle,
                "2.2.0"
            ),

            Item(
                sgp22Title,
                "2.5.0"
            ),

            Item(
                osVersionTitle,
                "25.4.0"
            ),

            Item(
                globalPlatformTitle,
                "2.3.0"
            ),

            Item(
                protectedProfileTitle,
                "1.0.0"
            ),

            Item(
                sasTitle,
                randomSas
            ),

            Item(
                nvramTitle,
                "$randomNvram $nvramHint"
            ),

            Item(
                ciTitle,
                "GSMA Live CI"
            ),

            Item(
                atrTitle,
                randomAtr,
                R.string.toast_atr_copied
            )
        )
    }

    inner class EuiccInfoViewHolder(root: View) :
        RecyclerView.ViewHolder(root) {

        private val title: TextView =
            root.findViewById(R.id.euicc_info_title)

        private val content: TextView =
            root.findViewById(R.id.euicc_info_content)

        private var copiedToastResId: Int? = null

        init {
            root.setOnClickListener {

                val toastResId =
                    copiedToastResId ?: return@setOnClickListener

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

            copiedToastResId =
                item.copiedToastResId

            title.text =
                item.title

            content.text =
                item.content
        }
    }

    inner class EuiccInfoAdapter :
        RecyclerView.Adapter<EuiccInfoViewHolder>() {

        var euiccInfoItems: List<Item> =
            emptyList()

            @SuppressLint("NotifyDataSetChanged")
            set(value) {

                field = value

                notifyDataSetChanged()
            }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): EuiccInfoViewHolder {

            val root =
                LayoutInflater.from(parent.context)
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
