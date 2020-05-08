# GSigns

GSigns is a spigot plugin that allows the creation of item frames that hold images and gifs.
The spigotmc page, including a download link, can be found [here](https://www.spigotmc.org/resources/g-signs-a-unique-map-signs-plugin-for-lobbies.73693/).

## Map Sending

Instead of sending a giant packet that holds the entire map data each time the sign is updated, the map data is send once to player as he walks up to the sign. After that premade entity metadata packets for the item frames are send that tell the client which of the maps it received earlier needs to be displayed.
The sending of the maps is also optimized as the images are not being rendered onto the map via MapCanvas#drawImage which would use the very slow MapPalette#matchColor function. Rather the r, g and b values will be pre converted into the corresponding byte colors which will then be put onto the map:
```java
for(int i=0; i<data.length; i++) {
	canvas.setPixel(i%128, i/128, data[i]);
}
```

## GSIGN-Format


