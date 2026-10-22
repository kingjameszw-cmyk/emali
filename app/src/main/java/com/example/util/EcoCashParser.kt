package com.example.util

import com.example.data.local.entities.TransactionEntity
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.regex.Pattern

data class ParsedEcoCashResult(
    val transactions: List<TransactionEntity>,
    val duplicateCount: Int = 0,
    val errors: List<String> = emptyList()
)

object EcoCashParser {

    /**
     * Parse unstructured raw text (SMS or copy-pasted statement lines)
     */
    fun parseRawText(rawText: String, accountId: Long = 1): List<TransactionEntity> {
        val lines = rawText.split("\n", ";").map { it.trim() }.filter { it.isNotEmpty() }
        val parsedList = mutableListOf<TransactionEntity>()

        for (line in lines) {
            val parsed = parseSingleLine(line, accountId)
            if (parsed != null) {
                parsedList.add(parsed)
            }
        }
        return parsedList
    }

    private fun parseSingleLine(line: String, accountId: Long): TransactionEntity? {
        val clean = line.trim()
        if (clean.length < 15) return null

        // 1. Try CSV match if line contains commas
        if (clean.contains(",")) {
            val csvParsed = parseCsvLine(clean, accountId)
            if (csvParsed != null) return csvParsed
        }

        // 2. Pattern matching for EcoCash SMS
        // Look for Trans ID or Reference
        val refPattern = Pattern.compile("(?i)(?:Trans(?:action)?\\s*ID|Ref(?:erence)?|Approval\\s*Code)[:\\s]+([A-Z0-9.]+)")
        val refMatcher = refPattern.matcher(clean)
        val rawRef = if (refMatcher.find()) refMatcher.group(1).trim() else "EC-" + System.currentTimeMillis().toString().takeLast(8)
        val reference = rawRef.trimEnd('.', ',', ';', ':')

        // Amount detection (e.g., USD 25.00, $25.00, 25.00 USD, ZiG 150)
        val amountPattern = Pattern.compile("(?i)(?:USD|\\$|ZWG|ZiG)?\\s*([0-9]+(?:\\.[0-9]{1,2})?)\\s*(?:USD|ZWG|ZiG)?")
        val amount = extractAmount(clean) ?: return null

        // Fee detection (e.g. Fee: USD 0.50 or Fee: 0.35)
        val feePattern = Pattern.compile("(?i)Fee[:\\s]+(?:USD|\\$|ZWG|ZiG)?\\s*([0-9]+(?:\\.[0-9]{1,2})?)")
        val feeMatcher = feePattern.matcher(clean)
        val fee = if (feeMatcher.find()) {
            feeMatcher.group(1).toDoubleOrNull() ?: 0.0
        } else {
            // EcoCash standard IMTT estimation if fee not explicitly stated: ~2%
            if (clean.contains("sent", ignoreCase = true) || clean.contains("paid", ignoreCase = true)) {
                (amount * 0.02).coerceAtMost(5.0)
            } else 0.0
        }

        // Determine transaction type
        val isIncome = clean.contains("received", ignoreCase = true) ||
                clean.contains("cash in", ignoreCase = true) ||
                clean.contains("credited", ignoreCase = true)

        val type = if (isIncome) "INCOME" else "EXPENSE"

        // Recipient / Sender detection
        val recipientOrSender = extractRecipientOrSender(clean)
        val category = autoCategorize(clean, recipientOrSender)

        return TransactionEntity(
            accountId = accountId,
            type = type,
            amount = amount,
            fee = fee,
            totalDeducted = if (type == "EXPENSE") amount + fee else amount,
            currency = if (clean.contains("ZWG", ignoreCase = true) || clean.contains("ZiG", ignoreCase = true)) "ZWG" else "USD",
            category = category,
            recipientOrSender = recipientOrSender,
            reference = reference,
            description = clean.take(120),
            dateMillis = System.currentTimeMillis(),
            status = "COMPLETED",
            rawStatementLine = clean
        )
    }

    private fun extractAmount(text: String): Double? {
        val regex = Regex("""(?i)(?:sent|paid|transfer|received|out|bill|amount|usd|\$)\s*[:]?\s*(?:usd|\$)?\s*([0-9]+(?:\.[0-9]{1,2})?)""")
        val match = regex.find(text)
        if (match != null) {
            return match.groupValues[1].toDoubleOrNull()
        }
        // Fallback: any standalone decimal
        val fallback = Regex("""\b([0-9]+\.[0-9]{2})\b""").find(text)
        return fallback?.groupValues?.get(1)?.toDoubleOrNull()
    }

    private fun extractRecipientOrSender(text: String): String {
        // e.g., "sent to INNSCOR AFRICA LTD", "from Simba Moyo"
        val toMatch = Regex("""(?i)(?:sent to|paid to|to)\s+([A-Za-z0-9\s&.'-]+?)(?:\.|\s+Fee|\s+Trans|\s+Ref|$)""").find(text)
        if (toMatch != null) {
            val res = toMatch.groupValues[1].trim()
            if (res.isNotBlank()) return res
        }
        val fromMatch = Regex("""(?i)(?:from)\s+([A-Za-z0-9\s&.'-]+?)(?:\.|\s+Trans|\s+Ref|\s*\(|$)""").find(text)
        if (fromMatch != null) {
            val res = fromMatch.groupValues[1].trim()
            if (res.isNotBlank()) return res
        }
        return "EcoCash Merchant/User"
    }

    fun autoCategorize(text: String, recipient: String): String {
        val combined = "$text $recipient".lowercase(Locale.ROOT)
        return when {
            combined.contains("zetdc") || combined.contains("zesa") || combined.contains("electricity") || combined.contains("power") -> "Electricity"
            combined.contains("innscor") || combined.contains("chicken inn") || combined.contains("pizza inn") ||
                    combined.contains("bakers inn") || combined.contains("steers") || combined.contains("nandos") ||
                    combined.contains("restaurant") || combined.contains("cafe") || combined.contains("food") -> "Food & Dining"
            combined.contains("airtime") || combined.contains("data") || combined.contains("econet") || combined.contains("netone") || combined.contains("telecel") -> "Airtime & Data"
            combined.contains("spar") || combined.contains("ok zimbabwe") || combined.contains("tm pick n pay") ||
                    combined.contains("bon marche") || combined.contains("groceries") || combined.contains("supermarket") -> "Groceries"
            combined.contains("total") || combined.contains("zuva") || combined.contains("puma") || combined.contains("fuel") ||
                    combined.contains("diesel") || combined.contains("petrol") || combined.contains("transport") || combined.contains("kombi") -> "Transport & Fuel"
            combined.contains("school") || combined.contains("college") || combined.contains("university") || combined.contains("tuition") || combined.contains("fees") -> "School Fees"
            combined.contains("pharmacy") || combined.contains("clinic") || combined.contains("hospital") || combined.contains("doctor") || combined.contains("med") -> "Healthcare"
            combined.contains("rent") || combined.contains("accommodation") || combined.contains("rates") || combined.contains("city of harare") -> "Rent & Housing"
            combined.contains("c-trade") || combined.contains("ctrade") || combined.contains("shares") || combined.contains("vfex") || combined.contains("zse") -> "Investments"
            combined.contains("savings") || combined.contains("mukando") || combined.contains("fund") -> "Savings"
            combined.contains("salary") || combined.contains("wages") || combined.contains("payroll") -> "Salary"
            combined.contains("poultry") || combined.contains("farm") || combined.contains("chicks") || combined.contains("feed") -> "Business Expense"
            else -> "Shopping"
        }
    }

    private fun parseCsvLine(line: String, accountId: Long): TransactionEntity? {
        val parts = line.split(",").map { it.trim().removeSurrounding("\"") }
        if (parts.size < 4) return null

        // Ignore header line
        if (parts[0].equals("date", ignoreCase = true) || parts[1].equals("reference", ignoreCase = true)) {
            return null
        }

        // Expect: Date, Reference, Description, Type, Amount, Fee, Recipient, Category
        // Or simplified: Reference, Amount, Recipient, Description
        val ref = parts.getOrNull(1)?.takeIf { it.isNotBlank() } ?: parts[0]
        val amount = parts.getOrNull(4)?.toDoubleOrNull()
            ?: parts.getOrNull(1)?.toDoubleOrNull()
            ?: parts.firstNotNullOfOrNull { it.toDoubleOrNull() }
            ?: return null

        val fee = parts.getOrNull(5)?.toDoubleOrNull() ?: 0.0
        val type = if (parts.any { it.equals("INCOME", true) || it.equals("RECEIVED", true) }) "INCOME" else "EXPENSE"
        val recipient = parts.getOrNull(6) ?: parts.getOrNull(2) ?: "Merchant"
        val category = parts.getOrNull(7) ?: autoCategorize(line, recipient)

        return TransactionEntity(
            accountId = accountId,
            type = type,
            amount = amount,
            fee = fee,
            totalDeducted = if (type == "EXPENSE") amount + fee else amount,
            currency = "USD",
            category = category,
            recipientOrSender = recipient,
            reference = ref,
            description = parts.getOrNull(2) ?: line.take(80),
            dateMillis = System.currentTimeMillis(),
            status = "COMPLETED",
            rawStatementLine = line
        )
    }
}
