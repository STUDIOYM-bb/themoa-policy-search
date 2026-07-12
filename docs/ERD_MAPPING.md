# ERD 매핑

| ERD 테이블 | JPA 엔티티 | Flyway 파일 | PK | 주요 FK | 구현 상태 |
| --- | --- | --- | --- | --- | --- |
| member | Member | V1 | id | - | 엔티티/Repository |
| region_code | RegionCode | V1 | id | parent_id | 엔티티/Repository |
| policy | Policy | V1 | id | - | 엔티티/Repository/Service/API |
| policy_condition | PolicyCondition | V1 | id | policy_id | 엔티티/Repository |
| policy_region | PolicyRegion | V1 | id | policy_id, region_id | 엔티티/Repository |
| user_policy_profile | UserPolicyProfile | V1 | id | member_id, residence_region_id | 스키마 반영 |
| policy_bookmark | PolicyBookmark | V1 | id | member_id, policy_id | 엔티티/Repository/API |
| policy_calendar_event | PolicyCalendarEvent | V1 | id | member_id, policy_id | 엔티티/Repository/API |
| policy_notification | PolicyNotification | V1 | id | member_id, bookmark_id, policy_id | 스키마 반영 |
| savings_product | SavingsProduct | V1 | id | - | 스키마 반영 |
| savings_product_option | SavingsProductOption | V1 | id | savings_product_id | 스키마 반영 |
| pension_product | PensionProduct | V1 | id | - | 스키마 반영 |
| pension_product_option | PensionProductOption | V1 | id | pension_product_id | 스키마 반영 |
| loan_product | LoanProduct | V1 | id | - | 스키마 반영 |
| loan_product_option | LoanProductOption | V1 | id | loan_product_id | 스키마 반영 |
| financial_profile | FinancialProfile | V1 | id | member_id, goal_id | 스키마 반영 |
| product_bookmark | ProductBookmark | V1 | id | member_id | 스키마 반영 |
| product_recommendation | ProductRecommendation | V1 | id | member_id | 스키마 반영 |
| goal | Goal | V1 | id | member_id | 스키마 반영 |
| goal_history | GoalHistory | V1 | id | member_id, goal_id | 스키마 반영 |
| surplus_fund | SurplusFund | V1 | id | member_id, goal_id | 스키마 반영 |
| budget | Budget | V1 | id | - | 스키마 반영 |
| category | Category | V1 | id | - | 스키마 반영 |
| cards | Cards | V1 | id | user_id | 스키마 반영 |
| merchant_alias | MerchantAlias | V1 | id | default_category_id | 스키마 반영 |
| merchant | Merchant | V1 | id | merchant_alias_id | 스키마 반영 |
| merchant_alias_terms | MerchantAliasTerms | V1 | id | merchant_alias_id, member_id | 스키마 반영 |
| card_transaction | CardTransaction | V1 | id | user_id, card_id, merchant_id, goal_id, category_id | 스키마 반영 |
| recurring_payment_group | RecurringPaymentGroup | V1 | id | merchant_id | 스키마 반영 |
| fixed_expense_candidate | FixedExpenseCandidate | V1 | id | recurring_group_id, recommended_category_id | 스키마 반영 |
| fixed_expense | FixedExpense | V1 | id | candidate_id, category_id, card_id, merchant_id | 스키마 반영 |
| recurring_payment_group_transaction | RecurringPaymentGroupTransaction | V1 | recurring_group_id, transaction_id | recurring_group_id, transaction_id | 스키마 반영 |
| habit_expense | HabitExpense | V1 | id | candidate_id, merchant_id, category_id | 스키마 반영 |
| notification | Notification | V1 | id | fixed_expense_id | 스키마 반영 |
| user_merchant_preferences | UserMerchantPreferences | V1 | id | merchant_id, category_id | 스키마 반영 |
| policy_raw_data | PolicyRawData | V2 | id | - | 엔티티/Repository |
| policy_collection_run | PolicyCollectionRun | V2 | id | - | 엔티티/Repository |
| policy_collection_error | PolicyCollectionError | V2 | id | collection_run_id, raw_data_id | 엔티티/Repository |
| policy_embedding_sync | PolicyEmbeddingSync | V2 | id | policy_id | 엔티티/Repository |
