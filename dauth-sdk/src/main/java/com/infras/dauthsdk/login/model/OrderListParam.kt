package com.infras.dauthsdk.login.model

/**
 * Order list param
 *
 * @property next_id 分页
 * @property page_size 分页最大条数（最大值100）
 * @property state 订单状态 CREATE_FAIL：订单创建失败,UNPAID：未⽀付,PAID：已⽀付,CANCEL：已取消,COMPLETED：已完成,APPEAL：申诉中,WITHDRAW_FAIL：提现失败,WITHDRAW_SUCCESS：提现成功
 * @constructor Create empty Order list param
 */
class OrderListParam(
    val next_id: Int,
    val page_size: Int,
    val state: String,
) : IAccessTokenRequest, IAuthorizationRequest