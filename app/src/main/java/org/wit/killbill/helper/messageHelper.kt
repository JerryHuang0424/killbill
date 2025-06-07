package org.wit.killbill.helper

class messageHelper: messageDeal {
    /**
     * *Check if it is the name of the target payment application package
     * *@ param packageName Application package name
     * *Is @ return the target payment application
     */
    override fun checkTargetPackageName(packageName: String): Boolean {
        return packageName in setOf("com.tencent.mm", "com.eg.android.AlipayGphone")
    }

    override fun checkPaymentTitle(title: String): Boolean {
        val paymentKeywords = listOf(
            "Payment","Transaction","Payment","Receipt",
            "Consumption","Billing","Deduction","Transfer",
            "Recharge","Settlement"
        )
        return paymentKeywords.any { keyword -> title.contains(keyword) }
    }

    override fun dealMessage(message: String): String?{
        val patterns = listOf(
            // Match formats such as "$ 15.80" and "$ 1.00"
            """[$](\d+\.\d{2})""".toRegex(),
            // Match formats such as "$ 1.00" and "$ 2.10"
            """[$](\d+\.\d{2})""".toRegex(),
            // Match formats such as' a payment of 1.00 yuan
            """A bill of [$](\d+\.\d{2})""".toRegex(),
            // Match looser amount formats (including 1 decimal place)
            """(\d+\.\d{1,2})""".toRegex()
        )

        for (pattern in patterns) {
            pattern.find(message)?.let {
                return it.groupValues[1] // Return the first capture group (amount portion)
            }
        }
        return null
    }
}