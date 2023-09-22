package com.infras.dauth.ui.fiat.transaction

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.Visibility
import coil.compose.rememberAsyncImagePainter
import com.infras.dauth.R
import com.infras.dauth.app.BaseActivity
import com.infras.dauth.app.BaseViewModel
import com.infras.dauth.entity.BuyAndSellPageEntity.TagsEntity
import com.infras.dauth.entity.BuyAndSellPageEntity.TokenInfo
import com.infras.dauth.entity.BuyAndSellPageEntity.TokenInfoOfTag
import com.infras.dauth.entity.BuyTokenPageInputEntity
import com.infras.dauth.entity.PagerEntity
import com.infras.dauth.ext.launch
import com.infras.dauth.manager.AccountManager
import com.infras.dauth.ui.fiat.transaction.test.BuyAndSellActivityMockData
import com.infras.dauth.ui.fiat.transaction.viewmodel.BuyAndSellViewModel
import com.infras.dauth.ui.fiat.transaction.widget.UnverifiedDialogFragment
import com.infras.dauth.ui.fiat.transaction.widget.VerifiedDialogFragment
import com.infras.dauth.ui.fiat.transaction.widget.VerifyFailedDialogFragment
import com.infras.dauth.util.ToastUtil
import com.infras.dauth.widget.compose.DComingSoonLayout
import com.infras.dauth.widget.compose.DFlowRow
import com.infras.dauth.widget.compose.TokenListItem
import com.infras.dauth.widget.compose.ViewPagerContent
import com.infras.dauth.widget.compose.ViewPagerIndicatorsWithUnderLine
import com.infras.dauth.widget.compose.constant.DColors
import com.infras.dauth.widget.compose.constant.DStrings
import com.infras.dauthsdk.login.model.DigitalCurrencyListRes

class BuyAndSellActivity : BaseActivity() {

    companion object {
        fun launch(context: Context) {
            context.launch(BuyAndSellActivity::class.java)
        }
    }

    private val viewModel: BuyAndSellViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainLayoutWithViewModel()
        }
        initViewModel()
        fetchData()
    }

    override fun getDefaultViewModel(): BaseViewModel {
        return viewModel
    }

    private fun initViewModel() {
        viewModel.kycBundledState.observe(this) {
            val authId = AccountManager.getAuthId()
            val fm = supportFragmentManager
            val state = it.kycState
            val isBound = it.isBound
            when (state) {
                null, 0 -> {
                    UnverifiedDialogFragment.newInstance(
                        authId, isBound
                    ).show(fm, UnverifiedDialogFragment.TAG)
                }

                1 -> {
                    VerifiedDialogFragment.newInstance(
                        authId
                    ).show(fm, VerifiedDialogFragment.TAG)
                }

                3 -> {
                    VerifyFailedDialogFragment.newInstance(
                        authId, isBound
                    ).show(fm, VerifyFailedDialogFragment.TAG)
                }
            }
        }
    }

    private fun fetchData() {
        viewModel.fetchCurrencyList()
        viewModel.fetchAccountDetail()
    }

    @Preview
    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    private fun TokenListView(
        modifier: Modifier = Modifier,
        tokens: List<TokenInfo> = BuyAndSellActivityMockData.tokenInfoList.first().tokenInfoList,
        onClickItem: (TokenInfo) -> Unit = {}
    ) {
        LazyColumn(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            itemsIndexed(tokens) { index, t ->
                ListItem(text = {
                    TokenListItem(t = t, onClickItem = onClickItem)
                })
            }
        }
    }

    @Preview
    @Composable
    private fun BuyTab(
        tokenInfoOfTag: List<TokenInfoOfTag> = BuyAndSellActivityMockData.tokenInfoList,
        onClickItem: (TokenInfo) -> Unit = {}
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            val (tvTokens, flTags, rvTokens) = createRefs()
            Text(text = "Tokens",
                modifier = Modifier.constrainAs(tvTokens) {
                    start.linkTo(parent.start, 20.dp)
                    top.linkTo(parent.top, 20.dp)
                }
            )
            var tagIndex by remember {
                mutableStateOf(0)
            }
            val entities = tokenInfoOfTag.mapIndexed { index, tokenInfoOfTag ->
                TagsEntity(
                    title = tokenInfoOfTag.tag.title,
                    selected = tagIndex == index,
                    onClick = {
                        tagIndex = index
                        tokenInfoOfTag.tag.onClick.invoke()
                    }
                )
            }
            DFlowRow.DFlowRow(
                modifier = Modifier
                    .constrainAs(flTags) {
                        start.linkTo(parent.start, 10.dp)
                        top.linkTo(tvTokens.bottom, 25.dp)
                    }
                    .padding(start = 10.dp),

                entities = entities,
            )

            val tokenListOfCurrentTag = tokenInfoOfTag[tagIndex].tokenInfoList
            TokenListView(modifier = Modifier
                .constrainAs(rvTokens) {
                    top.linkTo(flTags.bottom, 25.dp)
                    bottom.linkTo(parent.bottom)
                    height = Dimension.fillToConstraints
                }
                .fillMaxWidth(),
                tokens = tokenListOfCurrentTag,
                onClickItem = onClickItem
            )
        }
    }

    @Composable
    private fun SellTab() {
        DComingSoonLayout.ComingSoonLayout()
    }

    @Composable
    private fun MainLayoutWithViewModel(vm: BuyAndSellViewModel = viewModel) {
        val data by remember {
            vm.pageEntity
        }
        MainLayout(
            onClickCurrencyToggle = { vm.updateSelect(it) },
            pagerEntities = listOf(
                PagerEntity("Buy") {
                    BuyTab(data.buyTab) { tokenInfo ->
                        val fiatIndex = data.fiatSelectIndex ?: return@BuyTab
                        val verified = viewModel.kycBundledState.value?.kycState == 1
                        if (!verified) {
                            ToastUtil.show(this, "kyc not verified")
                            return@BuyTab
                        }
                        BuyTokenActivity.launch(
                            this, BuyTokenPageInputEntity(
                                crypto_info = tokenInfo.crypto,
                                fiat_info = data.fiatList,
                                selectedFiatIndex = fiatIndex,
                            )
                        )
                    }
                },
                PagerEntity("Sale") { SellTab() },
            ),
            fiatList = data.fiatList,
            fiatSelectIndex = data.fiatSelectIndex,
        )
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Preview
    @Composable
    private fun MainLayout(
        pagerEntities: List<PagerEntity> = listOf(
            PagerEntity("Buy") { BuyTab() },
            PagerEntity("Sale") { SellTab() },
        ),
        fiatList: List<DigitalCurrencyListRes.FiatInfo> = listOf(
            DigitalCurrencyListRes.FiatInfo("CNY")
        ),
        onClickCurrencyToggle: (Int) -> Unit = {},
        fiatSelectIndex: Int? = null,
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            val (cbCurrencySelector, ivBackArrow, pwCurrencyMenu) = createRefs()

            Column(
                modifier = Modifier
                    .background(Color.White)
                    .fillMaxHeight()
                    .fillMaxWidth()
            ) {
                val pageCount = pagerEntities.size
                val pagerState = rememberPagerState(
                    initialPage = 0,
                    pageCount = { pageCount }
                )
                ViewPagerIndicatorsWithUnderLine(
                    modifier = Modifier
                        .width(IntrinsicSize.Min)
                        .height(44.dp)
                        .align(Alignment.CenterHorizontally),
                    pagerState = pagerState,
                    pagerEntities = pagerEntities
                )
                ViewPagerContent(
                    pagerState = pagerState,
                    pagerEntities = pagerEntities,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, true)
                )
            }

            Image(
                painterResource(R.drawable.svg_ic_back_arrow),
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .constrainAs(ivBackArrow) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                    }
                    .clickable { finish() }
                    .padding(top = 12.dp, bottom = 12.dp, start = 10.dp, end = 10.dp)
                    .width(IntrinsicSize.Min)
                    .height(IntrinsicSize.Min)
            )

            var showPopup by remember {
                mutableStateOf(false)
            }
            var fiatText =
                if (fiatList.isEmpty()) "" else fiatList[fiatSelectIndex!!].fiatCode.orEmpty()

            Box(
                modifier = Modifier
                    .constrainAs(cbCurrencySelector) {
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                    }
                    .height(44.dp)
                    .clickable {
                        showPopup = !showPopup
                    }
                    .padding(start = 20.dp, end = 20.dp)
            ) {
                Text(
                    text = fiatText,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Box(
                modifier = Modifier.constrainAs(pwCurrencyMenu) {
                    top.linkTo(cbCurrencySelector.bottom)
                    end.linkTo(cbCurrencySelector.end)
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
                    fiatList.forEachIndexed { index, fiatList ->
                        val fiatCode = fiatList.fiatCode.orEmpty()
                        DropdownMenuItem(onClick = {
                            showPopup = false
                            fiatText = fiatCode
                            onClickCurrencyToggle.invoke(index)
                        }) {
                            Text(text = fiatCode)
                        }
                    }
                }
            }
        }
    }
}