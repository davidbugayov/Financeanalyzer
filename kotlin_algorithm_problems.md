# Алгоритмические задачи для собеседования Kotlin/Android разработчика в Авито

В этом документе собраны типичные алгоритмические задачи, которые могут встретиться на собеседовании в Авито, с примерами и решениями на Kotlin.

## Подготовка к алгоритмическому собеседованию

### Что проверяют в секции программирования
На этом этапе оцениваются:
- Навыки алгоритмизации и написания кода: способность решать задачи, используя эффективные алгоритмы и структуры данных
- Понимание сложности алгоритмов: умение анализировать временную и пространственную сложность решений
- Чистота и читаемость кода: использование понятных имён переменных, структурирование кода
- Умение рассуждать вслух: объяснение своих решений и подходов интервьюеру

### Как подготовиться
1. **Практика на платформах**
   - LeetCode и HackerRank, начиная с уровня Easy и переходя к Medium
   - Фокусируйтесь на: массивы и строки, хеш-таблицы, стеки и очереди, деревья и графы, сортировки и поиск

2. **Изучение теории**
   - «Грокаем алгоритмы» — Адитья Бхаргава
   - «Алгоритмы. Построение и анализ» — Кормен и др.
   - «Cracking the Coding Interview» — Гейл Лакман МакДауэлл

3. **Советы для успешного прохождения**
   - Говорите вслух: объясняйте свои мысли и подходы интервьюеру
   - Уточняйте условия задачи: если что-то непонятно, не стесняйтесь задавать вопросы
   - Проверяйте крайние случаи: убедитесь, что ваше решение работает для всех возможных входных данных
   - Оценивайте сложность: после решения проанализируйте временную и пространственную сложность алгоритма

## Задача 1: Merge Sorted Array (Easy)

**Условие:**
Даны два отсортированных массива `nums1` и `nums2`. Объедините их в один отсортированный массив.
Предполагается, что `nums1` имеет достаточно места (размер m + n) для вмещения всех элементов `nums2`.

**Пример:**
```
nums1 = [1,2,3,0,0,0], m = 3
nums2 = [2,5,6], n = 3
Результат: [1,2,2,3,5,6]
```

**Решение:**
```kotlin
fun merge(nums1: IntArray, m: Int, nums2: IntArray, n: Int) {
    var i = m - 1
    var j = n - 1
    var k = m + n - 1
    
    while (j >= 0) {
        if (i >= 0 && nums1[i] > nums2[j]) {
            nums1[k--] = nums1[i--]
        } else {
            nums1[k--] = nums2[j--]
        }
    }
}
```

## Задача 2: Add Two Numbers (Easy)

**Условие:**
Даны два числа, представленные связными списками, где каждый узел содержит одну цифру. Цифры хранятся в обратном порядке, а каждый узел содержит одну цифру. Сложите два числа и верните результат в виде связного списка.

**Пример:**
```
l1 = [2,4,3], l2 = [5,6,4]
Результат: [7,0,8]
Объяснение: 342 + 465 = 807
```

**Решение:**
```kotlin
class ListNode(var `val`: Int) {
    var next: ListNode? = null
}

fun addTwoNumbers(l1: ListNode?, l2: ListNode?): ListNode? {
    val dummy = ListNode(0)
    var p = l1
    var q = l2
    var current = dummy
    var carry = 0
    
    while (p != null || q != null) {
        val x = p?.`val` ?: 0
        val y = q?.`val` ?: 0
        val sum = carry + x + y
        carry = sum / 10
        current.next = ListNode(sum % 10)
        current = current.next!!
        
        p = p?.next
        q = q?.next
    }
    
    if (carry > 0) {
        current.next = ListNode(carry)
    }
    
    return dummy.next
}
```

## Задача 3: Top K Frequent Elements (Medium)

**Условие:**
Дан массив целых чисел `nums` и целое число `k`, верните `k` наиболее часто встречающихся элементов.

**Пример:**
```
nums = [1,1,1,2,2,3], k = 2
Результат: [1,2]
```

**Решение с использованием очереди с приоритетом:**
```kotlin
fun topKFrequent(nums: IntArray, k: Int): IntArray {
    // Подсчитываем частоту каждого элемента
    val frequencyMap = HashMap<Int, Int>()
    for (num in nums) {
        frequencyMap[num] = frequencyMap.getOrDefault(num, 0) + 1
    }
    
    // Используем очередь с приоритетом (min heap) размера k
    val heap = PriorityQueue<Int> { a, b -> 
        frequencyMap[a]!! - frequencyMap[b]!! 
    }
    
    for (num in frequencyMap.keys) {
        heap.add(num)
        if (heap.size > k) {
            heap.poll()
        }
    }
    
    // Преобразуем результат в массив
    return IntArray(k) { heap.poll() }.reversedArray()
}
```

**Альтернативное решение без очереди с приоритетом:**
```kotlin
fun topKFrequent(nums: IntArray, k: Int): IntArray {
    return nums.asSequence()
        .groupingBy { it }
        .eachCount()
        .entries
        .sortedByDescending { it.value }
        .take(k)
        .map { it.key }
        .toIntArray()
}
```

**Объяснение:**
Второе решение использует функциональный подход Kotlin:
1. Группируем элементы по значению и подсчитываем частоту
2. Сортируем записи по частоте (по убыванию)
3. Берем первые k элементов
4. Преобразуем в массив ключей

## Задача 4: Kth Largest Element in an Array (Medium)

**Условие:**
Найдите k-й по величине элемент в неотсортированном массиве.

**Пример:**
```
nums = [3,2,1,5,6,4], k = 2
Результат: 5
```

**Решение (с использованием QuickSelect):**
```kotlin
fun findKthLargest(nums: IntArray, k: Int): Int {
    return quickSelect(nums, 0, nums.size - 1, nums.size - k)
}

private fun quickSelect(nums: IntArray, left: Int, right: Int, kSmallest: Int): Int {
    if (left == right) return nums[left]
    
    val pivotIndex = partition(nums, left, right)
    
    return when {
        kSmallest == pivotIndex -> nums[kSmallest]
        kSmallest < pivotIndex -> quickSelect(nums, left, pivotIndex - 1, kSmallest)
        else -> quickSelect(nums, pivotIndex + 1, right, kSmallest)
    }
}

private fun partition(nums: IntArray, left: Int, right: Int): Int {
    val pivot = nums[right]
    var i = left
    
    for (j in left until right) {
        if (nums[j] <= pivot) {
            nums[i].also { nums[i] = nums[j]; nums[j] = it }
            i++
        }
    }
    
    nums[i].also { nums[i] = nums[right]; nums[right] = it }
    return i
}
```

**Пошаговое объяснение алгоритма QuickSelect на примере:**
- Ищем 2-й по величине элемент (5) в массиве [3, 2, 1, 5, 6, 4]
- kSmallest = n - k = 6 - 2 = 4 (4-й по возрастанию = 2-й по убыванию)

**Первая итерация quickSelect:**
- left = 0, right = 5, kSmallest = 4
- pivot = 4, i = 0
- После partition: [3, 2, 1, 4, 6, 5], pivotIndex = 3
- kSmallest (4) > pivotIndex (3), вызываем quickSelect для правой части

**Вторая итерация quickSelect:**
- left = 4, right = 5, kSmallest = 4
- pivot = 5, i = 4
- После partition: [3, 2, 1, 4, 5, 6], pivotIndex = 4
- kSmallest (4) == pivotIndex (4), возвращаем nums[4] = 5

## Задача 5: Combination Sum II (Medium)

**Условие:**
Дан массив `candidates` и целевое число `target`. Найдите все уникальные комбинации, где сумма чисел равна `target`. Каждый элемент в массиве может быть использован только один раз.

**Пример:**
```
candidates = [10,1,2,7,6,1,5], target = 8
Результат: [[1,1,6], [1,2,5], [1,7], [2,6]]
```

**Решение:**
```kotlin
fun combinationSum2(candidates: IntArray, target: Int): List<List<Int>> {
    val result = mutableListOf<List<Int>>()
    candidates.sort()
    backtrack(candidates, target, 0, mutableListOf(), result)
    return result
}

private fun backtrack(
    candidates: IntArray, 
    remain: Int, 
    start: Int, 
    current: MutableList<Int>, 
    result: MutableList<List<Int>>
) {
    if (remain == 0) {
        result.add(ArrayList(current))
        return
    }
    
    for (i in start until candidates.size) {
        // Пропускаем дубликаты
        if (i > start && candidates[i] == candidates[i-1]) continue
        
        val newRemain = remain - candidates[i]
        if (newRemain < 0) break // Оптимизация: массив отсортирован
        
        current.add(candidates[i])
        backtrack(candidates, newRemain, i + 1, current, result)
        current.removeAt(current.size - 1) // Возвращаемся назад
    }
}
```

## Задача 6: Sum of Subarray Minimums (Hard)

**Условие:**
Дан массив целых чисел `arr`. Найдите сумму `min(b)` для всех подмассивов `b` массива `arr`.

**Пример:**
```
arr = [3,1,2,4]
Результат: 17
Объяснение: 
Подмассивы: [3] с min=3, [1] с min=1, [2] с min=2, [4] с min=4, [3,1] с min=1, 
[1,2] с min=1, [2,4] с min=2, [3,1,2] с min=1, [1,2,4] с min=1, [3,1,2,4] с min=1
Сумма: 3 + 1 + 2 + 4 + 1 + 1 + 2 + 1 + 1 + 1 = 17
```

**Решение (с использованием монотонного стека):**
```kotlin
fun sumSubarrayMins(arr: IntArray): Int {
    val MOD = 1_000_000_007
    val n = arr.size
    val stack = Stack<Int>()
    var result = 0L
    
    // Для каждого элемента находим ближайший меньший элемент слева и справа
    for (i in 0..n) {
        // i == n - это сигнальное значение для обработки оставшихся элементов в стеке
        while (stack.isNotEmpty() && (i == n || arr[stack.peek()] >= arr[i])) {
            val mid = stack.pop()
            val left = if (stack.isEmpty()) -1 else stack.peek()
            val right = i
            
            // Количество подмассивов с arr[mid] как минимумом
            val count = ((mid - left).toLong() * (right - mid).toLong()) % MOD
            result = (result + count * arr[mid]) % MOD
        }
        stack.push(i)
    }
    
    return result.toInt()
}
```

**Пошаговое объяснение на примере arr = [3,1,2,4]:**

**i = 0 (arr[0] = 3)**
- Стек пуст, добавляем 0
- Стек: [0]

**i = 1 (arr[1] = 1)**
- 1 < 3, выталкиваем 0 из стека
- mid = 0, left = -1, right = 1
- Количество подмассивов: (0-(-1))*(1-0) = 1
- Добавляем в результат: 1*3 = 3
- Добавляем 1 в стек
- Стек: [1]

**i = 2 (arr[2] = 2)**
- 2 > 1, просто добавляем 2 в стек
- Стек: [1,2]

**i = 3 (arr[3] = 4)**
- 4 > 2, просто добавляем 3 в стек
- Стек: [1,2,3]

**i = 4 (выход за пределы массива)**
- Обрабатываем элемент 3 (значение 4): count = 1, добавляем 4
- Обрабатываем элемент 2 (значение 2): count = 2, добавляем 4
- Обрабатываем элемент 1 (значение 1): count = 6, добавляем 6

Итоговая сумма: 3 + 4 + 4 + 6 = 17

## Задача 7: Longest Substring Without Repeating Characters (Medium)

**Условие:**
Дана строка s, найдите длину самой длинной подстроки без повторяющихся символов.

**Пример:**
```
Input: s = "abcabcbb"
Output: 3
Объяснение: Ответ "abc" с длиной 3.

Input: s = "bbbbb"
Output: 1
Объяснение: Ответ "b" с длиной 1.
```

**Решение:**
```kotlin
fun lengthOfLongestSubstring(s: String): Int {
    val charMap = HashMap<Char, Int>()
    var maxLength = 0
    var start = 0
    
    for (i in s.indices) {
        val c = s[i]
        if (charMap.containsKey(c)) {
            // Если символ уже встречался, обновляем start
            start = maxOf(start, charMap[c]!! + 1)
        }
        // Обновляем позицию символа
        charMap[c] = i
        // Обновляем максимальную длину
        maxLength = maxOf(maxLength, i - start + 1)
    }
    
    return maxLength
}
```

**Пошаговое объяснение для "abcabcbb":**

1. i=0, c='a': добавляем в map={'a':0}, maxLength=1, start=0
2. i=1, c='b': добавляем в map={'a':0, 'b':1}, maxLength=2, start=0
3. i=2, c='c': добавляем в map={'a':0, 'b':1, 'c':2}, maxLength=3, start=0
4. i=3, c='a': 'a' уже в map, start=max(0,0+1)=1, map={'a':3, 'b':1, 'c':2}, maxLength=3, start=1
5. i=4, c='b': 'b' уже в map, start=max(1,1+1)=2, map={'a':3, 'b':4, 'c':2}, maxLength=3, start=2
6. i=5, c='c': 'c' уже в map, start=max(2,2+1)=3, map={'a':3, 'b':4, 'c':5}, maxLength=3, start=3
7. i=6, c='b': 'b' уже в map, start=max(3,4+1)=5, map={'a':3, 'b':6, 'c':5}, maxLength=3, start=5
8. i=7, c='b': 'b' уже в map, start=max(5,6+1)=7, map={'a':3, 'b':7, 'c':5}, maxLength=3, start=7

Результат: 3

## Задача 8: Group Anagrams (Medium)

**Условие:**
Дан массив строк, сгруппируйте анаграммы вместе. Анаграммы - это слова, которые содержат те же буквы, но в другом порядке.

**Пример:**
```
Input: strs = ["eat","tea","tan","ate","nat","bat"]
Output: [["bat"],["nat","tan"],["ate","eat","tea"]]
```

**Решение:**
```kotlin
fun groupAnagrams(strs: Array<String>): List<List<String>> {
    val map = HashMap<String, MutableList<String>>()
    
    for (s in strs) {
        // Сортируем символы строки для получения ключа
        val key = s.toCharArray().sorted().joinToString("")
        
        // Добавляем строку в соответствующую группу
        if (!map.containsKey(key)) {
            map[key] = mutableListOf()
        }
        map[key]!!.add(s)
    }
    
    return map.values.toList()
}
```

**Объяснение:**
Ключевая идея: все анаграммы дают одинаковую строку при сортировке их символов. 
Например, "eat", "tea" и "ate" все дают "aet" после сортировки.

## Задача 9: LRU Cache (Medium-Hard)

**Условие:**
Реализуйте структуру данных "LRU Cache":
- Конструктор принимает параметр capacity, задающий ёмкость кеша.
- get(key) возвращает значение ключа, если оно существует, иначе -1.
- put(key, value) устанавливает значение для ключа. Если количество элементов превышает capacity, удаляет наименее недавно использованный элемент.

**Пример:**
```
Input:
["LRUCache", "put", "put", "get", "put", "get", "put", "get", "get", "get"]
[[2], [1, 1], [2, 2], [1], [3, 3], [2], [4, 4], [1], [3], [4]]
Output:
[null, null, null, 1, null, -1, null, -1, 3, 4]
```

**Решение:**
```kotlin
class LRUCache(private val capacity: Int) {
    private val map = LinkedHashMap<Int, Int>(capacity, 0.75f, true)
    
    fun get(key: Int): Int {
        return map.getOrDefault(key, -1)
    }
    
    fun put(key: Int, value: Int) {
        map[key] = value
        if (map.size > capacity) {
            val oldest = map.entries.iterator().next().key
            map.remove(oldest)
        }
    }
}
```

**Объяснение:**
- Используем `LinkedHashMap` с параметром `accessOrder = true`, что обеспечивает порядок элементов по времени последнего доступа
- При добавлении нового элемента, если превышена емкость, удаляем первый элемент из итератора (наименее недавно использованный)
- Метод `get` автоматически перемещает элемент в конец списка доступа

## Задача 10: Valid Parentheses (Easy)

**Условие:**
Дана строка, содержащая только символы '(', ')', '{', '}', '[' и ']'. Определите, валидна ли строка.
Строка валидна, если:
1. Открывающие скобки должны быть закрыты скобками того же типа.
2. Открывающие скобки должны быть закрыты в правильном порядке.

**Пример:**
```
Input: s = "()[]{}"
Output: true

Input: s = "([)]"
Output: false
```

**Решение:**
```kotlin
fun isValid(s: String): Boolean {
    val stack = Stack<Char>()
    for (c in s) {
        when (c) {
            '(', '{', '[' -> stack.push(c)
            ')' -> if (stack.isEmpty() || stack.pop() != '(') return false
            '}' -> if (stack.isEmpty() || stack.pop() != '{') return false
            ']' -> if (stack.isEmpty() || stack.pop() != '[') return false
        }
    }
    return stack.isEmpty()
}
```

## Задача 11: Number of Islands (Medium)

**Условие:**
Дана 2D сетка, представляющая карту, где '1' представляет сушу, а '0' представляет воду. Посчитайте количество островов (связанных участков суши).

**Пример:**
```
Input:
[
  ['1','1','1','1','0'],
  ['1','1','0','1','0'],
  ['1','1','0','0','0'],
  ['0','0','0','0','0']
]
Output: 1

Input:
[
  ['1','1','0','0','0'],
  ['1','1','0','0','0'],
  ['0','0','1','0','0'],
  ['0','0','0','1','1']
]
Output: 3
```

**Решение (DFS):**
```kotlin
fun numIslands(grid: Array<CharArray>): Int {
    if (grid.isEmpty()) return 0
    
    val rows = grid.size
    val cols = grid[0].size
    var count = 0
    
    for (row in 0 until rows) {
        for (col in 0 until cols) {
            if (grid[row][col] == '1') {
                count++
                dfs(grid, row, col)
            }
        }
    }
    
    return count
}

private fun dfs(grid: Array<CharArray>, row: Int, col: Int) {
    val rows = grid.size
    val cols = grid[0].size
    
    // Проверяем границы и текущую ячейку
    if (row < 0 || row >= rows || col < 0 || col >= cols || grid[row][col] == '0') {
        return
    }
    
    // Отмечаем как посещенную
    grid[row][col] = '0'
    
    // Исследуем все 4 направления
    dfs(grid, row + 1, col)
    dfs(grid, row - 1, col)
    dfs(grid, row, col + 1)
    dfs(grid, row, col - 1)
}
```

## Задача 12: Merge Intervals (Medium)

**Условие:**
Дан массив интервалов, объедините перекрывающиеся интервалы.

**Пример:**
```
Input: [[1,3],[2,6],[8,10],[15,18]]
Output: [[1,6],[8,10],[15,18]]
Объяснение: Интервалы [1,3] и [2,6] перекрываются, поэтому они объединяются в [1,6].
```

**Решение:**
```kotlin
fun merge(intervals: Array<IntArray>): Array<IntArray> {
    if (intervals.isEmpty()) return emptyArray()
    
    // Сортируем интервалы по времени начала
    intervals.sortBy { it[0] }
    
    val result = mutableListOf<IntArray>()
    var current = intervals[0]
    
    for (i in 1 until intervals.size) {
        val interval = intervals[i]
        
        // Если текущий интервал перекрывается с предыдущим
        if (interval[0] <= current[1]) {
            // Обновляем конец текущего интервала
            current[1] = maxOf(current[1], interval[1])
        } else {
            // Добавляем текущий интервал и переходим к следующему
            result.add(current)
            current = interval
        }
    }
    
    // Добавляем последний интервал
    result.add(current)
    
    return result.toTypedArray()
}
```

## Задача 13: Binary Tree Level Order Traversal (Medium)

**Условие:**
Выполните обход бинарного дерева по уровням и верните его значения в виде списка списков.

**Пример:**
```
    3
   / \
  9  20
    /  \
   15   7

Output: [[3], [9, 20], [15, 7]]
```

**Решение:**
```kotlin
class TreeNode(var `val`: Int) {
    var left: TreeNode? = null
    var right: TreeNode? = null
}

fun levelOrder(root: TreeNode?): List<List<Int>> {
    val result = mutableListOf<List<Int>>()
    if (root == null) return result
    
    val queue = LinkedList<TreeNode>()
    queue.add(root)
    
    while (queue.isNotEmpty()) {
        val level = mutableListOf<Int>()
        val size = queue.size
        
        for (i in 0 until size) {
            val node = queue.poll()
            level.add(node.`val`)
            
            node.left?.let { queue.add(it) }
            node.right?.let { queue.add(it) }
        }
        
        result.add(level)
    }
    
    return result
}
```

## Задача 14: Meeting Rooms II (Medium)

**Условие:**
Дан массив временных интервалов (начало, конец) для встреч в переговорных комнатах. Определите минимальное количество переговорных комнат, необходимых для проведения всех встреч.

**Пример:**
```
Input: [[0, 30], [5, 10], [15, 20]]
Output: 2 (требуются 2 комнаты)

Input: [[7, 10], [2, 4]]
Output: 1 (требуется 1 комната)
```

**Решение:**
```kotlin
data class Interval(val start: Int, val end: Int)

fun minMeetingRooms(intervals: Array<Interval>): Int {
    if (intervals.isEmpty()) return 0
    
    // Сортируем по времени начала
    val starts = intervals.map { it.start }.sorted()
    // Сортируем по времени окончания
    val ends = intervals.map { it.end }.sorted()
    
    var rooms = 0
    var endIdx = 0
    
    for (startIdx in starts.indices) {
        // Если начало новой встречи меньше чем конец предыдущей, 
        // нужна новая комната
        if (starts[startIdx] < ends[endIdx]) {
            rooms++
        } else {
            // Иначе можем использовать освободившуюся комнату
            endIdx++
        }
    }
    
    return rooms
}
```

## Задача 15: Palindrome Linked List (Easy)

**Условие:**
Определите, является ли связный список палиндромом (читается одинаково слева направо и справа налево).

**Пример:**
```
Input: head = [1,2,2,1]
Output: true
```

**Решение:**
```kotlin
class ListNode(var `val`: Int) {
    var next: ListNode? = null
}

fun isPalindrome(head: ListNode?): Boolean {
    if (head?.next == null) return true
    
    // Находим середину списка
    var slow = head
    var fast = head
    while (fast?.next != null) {
        slow = slow?.next
        fast = fast.next?.next
    }
    
    // Переворачиваем вторую половину списка
    var prev: ListNode? = null
    var current = slow
    while (current != null) {
        val next = current.next
        current.next = prev
        prev = current
        current = next
    }
    
    // Сравниваем первую и перевернутую вторую половины
    var p1 = head
    var p2 = prev
    while (p2 != null) {
        if (p1?.`val` != p2.`val`) return false
        p1 = p1.next
        p2 = p2.next
    }
    
    return true
}
```

## Задача 16: Word Search (Medium)

**Условие:**
Дана матрица букв board и слово word, определите, существует ли это слово в матрице.
Слово может быть составлено из смежных клеток, где смежными считаются клетки, имеющие общие стороны.

**Пример:**
```
board = [
  ['A','B','C','E'],
  ['S','F','C','S'],
  ['A','D','E','E']
]

word = "ABCCED" -> true
word = "SEE" -> true
word = "ABCB" -> false
```

**Решение:**
```kotlin
fun exist(board: Array<CharArray>, word: String): Boolean {
    val rows = board.size
    val cols = board[0].size
    
    for (row in 0 until rows) {
        for (col in 0 until cols) {
            if (board[row][col] == word[0] && dfs(board, word, row, col, 0)) {
                return true
            }
        }
    }
    
    return false
}

private fun dfs(board: Array<CharArray>, word: String, row: Int, col: Int, index: Int): Boolean {
    // Если мы дошли до конца слова, значит слово найдено
    if (index == word.length) return true
    
    // Проверяем границы и текущий символ
    if (row < 0 || row >= board.size || col < 0 || col >= board[0].size || 
        board[row][col] != word[index]) {
        return false
    }
    
    // Отмечаем текущую ячейку как посещенную
    val temp = board[row][col]
    board[row][col] = '#'
    
    // Проверяем все 4 направления
    val found = dfs(board, word, row + 1, col, index + 1) ||
                dfs(board, word, row - 1, col, index + 1) ||
                dfs(board, word, row, col + 1, index + 1) ||
                dfs(board, word, row, col - 1, index + 1)
    
    // Восстанавливаем значение ячейки
    board[row][col] = temp
    
    return found
}
```

## Задача 17: Coin Change (Medium)

**Условие:**
У вас есть монеты разных номиналов и сумма денег. Напишите функцию для подсчета минимального количества монет, которые нужны, чтобы составить эту сумму. Если сумму нельзя составить, верните -1.

**Пример:**
```
coins = [1, 2, 5], amount = 11
Output: 3 (11 = 5 + 5 + 1)

coins = [2], amount = 3
Output: -1
```

**Решение (динамическое программирование):**
```kotlin
fun coinChange(coins: IntArray, amount: Int): Int {
    // Создаем массив для хранения минимального количества монет 
    // для каждой промежуточной суммы
    val dp = IntArray(amount + 1) { amount + 1 }
    dp[0] = 0
    
    for (coin in coins) {
        for (i in coin..amount) {
            dp[i] = minOf(dp[i], dp[i - coin] + 1)
        }
    }
    
    return if (dp[amount] > amount) -1 else dp[amount]
}
```

## Задача 18: Find First and Last Position of Element in Sorted Array (Medium)

**Условие:**
Дан отсортированный массив целых чисел и целевое значение, найдите начальную и конечную позиции этого значения в массиве. Если значение не найдено, верните [-1, -1].

**Пример:**
```
Input: nums = [5,7,7,8,8,10], target = 8
Output: [3,4]

Input: nums = [5,7,7,8,8,10], target = 6
Output: [-1,-1]
```

**Решение (бинарный поиск):**
```kotlin
fun searchRange(nums: IntArray, target: Int): IntArray {
    val result = intArrayOf(-1, -1)
    if (nums.isEmpty()) return result
    
    // Ищем первую позицию
    result[0] = findFirstPosition(nums, target)
    if (result[0] == -1) return result
    
    // Ищем последнюю позицию
    result[1] = findLastPosition(nums, target)
    
    return result
}

private fun findFirstPosition(nums: IntArray, target: Int): Int {
    var left = 0
    var right = nums.size - 1
    var result = -1
    
    while (left <= right) {
        val mid = left + (right - left) / 2
        
        if (nums[mid] >= target) {
            right = mid - 1
        } else {
            left = mid + 1
        }
        
        if (nums[mid] == target) {
            result = mid
        }
    }
    
    return result
}

private fun findLastPosition(nums: IntArray, target: Int): Int {
    var left = 0
    var right = nums.size - 1
    var result = -1
    
    while (left <= right) {
        val mid = left + (right - left) / 2
        
        if (nums[mid] <= target) {
            left = mid + 1
        } else {
            right = mid - 1
        }
        
        if (nums[mid] == target) {
            result = mid
        }
    }
    
    return result
}
```

## Задача 19: Container With Most Water (Medium)

**Условие:**
Дан массив высот, найдите два элемента, чтобы между ними можно было бы хранить максимальное количество воды.

**Пример:**
```
Input: height = [1,8,6,2,5,4,8,3,7]
Output: 49
Объяснение: Контейнер образуют элементы height[1]=8 и height[8]=7. Ширина = 7, высота = min(8, 7) = 7, площадь = 7 * 7 = 49.
```

**Решение:**
```kotlin
fun maxArea(height: IntArray): Int {
    var maxArea = 0
    var left = 0
    var right = height.size - 1
    
    while (left < right) {
        val width = right - left
        val h = minOf(height[left], height[right])
        val area = width * h
        
        maxArea = maxOf(maxArea, area)
        
        if (height[left] < height[right]) {
            left++
        } else {
            right--
        }
    }
    
    return maxArea
}
```

## Задача 20: Rotate Image (Medium)

**Условие:**
Дана квадратная матрица размером n x n, поверните изображение на 90 градусов (по часовой стрелке).
Необходимо выполнить это преобразование на месте, не используя другую матрицу.

**Пример:**
```
Input: matrix = [[1,2,3],[4,5,6],[7,8,9]]
Output: [[7,4,1],[8,5,2],[9,6,3]]
```

**Решение:**
```kotlin
fun rotate(matrix: Array<IntArray>) {
    val n = matrix.size
    
    // Транспонируем матрицу
    for (i in 0 until n) {
        for (j in i until n) {
            val temp = matrix[i][j]
            matrix[i][j] = matrix[j][i]
            matrix[j][i] = temp
        }
    }
    
    // Отражаем каждую строку
    for (i in 0 until n) {
        for (j in 0 until n / 2) {
            val temp = matrix[i][j]
            matrix[i][j] = matrix[i][n - 1 - j]
            matrix[i][n - 1 - j] = temp
        }
    }
}
```

## Заключение

Эти задачи охватывают различные алгоритмические концепции, которые часто встречаются на технических собеседованиях:

1. **Структуры данных**:
   - Массивы и строки
   - Связные списки
   - Деревья и графы
   - Стеки и очереди
   - Хеш-таблицы

2. **Алгоритмические подходы**:
   - Динамическое программирование
   - Подход "разделяй и властвуй"
   - Поиск в глубину/ширину (DFS/BFS)
   - Двоичный поиск
   - Двухуказательная техника (two pointers)
   - Метод скользящего окна (sliding window)

3. **Важные концепции**:
   - Рекурсия
   - Итерация
   - Бэктрекинг (перебор с возвратом)
   - Сортировка
   - Работа с интервалами

Главное в подготовке - не только решать задачи, но и понимать основные принципы и шаблоны решений. Практикуйтесь в объяснении своих решений вслух, анализируйте временную и пространственную сложность, и обращайте внимание на крайние случаи.

Удачи на собеседовании! 