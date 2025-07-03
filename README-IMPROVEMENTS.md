# Wallets Enhancement Roadmap

> **Purpose:** Increase user engagement with the **Wallets** feature so that more users create, top-up and actively manage multiple wallets for budgeting and saving goals.

---

## 1. Objectives

| Objective | Success Metric |
|-----------|---------------|
| 🏗 Adoption | ≥ 60 % новых пользователей создали ≥ 1 кошелек в первые 3 дня |
| 📈 Engagement | Среднее число кошельков на пользователя ≥ 3 в D30 |
| 💰 Savings | ≥ 40 % активных пользователей пополнили «Целевой кошелек» ≥ 1 раз в месяц |
| 🔄 Retention | D30 Retention среди пользователей кошельков на 15 п.п. выше базовой |

---

## 2. Product Feature Roadmap

| Sprint | Feature | Description |
|--------|---------|-------------|
| **S1** | **Simplified Wallet Creation** | • Обязательный мастер при первом запуске  <br>• Шаблоны: «Наличный», «Карта», «Сбережения»  <br>• Поясняющие подсказки |
| **S1** | **Wallet List UI Revamp** | • Sticky header с суммой активов  <br>• Цветовая индикация (зел/желт/красн)  <br>• Long-press: Пополнить/Снять/Перевод |
| **S2** | **Sub-Wallets (Envelopes)** | Внутри кошелька — подпулы (Groceries, Fun…). Перераспределение остатка одной кнопкой |
| **S2** | **Goals Wallets** | Checkbox «Это цель» → targetAmount + targetDate. Progress-bar + milestone push-уведомления |
| **S3** | **Auto Rules** | • Округление сдачи  <br>• Еженедельный автоперевод «10 % дохода»  |
| **S3** | **Insights & Gamification** | • Weekly digest «Вы близки к цели!»  <br>• Бейджи «5 недель в плюсе» |
| **S4** | **Bank Sync (Opt-in)** | Плагины для агрегаторов (Plaid / fin-API). CSV импорт как fallback |
| **S4** | **Referral + Leaderboard** | «+1 месяц Pro» за друга, leaderboard % выполнения целей |

---

## 3. Technical Tasks

### 3.1 Domain Layer

- `Wallet` → добавить поля  
  `type: WalletType`, `goalAmount: Money?`, `goalDate: LocalDate?`, `parentWalletId: String?`
- Новые use-cases:
  1. `CreateWalletUseCase` (упрощённый wizard)
  2. `AllocateIncomeUseCase` (распределение по подпулам)
  3. `UpdateGoalProgressUseCase`
  4. `SyncBankTransactionsUseCase`

### 3.2 Data Layer

- Room migration V17-18: добавить новые поля в таблицу `wallets`
- DAO: `getSubWallets(parentId)`, `getGoalProgress(walletId)`
- Repository: имплементация правил авто-перевода

### 3.3 Presentation Layer (Compose)

- `WalletListScreen` ⟶ новый дизайн  
- `WalletDetailsScreen` ⟶ табы *Transactions | Goals | Settings*
- `WalletSetupGraph` ⟶ onboarding flow (Navigation)
- Widgets: `WalletBalanceWidget` и `QuickAddTransactionWidget`

### 3.4 Background Workers

- `GoalProgressWorker` – ежедневно проверяет прогресс и пушит milestones
- `AutoRuleWorker` – раз в день выполняет округление/переводы

### 3.5 Sync Plugin Interface

```kotlin
interface BankSyncProvider {
    suspend fun connect(credentials: Credentials): Result<Unit>
    suspend fun fetchTransactions(): List<BankTransaction>
}
```

---

## 4. Marketing & Growth Plan

1. **Onboarding Carousel** (3-5 экранов). А/Б-тестируем иконки + CTA.
2. **Push Cadence**  
   • T+2 дня: «Завершите настройку кошельков»  
   • Перед зарплатой (predict) – «Распределите свежий доход»
3. **In-app Tips**: карточки «Создай 'Фонд 3-месячного резерва'»
4. **Referral Program** (+1 мес Pro) + share-deep-link.
5. **ASO**: Скрин с прогресс-баром, ключи «wallet goals»/«копилки».
6. **Email Digest** (HTML) – еженедельный отчёт + успехи целей.

---

## 5. Analytics & KPIs

- Custom events:
  - `wallet_created`, `wallet_goal_set`, `sub_wallet_created`, `auto_rule_enabled`
  - Funnel «Зарегистрировался → Создал кошелек → Добавил транзакцию → Пополнил цель»
- Amplitude dashboards & Mixpanel cohorts
- A/B flags via `remote-config`

---

## 6. Timeline (High-level)

| Week | Milestone |
|------|-----------|
| 1 | DB migration, Updated Wallet model, Wallet List UI revamp |
| 2 | Onboarding wizard, CreateWalletUseCase, analytics events |
| 3 | Sub-Wallets, AllocateIncomeUseCase |
| 4 | Goal wallets, progress-bar UI, GoalProgressWorker |
| 5 | Auto rules engine + AutoRuleWorker |
| 6 | Insights, weekly digest email, gamification badges |
| 7 | Bank Sync beta (1-2 партнёра), referral program launch |
| 8 | Full release, ASO-материалы, marketing campaign |

> **Note:** Timeline assumes 2 devs, 1 designer, 0.5 QA. Adjust for team size.

---

## 7. Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|-----------|
| Долгая сертификация банковских API | 🚨 | Запуск Bank Sync как beta-фичи, fallback – CSV импорт |
| Сложная Room-миграция | ⚠️ | Генерация тестовых данных, ручная валидация на staging |
| Push fatigue | ⚠️ | Frequency capping, user-opt-outs |
| Over-scope | ⚠️ | MVP-подход, MoSCoW-приоритезация |

---

## 8. Dependencies & Resources

- **Design**: Figma макеты Wallet v2 (UI/UX)  
- **Copywriting**: Рус/Eng лонг-копи для onboarding, push, email  
- **Backend** (optional): Endpoint `/v1/referral`  
- **Legal**: Проверка агрегаторов BankSync

---

_Последнее обновление: {{DATE → при коммите}}_ 