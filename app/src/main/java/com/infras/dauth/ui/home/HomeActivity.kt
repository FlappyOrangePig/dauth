package com.infras.dauth.ui.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.inputmethod.EditorInfo
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatEditText
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.viewModelScope
import coil.compose.rememberAsyncImagePainter
import com.infras.dauth.R
import com.infras.dauth.app.BaseActivity
import com.infras.dauth.entity.PagerEntity
import com.infras.dauth.entity.PersonalInfoEntity
import com.infras.dauth.ext.addressForShort
import com.infras.dauth.manager.sdk
import com.infras.dauth.ui.buy.BuyAndSellActivity
import com.infras.dauth.ui.main.MainActivity
import com.infras.dauth.util.DialogHelper
import com.infras.dauth.util.GasUtil
import com.infras.dauth.util.ToastUtil
import com.infras.dauth.widget.LoadingDialogFragment
import com.infras.dauth.widget.compose.DComingSoonLayout
import com.infras.dauth.widget.compose.DViewPager
import com.infras.dauthsdk.api.entity.DAuthResult
import kotlinx.coroutines.launch
import java.math.BigInteger


class HomeActivity : BaseActivity() {

    companion object {
        private const val COMING_SOON = "coming soon"

        fun launch(context: Context) {
            val intent = Intent(context, HomeActivity::class.java)
            context.startActivity(intent)
        }
    }

    private val viewModel: HomeViewModel by viewModels()
    private val loadingDialog = LoadingDialogFragment.newInstance()
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PageWithViewModel(viewModel)
        }
    }

    override fun onResume() {
        super.onResume()
        requestData()
        timer = object : CountDownTimer(5000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                //requestData()
            }
        }.start()
    }

    private fun releaseTimer() {
        timer?.cancel()
    }

    override fun onPause() {
        super.onPause()
        if (isFinishing) {
            releaseTimer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseTimer()
    }

    private fun requestData() {
        viewModel.fetch()
    }

    private fun copyToClipboard(text: String) {
        val clipboard: ClipboardManager =
            this.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", text)
        clipboard.setPrimaryClip(clip)
        ToastUtil.show(this, "copied")
    }

    private suspend fun onSend() {
        val a = this@HomeActivity
        val address = DialogHelper.suspendShowInputDialogMayHaveLeak(
            a,
            "Enter the transfer destination address",
        ) ?: return

        if (!Regex("^0x[0-9a-fA-F]{40}$").matches(address)) {
            ToastUtil.show(a, "input error")
            return
        }
        val wei = DialogHelper.suspendShowInputDialogMayHaveLeak(
            a,
            "Enter amount in wei",
            editTextHolder = {
                AppCompatEditText(a).also { it.inputType = EditorInfo.TYPE_CLASS_NUMBER }
            }
        ) ?: return

        val bigWei = kotlin.runCatching { BigInteger(wei) }.getOrNull()
        if (bigWei == null) {
            ToastUtil.show(a, "input error")
            return
        }

        loadingDialog.show(
            supportFragmentManager,
            LoadingDialogFragment.TAG
        )
        val result = viewModel.createUserOpAndEstimateGas(address, BigInteger(wei))
        loadingDialog.dismiss()
        if (result !is DAuthResult.Success) {
            ToastUtil.show(a, "create failure ${result.getError()}")
        } else {
            val data = result.data
            val message =
                "calc success\nverification cost：${GasUtil.getReadableGas(data.verificationCost)}\n" +
                        "call cost：${GasUtil.getReadableGas(data.callCost)}\nexecute?"
            val execute = DialogHelper.suspendShow2ButtonsDialogMayHaveLeak(
                a,
                message
            )
            if (!execute) {
                return
            }

            loadingDialog.show(supportFragmentManager, LoadingDialogFragment.TAG)
            val executeResult = sdk().execute(data.userOp)
            loadingDialog.dismiss()
            if (executeResult !is DAuthResult.Success) {
                ToastUtil.show(
                    a,
                    "exec failure, ${executeResult.getError()}"
                )
            } else {
                ToastUtil.show(
                    a,
                    "exec success, trHash=${executeResult.data.txHash}"
                )
                viewModel.fetchBalance()
            }
        }
    }

    @Composable
    fun PageWithViewModel(viewModel: HomeViewModel = this.viewModel) {
        val blockShowComingSoon = {
            ToastUtil.show(this, COMING_SOON)
        }
        val walletTab: @Composable () -> Unit = {
            WalletTab(
                viewModel.balance.value,
                viewModel.address.value.addressForShort(),
                onClickCopy = {
                    val address = viewModel.address.value
                    copyToClipboard(address)
                },
                onClickReceive = blockShowComingSoon,
                onClickBuy = {
                    BuyAndSellActivity.launch(this)
                },
                onClickSwap = blockShowComingSoon,
                onClickProperty = blockShowComingSoon,
                onClickSend = {
                    viewModel.viewModelScope.launch {
                        onSend()
                    }
                },
            )
        }
        val profileTab: @Composable () -> Unit = {
            ProfileTab(
                account = viewModel.account.value,
                avatarUrl = viewModel.avatarUrl.value,
                personalInfoList = viewModel.personalInfoList.value,
                onClickLogout = {
                    DialogHelper.show2ButtonsDialogMayHaveLeak(
                        this@HomeActivity,
                        "sign out?"
                    ) {
                        viewModel.logout(this@HomeActivity)
                    }
                },
                onClickDebug = { MainActivity.launch(this) }
            )
        }

        Page(
            listOf(
                PagerEntity("Explore") { ExploreTab() },
                PagerEntity("Wallets", walletTab),
                PagerEntity("Profile", profileTab),
            )
        )
    }

    @Composable
    fun ComingSoonLayout() = DComingSoonLayout.ComingSoonLayout()

    @Composable
    fun HomePageFunctionButton(title: String, onClick: () -> Unit) {
        Column(modifier = Modifier
            .clickable { onClick.invoke() }
            .padding(start = 17.dp, end = 17.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD9D9D9))
                    .size(35.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Text(
                text = title,
                color = Color.Black,
                fontSize = TextUnit(13F, TextUnitType.Sp),
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }

    @Composable
    fun Page(pagerEntities: List<PagerEntity>) {
        HomePageViewPager(pagerEntities = pagerEntities)
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun PersonalTab(personalInfoList: List<PersonalInfoEntity>) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            itemsIndexed(personalInfoList) { _, t ->
                ListItem(text = {
                    Text(
                        text = t.getText(),
                        modifier = Modifier.clickable {
                            t.getClipInfo()?.let { copyToClipboard(it) }
                        })
                })
            }
        }
    }

    @Composable
    fun ExploreTab() {
        ComingSoonLayout()
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Preview
    @Composable
    fun ProfileTab(
        account: String = "DAuth User",
        avatarUrl: String = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRpY-MZ8LFzxARaiMebgGUTfc8kQXkD0is5Lw&usqp=CAU",
        dauthId: String = "sdfasdfklasjdfl;asjdfk;ldjflkdjfa",
        email: String = "hello@163.com",
        phone: String = "+85-15555555555",
        personalInfoList: List<PersonalInfoEntity> = listOf(),
        pagerEntities: List<PagerEntity> = listOf(
            PagerEntity("Personal") {
                PersonalTab(
                    personalInfoList = personalInfoList
                )
            },
            PagerEntity("Socials") {
                ComingSoonLayout()
            },
            PagerEntity("Dapps") {
                ComingSoonLayout()
            },
        ),
        onClickLogout: () -> Unit = {},
        onClickDebug: () -> Unit = {},
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(Color.Transparent)
        ) {
            val (tvAccount, ivAvatar, rIndicators, vpTabs, ivSettings) = createRefs()

            Box(modifier = Modifier
                .constrainAs(ivSettings) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                }
                .combinedClickable(
                    onClick = onClickLogout,
                    onLongClick = onClickDebug
                )
                .padding(19.dp)
            ) {
                Image(
                    painterResource(R.drawable.svg_ic_settings),
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(15.dp)
                )
            }

            Box(
                modifier = Modifier
                    .constrainAs(ivAvatar) {
                        top.linkTo(parent.top, 35.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .size(45.dp),
            ) {
                val placeHolder = ColorPainter(Color.Gray)
                val painter: Painter = rememberAsyncImagePainter(
                    model = avatarUrl,
                    placeholder = placeHolder,
                    fallback = placeHolder,
                    error = placeHolder,
                )
                Image(
                    painter = painter,
                    contentDescription = "Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .clip(CircleShape)
                )
            }
            Text(
                text = account,
                color = Color.Black,
                fontSize = 10.sp,
                modifier = Modifier.constrainAs(tvAccount) {
                    top.linkTo(ivAvatar.bottom, 5.5.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            )

            val pageCount = pagerEntities.size
            val pagerState = rememberPagerState(
                initialPage = 0,
                pageCount = { pageCount }
            )
            Row(
                Modifier
                    .constrainAs(rIndicators) {
                        top.linkTo(tvAccount.bottom, 15.dp)
                    }
                    .height(35.dp)
                    .fillMaxWidth()
                    .background(Color.Transparent)
            ) {
                repeat(pageCount) { iteration ->
                    val color = if (pagerState.currentPage == iteration) Color.Red else Color.Black
                    val rememberScope = rememberCoroutineScope()
                    Box(modifier = Modifier
                        .fillMaxHeight()
                        .clickable {
                            rememberScope.launch {
                                pagerState.animateScrollToPage(
                                    iteration
                                )
                            }
                        }
                        .weight(1F, true)) {
                        Text(
                            text = pagerEntities.get(index = iteration).pagerTitle,
                            color = color,
                            fontSize = 20.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(vpTabs) {
                        top.linkTo(rIndicators.bottom)
                        bottom.linkTo(parent.bottom)
                        // 0dp等价的配置参考如下链接：
                        // https://stackoverflow.com/questions/64313409/equivalent-of-constraint-layout-0dp
                        height = Dimension.fillToConstraints
                    }
                    .background(Color.Transparent)
            ) { page ->
                // Our page content
                pagerEntities[page].createPager.invoke()
            }
        }
    }

    //@Preview
    @Composable
    fun WalletTab(
        balance: String = "$1929.54",
        address: String = "0x22B2....Daa0",
        onClickCopy: () -> Unit = {},
        onClickSend: () -> Unit = {},
        onClickReceive: () -> Unit = {},
        onClickBuy: () -> Unit = {},
        onClickSwap: () -> Unit = {},
        onClickProperty: () -> Unit = {},
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            val (tvWalletIndex, tvBalance, tvAddress, ivCopy, rButtons, ivProperty) = createRefs()
            Box(modifier = Modifier
                .constrainAs(ivProperty) {
                    end.linkTo(parent.end, 0.dp)
                    top.linkTo(parent.top, 0.dp)
                }
                .clickable { onClickProperty.invoke() }
                .padding(19.dp)
                .background(Color.Transparent))
            {
                Image(
                    painterResource(R.drawable.svg_ic_property),
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(15.dp)
                )
            }


            Text(
                text = "AA Wallet",
                color = Color.Black,
                fontSize = TextUnit(24F, TextUnitType.Sp),
                modifier = Modifier.constrainAs(tvWalletIndex) {
                    top.linkTo(parent.top, 67.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            )
            Text(
                text = balance,
                color = Color.Black,
                fontSize = TextUnit(30F, TextUnitType.Sp),
                modifier = Modifier.constrainAs(tvBalance) {
                    top.linkTo(tvWalletIndex.bottom, margin = 16.dp, goneMargin = 0.dp)
                    start.linkTo(tvWalletIndex.start)
                    end.linkTo(tvWalletIndex.end)
                }
            )
            Text(
                text = address,
                color = Color.Black,
                fontSize = 20.sp,
                modifier = Modifier
                    .constrainAs(tvAddress)
                    {
                        top.linkTo(tvBalance.bottom, margin = 24.dp, goneMargin = 0.dp)
                        start.linkTo(tvBalance.start)
                        end.linkTo(tvBalance.end)
                    }
                    .background(
                        Color(0xFFD9D9D9),
                        RoundedCornerShape(
                            50,
                            50,
                            50,
                            50
                        )
                    )
                    .clickable { onClickCopy.invoke() }
                    .padding(20.dp, 0.dp, 50.dp, 0.dp)
            )
            Box(modifier = Modifier
                .constrainAs(ivCopy) {
                    top.linkTo(tvAddress.top)
                    bottom.linkTo(tvAddress.bottom)
                    end.linkTo(tvAddress.end, 15.dp)
                }
                .background(Color.Transparent))
            {
                Image(
                    painterResource(R.drawable.svg_ic_copy),
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(10.dp)
                )
            }

            Row(modifier = Modifier
                .constrainAs(rButtons) {
                    top.linkTo(tvAddress.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .padding(top = 40.dp)
            ) {

                listOf(
                    "Send" to onClickSend,
                    "Receive" to onClickReceive,
                    "Buy" to onClickBuy,
                    "Swap" to onClickSwap,
                ).forEach {
                    HomePageFunctionButton(it.first, it.second)
                }
            }
        }
    }

    @Preview
    @Composable
    fun HomePageViewPager(
        pagerEntities: List<PagerEntity> = listOf(
            PagerEntity("Explorer") { ExploreTab() },
            PagerEntity("Wallet") { WalletTab() },
            PagerEntity("Profile") { ProfileTab() },
        )
    ) {
        DViewPager.BundledViewPager(
            initialPage = 1,
            indicatorAtTop = false,
            pagerEntities = pagerEntities
        )
    }
}