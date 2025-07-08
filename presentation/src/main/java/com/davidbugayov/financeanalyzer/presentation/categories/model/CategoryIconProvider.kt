package com.davidbugayov.financeanalyzer.presentation.categories.model

// import androidx.compose.material.icons.outlined.* // Outlined пока не используем активно, чтобы не раздувать список
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAlarm
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.Anchor
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AssuredWorkload
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Balcony
import androidx.compose.material.icons.filled.Ballot
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Blender
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.BrunchDining
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CarRental
import androidx.compose.material.icons.filled.CarRepair
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Carpenter
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.ChildFriendly
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Commute
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CreditScore
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.CurrencyFranc
import androidx.compose.material.icons.filled.CurrencyLira
import androidx.compose.material.icons.filled.CurrencyPound
import androidx.compose.material.icons.filled.CurrencyRuble
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.CurrencyYen
import androidx.compose.material.icons.filled.CurrencyYuan
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.DoneOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.DryCleaning
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.EuroSymbol
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Garage
import androidx.compose.material.icons.filled.GasMeter
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Hiking
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HomeRepairService
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.Hvac
import androidx.compose.material.icons.filled.Icecream
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LaptopMac
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Liquor
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Microwave
import androidx.compose.material.icons.filled.MiscellaneousServices
import androidx.compose.material.icons.filled.ModeNight
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Museum
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Nightlife
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PedalBike
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Plumbing
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RealEstateAgent
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.RestorePage
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.SaveAs
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Scanner
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.SensorDoor
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.SmokingRooms
import androidx.compose.material.icons.filled.SocialDistance
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SportsBaseball
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SportsFootball
import androidx.compose.material.icons.filled.SportsGolf
import androidx.compose.material.icons.filled.SportsHandball
import androidx.compose.material.icons.filled.SportsHockey
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material.icons.filled.SportsMma
import androidx.compose.material.icons.filled.SportsMotorsports
import androidx.compose.material.icons.filled.SportsRugby
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.SportsVolleyball
import androidx.compose.material.icons.filled.StackedLineChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material.icons.filled.ThumbsUpDown
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WebAsset
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.WorkHistory
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.CameraFront
import androidx.compose.material.icons.outlined.Countertops
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalAtm
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.Money
import androidx.compose.material.icons.outlined.PriceChange
import androidx.compose.material.icons.outlined.RealEstateAgent
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Textsms
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.WorkOutline
import androidx.compose.ui.graphics.vector.ImageVector
import com.davidbugayov.financeanalyzer.core.model.Money

object CategoryIconProvider {
    fun getIconByName(name: String): ImageVector =
        when (name) {
            // Расходы (Expenses)
            "ShoppingCart" -> Icons.Filled.ShoppingCart
            "Restaurant" -> Icons.Filled.Restaurant
            "DirectionsCar" -> Icons.Filled.DirectionsCar
            "LocalMovies" -> Icons.Filled.LocalMovies
            "LocalHospital" -> Icons.Filled.LocalHospital
            "Checkroom" -> Icons.Filled.Checkroom // Гардероб, одежда
            "Home" -> Icons.Filled.Home // Дом, жилье
            "Phone" -> Icons.Filled.Phone // Телефон, связь
            "Pets" -> Icons.Filled.Pets // Домашние животные
            "Payments" -> Icons.Filled.Payments // Платежи, счета
            "CreditCard" -> Icons.Filled.CreditCard // Кредитная карта
            "MoneyOff" -> Icons.Filled.MoneyOff // Деньги потрачены, скидки
            "ShoppingBag" -> Icons.Filled.ShoppingBag // Сумка для покупок
            "LocalGroceryStore" -> Icons.Filled.LocalGroceryStore // Продуктовый магазин
            "Fastfood" -> Icons.Filled.Fastfood // Фастфуд
            "LocalCafe" -> Icons.Filled.LocalCafe // Кафе
            "School" -> Icons.Filled.School // Обучение, школа
            "ChildCare" -> Icons.Filled.ChildCare // Уход за детьми
            "SportsBasketball" -> Icons.Filled.SportsBasketball // Спорт (баскетбол)
            "FitnessCenter" -> Icons.Filled.FitnessCenter // Фитнес-центр
            "LocalPharmacy" -> Icons.Filled.LocalPharmacy // Аптека
            "Flight" -> Icons.Filled.Flight // Авиаперелеты
            "Train" -> Icons.Filled.Train // Поезд
            "DirectionsBus" -> Icons.Filled.DirectionsBus // Автобус
            "Commute" -> Icons.Filled.Commute // Поездки на работу
            "HomeRepairService" -> Icons.Filled.HomeRepairService // Ремонт дома
            "LocalLaundryService" -> Icons.Filled.LocalLaundryService // Прачечная
            "ElectricBolt" -> Icons.Filled.ElectricBolt // Электричество
            "WaterDrop" -> Icons.Filled.WaterDrop // Вода (коммунальные) - изменено с Water на WaterDrop
            "Celebration" -> Icons.Filled.Celebration // Праздники, мероприятия
            "Smartphone" -> Icons.Filled.Smartphone // Смартфон (гаджеты)
            "Computer" -> Icons.Filled.Computer // Компьютер (техника)
            "SportsEsports" -> Icons.Filled.SportsEsports // Киберспорт, игры
            "MusicNote" -> Icons.Filled.MusicNote // Музыка, концерты
            "HealthAndSafety" -> Icons.Filled.HealthAndSafety // Здоровье и безопасность
            "LocalFlorist" -> Icons.Filled.LocalFlorist // Цветы
            "Cake" -> Icons.Filled.Cake // Торты, сладости
            "FamilyRestroom" -> Icons.Filled.FamilyRestroom // Семья, дети
            "ChildFriendly" -> Icons.Filled.ChildFriendly // Товары для детей
            "Build" -> Icons.Filled.Build // Строительство, ремонт
            "Brush" -> Icons.Filled.Brush // Косметика, уход
            "Book" -> Icons.Filled.Book // Книги, чтение
            "Coffee" -> Icons.Filled.Coffee // Кофе
            "SmokingRooms" -> Icons.Filled.SmokingRooms // Сигареты, табак
            "GasMeter" -> Icons.Filled.GasMeter // Газ (коммунальные)
            "Microwave" -> Icons.Filled.Microwave // Бытовая техника
            "Weekend" -> Icons.Filled.Weekend // Отдых, выходные
            "Palette" -> Icons.Filled.Palette // Хобби, творчество
            "Museum" -> Icons.Filled.Museum // Культура, музеи
            "TheaterComedy" -> Icons.Filled.TheaterComedy // Театр, комедия
            "Icecream" -> Icons.Filled.Icecream // Мороженое
            "Liquor" -> Icons.Filled.Liquor // Алкоголь
            "Nightlife" -> Icons.Filled.Nightlife // Ночная жизнь
            "Park" -> Icons.Filled.Park // Парк, прогулки
            "Hiking" -> Icons.Filled.Hiking // Походы, туризм
            "TwoWheeler" -> Icons.Filled.TwoWheeler // Мотоцикл, скутер
            "PedalBike" -> Icons.Filled.PedalBike // Велосипед
            "Anchor" -> Icons.Filled.Anchor // Путешествия по воде
            "BeachAccess" -> Icons.Filled.BeachAccess // Пляж
            "Casino" -> Icons.Filled.Casino // Казино, азартные игры
            "ContentCut" -> Icons.Filled.ContentCut // Парикмахерская
            "DryCleaning" -> Icons.Filled.DryCleaning // Химчистка
            "Plumbing" -> Icons.Filled.Plumbing // Сантехника
            "CarRental" -> Icons.Filled.CarRental // Аренда авто
            "CarRepair" -> Icons.Filled.CarRepair // Ремонт авто
            "LocalGasStation" -> Icons.Filled.LocalGasStation // Заправка
            "MiscellaneousServices" -> Icons.Filled.MiscellaneousServices // Разные услуги
            "CleaningServices" -> Icons.Filled.CleaningServices // Уборка
            "ElectricalServices" -> Icons.Filled.ElectricalServices // Электрик
            "Construction" -> Icons.Filled.Construction // Строительство
            "Power" -> Icons.Filled.Power // Энергия
            "Wifi" -> Icons.Filled.Wifi // Интернет
            "Router" -> Icons.Filled.Router // Роутер, оборудование
            "Tv" -> Icons.Filled.Tv // Телевидение
            "Headphones" -> Icons.Filled.Headphones // Наушники
            "Watch" -> Icons.Filled.Watch // Часы, аксессуары
            "Diamond" -> Icons.Filled.Diamond // Ювелирные изделия
            "VolunteerActivism" -> Icons.Filled.VolunteerActivism // Благотворительность
            "Wallet" -> Icons.Filled.Wallet // Кошелек (для расходов)
            "PointOfSale" -> Icons.Filled.PointOfSale // Кассовый аппарат (покупки)
            "ReceiptLong" -> Icons.AutoMirrored.Filled.ReceiptLong // Длинный чек (крупные покупки)
            "Gavel" -> Icons.Filled.Gavel // Юридические расходы, штрафы
            "Balance" -> Icons.Filled.Balance // Баланс (юридический)
            "Policy" -> Icons.Filled.Policy // Страховка
            "AccessibilityNew" -> Icons.Filled.AccessibilityNew // Доступность, помощь (расходы на это)

            // Дополнительные иконки для расходов - Outlined стиль
            "ShoppingCartOutlined" -> Icons.Outlined.ShoppingCart // Корзина (контур)
            "RestaurantOutlined" -> Icons.Outlined.Restaurant // Ресторан (контур)
            "FastfoodOutlined" -> Icons.Outlined.Fastfood // Фастфуд (контур)
            "LocalCafeOutlined" -> Icons.Outlined.LocalCafe // Кафе (контур)
            "BrunchDining" -> Icons.Filled.BrunchDining // Бранч, обед
            "DinnerDining" -> Icons.Filled.DinnerDining // Ужин
            "LunchDining" -> Icons.Filled.LunchDining // Ланч
            "LocalPizza" -> Icons.Filled.LocalPizza // Пицца
            "LocalBar" -> Icons.Filled.LocalBar // Бар
            "House" -> Icons.Filled.House // Дом (альтернатива)
            "Yard" -> Icons.Filled.Yard // Двор
            "Grass" -> Icons.Filled.Grass // Трава
            "Bathtub" -> Icons.Filled.Bathtub // Ванна
            "Kitchen" -> Icons.Filled.Kitchen // Кухня
            "Chair" -> Icons.Filled.Chair // Стул
            "TableRestaurant" -> Icons.Filled.TableRestaurant // Стол для ресторана
            "CountertopsOutlined" -> Icons.Outlined.Countertops // Столешницы
            "Hvac" -> Icons.Filled.Hvac // Отопление, вентиляция, кондиционирование
            "SportsMartialArts" -> Icons.Filled.SportsMartialArts // Боевые искусства
            "SportsFootball" -> Icons.Filled.SportsFootball // Футбол
            "SportsSoccer" -> Icons.Filled.SportsSoccer // Футбол американский
            "SportsVolleyball" -> Icons.Filled.SportsVolleyball // Волейбол
            "SportsTennis" -> Icons.Filled.SportsTennis // Теннис
            "SportsHandball" -> Icons.Filled.SportsHandball // Гандбол
            "SportsRugby" -> Icons.Filled.SportsRugby // Регби
            "SportsHockey" -> Icons.Filled.SportsHockey // Хоккей
            "SportsBaseball" -> Icons.Filled.SportsBaseball // Бейсбол
            "SportsCricket" -> Icons.Filled.SportsCricket // Крикет
            "SportsKabaddi" -> Icons.Filled.SportsKabaddi // Кабадди
            "SportsGolf" -> Icons.Filled.SportsGolf // Гольф
            "SportsMotorsports" -> Icons.Filled.SportsMotorsports // Автоспорт
            "SportsMma" -> Icons.Filled.SportsMma // ММА
            "LocalMall" -> Icons.Filled.LocalMall // Торговый центр
            "Storefront" -> Icons.Filled.Storefront // Витрина, магазин
            "Balcony" -> Icons.Filled.Balcony // Балкон
            "SensorDoor" -> Icons.Filled.SensorDoor // Дверь с датчиком
            "LocalParking" -> Icons.Filled.LocalParking // Парковка
            "Garage" -> Icons.Filled.Garage // Гараж
            "EventSeat" -> Icons.Filled.EventSeat // Место мероприятия
            "LiveTv" -> Icons.Filled.LiveTv // Прямая трансляция ТВ
            "Scanner" -> Icons.Filled.Scanner // Сканер
            "Print" -> Icons.Filled.Print // Принтер
            "Security" -> Icons.Filled.Security // Безопасность

            // Доходы (Incomes)
            "TrendingUp" -> Icons.AutoMirrored.Filled.TrendingUp // Рост, тренд вверх
            "Work" -> Icons.Filled.Work // Работа, зарплата
            "AttachMoney" -> Icons.Filled.AttachMoney // Прикрепленные деньги, доход
            "MonetizationOn" -> Icons.Filled.MonetizationOn // Монетизация
            "AccountBalance" -> Icons.Filled.AccountBalance // Баланс счета
            "Business" -> Icons.Filled.Business // Бизнес
            "CardGiftcard" -> Icons.Filled.CardGiftcard // Подарочная карта (доход)
            "Handshake" -> Icons.Filled.Handshake // Сделка, партнерство
            "Apartment" -> Icons.Filled.Apartment // Аренда квартиры (доход)
            "Stars" -> Icons.Filled.Stars // Бонусы, награды
            "Savings" -> Icons.Filled.Savings // Сбережения, накопления
            "LocalAtm" -> Icons.Filled.LocalAtm // Банкомат (снятие наличных)
            "AccountBalanceWallet" -> Icons.Filled.AccountBalanceWallet // Баланс кошелька (пополнение)
            "PriceCheck" -> Icons.Filled.PriceCheck // Проверка цены (может быть связано с продажами)
            "Sell" -> Icons.Filled.Sell // Продажи
            "Receipt" -> Icons.Filled.Receipt // Чек (может быть доходным)
            "ShowChart" -> Icons.AutoMirrored.Filled.ShowChart // График роста (инвестиции)
            "StackedLineChart" -> Icons.Filled.StackedLineChart // Еще один график (инвестиции)
            "AssuredWorkload" -> Icons.Filled.AssuredWorkload // Гарантированная работа (стабильный доход)
            "CurrencyExchange" -> Icons.Filled.CurrencyExchange // Обмен валют (доход от курсов)
            "Insights" -> Icons.Filled.Insights // Аналитика, инсайты (доход от консультаций)
            "Leaderboard" -> Icons.Filled.Leaderboard // Лидерство, премии
            "Paid" -> Icons.Filled.Paid // Оплачено (получение платежа)
            "QueryStats" -> Icons.Filled.QueryStats // Статистика запросов (для фрилансеров)
            "RequestQuote" -> Icons.Filled.RequestQuote // Запрос цены (потенциальный доход)
            "Redeem" -> Icons.Filled.Redeem // Подарки (доход)
            "TrendingFlat" -> Icons.AutoMirrored.Filled.TrendingFlat // Стабильный доход (без изменений)
            "TrendingDown" -> Icons.AutoMirrored.Filled.TrendingDown // Уменьшение дохода
            "EuroSymbol" -> Icons.Filled.EuroSymbol // Евро (валюта)
            "CurrencyBitcoin" -> Icons.Filled.CurrencyBitcoin // Биткоин
            "CurrencyRuble" -> Icons.Filled.CurrencyRuble // Рубль
            "Percent" -> Icons.Filled.Percent // Проценты (депозиты, кэшбек)
            "RealEstateAgent" -> Icons.Filled.RealEstateAgent // Агент по недвижимости (доход от сделок)
            "SocialDistance" -> Icons.Filled.SocialDistance // Социальные выплаты
            "SupportAgent" -> Icons.Filled.SupportAgent // Поддержка (оплата за услуги)

            // Дополнительные иконки для доходов
            "TrendingUpOutlined" -> Icons.AutoMirrored.Outlined.TrendingUp // Тренд вверх (контур)
            "WorkOutline" -> Icons.Outlined.Work // Работа (контур)
            "AttachMoneyOutlined" -> Icons.Outlined.AttachMoney // Деньги (контур)
            "PriceChangeOutlined" -> Icons.Outlined.PriceChange // Изменение цены (контур)
            "LocalAtmOutlined" -> Icons.Outlined.LocalAtm // Банкомат (контур)
            "SellOutlined" -> Icons.Outlined.Sell // Продажи (контур)
            "AccountBalanceOutlined" -> Icons.Outlined.AccountBalance // Банк (контур)
            "MoneyOutlined" -> Icons.Outlined.Money // Деньги (контур)
            "WorkHistory" -> Icons.Filled.WorkHistory // История работы
            "WorkOutlineRounded" -> Icons.Rounded.WorkOutline // Работа (округлый контур)
            "BakeryDining" -> Icons.Filled.BakeryDining // Пекарня (для дохода от бизнеса)
            "Ballot" -> Icons.Filled.Ballot // Бюллетень (для дохода от голосования/дивидендов)
            "Blender" -> Icons.Filled.Blender // Блендер (для различных источников дохода)
            "Campaign" -> Icons.Filled.Campaign // Кампания (для дохода от маркетинга)
            "CameraFrontOutlined" -> Icons.Outlined.CameraFront // Камера спереди (для дохода от фото/видео услуг)
            "CurrencyLira" -> Icons.Filled.CurrencyLira // Лира
            "CurrencyFranc" -> Icons.Filled.CurrencyFranc // Франк
            "CurrencyPound" -> Icons.Filled.CurrencyPound // Фунт
            "CurrencyRupee" -> Icons.Filled.CurrencyRupee // Рупия
            "CurrencyYen" -> Icons.Filled.CurrencyYen // Йена
            "CurrencyYuan" -> Icons.Filled.CurrencyYuan // Юань
            "LocalShipping" -> Icons.Filled.LocalShipping // Доставка (для дохода от доставки)
            "Engineering" -> Icons.Filled.Engineering // Инженерия (доход от инженерной деятельности)
            "Agriculture" -> Icons.Filled.Agriculture // Сельское хозяйство (доход от урожая)
            "Science" -> Icons.Filled.Science // Наука (доход от научной деятельности)
            "Psychology" -> Icons.Filled.Psychology // Психология (доход от консультаций)
            "PeopleAlt" -> Icons.Filled.PeopleAlt // Люди (доход от работы с клиентами)
            "Groups" -> Icons.Filled.Groups // Группы (доход от групповой работы)
            "AddBusiness" -> Icons.Filled.AddBusiness // Новый бизнес
            "Store" -> Icons.Filled.Store // Магазин
            "LaptopMac" -> Icons.Filled.LaptopMac // Ноутбук Mac (IT доход)
            "WebAsset" -> Icons.Filled.WebAsset // Веб-ресурс (доход от интернет-активов)
            "Spa" -> Icons.Filled.Spa // СПА (доход от велнес индустрии)
            "Carpenter" -> Icons.Filled.Carpenter // Плотник (ремесленный доход)
            "Inventory" -> Icons.Filled.Inventory // Инвентарь (доход от продаж)
            "Inventory2" -> Icons.Filled.Inventory2 // Инвентарь 2
            "StraightenRounded" -> Icons.Rounded.Straighten // Выпрямитель (строительный инструмент)
            "RealEstateAgentOutlined" -> Icons.Outlined.RealEstateAgent // Риэлтор (контур)
            "Public" -> Icons.Filled.Public // Глобус (международный доход)

            // Общие (Common/Neutral)
            "SwapHoriz" -> Icons.Filled.SwapHoriz // Переводы между счетами
            "MoreHoriz" -> Icons.Filled.MoreHoriz // Другое, еще
            "Add" -> Icons.Filled.Add // Добавить
            "Category" -> Icons.Filled.Category // Категория (по умолчанию)
            "Adjust" -> Icons.Filled.Adjust // Корректировка
            "Explore" -> Icons.Filled.Explore // Исследовать, открыть новое
            "Language" -> Icons.Filled.Language // Язык
            "Settings" -> Icons.Filled.Settings // Настройки
            "Info" -> Icons.Filled.Info // Информация
            "HelpOutline" -> Icons.AutoMirrored.Filled.HelpOutline // Помощь
            "History" -> Icons.Filled.History // История
            "Sync" -> Icons.Filled.Sync // Синхронизация
            "CloudUpload" -> Icons.Filled.CloudUpload // Загрузка в облако
            "CloudDownload" -> Icons.Filled.CloudDownload // Скачивание из облака
            "FileCopy" -> Icons.Filled.FileCopy // Копирование
            "Share" -> Icons.Filled.Share // Поделиться
            "Archive" -> Icons.Filled.Archive // Архив
            "Unarchive" -> Icons.Filled.Unarchive // Разархивировать
            "Report" -> Icons.Filled.Report // Отчет
            "BarChart" -> Icons.Filled.BarChart // Гистограмма
            "PieChart" -> Icons.Filled.PieChart // Круговая диаграмма
            "AutoStories" -> Icons.Filled.AutoStories // Авто-истории, дневник
            "Calculate" -> Icons.Filled.Calculate // Калькулятор, расчеты
            "Edit" -> Icons.Filled.Edit // Редактировать
            "Delete" -> Icons.Filled.Delete // Удалить
            "Search" -> Icons.Filled.Search // Поиск
            "FilterList" -> Icons.Filled.FilterList // Фильтр
            "Sort" -> Icons.AutoMirrored.Filled.Sort // Сортировка
            "Refresh" -> Icons.Filled.Refresh // Обновить
            "Lock" -> Icons.Filled.Lock // Блокировка, безопасность
            "LockOpen" -> Icons.Filled.LockOpen // Разблокировка
            "Key" -> Icons.Filled.Key // Ключ, доступ
            "Fingerprint" -> Icons.Filled.Fingerprint // Отпечаток пальца
            "Notifications" -> Icons.Filled.Notifications // Уведомления
            "NotificationsActive" -> Icons.Filled.NotificationsActive // Активные уведомления
            "AccountCircle" -> Icons.Filled.AccountCircle // Профиль пользователя
            "Group" -> Icons.Filled.Group // Группа, совместный доступ
            "CreditScore" -> Icons.Filled.CreditScore // Кредитный рейтинг
            "SwapVert" -> Icons.Filled.SwapVert // Вертикальный обмен
            "Transform" -> Icons.Filled.Transform // Трансформация, конвертация
            "DataUsage" -> Icons.Filled.DataUsage // Использование данных
            "Schedule" -> Icons.Filled.Schedule // Расписание, график
            "ThumbsUpDown" -> Icons.Filled.ThumbsUpDown // Оценка, рейтинг
            "Lightbulb" -> Icons.Filled.Lightbulb // Идеи, советы
            "Flag" -> Icons.Filled.Flag // Флаг, отметка
            "Bookmarks" -> Icons.Filled.Bookmarks // Закладки

            // Дополнительные общие иконки
            "DeleteOutlined" -> Icons.Outlined.Delete // Удалить (контур)
            "EditOutlined" -> Icons.Outlined.Edit // Редактировать (контур)
            "SearchOutlined" -> Icons.Outlined.Search // Поиск (контур)
            "AddOutlined" -> Icons.Outlined.Add // Добавить (контур)
            "RemoveOutlined" -> Icons.Outlined.Remove // Удалить (контур)
            "InfoOutlined" -> Icons.Outlined.Info // Информация (контур)
            "DarkMode" -> Icons.Filled.DarkMode // Темный режим
            "LightMode" -> Icons.Filled.LightMode // Светлый режим
            "ModeNight" -> Icons.Filled.ModeNight // Ночной режим
            "AddAlarm" -> Icons.Filled.AddAlarm // Добавить будильник
            "AccessTime" -> Icons.Filled.AccessTime // Время доступа
            "Timer" -> Icons.Filled.Timer // Таймер
            "Alarm" -> Icons.Filled.Alarm // Будильник
            "AlarmOn" -> Icons.Filled.AlarmOn // Будильник включен
            "AlarmOff" -> Icons.Filled.AlarmOff // Будильник выключен
            "SystemUpdate" -> Icons.Filled.SystemUpdate // Обновление системы
            "FileDownload" -> Icons.Filled.FileDownload // Скачивание файла
            "FileUpload" -> Icons.Filled.FileUpload // Загрузка файла
            "Download" -> Icons.Filled.Download // Скачивание
            "Upload" -> Icons.Filled.Upload // Загрузка
            "SaveAlt" -> Icons.Filled.SaveAlt // Сохранить
            "SaveAs" -> Icons.Filled.SaveAs // Сохранить как
            "Save" -> Icons.Filled.Save // Сохранить
            "Backup" -> Icons.Filled.Backup // Резервное копирование
            "Restore" -> Icons.Filled.Restore // Восстановление
            "RestorePage" -> Icons.Filled.RestorePage // Восстановление страницы
            "RestoreFromTrash" -> Icons.Filled.RestoreFromTrash // Восстановление из корзины
            "DeleteForever" -> Icons.Filled.DeleteForever // Удалить навсегда
            "RemoveCircle" -> Icons.Filled.RemoveCircle // Удалить круг
            "AddCircle" -> Icons.Filled.AddCircle // Добавить круг
            "Create" -> Icons.Filled.Create // Создать
            "Done" -> Icons.Filled.Done // Готово
            "DoneAll" -> Icons.Filled.DoneAll // Все готово
            "DoneOutline" -> Icons.Filled.DoneOutline // Готово (контур)
            "Cancel" -> Icons.Filled.Cancel // Отмена
            "Close" -> Icons.Filled.Close // Закрыть
            "Block" -> Icons.Filled.Block // Блокировать
            "DragIndicator" -> Icons.Filled.DragIndicator // Индикатор перетаскивания
            "Menu" -> Icons.Filled.Menu // Меню
            "MoreVert" -> Icons.Filled.MoreVert // Еще (вертикально)
            "OpenInNew" -> Icons.AutoMirrored.Filled.OpenInNew // Открыть в новом
            "OpenInFull" -> Icons.Filled.OpenInFull // Открыть полностью
            "CloseFullscreen" -> Icons.Filled.CloseFullscreen // Закрыть полноэкранный режим
            "Fullscreen" -> Icons.Filled.Fullscreen // Полноэкранный режим
            "FullscreenExit" -> Icons.Filled.FullscreenExit // Выход из полноэкранного режима
            "Visibility" -> Icons.Filled.Visibility // Видимость
            "VisibilityOff" -> Icons.Filled.VisibilityOff // Видимость выключена
            "ZoomIn" -> Icons.Filled.ZoomIn // Увеличить
            "ZoomOut" -> Icons.Filled.ZoomOut // Уменьшить
            "Check" -> Icons.Filled.Check // Проверить
            "CheckBox" -> Icons.Filled.CheckBox // Флажок
            "CheckCircle" -> Icons.Filled.CheckCircle // Проверить круг
            "RadioButtonChecked" -> Icons.Filled.RadioButtonChecked // Переключатель выбран
            "RadioButtonUnchecked" -> Icons.Filled.RadioButtonUnchecked // Переключатель не выбран
            "Star" -> Icons.Filled.Star // Звезда
            "StarBorder" -> Icons.Filled.StarBorder // Граница звезды
            "StarHalf" -> Icons.AutoMirrored.Filled.StarHalf // Половина звезды
            "Favorite" -> Icons.Filled.Favorite // Избранное
            "FavoriteBorder" -> Icons.Filled.FavoriteBorder // Граница избранного
            "Chat" -> Icons.AutoMirrored.Filled.Chat // Чат
            "Comment" -> Icons.AutoMirrored.Filled.Comment // Комментарий
            "TextsmsOutlined" -> Icons.Outlined.Textsms // СМС (контур)

            else -> Icons.Filled.Category // Иконка по умолчанию
        }

    fun getIconName(icon: ImageVector?): String =
        when (icon) {
            // Расходы
            Icons.Filled.ShoppingCart -> "ShoppingCart"
            Icons.Filled.Restaurant -> "Restaurant"
            Icons.Filled.DirectionsCar -> "DirectionsCar"
            Icons.Filled.LocalMovies -> "LocalMovies"
            Icons.Filled.LocalHospital -> "LocalHospital"
            Icons.Filled.Checkroom -> "Checkroom"
            Icons.Filled.Home -> "Home"
            Icons.Filled.Phone -> "Phone"
            Icons.Filled.Pets -> "Pets"
            Icons.Filled.Payments -> "Payments"
            Icons.Filled.CreditCard -> "CreditCard"
            Icons.Filled.MoneyOff -> "MoneyOff"
            Icons.Filled.ShoppingBag -> "ShoppingBag"
            Icons.Filled.LocalGroceryStore -> "LocalGroceryStore"
            Icons.Filled.Fastfood -> "Fastfood"
            Icons.Filled.LocalCafe -> "LocalCafe"
            Icons.Filled.School -> "School"
            Icons.Filled.ChildCare -> "ChildCare"
            Icons.Filled.SportsBasketball -> "SportsBasketball"
            Icons.Filled.FitnessCenter -> "FitnessCenter"
            Icons.Filled.LocalPharmacy -> "LocalPharmacy"
            Icons.Filled.Flight -> "Flight"
            Icons.Filled.Train -> "Train"
            Icons.Filled.DirectionsBus -> "DirectionsBus"
            Icons.Filled.Commute -> "Commute"
            Icons.Filled.HomeRepairService -> "HomeRepairService"
            Icons.Filled.LocalLaundryService -> "LocalLaundryService"
            Icons.Filled.ElectricBolt -> "ElectricBolt"
            Icons.Filled.WaterDrop -> "WaterDrop"
            Icons.Filled.Celebration -> "Celebration"
            Icons.Filled.Smartphone -> "Smartphone"
            Icons.Filled.Computer -> "Computer"
            Icons.Filled.SportsEsports -> "SportsEsports"
            Icons.Filled.MusicNote -> "MusicNote"
            Icons.Filled.HealthAndSafety -> "HealthAndSafety"
            Icons.Filled.LocalFlorist -> "LocalFlorist"
            Icons.Filled.Cake -> "Cake"
            Icons.Filled.FamilyRestroom -> "FamilyRestroom"
            Icons.Filled.ChildFriendly -> "ChildFriendly"
            Icons.Filled.Build -> "Build"
            Icons.Filled.Brush -> "Brush"
            Icons.Filled.Book -> "Book"
            Icons.Filled.Coffee -> "Coffee"
            Icons.Filled.SmokingRooms -> "SmokingRooms"
            Icons.Filled.GasMeter -> "GasMeter"
            Icons.Filled.Microwave -> "Microwave"
            Icons.Filled.Weekend -> "Weekend"
            Icons.Filled.Palette -> "Palette"
            Icons.Filled.Museum -> "Museum"
            Icons.Filled.TheaterComedy -> "TheaterComedy"
            Icons.Filled.Icecream -> "Icecream"
            Icons.Filled.Liquor -> "Liquor"
            Icons.Filled.Nightlife -> "Nightlife"
            Icons.Filled.Park -> "Park"
            Icons.Filled.Hiking -> "Hiking"
            Icons.Filled.TwoWheeler -> "TwoWheeler"
            Icons.Filled.PedalBike -> "PedalBike"
            Icons.Filled.Anchor -> "Anchor"
            Icons.Filled.BeachAccess -> "BeachAccess"
            Icons.Filled.Casino -> "Casino"
            Icons.Filled.ContentCut -> "ContentCut"
            Icons.Filled.DryCleaning -> "DryCleaning"
            Icons.Filled.Plumbing -> "Plumbing"
            Icons.Filled.CarRental -> "CarRental"
            Icons.Filled.CarRepair -> "CarRepair"
            Icons.Filled.LocalGasStation -> "LocalGasStation"
            Icons.Filled.MiscellaneousServices -> "MiscellaneousServices"
            Icons.Filled.CleaningServices -> "CleaningServices"
            Icons.Filled.ElectricalServices -> "ElectricalServices"
            Icons.Filled.Construction -> "Construction"
            Icons.Filled.Power -> "Power"
            Icons.Filled.Wifi -> "Wifi"
            Icons.Filled.Router -> "Router"
            Icons.Filled.Tv -> "Tv"
            Icons.Filled.Headphones -> "Headphones"
            Icons.Filled.Watch -> "Watch"
            Icons.Filled.Diamond -> "Diamond"
            Icons.Filled.VolunteerActivism -> "VolunteerActivism"
            Icons.Filled.Wallet -> "Wallet"
            Icons.Filled.PointOfSale -> "PointOfSale"
            Icons.AutoMirrored.Filled.ReceiptLong -> "ReceiptLong"
            Icons.Filled.Gavel -> "Gavel"
            Icons.Filled.Balance -> "Balance"
            Icons.Filled.Policy -> "Policy"
            Icons.Filled.AccessibilityNew -> "AccessibilityNew"

            // Дополнительные иконки для расходов - Outlined стиль
            Icons.Outlined.ShoppingCart -> "ShoppingCartOutlined"
            Icons.Outlined.Restaurant -> "RestaurantOutlined"
            Icons.Outlined.Fastfood -> "FastfoodOutlined"
            Icons.Outlined.LocalCafe -> "LocalCafeOutlined"
            Icons.Filled.BrunchDining -> "BrunchDining"
            Icons.Filled.DinnerDining -> "DinnerDining"
            Icons.Filled.LunchDining -> "LunchDining"
            Icons.Filled.LocalPizza -> "LocalPizza"
            Icons.Filled.LocalBar -> "LocalBar"
            Icons.Filled.House -> "House"
            Icons.Filled.Yard -> "Yard"
            Icons.Filled.Grass -> "Grass"
            Icons.Filled.Bathtub -> "Bathtub"
            Icons.Filled.Kitchen -> "Kitchen"
            Icons.Filled.Chair -> "Chair"
            Icons.Filled.TableRestaurant -> "TableRestaurant"
            Icons.Outlined.Countertops -> "CountertopsOutlined"
            Icons.Filled.Hvac -> "Hvac"
            Icons.Filled.SportsMartialArts -> "SportsMartialArts"
            Icons.Filled.SportsFootball -> "SportsFootball"
            Icons.Filled.SportsSoccer -> "SportsSoccer"
            Icons.Filled.SportsVolleyball -> "SportsVolleyball"
            Icons.Filled.SportsTennis -> "SportsTennis"
            Icons.Filled.SportsHandball -> "SportsHandball"
            Icons.Filled.SportsRugby -> "SportsRugby"
            Icons.Filled.SportsHockey -> "SportsHockey"
            Icons.Filled.SportsBaseball -> "SportsBaseball"
            Icons.Filled.SportsCricket -> "SportsCricket"
            Icons.Filled.SportsKabaddi -> "SportsKabaddi"
            Icons.Filled.SportsGolf -> "SportsGolf"
            Icons.Filled.SportsMotorsports -> "SportsMotorsports"
            Icons.Filled.SportsMma -> "SportsMma"
            Icons.Filled.LocalMall -> "LocalMall"
            Icons.Filled.Storefront -> "Storefront"
            Icons.Filled.Balcony -> "Balcony"
            Icons.Filled.SensorDoor -> "SensorDoor"
            Icons.Filled.LocalParking -> "LocalParking"
            Icons.Filled.Garage -> "Garage"
            Icons.Filled.EventSeat -> "EventSeat"
            Icons.Filled.LiveTv -> "LiveTv"
            Icons.Filled.Scanner -> "Scanner"
            Icons.Filled.Print -> "Print"
            Icons.Filled.Security -> "Security"

            // Доходы
            Icons.AutoMirrored.Filled.TrendingUp -> "TrendingUp"
            Icons.Filled.Work -> "Work"
            Icons.Filled.AttachMoney -> "AttachMoney"
            Icons.Filled.MonetizationOn -> "MonetizationOn"
            Icons.Filled.AccountBalance -> "AccountBalance"
            Icons.Filled.Business -> "Business"
            Icons.Filled.CardGiftcard -> "CardGiftcard"
            Icons.Filled.Handshake -> "Handshake"
            Icons.Filled.Apartment -> "Apartment"
            Icons.Filled.Stars -> "Stars"
            Icons.Filled.Savings -> "Savings"
            Icons.Filled.LocalAtm -> "LocalAtm"
            Icons.Filled.AccountBalanceWallet -> "AccountBalanceWallet"
            Icons.Filled.PriceCheck -> "PriceCheck"
            Icons.Filled.Sell -> "Sell"
            Icons.Filled.Receipt -> "Receipt"
            Icons.AutoMirrored.Filled.ShowChart -> "ShowChart"
            Icons.Filled.StackedLineChart -> "StackedLineChart"
            Icons.Filled.AssuredWorkload -> "AssuredWorkload"
            Icons.Filled.CurrencyExchange -> "CurrencyExchange"
            Icons.Filled.Insights -> "Insights"
            Icons.Filled.Leaderboard -> "Leaderboard"
            Icons.Filled.Paid -> "Paid"
            Icons.Filled.QueryStats -> "QueryStats"
            Icons.Filled.RequestQuote -> "RequestQuote"
            Icons.Filled.Redeem -> "Redeem"
            Icons.AutoMirrored.Filled.TrendingFlat -> "TrendingFlat"
            Icons.AutoMirrored.Filled.TrendingDown -> "TrendingDown"
            Icons.Filled.EuroSymbol -> "EuroSymbol"
            Icons.Filled.CurrencyBitcoin -> "CurrencyBitcoin"
            Icons.Filled.CurrencyRuble -> "CurrencyRuble"
            Icons.Filled.Percent -> "Percent"
            Icons.Filled.RealEstateAgent -> "RealEstateAgent"
            Icons.Filled.SocialDistance -> "SocialDistance"
            Icons.Filled.SupportAgent -> "SupportAgent"

            // Дополнительные иконки для доходов
            Icons.Filled.WorkHistory -> "WorkHistory"
            Icons.Filled.BakeryDining -> "BakeryDining"
            Icons.Filled.CurrencyLira -> "CurrencyLira"
            Icons.Filled.CurrencyFranc -> "CurrencyFranc"
            Icons.Filled.CurrencyPound -> "CurrencyPound"
            Icons.Filled.CurrencyRupee -> "CurrencyRupee"
            Icons.Filled.CurrencyYen -> "CurrencyYen"
            Icons.Filled.CurrencyYuan -> "CurrencyYuan"
            Icons.Filled.LocalShipping -> "LocalShipping"
            Icons.Filled.Engineering -> "Engineering"
            Icons.Filled.Agriculture -> "Agriculture"
            Icons.Filled.Science -> "Science"
            Icons.Filled.Psychology -> "Psychology"
            Icons.Filled.PeopleAlt -> "PeopleAlt"
            Icons.Filled.Groups -> "Groups"
            Icons.Filled.AddBusiness -> "AddBusiness"
            Icons.Filled.Store -> "Store"
            Icons.Filled.LaptopMac -> "LaptopMac"
            Icons.Filled.WebAsset -> "WebAsset"
            Icons.Filled.Spa -> "Spa"
            Icons.Filled.Carpenter -> "Carpenter"
            Icons.Filled.Inventory -> "Inventory"
            Icons.Filled.Inventory2 -> "Inventory2"
            Icons.Rounded.Straighten -> "StraightenRounded"
            Icons.Outlined.RealEstateAgent -> "RealEstateAgentOutlined"
            Icons.Filled.Public -> "Public"

            // Общие
            Icons.Filled.SwapHoriz -> "SwapHoriz"
            Icons.Filled.MoreHoriz -> "MoreHoriz"
            Icons.Filled.Add -> "Add"
            Icons.Filled.Category -> "Category"
            Icons.Filled.Adjust -> "Adjust"
            Icons.Filled.Explore -> "Explore"
            Icons.Filled.Language -> "Language"
            Icons.Filled.Settings -> "Settings"
            Icons.Filled.Info -> "Info"
            Icons.AutoMirrored.Filled.HelpOutline -> "HelpOutline"
            Icons.Filled.History -> "History"
            Icons.Filled.Sync -> "Sync"
            Icons.Filled.CloudUpload -> "CloudUpload"
            Icons.Filled.CloudDownload -> "CloudDownload"
            Icons.Filled.FileCopy -> "FileCopy"
            Icons.Filled.Share -> "Share"
            Icons.Filled.Archive -> "Archive"
            Icons.Filled.Unarchive -> "Unarchive"
            Icons.Filled.Report -> "Report"
            Icons.Filled.BarChart -> "BarChart"
            Icons.Filled.PieChart -> "PieChart"
            Icons.Filled.AutoStories -> "AutoStories"
            Icons.Filled.Calculate -> "Calculate"
            Icons.Filled.Edit -> "Edit"
            Icons.Filled.Delete -> "Delete"
            Icons.Filled.Search -> "Search"
            Icons.Filled.FilterList -> "FilterList"
            Icons.AutoMirrored.Filled.Sort -> "Sort"
            Icons.Filled.Refresh -> "Refresh"
            Icons.Filled.Lock -> "Lock"
            Icons.Filled.LockOpen -> "LockOpen"
            Icons.Filled.Key -> "Key"
            Icons.Filled.Fingerprint -> "Fingerprint"
            Icons.Filled.Notifications -> "Notifications"
            Icons.Filled.NotificationsActive -> "NotificationsActive"
            Icons.Filled.AccountCircle -> "AccountCircle"
            Icons.Filled.Group -> "Group"
            Icons.Filled.CreditScore -> "CreditScore"
            Icons.Filled.SwapVert -> "SwapVert"
            Icons.Filled.Transform -> "Transform"
            Icons.Filled.DataUsage -> "DataUsage"
            Icons.Filled.Schedule -> "Schedule"
            Icons.Filled.ThumbsUpDown -> "ThumbsUpDown"
            Icons.Filled.Lightbulb -> "Lightbulb"
            Icons.Filled.Flag -> "Flag"
            Icons.Filled.Bookmarks -> "Bookmarks"

            // Новые общие иконки
            Icons.Filled.DarkMode -> "DarkMode"
            Icons.Filled.LightMode -> "LightMode"
            Icons.Filled.AccessTime -> "AccessTime"
            Icons.Filled.Timer -> "Timer"
            Icons.Filled.Alarm -> "Alarm"
            Icons.Filled.FileDownload -> "FileDownload"
            Icons.Filled.FileUpload -> "FileUpload"
            Icons.Filled.Save -> "Save"
            Icons.Filled.Backup -> "Backup"
            Icons.Filled.Restore -> "Restore"
            Icons.Filled.DeleteForever -> "DeleteForever"
            Icons.Filled.AddCircle -> "AddCircle"
            Icons.Filled.Create -> "Create"
            Icons.Filled.Done -> "Done"
            Icons.Filled.DoneAll -> "DoneAll"
            Icons.Filled.Cancel -> "Cancel"
            Icons.Filled.Close -> "Close"
            Icons.Filled.Block -> "Block"
            Icons.Filled.Menu -> "Menu"
            Icons.Filled.MoreVert -> "MoreVert"
            Icons.AutoMirrored.Filled.OpenInNew -> "OpenInNew"
            Icons.Filled.Visibility -> "Visibility"
            Icons.Filled.VisibilityOff -> "VisibilityOff"
            Icons.Filled.Check -> "Check"
            Icons.Filled.Star -> "Star"
            Icons.Filled.StarBorder -> "StarBorder"
            Icons.Filled.Favorite -> "Favorite"
            Icons.AutoMirrored.Filled.Chat -> "Chat"
            Icons.AutoMirrored.Filled.Comment -> "Comment"
            Icons.Outlined.Textsms -> "TextsmsOutlined"

            else -> "Category" // Имя для иконки по умолчанию
        }

    // Получить уникальный список иконок для диалога выбора иконки
    fun getUniqueIconsForPicker(): List<ImageVector> {
        return listOf(
            // Расходы
            Icons.Filled.ShoppingCart,
            Icons.Filled.Restaurant,
            Icons.Filled.DirectionsCar,
            Icons.Filled.LocalMovies,
            Icons.Filled.LocalHospital,
            Icons.Filled.Checkroom,
            Icons.Filled.Home,
            Icons.Filled.Phone,
            Icons.Filled.Pets,
            Icons.Filled.Payments,
            Icons.Filled.CreditCard,
            Icons.Filled.MoneyOff,
            Icons.Filled.ShoppingBag,
            Icons.Filled.LocalGroceryStore,
            Icons.Filled.Fastfood,
            Icons.Filled.LocalCafe,
            Icons.Filled.School,
            Icons.Filled.ChildCare,
            Icons.Filled.SportsBasketball,
            Icons.Filled.FitnessCenter,
            Icons.Filled.LocalPharmacy,
            Icons.Filled.Flight,
            Icons.Filled.Train,
            Icons.Filled.DirectionsBus,
            Icons.Filled.Commute,
            Icons.Filled.HomeRepairService,
            Icons.Filled.LocalLaundryService,
            Icons.Filled.ElectricBolt,
            Icons.Filled.WaterDrop,
            Icons.Filled.Celebration,
            Icons.Filled.Smartphone,
            Icons.Filled.Computer,
            Icons.Filled.SportsEsports,
            Icons.Filled.MusicNote,
            Icons.Filled.HealthAndSafety,
            Icons.Filled.LocalFlorist,
            Icons.Filled.Cake,
            Icons.Filled.FamilyRestroom,
            Icons.Filled.ChildFriendly,
            Icons.Filled.Build,
            Icons.Filled.Brush,
            Icons.Filled.Book,
            Icons.Filled.Coffee,
            Icons.Filled.SmokingRooms,
            Icons.Filled.GasMeter,
            Icons.Filled.Microwave,
            Icons.Filled.Weekend,
            Icons.Filled.Palette,
            Icons.Filled.Museum,
            Icons.Filled.TheaterComedy,
            Icons.Filled.Icecream,
            Icons.Filled.Liquor,
            Icons.Filled.Nightlife,
            Icons.Filled.Park,
            Icons.Filled.Hiking,
            Icons.Filled.TwoWheeler,
            Icons.Filled.PedalBike,
            Icons.Filled.Anchor,
            Icons.Filled.BeachAccess,
            Icons.Filled.Casino,
            Icons.Filled.ContentCut,
            Icons.Filled.DryCleaning,
            Icons.Filled.Plumbing,
            Icons.Filled.CarRental,
            Icons.Filled.CarRepair,
            Icons.Filled.LocalGasStation,
            Icons.Filled.MiscellaneousServices,
            Icons.Filled.CleaningServices,
            Icons.Filled.ElectricalServices,
            Icons.Filled.Construction,
            Icons.Filled.Power,
            Icons.Filled.Wifi,
            Icons.Filled.Router,
            Icons.Filled.Tv,
            Icons.Filled.Headphones,
            Icons.Filled.Watch,
            Icons.Filled.Diamond,
            Icons.Filled.VolunteerActivism,
            Icons.Filled.Wallet,
            Icons.Filled.PointOfSale,
            Icons.AutoMirrored.Filled.ReceiptLong,
            Icons.Filled.Gavel,
            Icons.Filled.Balance,
            Icons.Filled.Policy,
            Icons.Filled.AccessibilityNew,
            // Новые иконки для расходов
            Icons.Filled.BrunchDining,
            Icons.Filled.DinnerDining,
            Icons.Filled.LunchDining,
            Icons.Filled.LocalPizza,
            Icons.Filled.LocalBar,
            Icons.Filled.House,
            Icons.Filled.Yard,
            Icons.Filled.Grass,
            Icons.Filled.Bathtub,
            Icons.Filled.Kitchen,
            Icons.Filled.Chair,
            Icons.Filled.TableRestaurant,
            Icons.Filled.Hvac,
            Icons.Filled.SportsMartialArts,
            Icons.Filled.SportsFootball,
            Icons.Filled.SportsSoccer,
            Icons.Filled.SportsVolleyball,
            Icons.Filled.SportsTennis,
            Icons.Filled.SportsHandball,
            Icons.Filled.SportsRugby,
            Icons.Filled.SportsHockey,
            Icons.Filled.SportsBaseball,
            Icons.Filled.SportsCricket,
            Icons.Filled.SportsKabaddi,
            Icons.Filled.SportsGolf,
            Icons.Filled.SportsMotorsports,
            Icons.Filled.SportsMma,
            Icons.Filled.LocalMall,
            Icons.Filled.Storefront,
            Icons.Filled.Balcony,
            Icons.Filled.SensorDoor,
            Icons.Filled.LocalParking,
            Icons.Filled.Garage,
            Icons.Filled.EventSeat,
            Icons.Filled.LiveTv,
            Icons.Filled.Scanner,
            Icons.Filled.Print,
            Icons.Filled.Security,
            // Доходы
            Icons.AutoMirrored.Filled.TrendingUp,
            Icons.Filled.Work,
            Icons.Filled.AttachMoney,
            Icons.Filled.MonetizationOn,
            Icons.Filled.AccountBalance,
            Icons.Filled.Business,
            Icons.Filled.CardGiftcard,
            Icons.Filled.Handshake,
            Icons.Filled.Apartment,
            Icons.Filled.Stars,
            Icons.Filled.Savings,
            Icons.Filled.LocalAtm,
            Icons.Filled.AccountBalanceWallet,
            Icons.Filled.PriceCheck,
            Icons.Filled.Sell,
            Icons.Filled.Receipt,
            Icons.AutoMirrored.Filled.ShowChart,
            Icons.Filled.StackedLineChart,
            Icons.Filled.AssuredWorkload,
            Icons.Filled.CurrencyExchange,
            Icons.Filled.Insights,
            Icons.Filled.Leaderboard,
            Icons.Filled.Paid,
            Icons.Filled.QueryStats,
            Icons.Filled.RequestQuote,
            Icons.Filled.Redeem,
            Icons.AutoMirrored.Filled.TrendingFlat,
            Icons.AutoMirrored.Filled.TrendingDown,
            Icons.Filled.EuroSymbol,
            Icons.Filled.CurrencyBitcoin,
            Icons.Filled.CurrencyRuble,
            Icons.Filled.Percent,
            Icons.Filled.RealEstateAgent,
            Icons.Filled.SocialDistance,
            Icons.Filled.SupportAgent,
            // Новые иконки для доходов
            Icons.Filled.WorkHistory,
            Icons.Filled.BakeryDining,
            Icons.Filled.CurrencyLira,
            Icons.Filled.CurrencyFranc,
            Icons.Filled.CurrencyPound,
            Icons.Filled.CurrencyRupee,
            Icons.Filled.CurrencyYen,
            Icons.Filled.CurrencyYuan,
            Icons.Filled.LocalShipping,
            Icons.Filled.Engineering,
            Icons.Filled.Agriculture,
            Icons.Filled.Science,
            Icons.Filled.Psychology,
            Icons.Filled.PeopleAlt,
            Icons.Filled.Groups,
            Icons.Filled.AddBusiness,
            Icons.Filled.Store,
            Icons.Filled.LaptopMac,
            Icons.Filled.WebAsset,
            Icons.Filled.Spa,
            Icons.Filled.Carpenter,
            Icons.Filled.Inventory,
            Icons.Filled.Inventory2,
            Icons.Rounded.Straighten,
            Icons.Outlined.RealEstateAgent,
            Icons.Filled.Public,
            // Общие иконки
            Icons.Filled.SwapHoriz,
            Icons.Filled.MoreHoriz,
            Icons.Filled.Add,
            Icons.Filled.Category,
            Icons.Filled.Adjust,
            Icons.Filled.Explore,
            Icons.Filled.Language,
            Icons.Filled.Settings,
            Icons.Filled.Info,
            Icons.AutoMirrored.Filled.HelpOutline,
            Icons.Filled.History,
            Icons.Filled.Sync,
            Icons.Filled.CloudUpload,
            Icons.Filled.CloudDownload,
            Icons.Filled.FileCopy,
            Icons.Filled.Share,
            Icons.Filled.Archive,
            Icons.Filled.Unarchive,
            Icons.Filled.Report,
            Icons.Filled.BarChart,
            Icons.Filled.PieChart,
            Icons.Filled.AutoStories,
            Icons.Filled.Calculate,
            Icons.Filled.Edit,
            Icons.Filled.Delete,
            Icons.Filled.Search,
            Icons.Filled.FilterList,
            Icons.AutoMirrored.Filled.Sort,
            Icons.Filled.Refresh,
            Icons.Filled.Lock,
            Icons.Filled.LockOpen,
            Icons.Filled.Key,
            Icons.Filled.Fingerprint,
            Icons.Filled.Notifications,
            Icons.Filled.NotificationsActive,
            Icons.Filled.AccountCircle,
            Icons.Filled.Group,
            Icons.Filled.CreditScore,
            Icons.Filled.SwapVert,
            Icons.Filled.Transform,
            Icons.Filled.DataUsage,
            Icons.Filled.Schedule,
            Icons.Filled.ThumbsUpDown,
            Icons.Filled.Lightbulb,
            Icons.Filled.Flag,
            Icons.Filled.Bookmarks,
            // Новые общие иконки
            Icons.Filled.DarkMode,
            Icons.Filled.LightMode,
            Icons.Filled.AccessTime,
            Icons.Filled.Timer,
            Icons.Filled.Alarm,
            Icons.Filled.FileDownload,
            Icons.Filled.FileUpload,
            Icons.Filled.Save,
            Icons.Filled.Backup,
            Icons.Filled.Restore,
            Icons.Filled.DeleteForever,
            Icons.Filled.AddCircle,
            Icons.Filled.Create,
            Icons.Filled.Done,
            Icons.Filled.DoneAll,
            Icons.Filled.Cancel,
            Icons.Filled.Close,
            Icons.Filled.Block,
            Icons.Filled.Menu,
            Icons.Filled.MoreVert,
            Icons.AutoMirrored.Filled.OpenInNew,
            Icons.Filled.Visibility,
            Icons.Filled.VisibilityOff,
            Icons.Filled.Check,
            Icons.Filled.Star,
            Icons.Filled.StarBorder,
            Icons.Filled.Favorite,
            Icons.AutoMirrored.Filled.Chat,
            Icons.AutoMirrored.Filled.Comment,
        ).distinct() // Убедимся, что все иконки уникальны
    }
}
