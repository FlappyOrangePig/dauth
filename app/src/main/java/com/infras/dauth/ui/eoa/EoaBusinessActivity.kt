package com.infras.dauth.ui.eoa

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.infras.dauth.app.BaseActivity
import com.infras.dauth.app.BaseViewModel
import com.infras.dauth.ext.launch
import com.infras.dauth.util.ToastUtil
import kotlinx.coroutines.launch

class EoaBusinessActivity : BaseActivity() {

    companion object {
        fun launch(context: Context) {
            context.launch(EoaBusinessActivity::class.java)
        }
    }

    private val viewModel: EoaBusinessViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PageWithViewModel()
        }
    }

    override fun getDefaultViewModel(): BaseViewModel {
        return viewModel
    }

    @Composable
    fun PageWithViewModel(vm: EoaBusinessViewModel = viewModel) {
        Page(vm.textState.value,
            { viewModel.connect(this@EoaBusinessActivity) },
            { viewModel.getAddress() },
            { viewModel.personalSign(this@EoaBusinessActivity) },
            { viewModel.sendTransaction(this@EoaBusinessActivity) }
        )
    }

    @Preview
    @Composable
    fun Page(
        eoaAccountAddress: String = "我是EOA地址",
        onClickConnect: () -> Unit = {},
        onClickGetAddress: () -> Unit = {},
        onClickPersonalSign: () -> Unit = {},
        onClickSendTransaction: () -> Unit = {},
    ) {
        MaterialTheme(colors = lightColors()) {
            Column(verticalArrangement = Arrangement.Top) {
                EoaAccountAddress(eoaAccountAddress)
                SubmitButton("connect", onClickConnect)
                SubmitButton("getAddress", onClickGetAddress)
                SubmitButton("personalSign", onClickPersonalSign)
                SubmitButton("sendTransaction", onClickSendTransaction)
            }
        }
    }

    @Composable
    fun EoaAccountAddress(name: String) {
        Text(
            text = "address:$name",
            color = Color.Black,
            modifier = Modifier
                .size(Dp.Unspecified)
                .background(Color.Transparent, RectangleShape)
                .requiredWidth(Dp.Unspecified)
                .requiredHeight(Dp.Unspecified),
            fontSize = 12F.sp
        )
    }

    @Composable
    fun SubmitButton(name: String, onClick: () -> Unit) {
        Button(
            modifier = Modifier
                .requiredWidth(Dp.Unspecified)
                .requiredHeight(Dp.Unspecified),
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF4182FF),
                contentColor = Color.White
            )
        ) {
            Text(text = name)
        }
    }
}