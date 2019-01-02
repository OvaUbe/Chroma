# Chroma


Chroma is a cross-platform 2d top-down multiplayer spaceshooter.

Actually, the foremost platform for Chroma is Android, but you can run it on any platform with JVM installed.
The controls are made primarily for sensor devices.

Chroma is written on Java with the use of libGDX framework (including box2D physics engine) and a Kryonet library
for network connection. It's worth mentioning that this game supports only LAN connection.

The rules are pretty simple - kill or be killed. You have a list of maps to choose from. Every map has static objects:
blocks, healers and damagers. Blocks are just obstacles, damagers and healers decrease and increase player's health,
respectively, in case player overlaps them. Healers are also interactive objects. If a player's vessel is currently
overlapping healer, player may press button "SET TURRET" and turn his ship into a static turret. The turret has a 
shield, which reflect most of the enemy bullets. The shield does not protect player if enemy is shooting at him at
close range. There are also two types of pickups: red one and a green one. Red makes player's weapon cool down faster
for 15 seconds. Green makes player invicible for 10 seconds. To be on top you should keep the Kills / Deaths 
statistics higher than others.

It's laggy as hell, though

[Download .apk](https://github.com/OvaUbe/Chroma/blob/master/android/build/outputs/apk/ChromaApp.apk?raw=true)

Have fun!
