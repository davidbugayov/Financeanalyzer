import { Transaction, SpendingProjection, SpendingProjectionMonth, SpendingChartPoint } from '../types';

const MONTH_NAMES_SHORT = [
  'Янв',
  'Фев',
  'Мар',
  'Апр',
  'Май',
  'Июн',
  'Июл',
  'Авг',
  'Сен',
  'Окт',
  'Ноя',
  'Дек',
];

const MONTH_NAMES_FULL = [
  'Январь',
  'Февраль',
  'Март',
  'Апрель',
  'Май',
  'Июнь',
  'Июль',
  'Август',
  'Сентябрь',
  'Октябрь',
  'Ноябрь',
  'Декабрь',
];

/**
 * Calculates a robust 3-month spending forecast / projection based on
 * historical expense velocity, weighted rolling averages, recurring commitments,
 * and seasonal factors.
 */
export function calculate3MonthSpendingProjection(
  transactions: Transaction[],
  categoryBudgets: Record<string, number> = {}
): SpendingProjection {
  const now = new Date();
  const currentYear = now.getFullYear();
  const currentMonthIdx = now.getMonth(); // 0-11
  const currentDay = now.getDate();
  const daysInCurrentMonth = new Date(currentYear, currentMonthIdx + 1, 0).getDate();

  // 1. Group past expenses by month (YYYY-MM)
  const monthlyExpensesMap: Record<string, number> = {};

  transactions.forEach((tx) => {
    if (tx.type === 'expense' && tx.amount > 0) {
      const ym = tx.date ? tx.date.substring(0, 7) : '';
      if (ym && ym.length === 7) {
        monthlyExpensesMap[ym] = (monthlyExpensesMap[ym] || 0) + tx.amount;
      }
    }
  });

  const currentMonthKey = `${currentYear}-${String(currentMonthIdx + 1).padStart(2, '0')}`;
  const currentMonthSpent = monthlyExpensesMap[currentMonthKey] || 0;

  // Project current month full spend based on daily run-rate
  const daysPassed = Math.max(1, currentDay);
  const currentMonthProjected =
    currentDay >= daysInCurrentMonth - 2
      ? currentMonthSpent
      : Math.round((currentMonthSpent / daysPassed) * daysInCurrentMonth);

  // 2. Collect up to 6 previous completed months
  const completedMonths: { monthKey: string; label: string; fullLabel: string; expense: number }[] = [];
  for (let offset = 6; offset >= 1; offset--) {
    const d = new Date(currentYear, currentMonthIdx - offset, 1);
    const y = d.getFullYear();
    const m = d.getMonth();
    const ym = `${y}-${String(m + 1).padStart(2, '0')}`;
    const exp = monthlyExpensesMap[ym] || 0;
    if (exp > 0 || completedMonths.length > 0) {
      completedMonths.push({
        monthKey: ym,
        label: `${MONTH_NAMES_SHORT[m]} ${y}`,
        fullLabel: `${MONTH_NAMES_FULL[m]} ${y}`,
        expense: exp,
      });
    }
  }

  // 3. Calculate recurring expense commitments for upcoming months
  const recurringTxs = transactions.filter((t) => t.isRecurring && t.type === 'expense');
  let recurringMonthlyCommitment = 0;
  recurringTxs.forEach((t) => {
    if (t.recurrenceInterval === 'daily') recurringMonthlyCommitment += t.amount * 30;
    else if (t.recurrenceInterval === 'weekly') recurringMonthlyCommitment += t.amount * 4.33;
    else if (t.recurrenceInterval === 'biweekly') recurringMonthlyCommitment += t.amount * 2.16;
    else if (t.recurrenceInterval === 'yearly') recurringMonthlyCommitment += t.amount / 12;
    else recurringMonthlyCommitment += t.amount; // default monthly
  });
  recurringMonthlyCommitment = Math.round(recurringMonthlyCommitment);

  // 4. Weighted moving average & trend slope
  // Recent 3 months take weights: [0.5, 0.3, 0.2]
  const recentValues: number[] = [];
  if (completedMonths.length > 0) {
    const last3 = completedMonths.slice(-3);
    last3.forEach((m) => recentValues.push(m.expense));
  }

  // If we have current month projected, we can include it as the newest signal
  if (currentMonthProjected > 0) {
    recentValues.push(currentMonthProjected);
  }

  let baseline = 30000; // fallback if no transactions
  if (recentValues.length > 0) {
    if (recentValues.length === 1) {
      baseline = recentValues[0];
    } else if (recentValues.length === 2) {
      baseline = recentValues[1] * 0.6 + recentValues[0] * 0.4;
    } else {
      const n = recentValues.length;
      baseline =
        recentValues[n - 1] * 0.5 +
        recentValues[n - 2] * 0.3 +
        recentValues[n - 3] * 0.2;
    }
  }

  // Factor in category budgets if set (budget ceiling anchor)
  const totalBudgets = Object.values(categoryBudgets).reduce((sum, b) => sum + (b || 0), 0);
  if (totalBudgets > 1000) {
    // 80% weight on actual historical spending + 20% anchor to budget ceiling
    baseline = Math.round(baseline * 0.8 + totalBudgets * 0.2);
  }

  // Calculate trend slope between recent periods (clamped between -8% and +12% per month)
  let trendSlope = 0.01; // slight default baseline drift
  if (recentValues.length >= 2) {
    const prev = recentValues[recentValues.length - 2];
    const curr = recentValues[recentValues.length - 1];
    if (prev > 0) {
      const rawGrowth = (curr - prev) / prev;
      trendSlope = Math.max(-0.08, Math.min(0.12, rawGrowth * 0.5));
    }
  }

  // 5. Generate projections for Month +1, Month +2, Month +3
  const projectedMonths: SpendingProjectionMonth[] = [];
  const seasonalFactors = [1.0, 1.02, 1.05]; // slight drift

  for (let i = 1; i <= 3; i++) {
    const targetDate = new Date(currentYear, currentMonthIdx + i, 1);
    const targetY = targetDate.getFullYear();
    const targetM = targetDate.getMonth();
    const monthKey = `${targetY}-${String(targetM + 1).padStart(2, '0')}`;
    const label = `${MONTH_NAMES_SHORT[targetM]} ${targetY}`;
    const fullLabel = `${MONTH_NAMES_FULL[targetM]} ${targetY}`;

    // Specific seasonal tweaks: December holiday (+12%), January post-holiday (-5%)
    let customSeasonal = seasonalFactors[i - 1];
    if (targetM === 11) customSeasonal = 1.12; // December
    if (targetM === 0) customSeasonal = 0.95; // January

    const rawProjected = Math.round(
      baseline * Math.pow(1 + trendSlope, i) * customSeasonal
    );

    // Floor projected with recurring commitments
    const projectedExpense = Math.max(recurringMonthlyCommitment, rawProjected);

    // Confidence decreases with distance into the future
    const confidenceScore = Math.max(60, Math.round(92 - i * 8));

    // Bounds: Lower bound (saving scenario) & Upper bound (unexpected spending)
    const lowerBound = Math.round(projectedExpense * (1 - 0.12 - i * 0.02));
    const upperBound = Math.round(projectedExpense * (1 + 0.15 + i * 0.03));

    projectedMonths.push({
      monthKey,
      label,
      fullLabel,
      projectedExpense,
      lowerBound,
      upperBound,
      baselineExpense: Math.round(baseline),
      recurringCommitment: recurringMonthlyCommitment,
      confidenceScore,
    });
  }

  // 6. Build historical points for the chart
  const historicalDisplayCount = 4;
  const recentHistorical = completedMonths.slice(-historicalDisplayCount);

  const historicalMonths = recentHistorical.map((h) => ({
    monthKey: h.monthKey,
    label: h.label,
    actualExpense: h.expense,
  }));

  // 7. Assemble combinedChartData for Recharts
  // Points: [Past Completed Months] -> [Current Month Anchor] -> [3 Projected Months]
  const combinedChartData: SpendingChartPoint[] = [];

  recentHistorical.forEach((h) => {
    combinedChartData.push({
      monthKey: h.monthKey,
      label: h.label,
      fullLabel: h.fullLabel,
      actualExpense: h.expense,
      isForecast: false,
    });
  });

  // Current month anchor point
  const currentMonthPoint: SpendingChartPoint = {
    monthKey: currentMonthKey,
    label: `${MONTH_NAMES_SHORT[currentMonthIdx]} ${currentYear}`,
    fullLabel: `${MONTH_NAMES_FULL[currentMonthIdx]} ${currentYear} (текущий)`,
    actualExpense: currentMonthSpent,
    projectedExpense: currentMonthSpent, // Connect lines at the transition point
    isForecast: false,
    isAnchor: true,
  };
  combinedChartData.push(currentMonthPoint);

  // Add the 3 forecast months
  projectedMonths.forEach((pm) => {
    combinedChartData.push({
      monthKey: pm.monthKey,
      label: pm.label,
      fullLabel: pm.fullLabel,
      projectedExpense: pm.projectedExpense,
      lowerBound: pm.lowerBound,
      upperBound: pm.upperBound,
      isForecast: true,
    });
  });

  // 8. Aggregated totals and comparative metrics
  const totalThreeMonthProjected = projectedMonths.reduce(
    (sum, m) => sum + m.projectedExpense,
    0
  );
  const averageMonthlyProjected = Math.round(totalThreeMonthProjected / 3);

  // Compare forecast 3-month average vs past 3-month actual average
  const past3Total = recentValues.slice(-3).reduce((sum, v) => sum + v, 0);
  const past3Avg = past3Total > 0 ? past3Total / Math.min(3, recentValues.length) : baseline;
  const trendPercentage =
    past3Avg > 0
      ? Math.round(((averageMonthlyProjected - past3Avg) / past3Avg) * 100)
      : 0;

  return {
    projectedMonths,
    historicalMonths,
    combinedChartData,
    totalThreeMonthProjected,
    averageMonthlyProjected,
    trendPercentage,
    recurringCommitmentsTotal: recurringMonthlyCommitment * 3,
    methodology:
      'Адаптивный прогноз на базе взвешенного скользящего среднего, выявленного тренда динамики и зафиксированных регулярных обязательств',
  };
}
