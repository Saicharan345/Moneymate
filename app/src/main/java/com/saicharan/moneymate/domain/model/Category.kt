package com.saicharan.moneymate.domain.model

enum class TransactionType(val label: String) {
    INCOME("Income"),
    EXPENSE("Expense");

    companion object {
        fun fromName(name: String): TransactionType? = entries.firstOrNull { it.name == name }
    }
}

/** Fixed category set. [colorArgb] is a plain ARGB value so the domain layer stays UI-framework free. */
enum class Category(val label: String, val type: TransactionType, val colorArgb: Long) {
    FOOD("Food & Dining", TransactionType.EXPENSE, 0xFFF59E0B),
    TRANSPORT("Transport", TransactionType.EXPENSE, 0xFF3B82F6),
    SHOPPING("Shopping", TransactionType.EXPENSE, 0xFFEC4899),
    BILLS("Bills & Utilities", TransactionType.EXPENSE, 0xFF8B5CF6),
    ENTERTAINMENT("Entertainment", TransactionType.EXPENSE, 0xFF14B8A6),
    HEALTH("Health", TransactionType.EXPENSE, 0xFFEF4444),
    EDUCATION("Education", TransactionType.EXPENSE, 0xFF6366F1),
    OTHER_EXPENSE("Other", TransactionType.EXPENSE, 0xFF64748B),

    SALARY("Salary", TransactionType.INCOME, 0xFF12A150),
    FREELANCE("Freelance", TransactionType.INCOME, 0xFF0EA5E9),
    INVESTMENTS("Investments", TransactionType.INCOME, 0xFF84CC16),
    OTHER_INCOME("Other", TransactionType.INCOME, 0xFF64748B);

    companion object {
        fun forType(type: TransactionType): List<Category> = entries.filter { it.type == type }
        fun fromName(name: String): Category? = entries.firstOrNull { it.name == name }
    }
}
