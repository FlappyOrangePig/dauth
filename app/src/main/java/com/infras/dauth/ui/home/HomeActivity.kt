package com.infras.dauth.ui.home

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.inputmethod.EditorInfo
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatEditText
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import coil.compose.rememberAsyncImagePainter
import com.infras.dauth.R
import com.infras.dauth.app.BaseActivity
import com.infras.dauth.entity.PagerEntity
import com.infras.dauth.entity.PersonalInfoEntity
import com.infras.dauth.ext.addressForShort
import com.infras.dauth.ext.handleByToast
import com.infras.dauth.ext.launch
import com.infras.dauth.manager.AccountManager
import com.infras.dauth.ui.fiat.transaction.BuyAndSellActivity
import com.infras.dauth.ui.fiat.transaction.OrdersActivity
import com.infras.dauth.ui.main.MainActivity
import com.infras.dauth.util.ClipBoardUtil
import com.infras.dauth.util.DialogHelper
import com.infras.dauth.util.GasUtil
import com.infras.dauth.util.ToastUtil
import com.infras.dauth.util.getEnv
import com.infras.dauth.widget.LoadingDialogFragment
import com.infras.dauth.widget.compose.DComingSoonLayout
import com.infras.dauth.widget.compose.TokenListItem
import com.infras.dauth.widget.compose.ViewPagerContent
import com.infras.dauth.widget.compose.ViewPagerIndicators
import com.infras.dauth.widget.compose.ViewPagerIndicatorsPacked
import com.infras.dauth.widget.compose.constant.DColors
import com.infras.dauth.widget.compose.constant.DStrings
import com.infras.dauthsdk.api.entity.DAuthResult
import com.infras.dauthsdk.login.model.SetPasswordParam
import kotlinx.coroutines.launch
import java.math.BigInteger


class HomeActivity : BaseActivity() {

    companion object {
        fun launch(context: Context) {
            context.launch(HomeActivity::class.java)
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
        ClipBoardUtil.copyToClipboard(this, text)
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
        loadingDialog.dismissAllowingStateLoss()
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
            val executeResult = AccountManager.sdk.execute(data.userOp)
            loadingDialog.dismissAllowingStateLoss()
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

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun PageWithViewModel(viewModel: HomeViewModel = this.viewModel) {
        val blockShowComingSoon = {
            ToastUtil.show(this, DStrings.COMING_SOON)
        }

        val copyAddressBlock = { copyToClipboard(viewModel.address.value) }

        val walletTab: @Composable () -> Unit = {
            WalletTab(
                viewModel.balance.value,
                viewModel.address.value.addressForShort(),
                onClickCopy = copyAddressBlock,
                onClickReceive = copyAddressBlock,
                onClickBuy = {
                    BuyAndSellActivity.launch(this)
                },
                onClickSwap = {
                    blockShowComingSoon.invoke()
                },
                onClickProperty = blockShowComingSoon,
                onClickSend = {
                    viewModel.viewModelScope.launch {
                        onSend()
                    }
                },
                pagerEntities = listOf(
                    PagerEntity("Tokens") {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                        ) {
                            itemsIndexed(viewModel.tokenInfoList.value) { _, t ->
                                ListItem(text = {
                                    TokenListItem(t = t, onClickItem = {})
                                })
                            }
                        }
                    },
                    PagerEntity("Collections") { ComingSoonLayout("no data") },
                    PagerEntity("Txs") { ComingSoonLayout("no data") },
                )
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
                        AccountManager.logout(this@HomeActivity)
                    }
                },
                onClickDebug = { MainActivity.launch(this) },
                onClickSetEmailPassword = {
                    DialogHelper.showInputDialogMayHaveLeak(this, "Enter Password") { password ->
                        lifecycleScope.launch {
                            loadingDialog.show(supportFragmentManager, LoadingDialogFragment.TAG)
                            val result = AccountManager.sdk.setPassword(SetPasswordParam().apply {
                                this.password = password
                            })
                            loadingDialog.dismissAllowingStateLoss()
                            result.handleByToast()
                        }
                    }
                }
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
    fun ComingSoonLayout(text: String? = null) = DComingSoonLayout.ComingSoonLayout(text)

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
                        fontSize = 14.sp,
                        maxLines = 1,
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
        onClickSetEmailPassword: () -> Unit = {}
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(Color.Transparent)
        ) {
            val (tvAccount, ivAvatar, rIndicators, vpTabs, ivSettings, pwSettingsMenu) = createRefs()

            val settingMenuItems = listOf(
                "Sign out" to {
                    onClickLogout.invoke()
                },
                "Set email password" to {
                    onClickSetEmailPassword.invoke()
                }
            )

            var showPopup by remember {
                mutableStateOf(false)
            }
            Box(modifier = Modifier
                .constrainAs(ivSettings) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                }
                .combinedClickable(
                    onClick = {
                        showPopup = !showPopup
                    },
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
                modifier = Modifier.constrainAs(pwSettingsMenu) {
                    top.linkTo(ivSettings.bottom)
                    end.linkTo(parent.end)
                }
            ) {
                DropdownMenu(
                    expanded = showPopup,
                    onDismissRequest = { showPopup = false },
                    properties = PopupProperties(
                        focusable = true,
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true
                    ),
                    modifier = Modifier
                        .width(IntrinsicSize.Min)
                        .height(IntrinsicSize.Min)
                ) {
                    settingMenuItems.forEachIndexed { _, item ->
                        DropdownMenuItem(onClick = {
                            showPopup = false
                            item.second.invoke()
                        }) {
                            Text(text = item.first)
                        }
                    }
                }
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
                val placeHolder = ColorPainter(DColors.GRAY)
                val painter: Painter = rememberAsyncImagePainter(
                    model = avatarUrl,
                    placeholder = placeHolder,
                    fallback = placeHolder,
                    error = placeHolder,
                )
                Image(
                    painter = painter,
                    contentDescription = DStrings.IMAGE_DEFAULT_CONTENT_DESCRIPTION,
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

    @OptIn(ExperimentalFoundationApi::class)
    @Preview
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
        pagerEntities: List<PagerEntity> = listOf(
            PagerEntity("Tokens") { ComingSoonLayout() },
            PagerEntity("Collections") { ComingSoonLayout() },
            PagerEntity("Txs") { ComingSoonLayout() },
        )
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            val (
                tvChains,
                tvWalletIndex,
                tvBalance,
                tvAddress,
                ivCopy,
                rButtons,
                ivProperty,
                vSplit,
                vpContent,
            ) = createRefs()

            Box(
                modifier = Modifier
                    .constrainAs(tvChains) {
                        start.linkTo(parent.start, 13.dp)
                        top.linkTo(parent.top, 0.dp)
                        width = Dimension.wrapContent
                    }
                    .height(44.dp)
                    .padding(top = 9.dp, bottom = 9.dp)
                    .border(1.dp, Color.Black, shape = RoundedCornerShape(2.5.dp))
                    .clickable {
                        ToastUtil.show(
                            this@HomeActivity,
                            "not supported"
                        )
                    }
                    .padding(start = 8.dp, end = 8.dp)
            ) {
                Text(
                    text = "${getEnv().chainName} ◢",
                    fontSize = 10.sp,
                    modifier = Modifier
                        .width(IntrinsicSize.Max)
                        .height(IntrinsicSize.Min)
                        .align(Alignment.Center),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .constrainAs(ivProperty) {
                        end.linkTo(parent.end, 0.dp)
                        top.linkTo(parent.top, 0.dp)
                    }
                    .width(44.dp)
                    .height(44.dp)
                    .clickable { onClickProperty.invoke() }
                    .background(Color.Transparent))
            {
                Image(
                    painterResource(R.drawable.svg_ic_property),
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(15.dp)
                        .align(Alignment.Center)
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
                fontWeight = FontWeight.Bold,
                modifier = Modifier.constrainAs(tvBalance) {
                    top.linkTo(tvWalletIndex.bottom, margin = 8.dp, goneMargin = 0.dp)
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
                        top.linkTo(tvBalance.bottom, margin = 12.dp, goneMargin = 0.dp)
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
                .padding(top = 25.dp)
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

            Box(
                modifier = Modifier
                    .constrainAs(vSplit) {
                        top.linkTo(rButtons.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    }
                    .padding(top = 25.dp)
                    .height(0.5.dp)
                    .padding(
                        start = 15.dp, end = 15.dp
                    )
                    .background(Color.Gray)
            )

            val pageCount = pagerEntities.size
            val pagerState = rememberPagerState(
                initialPage = 0,
                pageCount = { pageCount }
            )
            Column(modifier = Modifier
                .constrainAs(vpContent) {
                    top.linkTo(vSplit.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.matchParent
                    height = Dimension.fillToConstraints
                }
                .height(IntrinsicSize.Min)) {
                ViewPagerIndicatorsPacked(
                    modifier = Modifier
                        .padding(top = 15.dp, start = 20.dp)
                        .width(IntrinsicSize.Min)
                        .height(IntrinsicSize.Min),
                    pagerState = pagerState,
                    pagerEntities = pagerEntities,
                )
                ViewPagerContent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp) // 不加就崩溃，为什么？
                        .weight(1F, true),
                    pagerState = pagerState, pagerEntities = pagerEntities
                )
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    //@Preview
    @Composable
    fun HomePageViewPager(
        pagerEntities: List<PagerEntity> = listOf(
            PagerEntity("Explorer") { ExploreTab() },
            PagerEntity("Wallet") { WalletTab() },
            PagerEntity("Profile") { ProfileTab() },
        )
    ) {

        Column(
            modifier = Modifier
                .background(Color.White)
                .fillMaxHeight()
                .fillMaxWidth()
        ) {
            val pageCount = pagerEntities.size
            val pagerState = rememberPagerState(
                initialPage = 1,
                pageCount = { pageCount }
            )

            ViewPagerContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, true),
                pagerState = pagerState,
                pagerEntities = pagerEntities,
                userScrollEnabled = false
            )
            ViewPagerIndicators(pagerState = pagerState, pagerEntities = pagerEntities)
        }
    }
}