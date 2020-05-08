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

When saving a single item frame to a file, for example when the server gets restarted, a special format is used.
First a file is created with a name that holds information about the location of the item frame. The name is structured in the following way:
```
WORLD_NAME,X,Y,Z,FACING,DELAY
```
"FACING" is the direction the item frame is facing.
"DELAY" is the amount of milliseconds between frames. If the frame is a still image the value is 0.

After that the file will now be filled with the data of the map views. Each map view (or frame) will be stored in order, one after the other:
1. The map id of the map is stored as `4` bytes.
1. The pixel data will be stored as a byte array of size `16384`, as each frame has a width and height of 128.
When each mapview has been stored the entire map data will be compressed using the Deflater to reduce file size:
```java
Deflater compressor = new Deflater();
compressor.setLevel(Deflater.BEST_SPEED);
```

GSIGN-files in the "signs" folder that do not follow this format will be deleted upon loading for being outdated or corrupted.
