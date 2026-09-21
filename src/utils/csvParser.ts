import { Transaction, TransactionType } from '../types';

export function exportTransactionsToCSV(transactions: Transaction[]): string {
  const headers = ['ID', 'Дата', 'Тип', 'Сумма', 'Категория', 'Подкатегория', 'Кошелек', 'Заметка'];
  const rows = transactions.map((t) => [
    t.id,
    t.date,
    t.type === 'expense' ? 'Расход' : t.type === 'income' ? 'Доход' : 'Перевод',
    t.amount.toString(),
    `"${(t.category || '').replace(/"/g, '""')}"`,
    `"${(t.subcategory || '').replace(/"/g, '""')}"`,
    t.walletId,
    `"${(t.note || '').replace(/"/g, '""')}"`,
  ]);

  return [headers.join(';'), ...rows.map((r) => r.join(';'))].join('\n');
}

export function parseBankStatement(
  content: string,
  bank: 'tinkoff' | 'sberbank' | 'alfabank' | 'generic',
  defaultWalletId: string
): { transactions: Transaction[]; errors: string[] } {
  const lines = content.split(/\r?\n/).map((l) => l.trim()).filter((l) => l.length > 0);
  const result: Transaction[] = [];
  const errors: string[] = [];

  if (lines.length < 2) {
    return { transactions: [], errors: ['Файл пуст или содержит только заголовок'] };
  }

  // Detect delimiter: semicolon or comma or tab
  const header = lines[0];
  const delimiter = header.includes(';') ? ';' : header.includes('\t') ? '\t' : ',';

  // Helper to strip quotes
  const clean = (val: string) => (val || '').replace(/^["']|["']$/g, '').trim();

  // Iterate over data rows
  for (let i = 1; i < lines.length; i++) {
    const rawLine = lines[i];
    if (!rawLine) continue;

    // Simple CSV splitter handling quotes
    const regex = new RegExp(`(?:${delimiter}|^)(?:"([^"]*)"|([^"${delimiter}]*))`, 'g');
    const cols: string[] = [];
    let match;
    while ((match = regex.exec(rawLine)) !== null) {
      cols.push(match[1] !== undefined ? match[1] : match[2]);
    }

    if (cols.length < 3) continue;

    try {
      let date = new Date().toISOString().split('T')[0];
      let amount = 0;
      let isExpense = true;
      let category = 'Прочее';
      let note = '';

      if (bank === 'tinkoff') {
        // Tinkoff format: Дата;...;Сумма операции;...;Категория;...;Описание
        // Usually cols: [0] Date, [4] Amount, [9] Category, [11] Description
        const rawDate = clean(cols[0]);
        if (rawDate.includes('.')) {
          const parts = rawDate.split(' ')[0].split('.');
          if (parts.length === 3) date = `${parts[2]}-${parts[1].padStart(2, '0')}-${parts[0].padStart(2, '0')}`;
        }
        const rawAmount = parseFloat((clean(cols[4]) || clean(cols[6]) || '0').replace(',', '.').replace(/\s/g, ''));
        amount = Math.abs(rawAmount);
        isExpense = rawAmount < 0;
        category = clean(cols[9]) || clean(cols[8]) || 'Другие расходы';
        note = clean(cols[11]) || clean(cols[10]) || '';
      } else if (bank === 'sberbank') {
        // Sber format: Дата;Время;...;Описание;Категория;Сумма
        const rawDate = clean(cols[0]);
        if (rawDate.includes('.')) {
          const parts = rawDate.split('.');
          if (parts.length === 3) date = `${parts[2]}-${parts[1].padStart(2, '0')}-${parts[0].padStart(2, '0')}`;
        }
        const rawAmount = parseFloat((clean(cols[cols.length - 2]) || clean(cols[cols.length - 1]) || '0').replace(',', '.').replace(/[^\d.-]/g, ''));
        amount = Math.abs(rawAmount);
        isExpense = rawAmount < 0 || (clean(cols[cols.length - 1]).includes('-'));
        category = clean(cols[4]) || 'Другие расходы';
        note = clean(cols[3]) || '';
      } else if (bank === 'alfabank') {
        // Alfa format: ...;Дата;...;Сумма;...;Назначение
        const rawDate = clean(cols[2]) || clean(cols[1]);
        if (rawDate.includes('.')) {
          const parts = rawDate.split('.');
          if (parts.length === 3) date = `${parts[2]}-${parts[1].padStart(2, '0')}-${parts[0].padStart(2, '0')}`;
        }
        const rawAmount = parseFloat((clean(cols[5]) || clean(cols[3]) || '0').replace(',', '.').replace(/\s/g, ''));
        amount = Math.abs(rawAmount);
        isExpense = rawAmount < 0;
        category = 'Банковские операции';
        note = clean(cols[cols.length - 1]) || '';
      } else {
        // Generic CSV format: Date, Type, Category, Amount, Note
        const rawDate = clean(cols[0]);
        if (rawDate.match(/^\d{4}-\d{2}-\d{2}$/)) {
          date = rawDate;
        } else if (rawDate.includes('.')) {
          const parts = rawDate.split('.');
          if (parts.length === 3) date = `${parts[2]}-${parts[1].padStart(2, '0')}-${parts[0].padStart(2, '0')}`;
        }
        const typeStr = clean(cols[1]).toLowerCase();
        isExpense = !typeStr.includes('доход') && !typeStr.includes('income');
        category = clean(cols[2]) || (isExpense ? 'Другие расходы' : 'Прочие доходы');
        amount = Math.abs(parseFloat((clean(cols[3]) || '0').replace(',', '.').replace(/[^\d.-]/g, '')));
        note = clean(cols[4]) || '';
      }

      if (amount > 0) {
        result.push({
          id: `imp_${Date.now()}_${i}`,
          amount,
          type: isExpense ? 'expense' : 'income',
          category,
          date,
          timestamp: new Date(date).getTime() || Date.now(),
          walletId: defaultWalletId,
          note: note || `Импорт: ${bank.toUpperCase()}`,
        });
      }
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : String(err);
      errors.push(`Строка ${i + 1}: ${message}`);
    }
  }

  return { transactions: result, errors };
}
