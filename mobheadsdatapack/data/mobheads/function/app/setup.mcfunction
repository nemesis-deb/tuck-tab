#> mobheads:app/setup
# Called on load

# Initiate all scoreboard objectives
function mobheads:app/scoreboard/add

execute unless score &loaded mobheads.check matches 1 run function mobheads:config/set_default

# Initiate set all scoreboard objectives
function mobheads:app/scoreboard/set

# Print the image
function mobheads:config/image