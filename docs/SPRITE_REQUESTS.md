# Sprite Requests

Этот файл уточняет `ART_MANIFEST.md` для этапа генерации спрайтов.

При расхождении размеров между `ART_MANIFEST.md` и этим файлом
на этапе генерации использовать размеры из этого файла.

По умолчанию для каждого спрайта генерируются 3 варианта в:

```text
.ai-generated/<sprite_id>/1.png
.ai-generated/<sprite_id>/2.png
.ai-generated/<sprite_id>/3.png
```

Финальные варианты после выбора пользователя копируются в canonical destination.

---

## Status legend

- `DONE` — спрайт сгенерирован и выбран финальный вариант.
- `TODO` — нужно сгенерировать.
- `OPTIONAL` — желательно, но не критично для текущего этапа.

---

## Already generated

| sprite_id | category | size | status | current file | notes |
|---|---|---:|---|---|---|
| `obj_magic_lantern` | object | `128x128` | DONE | `assets/images/lantern.png` | Временный путь из TASK-015. Позже можно привести к canonical destination. |

---

## Room backgrounds

| sprite_id | category | size | short prompt | canonical destination | status |
|---|---|---:|---|---|---|
| `bg_entrance_hall` | room | `1280x720` | dusty archive entrance hall, cracked stone floor, broken chandelier lying on floor, cobwebs, small dim window, old wooden door | `assets/images/rooms/bg_entrance_hall.png` | TODO |
| `bg_map_hall` | room | `1280x720` | archive map hall, large wooden table with unfolded old maps, bookshelves, stone walls, warm dim magical lantern light | `assets/images/rooms/bg_map_hall.png` | TODO |
| `bg_flooded_gallery` | room | `1280x720` | flooded stone gallery, floor partly covered with glowing orange magical dust, tall columns, dark gothic archive | `assets/images/rooms/bg_flooded_gallery.png` | TODO |
| `bg_gatekeeper_cell` | room | `1280x720` | small gatekeeper cell, rough stone walls, old chest in corner, iron-banded door, dim shadows | `assets/images/rooms/bg_gatekeeper_cell.png` | TODO |
| `bg_hidden_cell` | room | `1280x720` | hidden ritual cell, ancient stone altar, glowing blue runes, narrow staircase upward, mystical darkness | `assets/images/rooms/bg_hidden_cell.png` | TODO |

---

## Objects

| sprite_id | category | size | short prompt | canonical destination | status |
|---|---|---:|---|---|---|
| `obj_broken_chandelier` | object | `128x128` | broken crystal chandelier lying on stone floor | `assets/images/objects/obj_broken_chandelier.png` | DONE |
| `obj_door_closed` | object | `128x128` | closed old wooden door with iron hinges | `assets/images/objects/obj_door_closed.png` | DONE |
| `obj_stone_altar` | object | `128x128` | ancient stone altar carved with faint runes | `assets/images/objects/obj_stone_altar.png` | DONE |
| `obj_stairs_up` | object | `128x128` | narrow stone staircase leading upward | `assets/images/objects/obj_stairs_up.png` | DONE |
| `obj_map_table` | object | `128x128` | large wooden table with unfolded old maps | `assets/images/objects/obj_map_table.png` | DONE |
| `obj_old_chest_closed` | object | `128x128` | old wooden treasure chest, closed, iron bands and lock | `assets/images/objects/obj_old_chest_closed.png` | DONE |
| `obj_old_chest_open` | object | `128x128` | old wooden treasure chest, open and empty | `assets/images/objects/obj_old_chest_open.png` | TODO |
| `obj_bookshelf` | object | `128x128` | tall wooden bookshelf filled with dusty books | `assets/images/objects/obj_bookshelf.png` | TODO |
| `obj_magic_lantern_lit` | object | `128x128` | magic lantern on a stand, brighter glowing warm light inside, ornate metalwork | `assets/images/objects/obj_magic_lantern_lit.png` | OPTIONAL |
| `obj_ancient_folio` | object | `128x128` | ancient leather-bound folio with metal clasp | `assets/images/objects/obj_ancient_folio.png` | TODO |
| `obj_hidden_rune` | object | `128x128` | glowing blue magical rune carved in stone wall | `assets/images/objects/obj_hidden_rune.png` | TODO |

---

## Items

| sprite_id | category | size | short prompt | canonical destination | status |
|---|---|---:|---|---|---|
| `item_spell_scroll_spark` | item | `128x128` | rolled spell scroll with orange wax seal and simple embossed spark symbol, no letters | `assets/images/items/item_spell_scroll_spark.png` | TODO |
| `item_spell_scroll_push` | item | `128x128` | rolled spell scroll with blue wax seal and simple embossed wave symbol, no letters | `assets/images/items/item_spell_scroll_push.png` | TODO |
| `item_spell_scroll_seeker` | item | `128x128` | rolled spell scroll with green wax seal and simple embossed eye symbol, no letters | `assets/images/items/item_spell_scroll_seeker.png` | TODO |
| `item_artifact_ring_fire` | item | `128x128` | simple metal ring with small glowing red gem | `assets/images/items/item_artifact_ring_fire.png` | TODO |

---

## Creatures

| sprite_id | category | size | short prompt | canonical destination | status |
|---|---|---:|---|---|---|
| `creature_ash_slug` | creature | `128x128` | ashen gray slug-like monster, amorphous body, faint ember glow, menacing but slow | `assets/images/creatures/creature_ash_slug.png` | TODO |
| `creature_ash_slug_neutralized` | creature | `128x128` | deflated ashen gray slug remains, no glow | `assets/images/creatures/creature_ash_slug_neutralized.png` | OPTIONAL |

---

## Player

| sprite_id | category | size | short prompt | canonical destination | status |
|---|---|---:|---|---|---|
| `player_idle` | player | `128x128` | apprentice wizard in dark blue hooded robe, holding wooden staff, calm front-facing standing pose | `assets/images/player/player_idle.png` | TODO |
| `player_walk_sheet` | player | `128x128` | four-frame walking animation of apprentice wizard in dark blue hooded robe, sprite sheet, horizontal strip | `assets/images/player/player_walk_sheet.png` | OPTIONAL |

---

## Spirits

| sprite_id | category | size | short prompt | canonical destination | status |
|---|---|---:|---|---|---|
| `spirit_seeker` | spirit | `128x128` | translucent floating seeker spirit, glowing green eyes, wispy ethereal body, magical aura | `assets/images/spirits/spirit_seeker.png` | TODO |

---

## UI icons

| sprite_id | category | size | short prompt | canonical destination | status |
|---|---|---:|---|---|---|
| `icon_spell_spark` | ui | `128x128` | orange magical spark burst icon | `assets/images/ui/icon_spell_spark.png` | TODO |
| `icon_spell_push` | ui | `128x128` | blue kinetic push wave icon | `assets/images/ui/icon_spell_push.png` | TODO |
| `icon_spell_seeker` | ui | `128x128` | green all-seeing spirit eye icon | `assets/images/ui/icon_spell_seeker.png` | TODO |
| `icon_action_look` | ui | `128x128` | simple eye icon | `assets/images/ui/icon_action_look.png` | TODO |
| `icon_action_search` | ui | `128x128` | simple magnifying glass icon | `assets/images/ui/icon_action_search.png` | TODO |
| `icon_action_move` | ui | `128x128` | simple doorway arrow icon | `assets/images/ui/icon_action_move.png` | TODO |
| `icon_action_pick_up` | ui | `128x128` | simple open hand taking item icon | `assets/images/ui/icon_action_pick_up.png` | TODO |
| `icon_action_ascend` | ui | `128x128` | simple upward staircase arrow icon | `assets/images/ui/icon_action_ascend.png` | TODO |
| `icon_mastery_mark_empty` | ui | `128x128` | empty circular mastery seal with rune, dark gray | `assets/images/ui/icon_mastery_mark_empty.png` | TODO |
| `icon_mastery_mark_filled` | ui | `128x128` | filled golden circular mastery seal with glowing rune | `assets/images/ui/icon_mastery_mark_filled.png` | TODO |

---

## Effects

| sprite_id | category | size | short prompt | canonical destination | status |
|---|---|---:|---|---|---|
| `effect_spark` | effect | `128x128` | small orange magical spark burst | `assets/images/effects/effect_spark.png` | OPTIONAL |
| `effect_push` | effect | `128x128` | blue magical push shockwave | `assets/images/effects/effect_push.png` | OPTIONAL |
| `effect_summon` | effect | `128x128` | green circular summoning glyph with floating motes | `assets/images/effects/effect_summon.png` | OPTIONAL |