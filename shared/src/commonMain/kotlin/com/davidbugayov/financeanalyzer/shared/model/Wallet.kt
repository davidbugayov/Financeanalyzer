package com.davidbugayov.financeanalyzer.shared.model

enum class WalletType { CASH, CARD, SAVINGS, INVEST }

data class Wallet(
    val id: String,
    val name: String,
    val type: WalletType = WalletType.CASH,
    val balance: Money = Money.zero(),
    val limit: Money = Money.zero(),
    val spent: Money = Money.zero(),
)


