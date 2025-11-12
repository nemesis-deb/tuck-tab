scoreboard players enable @a mobheads.config
execute as @a if score @s mobheads.config matches 1 run \
 function mobheads:config/dialog_config with storage mobheads:root

execute as @a if score @s mobheads.mined_creaking_heart matches 1.. run function mobheads:app/notification/run/creaking

scoreboard players enable @a get_mob_head
execute as @a if score @s get_mob_head matches 1 run function mobheads:app/get_mob_head/get_mob_head_dialog