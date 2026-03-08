# Дизайн: меню «Информация о регионе» (4 варианта и настраиваемые команды)

## Что уже сделано (реализовано)

- В `messages_en.yml` и `messages_ru.yml` добавлены ключи для кнопок:
  - `commands.region.print.add-o-label`, `add-o-hover`
  - `commands.region.print.add-m-label`, `add-m-hover`
  - `commands.region.print.remove-o-label`, `remove-o-hover`
  - `commands.region.print.remove-m-label`, `remove-m-hover`

- В **RegionPrintoutBuilder** реализовано:
  - **ViewMode** (ADMIN, OWNER, MEMBER, OUTSIDER) — вычисляется по праву `worldguard.admin` и членству в регионе.
  - Отдельные кнопки для владельцев (add-o-label, remove-o-label) и участников (add-m-label, remove-m-label); команды по умолчанию `/rg addowner`, `/rg addmember`, `/rg removeowner`, `/rg removemember`.
  - **Четыре варианта меню:**
    - **ADMIN** — полное меню (тип, приоритет, флаги с кнопкой, родитель с unlink, владельцы/участники с кнопками и clear, границы с select и телепорт).
    - **OWNER** — без типа/приоритета в шапке; флаги с кнопкой; родитель без unlink и без (priority); кнопки +О/−О и +М/−М и clear; границы с select, без телепорта.
    - **MEMBER** — как owner, но без кнопки [Флаги] и без клика по флагам; кнопки только +М/−М (и clear для участников); границы без телепорта.
    - **OUTSIDER** — только название региона, владельцы и участники (списки без кнопок), границы без select и без телепорта; без флагов и без родителя.

- В **RegionPermissionModel** добавлен метод **isAdministrator()** (проверка `worldguard.admin`).

---

## OUTSIDER

**OUTSIDER** в WorldGuard — это любой игрок, который **не входит в регион** ни как владелец (owner), ни как участник (member), и при этом не имеет права `worldguard.admin`. То есть это «посторонний» для региона: он не в списке owners и не в списке members. Для таких показывается минимальное меню (название региона, списки владельцев и участников, границы без кнопок).

---

## Конфиг команд для кнопок меню (реализовано)

В `config.yml` добавлена секция:

```yaml
region-info:
  commands:
    add-owner:
      type: suggest
      command: "/rg addowner -w \"{world}\" {region_name} "
    add-member:
      type: suggest
      command: "/rg addmember -w \"{world}\" {region_name} "
    remove-owner:
      type: suggest
      command: "/rg removeowner -w \"{world}\" {region_name} "
    remove-member:
      type: suggest
      command: "/rg removemember -w \"{world}\" {region_name} "
```

Плейсхолдеры (подставляются платформой):

- `{player}` — имя игрока, который смотрит меню
- `{world}` — мир региона
- `{region_name}` — ID региона

Если команда не задана в конфиге, используются стандартные команды.

- **type: run** — реализовано. Для каждой кнопки в конфиге можно указать `type: run`; тогда по клику команда выполняется от имени игрока (RUN_COMMAND), а не подставляется в чат (SUGGEST_COMMAND). По умолчанию `type: suggest`.
- **PlaceholderAPI** — реализовано. Если установлен плагин PlaceholderAPI, в строках команд после подстановки `{world}`, `{region_name}`, `{player}` дополнительно вызывается `PlaceholderAPI.setPlaceholders(player, command)`, так что плейсхолдеры вида `%player_name%` и другие работают.

### 2. Четыре варианта меню (по роли)

Определить «роль» при построении меню:

- **admin** — оператор или право `worldguard.admin`: полное меню (тип, приоритет, родитель, флаги, владельцы/участники с кнопками, телепорт, границы, выбор).
- **owner** — игрок является владельцем региона, но не админ: скрыть тип/приоритет/родитель/телепорт в центр/priority-hover; оставить флаги, владельцев/участников с кнопками add-o/add-m/remove-o/remove-m, границы, выбор.
- **member** — только участник, не владелец: скрыть управление флагами (flags-button, flag-hover, flag-group-format), родителя, add-o/remove-o; оставить add-m/remove-m, список владельцев/участников, границы (без кнопки очистки, если она про границы), без телепорта/выбора.
- **outsider** — не владелец и не участник: только название региона, владельцы/участники (без кнопок), без флагов, без родителя, без телепорта, без выбора.

В `RegionPrintoutBuilder` (или в месте вызова) передавать контекст: `RegionPermissionModel` + флаги «isOwner», «isMember». По ним решать, вызывать ли `appendBasics()` полностью, `appendFlags()`, `appendParents()`, `appendDomain()` с теми или иными кнопками, `appendBounds()`, и какие click/hover использовать (из конфига или из локали).

### 3. Использование новых кнопок в коде

В `RegionPrintoutBuilder.addDomainString()` сейчас один вызов для owners (addowner/removeowner) и один для members (addmember/removemember). Нужно:

- Для блока владельцев: кнопки с ключами `add-o-label`/`add-o-hover` и `remove-o-label`/`remove-o-hover`, команды из конфига `add-owner` и `remove-owner` (с подстановкой `{region_name}`, `{world}` и т.д.).
- Для блока участников: то же с `add-m-label`/`remove-m-label` и конфигом `add-member`/`remove-member`.
- Режим `suggest`: `ClickEvent.Action.SUGGEST_COMMAND` с подставленной командой.
- Режим `run`: `ClickEvent.Action.RUN_COMMAND` (выполнить от имени игрока).

Подстановку плейсхолдеров делать в общем методе (например, в Bukkit-слое, где есть доступ к игроку и региону). PlaceholderAPI вызывать только если плагин присутствует.

### 4. Разделение меню по ролям

Варианты:

- **A)** Один `RegionPrintoutBuilder`, в конструктор передавать «режим» (admin/owner/member/outsider) и внутри каждого `append*()` проверять режим и не добавлять ненужные части.
- **B)** Четыре фабрики/билдера под каждый режим, общая база через хелперы.

Рекомендуется вариант A: один билдер, один метод `appendRegionInformation()`, внутри — проверки по режиму и правам (например, `perms.maySetFlag(region)`, `isOwner`, `isMember`).

Определение роли: при создании билдера в `RegionCommands` (или аналоге) вычислить:

- `boolean isAdmin = actor.hasPermission("worldguard.admin") || actor.isOp()`
- `boolean isOwner = region.getOwners().contains(player)`
- `boolean isMember = region.getMembers().contains(player)`

И передать в билдер флаги и конфиг команд.

---

## Итог

- Ключи локализации для add-o/add-m/remove-o/remove-m уже добавлены.
- Остаётся: конфиг команд с плейсхолдерами, логика подстановки (и при необходимости PlaceholderAPI), разделение отображения меню по ролям в `RegionPrintoutBuilder` и использование новых ключей и команд в `addDomainString()` и при построении кнопок «Флаги»/телепорт и т.д.
