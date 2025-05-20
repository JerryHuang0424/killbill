package org.wit.killbill.messageDeal

class messageHelper: messageDeal {

    /**
     * 检查是否为目标支付应用包名
     * @param packageName 应用包名
     * @return 是否为目标支付应用
     */
    override fun checkTargetPackageName(packageName: String): Boolean {
        return packageName in setOf("com.tencent.mm", "com.eg.android.AlipayGphone")
    }

    override fun checkPaymentTitle(title: String): Boolean {
        val paymentKeywords = listOf(
            "支付", "交易", "付款", "收款",
            "消费", "账单", "扣款", "转账",
            "充值", "结算"
        )
        return paymentKeywords.any { keyword -> title.contains(keyword) }
    }

    override fun dealMessage(message: String): String?{
        val patterns = listOf(
            // 匹配 "￥15.80"、"¥1.00" 等格式
            """[¥￥](\d+\.\d{2})""".toRegex(),
            // 匹配 "1.00元"、"2.10元" 等格式
            """(\d+\.\d{2})元""".toRegex(),
            // 匹配 "一笔1.00元的支出" 等格式
            """一笔(\d+\.\d{2})元的""".toRegex(),
            // 匹配更宽松的金额格式（含1位小数）
            """(\d+\.\d{1,2})""".toRegex()
        )

        for (pattern in patterns) {
            pattern.find(message)?.let {
                return it.groupValues[1] // 返回第一个捕获组（金额部分）
            }
        }
        return null
    }
}