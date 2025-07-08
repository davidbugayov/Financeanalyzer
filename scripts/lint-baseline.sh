#!/bin/bash

# Скрипт для управления Lint Baseline в проекте Finance Analyzer

set -e

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Функция для вывода заголовков
print_header() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}"
}

# Функция для вывода успеха
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

# Функция для вывода предупреждения
print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

# Функция для вывода ошибки
print_error() {
    echo -e "${RED}✗ $1${NC}"
}

# Список всех модулей проекта
MODULES=(
    "app"
    "core" 
    "data"
    "domain"
    "ui"
    "utils"
    "navigation"
    "presentation"
    "feature"
    "feature:home"
    "feature:budget" 
    "feature:transaction"
    "feature:history"
    "feature:statistics"
    "feature:profile"
    "feature:onboarding"
    "feature:widget"
)

# Функция для проверки lint для всех модулей
run_lint_all() {
    print_header "Запуск Lint для всех модулей"
    
    local failed_modules=()
    
    for module in "${MODULES[@]}"; do
        echo -e "\n${BLUE}Проверка модуля: $module${NC}"
        
        if ./gradlew ":$module:lintRustoreDebug" --quiet; then
            print_success "Модуль $module прошел проверку"
        else
            print_error "Ошибка в модуле $module"
            failed_modules+=("$module")
        fi
    done
    
    if [ ${#failed_modules[@]} -eq 0 ]; then
        print_success "Все модули прошли проверку lint!"
    else
        print_error "Модули с ошибками: ${failed_modules[*]}"
        return 1
    fi
}

# Функция для сброса всех baseline файлов
reset_baselines() {
    print_header "Сброс всех Baseline файлов"
    
    for module in "${MODULES[@]}"; do
        local baseline_path
        if [[ "$module" == *":"* ]]; then
            baseline_path="${module//://}/lint-baseline.xml"
        else
            baseline_path="$module/lint-baseline.xml"
        fi
        
        if [ -f "$baseline_path" ]; then
            rm "$baseline_path"
            print_success "Удален baseline для $module"
        else
            print_warning "Baseline для $module не найден"
        fi
    done
    
    print_success "Все baseline файлы сброшены!"
}

# Функция для создания baseline файлов
create_baselines() {
    print_header "Создание Baseline файлов"
    
    for module in "${MODULES[@]}"; do
        echo -e "\n${BLUE}Создание baseline для: $module${NC}"
        
        if ./gradlew ":$module:updateLintBaseline" --quiet; then
            print_success "Baseline создан для $module"
        else
            print_warning "Не удалось создать baseline для $module"
        fi
    done
    
    print_success "Создание baseline файлов завершено!"
}

# Функция для показа статистики
show_stats() {
    print_header "Статистика Baseline файлов"
    
    local total_modules=${#MODULES[@]}
    local existing_baselines=0
    
    for module in "${MODULES[@]}"; do
        local baseline_path
        if [[ "$module" == *":"* ]]; then
            baseline_path="${module//://}/lint-baseline.xml"
        else
            baseline_path="$module/lint-baseline.xml"
        fi
        
        if [ -f "$baseline_path" ]; then
            existing_baselines=$((existing_baselines + 1))
            local issues_count=$(grep -c "<issue" "$baseline_path" 2>/dev/null || echo "0")
            echo -e "${GREEN}✓${NC} $module: $issues_count проблем в baseline"
        else
            echo -e "${RED}✗${NC} $module: baseline отсутствует"
        fi
    done
    
    echo -e "\n${BLUE}Общая статистика:${NC}"
    echo -e "Всего модулей: $total_modules"
    echo -e "Модулей с baseline: $existing_baselines"
    echo -e "Модулей без baseline: $((total_modules - existing_baselines))"
}

# Функция для показа помощи
show_help() {
    echo -e "${BLUE}Скрипт управления Lint Baseline${NC}"
    echo ""
    echo "Использование: $0 [команда]"
    echo ""
    echo "Команды:"
    echo "  check       - Проверить lint для всех модулей"
    echo "  reset       - Сбросить все baseline файлы"
    echo "  create      - Создать baseline файлы для всех модулей"
    echo "  recreate    - Сбросить и создать заново все baseline файлы"
    echo "  stats       - Показать статистику baseline файлов"
    echo "  help        - Показать эту справку"
    echo ""
    echo "Примеры:"
    echo "  $0 check     # Проверить все модули"
    echo "  $0 recreate  # Пересоздать все baseline файлы"
}

# Основная логика
case "${1:-help}" in
    "check")
        run_lint_all
        ;;
    "reset")
        reset_baselines
        ;;
    "create")
        create_baselines
        ;;
    "recreate")
        reset_baselines
        echo ""
        create_baselines
        ;;
    "stats")
        show_stats
        ;;
    "help"|"--help"|"-h")
        show_help
        ;;
    *)
        print_error "Неизвестная команда: $1"
        echo ""
        show_help
        exit 1
        ;;
esac 